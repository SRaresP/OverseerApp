package com.example.overseerapp.tracking;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.ServerHandler;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TrackedUser {
	private static final String TAG = "TrackedUser";

	private final int id;
	private final String name;
	private String locationHistory;
	private int updateInterval;
	private Timer updateTimer;
	private TrackingTimerTask trackingTimerTask;

	public TrackedUser(int id, @NonNull String Name) {
		this.id = id;
		this.name = Name;
	}

	public TrackedUser(int id, @NonNull String Name, @NonNull String locationHistory) {
		this.id = id;
		this.name = Name;
		this.locationHistory = locationHistory;
	}

	public TrackedUser(int id, @NonNull String Name, @NonNull String locationHistory, @NonNull int updateInterval) {
		this.id = id;
		this.name = Name;
		this.locationHistory = locationHistory;
		this.updateInterval = updateInterval;
		updateTimer = new Timer("timer#" + id, true);
		trackingTimerTask = new TrackingTimerTask(id, this);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLocationHistory() { return locationHistory; }

	public void updateLocationHistory(String locationHistory) {
		this.locationHistory = locationHistory;
	}

	public int getUpdateInterval() { return updateInterval; }

	public void setUpdateInterval(int updateInterval) { this.updateInterval = updateInterval; }

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

		private boolean cancelled;

		private final int id;
		private final TrackedUser user;

		public TrackingTimerTask(int id, TrackedUser user) {
			this.id = id;
			this.user = user;
			this.cancelled = false;
		}

		@Override
		public void run() {
			try {
				Socket socket = ServerHandler.getTargetLocationAndInterval(id);
				String[] response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));
				switch (response[0]) {
					case ServerHandler.GOT_TARGET_LOCATION_AND_INTERVAL:
						user.updateLocationHistory(response[1]);
						user.setUpdateInterval(Integer.parseInt(response[2]));
						Log.e(TAG, "Last location: " + LocationHandler.getLastLocation(user.getLocationHistory()));
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
		try{
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
		try{
			updateTimer.cancel();
			updateTimer.purge();
		} catch (NullPointerException e) {
			Log.i(TAG, e.getMessage());
		}
	}
}
