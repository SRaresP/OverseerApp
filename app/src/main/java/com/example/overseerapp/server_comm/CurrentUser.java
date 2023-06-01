package com.example.overseerapp.server_comm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.GeoArea;
import com.example.overseerapp.server_comm.exceptions.TrackingSlotsAmountException;
import com.example.overseerapp.storage.EncryptedStorageController;
import com.example.overseerapp.tracking.TrackedUser;
import com.example.overseerapp.tracking.UserNotFoundException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CurrentUser {
	private static final String TAG = "CurrentUser";
	private static final String SAVED_USER_FILENAME = "CurrentUser";
	// SHOULD BE THE SAME AS THE CAPACITY SET IN SERVER
	private static final int TRACKED_USER_CAPACITY = 20;

	// stored on disk
	public static String email = "";
	public static String name = "";
	public static String password = "";
	private static String trackedUserIDs = "";

	// not stored on disk
	private static HashMap<Integer, TrackedUser> trackedUsers = new HashMap(TRACKED_USER_CAPACITY);
	private static long currentNrOfTrackedUserIds = 0;
	private static int additionIndex = 0;

	// do not call constructor, use static methods
	private CurrentUser() { }

	public static void setCurrentUser(final @NonNull String Email, final @NonNull String Name, final @NonNull String password, final @NonNull String trackedUserIDs) throws TrackingSlotsAmountException {
		CurrentUser.email = Email;
		CurrentUser.name = Name;
		CurrentUser.password = password;
		setTrackedUserIDs(trackedUserIDs);
	}

	public static String toText() {
		return email + OverseerApp.USER_SEPARATOR + name + OverseerApp.USER_SEPARATOR + password + OverseerApp.USER_SEPARATOR + trackedUserIDs;
	}

	private static void fromText(final @NonNull String userString) throws IllegalArgumentException {
		String[] parameterUser = userString.split(String.valueOf(OverseerApp.USER_SEPARATOR));
		if (parameterUser.length == 3) {
			email = parameterUser[0];
			name = parameterUser[1];
			password = parameterUser[2];
		}
		else if (parameterUser.length == 4) {
			email = parameterUser[0];
			name = parameterUser[1];
			password = parameterUser[2];
			trackedUserIDs = parameterUser[3];
		} else {
			throw new IllegalArgumentException("userString was not properly formatted");
		}
	}

	public static void saveToDisk() throws IOException {
		EncryptedStorageController encryptedStorageController = EncryptedStorageController.getInstance(OverseerApp.getInstance().getApplicationContext());
		encryptedStorageController.add(SAVED_USER_FILENAME, toText(), true);
	}

	public static void setCurrentUserFromDisk() throws IOException {
		EncryptedStorageController encryptedStorageController = EncryptedStorageController.getInstance(OverseerApp.getInstance().getApplicationContext());
		fromText(encryptedStorageController.get(SAVED_USER_FILENAME));
	}

	public static void setTrackedUserIDs (String trackedUserIDs) throws TrackingSlotsAmountException {
		long newAmountOfUserIds = trackedUserIDs.chars().filter((character) -> character == OverseerApp.TRACKED_USER_SEPARATOR).count();
		if (newAmountOfUserIds > TRACKED_USER_CAPACITY) {
			throw new TrackingSlotsAmountException("Too many user id's to track.");
		}
		currentNrOfTrackedUserIds = newAmountOfUserIds;
		CurrentUser.trackedUserIDs = trackedUserIDs;
	}

	public static void addToTrackedUserIds(String id) throws TrackingSlotsAmountException {
		if (freeTrackingSlots() < 1) {
			throw new TrackingSlotsAmountException("No free tracking slots.");
		}
		trackedUserIDs += id + OverseerApp.TRACKED_USER_SEPARATOR;
		++currentNrOfTrackedUserIds;
	}

	public static void addToTrackedUserIds(int id) throws TrackingSlotsAmountException {
		if (freeTrackingSlots() < 1) {
			throw new TrackingSlotsAmountException("No free tracking slots.");
		}
		trackedUserIDs += "" + id + OverseerApp.TRACKED_USER_SEPARATOR;
		++currentNrOfTrackedUserIds;
	}

	public static void removeTrackedUserId(String id) {
		trackedUserIDs = trackedUserIDs.replace(id + OverseerApp.TRACKED_USER_SEPARATOR, "");
		currentNrOfTrackedUserIds = CurrentUser.trackedUserIDs.chars().filter((character) -> character == OverseerApp.TRACKED_USER_SEPARATOR).count();
	}

	public static void removeTrackedUserId(int id) {
		trackedUserIDs = trackedUserIDs.replace("" + id + OverseerApp.TRACKED_USER_SEPARATOR, "");
		currentNrOfTrackedUserIds = CurrentUser.trackedUserIDs.chars().filter((character) -> character == OverseerApp.TRACKED_USER_SEPARATOR).count();
	}

	public static long freeTrackingSlots() {
		return TRACKED_USER_CAPACITY - currentNrOfTrackedUserIds;
	}

	public static void resetTrackedUsers() {
		trackedUsers = new HashMap<Integer, TrackedUser>();
		additionIndex = 0;
	}

	public static void addTrackedUsersFromIds() {
		String[] IDs = CurrentUser.trackedUserIDs.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
		for (String id : IDs) {
			if (id.equals("")) continue;
			try {
				Socket socket = ServerHandler.getUser(id);
				String[] response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));

				//didn't receive location history
				if (!response[0].equals(ServerHandler.GOT_USER)) {
					Log.e(TAG, "Could not get location histories for id: " + id + ". Received response : " + response[0]);
					continue;
				}

				//received location history
				TrackedUser user = new TrackedUser(response[1]);
				int idInt = Integer.parseInt(id);

				socket = ServerHandler.getGeoAreas(idInt);
				response = ServerHandler.receive(socket).trim().split(String.valueOf(OverseerApp.COMM_SEPARATOR));

				if (ServerHandler.GOT_GEOAREAS.equals(response[0])) {
					// split doesn't return empty strings if the delimiter is at the end...
					if (response.length > 1) {
						user.geoAreas = GeoArea.GetGeoAreaMap(response[1]);
					} else {
						user.geoAreas = GeoArea.GetGeoAreaMap("");
					}
				} else {
					Log.e(TAG, response[0] + " server response when requesting geoareas for id " + id + ": " + String.join("", response));
				}

				CurrentUser.addTrackedUser(user);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public static void addTrackedUserFromString(@NonNull String trackedUserString) {
		String[] userDetails = trackedUserString.split(String.valueOf(OverseerApp.USER_SEPARATOR));
		TrackedUser trackedUser = new TrackedUser(Integer.parseInt(userDetails[0]), userDetails[1], userDetails[2], Integer.parseInt(userDetails[3]));
		trackedUsers.put(trackedUser.getId(), trackedUser);
		++additionIndex;
	}

	public static void addTrackedUser(@NonNull TrackedUser user) {
		trackedUsers.put(user.getId(), user);
		++additionIndex;
	}

	public static HashMap<Integer, TrackedUser> getTrackedUsers() {
		return trackedUsers;
	}

	public static void updateTrackedUser(int userId, String locationHistory, int updateInterval) throws UserNotFoundException {
		if (!trackedUsers.containsKey(userId)) {
			throw new UserNotFoundException("Could not find user when updating user id " + userId);
		}

		TrackedUser user = trackedUsers.get(userId);
		if (user == null) {
			throw new UserNotFoundException("User was null when updating user id " + userId);
		}
		user.updateLocationHistory(locationHistory);
		user.setUpdateInterval(updateInterval);
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
				String[] userDetails = response[1].split(String.valueOf(OverseerApp.USER_SEPARATOR));
				CurrentUser.updateTrackedUser(Integer.parseInt(id),
						userDetails[2],
						Integer.parseInt(userDetails[3]));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}
