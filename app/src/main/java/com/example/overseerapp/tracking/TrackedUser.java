package com.example.overseerapp.tracking;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.overseerapp.location.LocationHandler;

public class TrackedUser {
	private final int id;
	private final String name;
	private String locationHistory;

	public TrackedUser(int id, @NonNull String Name) {
		this.id = id;
		this.name = Name;
	}

	public TrackedUser(int id, @NonNull String Name, @NonNull String locationHistory) {
		this.id = id;
		this.name = Name;
		this.locationHistory = locationHistory;
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

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj instanceof TrackedUser) {
			TrackedUser parameter = (TrackedUser) obj;
			return id == parameter.id &&
					name.equals(parameter.name) &&
					locationHistory.equals(parameter.locationHistory);
		} else return false;
	}
}
