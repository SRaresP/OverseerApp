package com.example.overseerapp.ui;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.tracking.TrackedUser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.overseerapp.databinding.ActivityUsersMapBinding;

public class UsersMapActivity extends FragmentActivity implements OnMapReadyCallback {
	private static final String TAG = "UsersMapActivity";

	private GoogleMap mMap;
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
		mMap = googleMap;

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
		marker = mMap.addMarker(new MarkerOptions().position(targetPosition));

		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetPosition, 13f));

		Toast.makeText(this, "Lat: " + targetPosition.latitude + "; Lon: " + targetPosition.longitude, Toast.LENGTH_SHORT).show();

		user.subscribeLocationHistory(locationAnnouncer);
	}

	@Override
	protected void onPause() {
		super.onPause();
		user.unsubscribeLocationHistory(locationAnnouncer);
	}
}