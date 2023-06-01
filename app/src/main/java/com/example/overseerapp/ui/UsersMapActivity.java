package com.example.overseerapp.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.GeoArea;
import com.example.overseerapp.location.GeoFence;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.tracking.TrackedUser;
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
	private ArrayList<Circle> geoFences;

	private AppCompatRadioButton addRB;
	private AppCompatRadioButton removeRB;
	private AppCompatRadioButton moveRB;
	private AppCompatButton saveB;
	private SwitchCompat alertModeS;
	private AppCompatTextView alertModeTV;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		binding = ActivityUsersMapBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
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

		Intent intent = getIntent();
		int userId = intent.getIntExtra("userId", -1);
		if (userId == -1) {
			Log.e(TAG, "User not passed to UsersMapActivity.");
		}
		user = CurrentUser.getTrackedUsers().get(userId);
		if (user == null) {
			Log.e(TAG, "No user found by id in UsersMapActivity.");
			return;
		}

		geoFences = new ArrayList<>(1);
		Iterator<Map.Entry<Integer, GeoArea>> geoIt = user.geoAreas.entrySet().iterator();
		if (geoIt.hasNext()) {
			geoArea = geoIt.next().getValue();
			if (geoArea.mode == GeoArea.GeoAreaMode.ALERT_WHEN_INSIDE) {
				alertModeS.setChecked(true);
				alertModeTV.setText(R.string.alert_inside);
			}
			else {
				alertModeS.setChecked(false);
				alertModeTV.setText(R.string.alert_outside);
			}
			for (GeoFence geoFence : geoArea.getGeoFences()) {
				Circle circle = map.addCircle(new CircleOptions().center(new LatLng(geoFence.getLatitude(), geoFence.getLongitude()))
						.fillColor(geoAreaAlpha | geoArea.getColor())
						.radius(geoFence.getRadiusMeters())
						.strokeWidth(2)
						.strokeColor(0xffffffff));
				geoFences.add(circle);
			}
		}
		else {
			geoArea = new GeoArea();
		}

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
		marker = map.addMarker(new MarkerOptions().position(targetPosition));

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 13f));

		Toast.makeText(this, "Lat: " + targetPosition.latitude + "; Lon: " + targetPosition.longitude, Toast.LENGTH_SHORT).show();

		user.subscribeLocationHistory(locationAnnouncer);

		addRB = findViewById(R.id.mapAddRB);
		removeRB = findViewById(R.id.mapRemoveRB);
		saveB = findViewById(R.id.mapSaveB);
		alertModeS = findViewById(R.id.mapAlertModeS);
		alertModeTV = findViewById(R.id.mapAlertModeTV);
		moveRB = findViewById(R.id.mapMoveRB);

		addRB.setOnClickListener((view) -> {
			map.setOnCircleClickListener(null);
			map.setOnMapClickListener((position) -> {
				CircleOptions circle = new CircleOptions();
				circle.center(position);
				circle.fillColor(geoAreaAlpha | 0xffffff);
				circle.strokeWidth(2);
				circle.strokeColor(0xffffffff);
				circle.radius(500);
				circle.clickable(true);
				Circle newCircle = map.addCircle(circle);
				geoFences.add(newCircle);
			});
		});

		removeRB.setOnClickListener((view) -> {
			map.setOnMapClickListener(null);
			map.setOnCircleClickListener((circle -> {
				Log.e(TAG, "shtuff");
				circle.remove();
				geoFences.remove(circle);
			}));
		});

		moveRB.setOnClickListener((view) -> {
			map.setOnMapClickListener(null);
			map.setOnCircleClickListener(null);
		});

		alertModeS.setOnClickListener((view) -> {
			if (!(view instanceof SwitchCompat)) {
				Log.e(TAG, "View that was passed when pressing geofencing mode switch was a " + view.getClass() + "and not a SwitchCompat.");
				return;
			}
			SwitchCompat switchCompat = (SwitchCompat)view;
			if (switchCompat.isChecked()) {
				geoArea.mode = GeoArea.GeoAreaMode.ALERT_WHEN_INSIDE;
				alertModeTV.setText(R.string.alert_inside);
			}
			else {
				geoArea.mode = GeoArea.GeoAreaMode.ALERT_WHEN_OUTSIDE;
				alertModeTV.setText(R.string.alert_outside);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		user.unsubscribeLocationHistory(locationAnnouncer);
	}
}