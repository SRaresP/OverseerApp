package com.example.overseerapp.tracking;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.ui.PrimaryActivity;

import java.util.HashMap;

public class TrackerService extends Service {
	private final static String TAG = "TrackerService";
	private static final int STOP_REQUEST_CODE = 1;

	private static final String ACTION_STOP_UPDATES = "overseerapp.intent.action.stop_updates";

	HashMap<Integer, TrackedUser> targets;

	public TrackerService() {}

	private void startTracking() {
		targets = CurrentUser.getTrackedUsers();
		targets.forEach((id, user) -> {
			user.scheduleUpdates();
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getAction() == ACTION_STOP_UPDATES) {
			stopForeground(true);
			stopSelf();
		}

		Intent toAppIntent = new Intent(this, PrimaryActivity.class);
		toAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, toAppIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent stopTrackingIntent = new Intent(this, TrackerService.class)
				.setAction(ACTION_STOP_UPDATES);

		PendingIntent pStopTrackingIntent = PendingIntent.getService(
				this,
				STOP_REQUEST_CODE,
				stopTrackingIntent,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, OverseerApp.STATUS_NOTIFICATION_CHANNEL)
				.setSmallIcon(R.drawable.ic_baseline_location_on_24)
				.setContentTitle("Target tracking is enabled")
				.setContentText("Tap to open the app.")
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(false)
				.setContentIntent(notificationPendingIntent)
				.setOngoing(true)
				.setCategory(Notification.CATEGORY_STATUS)
				.addAction(R.drawable.ic_baseline_stop_24, "Stop", pStopTrackingIntent);
		Notification statusNotification = builder.build();

		startForeground(OverseerApp.STATUS_NOTIFICATION_ID, statusNotification);

		startTracking();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		targets.forEach((id, user) -> {
			user.stopUpdates();
		});
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}