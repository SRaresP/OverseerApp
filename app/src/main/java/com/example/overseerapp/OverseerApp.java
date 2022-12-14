package com.example.overseerapp;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverseerApp extends Application {
	public static final int PERM_REQ_CODE = 101;
	public static final int INTERVAL_USUAL = 3000;
	public static final int INTERVAL_FASTEST = 3000;

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

	private static OverseerApp instance;

	private final ExecutorService executorService = Executors.newFixedThreadPool(4);
	private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

	public void onCreate() {
		super.onCreate();
		instance = this;
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
}
