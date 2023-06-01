package com.example.overseerapp.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.GeoArea;
import com.example.overseerapp.location.GeoFence;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.tracking.TrackedUser;
import com.example.overseerapp.ui.custom.ColorPickerDialog;
import com.example.overseerapp.ui.custom.GeoControlsHelpDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.overseerapp.databinding.ActivityUsersMapBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class UsersMapActivity extends FragmentActivity implements OnMapReadyCallback {
	private static final String TAG = "UsersMapActivity";
	private static final int geoAreaAlpha = 0x55000000;

	private GoogleMap map;
	private ActivityUsersMapBinding binding;

	private TrackedUser user;
	private Marker marker;
	private final Observer<String> locationAnnouncer = (history) -> {
		String[] lastLocation = LocationHandler.getLastLocation(history)
				.split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));
		double latitude = Double.parseDouble(lastLocation[1]);
		double longitude = Double.parseDouble(lastLocation[2]);
		marker.setPosition(new LatLng(latitude, longitude));
	};
	private GeoArea geoArea;
	private ArrayList<Circle> circles;

	private AppCompatRadioButton addRB;
	private AppCompatRadioButton removeRB;
	private AppCompatRadioButton moveRB;
	private SwitchCompat alertModeS;
	private AppCompatTextView alertModeTV;
	private View colorV;
	private AppCompatButton saveB;
	private AppCompatButton clearB;
	private AppCompatButton helpB;

	private void initUser() {
		Intent intent = getIntent();
		int userId = intent.getIntExtra("userId", -1);
		if (userId == -1) {
			Log.e(TAG, "User not passed to UsersMapActivity.");
		}
		user = CurrentUser.getTrackedUsers().get(userId);
		if (user == null) {
			Log.e(TAG, "No user found by id in UsersMapActivity.");
		}
	}

	private void initGeoFencing() {

		circles = new ArrayList<>(1);
		Iterator<Map.Entry<Integer, GeoArea>> geoIt = user.geoAreas.entrySet().iterator();
		if (geoIt.hasNext()) {
			geoArea = geoIt.next().getValue();
			if (geoArea.mode == GeoArea.GeoAreaMode.ALERT_WHEN_INSIDE) {
				alertModeS.setChecked(true);
				alertModeTV.setText(R.string.inside);
			}
			else {
				alertModeS.setChecked(false);
				alertModeTV.setText(R.string.outside);
			}
			for (GeoFence geoFence : geoArea.getGeoFences()) {
				Circle circle = map.addCircle(new CircleOptions().center(new LatLng(geoFence.getLatitude(), geoFence.getLongitude()))
						.fillColor(geoAreaAlpha | geoArea.getColor())
						.radius(geoFence.getRadiusMeters())
						.strokeWidth(2)
						.strokeColor(GeoArea.DEFAULT_COLOR));
				circles.add(circle);
			}
		}
		else {
			geoArea = new GeoArea();
		}
	}

	private void initColorPicker() {

		colorV.setBackgroundColor(geoArea.getColor() | geoAreaAlpha);
		ColorPickerDialog colorDialog = new ColorPickerDialog(this);
		colorDialog.setOnSaveListener((view -> {
			// & disregards alpha
			int color = colorDialog.getColor() & 0x00ffffff;
			geoArea.color = color;
			color |= geoAreaAlpha;
			colorV.setBackgroundColor(color);
			colorDialog.cancel();
			for (Circle circle : circles) {
				circle.setFillColor(color);
			}
		}));
		colorV.setOnClickListener((view) -> {
			colorDialog.show(geoArea.getColor() | geoAreaAlpha);
		});
	}

	private void initMarker() {
		String[] dateLatLon = LocationHandler.getLastLocation(user.getLocationHistory()).split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));
		double latitude = 0, longitude = 0;
		try {
			latitude = Double.parseDouble(dateLatLon[1]);
			longitude = Double.parseDouble(dateLatLon[2]);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			Toast.makeText(this, "An error occured when parsing location", Toast.LENGTH_SHORT).show();
			return;
		}

		LatLng targetPosition = new LatLng(latitude, longitude);
		if (marker != null) {
			marker.remove();
			marker = null;
		}
		marker = map.addMarker(new MarkerOptions().position(targetPosition));

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 13f));

		Toast.makeText(this, "Lat: " + targetPosition.latitude + "; Lon: " + targetPosition.longitude, Toast.LENGTH_SHORT).show();
	}

	private void makeCirclesClickable(boolean clickable) {
		for (Circle circle : circles) {
			circle.setClickable(clickable);
		}
	}

	private void initViews() {
		addRB = findViewById(R.id.mapAddRB);
		removeRB = findViewById(R.id.mapRemoveRB);
		saveB = findViewById(R.id.mapSaveB);
		alertModeS = findViewById(R.id.mapAlertModeS);
		alertModeTV = findViewById(R.id.mapAlertModeTV);
		moveRB = findViewById(R.id.mapMoveRB);
		colorV = findViewById(R.id.mapColorV);
		saveB = findViewById(R.id.mapSaveB);
		helpB = findViewById(R.id.mapHelpB);
		clearB = findViewById(R.id.mapClearB);

		GeoControlsHelpDialog helpDialog = new GeoControlsHelpDialog(this);
		helpDialog.setContentView(R.layout.dialog_help_geo_controls);
		helpDialog.setCancelable(true);
		helpB.setOnClickListener((view) -> {
			helpDialog.show();
		});
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityUsersMapBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initViews();
		initUser();
		initGeoFencing();
		initColorPicker();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	private int getCircleRadiusBasedOnZoom(float zoom) {
		// please don't judge me
		if ((int)zoom > 19) {
			return 3;
		}
		else if ((int)zoom == 19) {
			return 6;
		}
		else if ((int)zoom == 18) {
			return 10;
		}
		else if ((int)zoom == 17) {
			return 21;
		}
		else if ((int)zoom == 16) {
			return 42;
		}
		else if ((int)zoom == 15) {
			return 85;
		}
		else if ((int)zoom == 14) {
			return 170;
		}
		else if ((int)zoom == 13) {
			return 340;
		}
		else if ((int)zoom == 12) {
			return 675;
		}
		else if ((int)zoom == 11) {
			return 1550;
		}
		else if ((int)zoom == 10) {
			return 3500;
		}
		else if ((int)zoom == 9) {
			return 7500;
		}
		else if ((int)zoom == 8) {
			return 15625;
		}
		else if ((int)zoom == 7) {
			return 31250;
		}
		else if ((int)zoom == 6) {
			return 62500;
		}
		else if ((int)zoom == 5) {
			return 125000;
		}
		else if ((int)zoom == 4) {
			return 250000;
		}
		else if ((int)zoom == 3) {
			return 500000;
		}
		else {
			return 1000000;
		}
	}

	public void initMapControls() {
		addRB.setOnClickListener((view) -> {
			makeCirclesClickable(false);
			map.setOnCircleClickListener(null);
			map.setOnMapClickListener((position) -> {
				CircleOptions circle = new CircleOptions();
				circle.center(position);
				circle.fillColor(geoAreaAlpha | geoArea.getColor());
				circle.strokeWidth(2);
				circle.strokeColor(GeoArea.DEFAULT_STROKE_COLOR);
				circle.radius(getCircleRadiusBasedOnZoom(map.getCameraPosition().zoom));
				circle.clickable(false);
				Circle newCircle = map.addCircle(circle);
				circles.add(newCircle);
			});
			map.setOnMapLongClickListener((position) -> {
				int circlesSize = circles.size();
				if (circlesSize < 1) {
					return;
				}
				Circle circle = circles.get(circlesSize - 1);
				float[] results = new float[1];
				Location.distanceBetween(circle.getCenter().latitude,
						circle.getCenter().longitude,
						position.latitude,
						position.longitude,
						results);
				circle.setRadius((int)results[0]);
				Toast.makeText(this, "Distance: " + results[0], Toast.LENGTH_SHORT).show();
			});
		});

		removeRB.setOnClickListener((view) -> {
			makeCirclesClickable(true);
			map.setOnMapClickListener(null);
			map.setOnMapLongClickListener(null);
			map.setOnCircleClickListener((circle -> {
				circle.remove();
				circles.remove(circle);
			}));
		});

		moveRB.setOnClickListener((view) -> {
			map.setOnMapClickListener(null);
			map.setOnCircleClickListener(null);
			map.setOnMapLongClickListener(null);
			makeCirclesClickable(false);
		});

		alertModeS.setOnClickListener((view) -> {
			if (!(view instanceof SwitchCompat)) {
				Log.e(TAG, "View that was passed when pressing geofencing mode switch was a " + view.getClass() + "and not a SwitchCompat.");
				return;
			}
			SwitchCompat switchCompat = (SwitchCompat)view;
			if (switchCompat.isChecked()) {
				geoArea.mode = GeoArea.GeoAreaMode.ALERT_WHEN_INSIDE;
				alertModeTV.setText(R.string.inside);
			}
			else {
				geoArea.mode = GeoArea.GeoAreaMode.ALERT_WHEN_OUTSIDE;
				alertModeTV.setText(R.string.outside);
			}
		});

		clearB.setOnClickListener((view) -> {
			new AlertDialog.Builder(this)
					.setCancelable(true)
					.setTitle(R.string.delete_geofences_confirmation)
					.setPositiveButton(R.string.delete_them, (dialogInterface, i) -> {
						Iterator<Circle> iterator = circles.iterator();
						while (iterator.hasNext()) {
							// remove circle from map
							iterator.next().remove();
							// remove circle from array list
							iterator.remove();
						}
					})
					.setNegativeButton(R.string.keep_them, (dialogInterface, i) -> {
						dialogInterface.cancel();
					})
					.show();
		});
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		initMarker();
		user.subscribeLocationHistory(locationAnnouncer);
		initMapControls();
	}

	@Override
	protected void onPause() {
		super.onPause();
		user.unsubscribeLocationHistory(locationAnnouncer);
	}
}