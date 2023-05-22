package com.example.overseerapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;

import java.net.Socket;

public class UserEntryLayout extends LinearLayoutCompat {
	private static String TAG = "UserEntryLayout";

	//mandatory constructors for Android to use for whatever it needs
	public UserEntryLayout(@NonNull Context context) {
		super(context);
	}

	public UserEntryLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public UserEntryLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	//constructor that is actually used in app code
	public UserEntryLayout(@NonNull Context context, Fragment fragmentToRefresh, @NonNull String userName, int userId, @NonNull String location, @NonNull String dateRecordedLocation) {
		super(context);

		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayoutCompat.HORIZONTAL);
		setGravity(Gravity.CENTER);

		//create user identifier label
		AppCompatTextView userIdentifierTV = new AppCompatTextView(context);
		userIdentifierTV.setId(ViewCompat.generateViewId());
		userIdentifierTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		userIdentifierTV.setText(context.getString(R.string.user_identifier, userName, userId));

		//create address label
		AppCompatTextView addressTV = new AppCompatTextView(context);
		addressTV.setId(ViewCompat.generateViewId());
		addressTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addressTV.setText(location);

		//create date label
		AppCompatTextView timeAgoTV = new AppCompatTextView(context);
		timeAgoTV.setId(ViewCompat.generateViewId());
		timeAgoTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		timeAgoTV.setText(dateRecordedLocation);

		//create the text wrapper
		LinearLayoutCompat detailsL = new LinearLayoutCompat(context);
		detailsL.setId(ViewCompat.generateViewId());
		detailsL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		detailsL.setOrientation(LinearLayoutCompat.VERTICAL);
		detailsL.addView(userIdentifierTV);
		detailsL.addView(addressTV);
		detailsL.addView(timeAgoTV);

		//create settings button
		AppCompatImageButton settingsB = new AppCompatImageButton(context);
		settingsB.setId(ViewCompat.generateViewId());
		settingsB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		settingsB.setImageResource(R.drawable.ic_baseline_settings_24);
		settingsB.setPadding(15, 15, 15, 15);
		settingsB.setOnClickListener((view) -> {
			Intent intent = new Intent(context, TrackedUserSettingsActivity.class);
			intent.putExtra("userId", userId);
			context.startActivity(intent);
		});

		//create delete button
		AppCompatImageButton deleteB = new AppCompatImageButton(context);
		deleteB.setId(ViewCompat.generateViewId());
		deleteB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		deleteB.setImageResource(R.drawable.ic_baseline_delete_24);
		deleteB.setPadding(15, 15, 15, 15);
		deleteB.setOnClickListener((view) -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.are_you_sure_you_want_to_stop_tracking_this_user)
					.setCancelable(true)
					.setNegativeButton(R.string.No_keep_following_them, (DialogInterface dialogInterface, int i) -> {
					})
					.setPositiveButton(R.string.yes, (DialogInterface dialogInterface, int i) -> {
						OverseerApp overseerApp = OverseerApp.getInstance();
						overseerApp.getExecutorService().execute(() -> {
							try {
								Socket socket = ServerHandler.removeTarget(userId);
								String[] response = ServerHandler.receive(socket).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

								if (response[0].trim().equals(ServerHandler.REMOVED_TARGET)) {
									CurrentUser.removeTrackedUserId(userId);
									// Call onResume to refresh list of tracked users
									if (fragmentToRefresh != null) {
										overseerApp.getMainThreadHandler().post(fragmentToRefresh::onResume);
									}
								}
								else {
									overseerApp.getMainThreadHandler().post(() -> {
										AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
										builder1.setTitle("Removing the user went wrong.")
												.setNeutralButton(R.string.ok, (dialogInterface1, i1) -> {})
												.show();
									});
								}
							} catch (Exception e) {
								overseerApp.getMainThreadHandler().post(() -> {
									AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
									builder1.setTitle("Removing the user went wrong.")
											.setNeutralButton(R.string.ok, (dialogInterface1, i1) -> {})
											.show();
								});
								Log.e(TAG, e.getMessage());
							}
						});
					})
					.show();
		});

		//create the map button
		AppCompatImageButton mapB = new AppCompatImageButton(context);
		mapB.setId(ViewCompat.generateViewId());
		mapB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mapB.setImageResource(R.drawable.ic_baseline_location_on_24);
		mapB.setPadding(15, 15, 15, 15);
		mapB.setOnClickListener((view) -> {
			Intent intent = new Intent(context, UsersMapActivity.class);
			intent.putExtra("userId", userId);
			context.startActivity(intent);
		});

		//create the buttons wrapper
		LinearLayoutCompat buttonsL = new LinearLayoutCompat(context);
		buttonsL.setId(ViewCompat.generateViewId());
		buttonsL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		buttonsL.setOrientation(LinearLayoutCompat.VERTICAL);
		buttonsL.addView(settingsB);
		buttonsL.addView(deleteB);


		//add everything to the layout
		this.addView(detailsL);
		this.addView(mapB);
		this.addView(buttonsL);
	}
}
