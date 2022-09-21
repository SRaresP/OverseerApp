package com.example.overseerapp.server_comm;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.storage.EncryptedStorageController;

import java.io.IOException;

public class CurrentUser {
	private static final String TAG = "CurrentUser";
	private static final String SAVED_USER_FILENAME = "CurrentUser";
	public static String email = "";
	public static String name = "";
	public static String password = "";

	//do not call constructor, use static methods
	private CurrentUser() { }

	public static void setCurrentUser(final @NonNull String Email, final @NonNull String Name, final @NonNull String password) {
		CurrentUser.email = Email;
		CurrentUser.name = Name;
		CurrentUser.password = password;
	}

	public static void reset() {
		email = "";
		name = "";
		password = "";
	}

	public static String toText() {
		return email + OverseerApp.USER_SEPARATOR + name + OverseerApp.USER_SEPARATOR + password;
	}

	public static void fromText(final @NonNull String userString) throws IllegalArgumentException {
		String[] parameterUser = userString.split(String.valueOf(OverseerApp.USER_SEPARATOR));
		if (parameterUser.length == 3) {
			email = parameterUser[0];
			name = parameterUser[1];
			password = parameterUser[2];
		}
		else if (parameterUser.length == 3) {
			email = parameterUser[0];
			name = parameterUser[1];
			password = parameterUser[2];
			//TODO: get the tracked users here as well
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
}
