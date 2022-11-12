package com.example.overseerapp.tracking;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;

import java.net.Socket;

public class TrackedUsersHandler {
	private static final String TAG = "TrackedUsersHandler";
	// SHOULD BE THE SAME AS THE CAPACITY SET IN SERVER
	private static final int CAPACITY = 20;

	private static TrackedUser[] trackedUsers = new TrackedUser[CAPACITY];
	private static int additionIndex = 0;

	private TrackedUsersHandler() {
	}

	public static void setUsersFromString(@NonNull String trackedUsersString) {
		String[] userStrings = trackedUsersString.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
		TrackedUser[] users = new TrackedUser[userStrings.length];

		String[] userDetails;
		int id;
		String name;
		for (additionIndex = 0; additionIndex < userStrings.length; ++additionIndex) {
			userDetails = userStrings[additionIndex].split(String.valueOf(OverseerApp.ID_NAME_SEPARATOR));
			id = Integer.parseInt(userDetails[0]);
			name = userDetails[1];
			users[additionIndex] = new TrackedUser(id, name);
		}
		trackedUsers = users;
	}

	public static TrackedUser[] getUsers() {
		return trackedUsers;
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

	public static int freeSlots() {
		return CAPACITY - additionIndex;
	}

	public static void updateUser(int userId, String locationHistory) throws UserNotFoundException {
		TrackedUser foundUser = null;
		for (TrackedUser user : trackedUsers) {
			if (user.getId() == userId) {
				foundUser = user;
				break;
			}
		}
		if (foundUser == null) {
			throw new UserNotFoundException("Could not find user when updating user id " + userId);
		}
		foundUser.updateLocationHistory(locationHistory);
	}

	public static void addTrackedUsersFromIds() {
		OverseerApp overseerApp = OverseerApp.getInstance();
		String[] IDs = CurrentUser.trackedUserIDs.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
		for (String id : IDs) {
			if (id.equals("")) continue;
			try {
				Socket socket = ServerHandler.getUser(id);
				String[] response = ServerHandler.receive(socket).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

				//didn't receive location history
				if (!response[0].equals(ServerHandler.GOT_USER)) {
					Log.e(TAG, "Could not get location histories for id: " + id + ". Received response : " + response[0]);
					continue;
				}

				//received location history
				TrackedUsersHandler.addUserFromString(response[1]);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public static void updateTrackedUsersFromIds() {
		OverseerApp overseerApp = OverseerApp.getInstance();
		String[] IDs = CurrentUser.trackedUserIDs.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
		for (String id : IDs) {
			if (id.equals("")) continue;
			try {
				Socket socket = ServerHandler.getUser(id);
				String[] response = ServerHandler.receive(socket).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

				//didn't receive location history
				if (!response[0].equals(ServerHandler.GOT_USER)) {
					Log.e(TAG, "Could not get location histories for id: " + id + ". Received response : " + response[0]);
					continue;
				}

				//received location history
				TrackedUsersHandler.updateUser(Integer.parseInt(id),
						response[1].split(String.valueOf(OverseerApp.USER_SEPARATOR))[2]);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}
