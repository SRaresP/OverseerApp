package com.example.overseerapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.overseerapp.R;
import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.tracking.TrackerService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

public class PrimaryActivity extends AppCompatActivity {
	private static final String TAG = "PrimaryActivity";

	private void startTrackerService() {
		Intent intent = new Intent(this, TrackerService.class);
		startForegroundService(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_primary);

		TrackingListFragment trackingListFragment = new TrackingListFragment();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.primaryWorkLayout, trackingListFragment);
		fragmentTransaction.commit();

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		AlertDialog noNotifPerdialog;
		if (!notificationManager.areNotificationsEnabled()) {
			noNotifPerdialog = new AlertDialog.Builder(this)
					.setCancelable(true)
					.setTitle("No notification permission.")
					.setMessage("Please enable app notifications for OverseerApp and try restarting the application. The app will quit in 10 seconds.")
					.create();
			noNotifPerdialog.show();
			Timer timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					noNotifPerdialog.dismiss();
					PrimaryActivity.this.finish();
				}
			}, 10000);
			return;
		}

		startTrackerService();
	}
}