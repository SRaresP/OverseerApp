package com.example.overseerapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.overseerapp.R;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.tracking.TrackedUser;

public class TrackedUserSettingsActivity extends AppCompatActivity {
	private static final String TAG = "TrackedUserSettingsActivity";

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
			Log.e(TAG, "Null user not passed to TrackedUserSettingsActivity.");
			return;
		}

		TextView targetUserTV = findViewById(R.id.trsetUserTV);
		targetUserTV.setText(user.getName());
	}
}