package com.example.overseerapp.tracking;

import androidx.annotation.NonNull;

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
}
