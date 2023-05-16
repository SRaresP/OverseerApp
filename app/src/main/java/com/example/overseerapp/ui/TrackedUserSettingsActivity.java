package com.example.overseerapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.tracking.TrackedUser;

import java.io.IOException;
import java.net.Socket;

public class TrackedUserSettingsActivity extends AppCompatActivity {
	private static final String TAG = "TrackedUserSettingsActivity";

	private EditText targetIntervalET;

	private void resetIntervalDisplay(int targetId) {
		OverseerApp overseerApp = OverseerApp.getInstance();
		overseerApp.getExecutorService().execute(() -> {
			try {
				Socket socket = ServerHandler.getSettings(targetId);
				String[] response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));
				switch (response[0]) {
					case ServerHandler.GOT_SETTINGS:
						int intervalSeconds = Integer.parseInt(response[1].trim()) / 1000;
						overseerApp.getMainThreadHandler().post(() -> {
							targetIntervalET.setText(String.valueOf(intervalSeconds));
						});
						break;
					case ServerHandler.NOT_FOUND:
						Log.e(TAG, "Overseer not found after requesting target settings.");
						break;
					case ServerHandler.WRONG_PASSWORD:
						Log.e(TAG, "Wrong password response received after requesting target settings.");
						break;
					case ServerHandler.NOT_A_TARGET_ID:
						Log.e(TAG, "Not a target id response received after requesting target settings. Id: " + targetId);
						break;
					default:
						Log.e(TAG, "Undefined response received after requesting target settings: " + String.join("", response));
						break;
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		});
	}

	private void sendSettingsToServer(int targetId) {
		int interval;
		try {
			interval = Integer.parseInt(targetIntervalET.getText().toString());
			long value = interval * 1000L;
			if (value > Integer.MAX_VALUE) {
				throw new NumberFormatException("Interval value was above max integer value.");
			}
			interval = (int)value;
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Toast.makeText(this, getString(R.string.interval_was_invalid, Integer.MAX_VALUE), Toast.LENGTH_SHORT).show();
			return;
		}
		OverseerApp overseerApp = OverseerApp.getInstance();
		int finalInterval = interval;
		overseerApp.getExecutorService().execute(() -> {
			try {
				Socket socket = ServerHandler.changeSettings(targetId, finalInterval);
				String[] response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));
				switch (response[0]) {
					case ServerHandler.CHANGED_SETTINGS:
						overseerApp.getMainThreadHandler().post(() -> {
							Toast.makeText(this, getString(R.string.successful_settings_update), Toast.LENGTH_SHORT).show();
						});
						break;
					case ServerHandler.NOT_FOUND:
						Log.e(TAG, "Overseer not found after requesting target settings.");
						break;
					case ServerHandler.WRONG_PASSWORD:
						Log.e(TAG, "Wrong password response received after requesting target settings.");
						break;
					case ServerHandler.NOT_A_TARGET_ID:
						Log.e(TAG, "Not a target id response received after requesting target settings. Id: " + targetId);
						break;
					case ServerHandler.NOT_AN_INTERVAL:
						Log.e(TAG, "Not an interval response received after requesting target settings. Interval: " + finalInterval);
						break;
					default:
						Log.e(TAG, "Undefined response received after requesting target settings: " + String.join("", response));
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracked_user_settings);

		AppCompatImageButton backB = findViewById(R.id.trsetBackB);
		backB.setOnClickListener((view) -> {
			finish();
		});

		Intent intent = getIntent();
		int userId = intent.getIntExtra("userId", -1);
		if (userId == -1) {
			Log.e(TAG, "User not passed to TrackedUserSettingsActivity.");
		}
		TrackedUser user = CurrentUser.getTrackedUsers().get(userId);
		if (user == null) {
			Log.e(TAG, "No user found by id in TrackedUserSettingsActivity.");
			return;
		}

		TextView targetUserTV = findViewById(R.id.trsetUserTV);
		targetIntervalET = findViewById(R.id.trsetIntervalET);
		AppCompatImageButton resetIntervalB = findViewById(R.id.trsetResetIntervalB);
		AppCompatButton saveB = findViewById(R.id.trsetSaveB);

		targetUserTV.setText(user.getName());

		resetIntervalDisplay(userId);

		resetIntervalB.setOnClickListener((view) -> resetIntervalDisplay(userId));

		saveB.setOnClickListener((view) -> { sendSettingsToServer(userId); });
	}
}