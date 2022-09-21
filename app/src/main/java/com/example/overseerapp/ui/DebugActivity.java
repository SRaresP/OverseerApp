package com.example.overseerapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.overseerapp.Location.LocationHandler;
import com.example.overseerapp.R;
import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.server_comm.exceptions.EmptyMessageException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

//TODO: send provider thru intent
public class DebugActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private TextView mainLatValueTV;
	private TextView mainLonValueTV;
	private TextView mainHistoryValueTV;
	private TextView mainDateValueTV;
	private AppCompatButton mainSendBTN;
	private AppCompatButton mainGetCodeBTN;
	private TextView mainCodeValueTV;

	private LocationRequest locationRequest;
	private LocationCallback locationCallBack;
	private FusedLocationProviderClient fusedLocationProviderClient;

	private static boolean arePermissionsGranted(Context context) {
		boolean hasInternetPermission =
				ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
						== PackageManager.PERMISSION_GRANTED;
		return hasInternetPermission;
	}

	private static void requestPermissions(Activity activity) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(
				activity,
				Manifest.permission.ACCESS_COARSE_LOCATION)) {
			//TODO: display a dialogue to explain why the app needs the permission
		}

		ActivityCompat.requestPermissions(
				activity,
				new String[]{Manifest.permission.INTERNET},
				OverseerApp.PERM_REQ_CODE);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == OverseerApp.PERM_REQ_CODE) {
			if ((grantResults.length < 1) || (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
				Toast.makeText(this, "App needs all of these permissions to run", Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
			}
		}
	}

	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);

		if (!arePermissionsGranted(this)) {
			requestPermissions(this);
		}

		mainLatValueTV = findViewById(R.id.mainLatValueTV);
		mainLonValueTV = findViewById(R.id.mainLonValueTV);
		mainSendBTN = findViewById(R.id.mainSendBTN);
		mainHistoryValueTV = findViewById(R.id.mainHistoryValueTV);
		mainDateValueTV = findViewById(R.id.mainDateValueTV);
		mainGetCodeBTN = findViewById(R.id.mainGetCodeB);
		mainCodeValueTV = findViewById(R.id.mainCodeValueTV);

		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
	}
}