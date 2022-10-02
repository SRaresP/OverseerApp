package com.example.overseerapp.tracking;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;

public class TrackedUsersHandler {
	private static final int CAPACITY = 20;

	private static TrackedUser[] trackedUsers = new TrackedUser[CAPACITY];
	private static int additionIndex = 0;

	private TrackedUsersHandler() { }

	public static TrackedUser[] getUsersFromString(@NonNull String trackedUsersString) {
		String[] userStrings = trackedUsersString.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
		TrackedUser[] users = new TrackedUser[userStrings.length];

		String[] userDetails;
		int id;
		String name;
		for (int i = 0; i < userStrings.length; ++i) {
			userDetails = userStrings[i].split(String.valueOf(OverseerApp.ID_NAME_SEPARATOR));
			id = Integer.parseInt(userDetails[0]);
			name = userDetails[1];
			users[i] = new TrackedUser(id, name);
		}
		return users;
	}

	public static void reset() {
		trackedUsers = new TrackedUser[CAPACITY];
		additionIndex = 0;
	}

	public static void addUserFromString(@NonNull String trackedUserString) {
		String[] userDetails = trackedUserString.split(String.valueOf(OverseerApp.USER_SEPARATOR));
		TrackedUser trackedUser = new TrackedUser(Integer.parseInt(userDetails[0]), userDetails[1], userDetails[2]);
		trackedUsers[additionIndex] = trackedUser;
		++additionIndex;
	}

	public static TrackedUser[] getTrackedUsers() {
		return trackedUsers;
	}
}
