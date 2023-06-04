package com.example.overseerapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import com.example.overseerapp.server_comm.CurrentUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverseerApp extends Application {
	private static final String TAG = "OverseerApp";
	public static final int PERM_REQ_CODE = 101;
	public static final int INTERVAL_USUAL = 3000;
	public static final int INTERVAL_FASTEST = 3000;

	public static int debugCounter = 0;

	public static void count() {
		Log.d(TAG, "Counted amount: " + ++debugCounter);
	}

	//Server communication constants
	// \/ used to separate pieces of a package to send (email, name, location etc) \/
	public static final char COMM_SEPARATOR = '■';
	// \/ used to separate a location's date, latitude and longitude \/
	public static final char DATE_LAT_LONG_SEPARATOR = '²';
	// \/ used to separate locations in a location history string \/
	public static final char LOC_HISTORY_SEPARATOR = 'ⁿ';
	// \/ used to separate a tracked user's id and name \/
	public static final char ID_NAME_SEPARATOR = '·';
	// \/ used to separate tracked users in a tracked user string \/
	public static final char TRACKED_USER_SEPARATOR = '°';
	// \/ used to separate user info like email, name, password etc \/
	public static final char USER_SEPARATOR = '√';

	public static final String STATUS_NOTIFICATION_CHANNEL = "status_notification_channel";

	public static final int STATUS_NOTIFICATION_ID = 1;

	private static OverseerApp instance;

	private final ExecutorService executorService = Executors.newFixedThreadPool(4);
	private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

	private NotificationChannel statusNotificationChannel;

	public void onCreate() {
		super.onCreate();
		instance = this;

		statusNotificationChannel = new NotificationChannel(
				STATUS_NOTIFICATION_CHANNEL,
				"Status notification",
				NotificationManager.IMPORTANCE_NONE);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(statusNotificationChannel);

		CurrentUser.resetTrackedUsers();
		CurrentUser.addTrackedUsersFromIds();
	}

	public static OverseerApp getInstance() {
		return instance;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public Handler getMainThreadHandler() {
		return mainThreadHandler;
	}

	public NotificationChannel getStatusNotificationChannel() { return statusNotificationChannel; }
}
