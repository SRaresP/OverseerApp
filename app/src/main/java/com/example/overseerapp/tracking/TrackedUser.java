package com.example.overseerapp.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.GeoArea;
import com.example.overseerapp.location.GeoFence;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.ui.UsersMapActivity;
import com.google.android.gms.maps.model.LatLng;

import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TrackedUser {
	private static final String TAG = "TrackedUser";
	// errors defined in ms
	private static final long MINIMUM_ERROR = 250;
	private static final long MAXIMUM_ERROR = 30000;

	private final int id;
	private final String name;
	private MutableLiveData<String> locationHistory;
	private int updateInterval;
	public HashMap<Integer, GeoArea> geoAreas;
	private Timer updateTimer;
	private TrackingTimerTask trackingTimerTask;
	private NotificationCompat.Builder notificationBuilder;

	private void init() {
		OverseerApp overseerApp = OverseerApp.getInstance();
		Intent toAppIntent = new Intent(overseerApp, UsersMapActivity.class);
		toAppIntent.putExtra("userId", id);
		toAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// when creating this pending intent, use a hash that takes in all the data that can change into
		// the request code, otherwise old, outdated intents can be sent without the proper data
		PendingIntent notificationPendingIntent = PendingIntent.getActivity(
				overseerApp,
				toAppIntent.hashCode(),
				toAppIntent,
				PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder =
				new NotificationCompat.Builder(OverseerApp.getInstance(), OverseerApp.ALERTS_NOTIFICATION_CHANNEL)
				.setSmallIcon(R.drawable.ic_baseline_alert_location_24)
				.setContentTitle(name + "#" + id + " has trespassed.")
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setAutoCancel(true)
				.setContentIntent(notificationPendingIntent)
				.setOngoing(false)
				.setCategory(Notification.CATEGORY_ALARM)
				.setOnlyAlertOnce(true);
	}

	public TrackedUser(int id, @NonNull String Name) {
		this.id = id;
		this.name = Name;
		locationHistory = new MutableLiveData<>();
		init();
	}

	public TrackedUser(int id, @NonNull String Name, @NonNull String locationHistory) {
		this.id = id;
		this.name = Name;
		this.locationHistory = new MutableLiveData<>(locationHistory);
		init();
	}

	public TrackedUser(int id, @NonNull String Name, @NonNull String locationHistory, int updateInterval) {
		this.id = id;
		this.name = Name;
		this.locationHistory = new MutableLiveData<>(locationHistory);
		this.updateInterval = updateInterval;
		updateTimer = new Timer("timer#" + id, true);
		trackingTimerTask = new TrackingTimerTask(id, this);
		init();
	}

	public TrackedUser(String trackedUser) {
		String[] userDetails = trackedUser.split(String.valueOf(OverseerApp.USER_SEPARATOR));
		this.id = Integer.parseInt(userDetails[0]);
		this.name = userDetails[1];
		this.locationHistory = new MutableLiveData<>(userDetails[2]);
		this.updateInterval = Integer.parseInt(userDetails[3]);
		init();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLocationHistory() {
		return locationHistory.getValue();
	}

	public void updateLocationHistory(String locationHistory) {
		this.locationHistory.setValue(locationHistory);
	}

	public void subscribeLocationHistory(Observer<String> callback) {
		this.locationHistory.observeForever(callback);
	}

	public void unsubscribeLocationHistory(Observer<String> callback) {
		this.locationHistory.removeObserver(callback);
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj instanceof TrackedUser) {
			TrackedUser parameter = (TrackedUser) obj;
			return id == parameter.id &&
					name.equals(parameter.name) &&
					locationHistory.equals(parameter.locationHistory) &&
					updateInterval == parameter.updateInterval;
		} else return false;
	}

	private class TrackingTimerTask extends TimerTask {
		private static final String TAG = "TrackingTimerTask";
		private static final String ALERTS_GROUP = "ALERTS_GROUP";

		private boolean cancelled;

		private final int id;
		private final TrackedUser user;

		public TrackingTimerTask(int id, TrackedUser user) {
			this.id = id;
			this.user = user;
			this.cancelled = false;
		}

		private void notifyTrespass(OverseerApp overseerApp, GeoArea geoArea) {
			notificationBuilder.setContentTitle(name + "#" + id + " has trespassed.");
			notificationBuilder.setContentText(geoArea.triggerMessage);
			notificationBuilder.setGroup(ALERTS_GROUP);
			Notification alertNotification = notificationBuilder.build();
			NotificationManager notificationManager = overseerApp.getSystemService(NotificationManager.class);
			notificationManager.notify(user.hashCode() + id, alertNotification);
		}

		private void notifyOldLocation(OverseerApp overseerApp, Date locationTime) {
			notificationBuilder.setContentTitle(user.name + "#" + id + " did not send timely location update.");
			notificationBuilder.setContentText("Location last recorded " + locationTime.toString());
			notificationBuilder.setGroup(ALERTS_GROUP);
			Notification alertNotification = notificationBuilder.build();
			NotificationManager notificationManager = overseerApp.getSystemService(NotificationManager.class);
			notificationManager.notify(user.hashCode() + id, alertNotification);
		}

		private long getAllowedTimeDifferenceError(long updateInterval) {
			long error = updateInterval / 10;
			if (error > MAXIMUM_ERROR) error = MAXIMUM_ERROR;
			if (error < MINIMUM_ERROR) error = MINIMUM_ERROR;
			return error;
		}

		@Override
		public void run() {
			OverseerApp overseerApp = OverseerApp.getInstance();
//			overseerApp.getMainThreadHandler().post(() -> {
//				Toast.makeText(overseerApp, "Updating location for " + user.name + "#" + user.id, Toast.LENGTH_SHORT).show();
//			});
			try {
				Socket socket = ServerHandler.getTargetLocationAndInterval(id);
				String[] response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));
				switch (response[0]) {
					case ServerHandler.GOT_TARGET_LOCATION_AND_INTERVAL:
						overseerApp.getMainThreadHandler().post(() -> {
							// update location
							user.updateLocationHistory(response[1]);
							// prepare data for checks
							String[] lastLoc = LocationHandler.getLastLocation(Objects.requireNonNull(user.locationHistory.getValue())).split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));
							Date locationTime = new Date(Long.parseLong(lastLoc[0]));
							LatLng lastLocation = new LatLng(
									Double.parseDouble(lastLoc[1]),
									Double.parseDouble(lastLoc[2])
							);
							try {
								// check location
								GeoArea geoArea = user.geoAreas.entrySet().iterator().next().getValue();
								boolean forbiddenLocation = geoArea.mode == GeoArea.GeoAreaMode.ALERT_WHEN_OUTSIDE;
								// only one geo area is supported for now
								float[] distanceResults = new float[1];
								// check if location is inside one of the geofences
								for (GeoFence geoFence : geoArea.geoFences) {
									Location.distanceBetween(
											lastLocation.latitude,
											lastLocation.longitude,
											geoFence.getLatitude(),
											geoFence.getLongitude(), distanceResults);
									if (distanceResults[0] <= geoFence.getRadiusMeters()) {
										forbiddenLocation = !forbiddenLocation;
										break;
									}
								}
								if (forbiddenLocation) {
									notifyTrespass(overseerApp, geoArea);
								}
							} catch (NoSuchElementException e) {
								Log.i(TAG, "The user " + user.name + "#" + user.id + " probably had no geo areas.");
								Log.i(TAG, e.getMessage(), e);
							}
							// check time
							try {
								Date currentTime = new Date();
								// allow error to account for networking and performance issues
								if (currentTime.getTime() - locationTime.getTime() >
										user.updateInterval + getAllowedTimeDifferenceError(user.updateInterval))
								{
									notifyOldLocation(overseerApp, locationTime);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
						});
						user.setUpdateInterval(Integer.parseInt(response[2]));
						break;
					case ServerHandler.NOT_FOUND:
						Log.e(TAG, "Overseer not found by server when trying to get location and interval for user id: " + id);
						break;
					case ServerHandler.WRONG_PASSWORD:
						Log.e(TAG, "Wrong overseer password when trying to get location and interval for user id: " + id);
						break;
					case ServerHandler.NOT_A_TARGET_ID:
						Log.e(TAG, "Not a target id when trying to get location and interval for user id: " + id);
					default:
						Log.e(TAG, "Undefined server response when trying to get location and interval for user id: " + id + "Response: " + String.join("", response));
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			if (cancelled) return;
			this.cancel();
			trackingTimerTask = new TrackingTimerTask(id, TrackedUser.this);
			updateTimer.schedule(trackingTimerTask, user.getUpdateInterval());
		}

		@Override
		public boolean cancel() {
			cancelled = true;
			return super.cancel();
		}
	}

	public void scheduleUpdates() {
		try {
			trackingTimerTask.cancel();
		} catch (NullPointerException e) {
			Log.i(TAG, e.getMessage());
		}
		try {
			updateTimer.cancel();
			updateTimer.purge();
		} catch (NullPointerException e) {
			Log.i(TAG, e.getMessage());
		}
		updateTimer = new Timer("timer#" + id, true);
		trackingTimerTask = new TrackingTimerTask(id, this);
		updateTimer.schedule(trackingTimerTask, getUpdateInterval());
	}

	public void stopUpdates() {
		try {
			trackingTimerTask.cancel();
		} catch (NullPointerException e) {
			Log.i(TAG, e.getMessage());
		}
		try {
			updateTimer.cancel();
			updateTimer.purge();
		} catch (NullPointerException e) {
			Log.i(TAG, e.getMessage());
		}
	}
}
