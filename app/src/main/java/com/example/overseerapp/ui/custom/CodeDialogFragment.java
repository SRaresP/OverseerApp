package com.example.overseerapp.ui.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.tracking.TrackedUsersHandler;
import com.example.overseerapp.ui.UserEntryLayout;

import java.io.IOException;
import java.net.Socket;

public class CodeDialogFragment extends DialogFragment {
	public static String TAG = "CodeDialogFragment";

	private OverseerApp overseerApp;
	private final ViewGroup trackingListL;

	public CodeDialogFragment(ViewGroup trackingListL) {
		super();
		this.trackingListL = trackingListL;
	}

	private void displayErrorAndCancelDialog(String error, Activity activity) {
		overseerApp.getMainThreadHandler().post(() -> {
			AlertDialog errorDialog = new AlertDialog.Builder(activity)
				.setTitle(error)
				.setNeutralButton(R.string.ok, (dialogInterface, i) -> {})
				.setCancelable(true)
				.create();
			dismiss();
			errorDialog.show();
		});
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		overseerApp = OverseerApp.getInstance();

		Activity activity = requireActivity();

		LayoutInflater inflater = LayoutInflater.from(activity);
		View view = inflater.inflate(R.layout.new_tracked_user_dialog, null);

		AppCompatEditText codeEt = view.findViewById(R.id.trackCodeET);

		DialogInterface.OnClickListener listener = (DialogInterface dialogInterface, int i) -> {
			String code = codeEt.getText().toString();

			overseerApp.getExecutorService().execute(() -> {
				try {
					Socket socket1 = ServerHandler.addTarget(code);
					String[] response1 = ServerHandler.receive(socket1).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

					response1[0] = response1[0].trim();
					if (ServerHandler.ADDED_TARGET.equals(response1[0])) {
						// do nothing, code will continue after switch
					} else if (ServerHandler.NOT_FOUND.equals(response1[0])) {
						displayErrorAndCancelDialog("Could not find you or the user that you want to track in database.", activity);
						return;
					} else if (ServerHandler.WRONG_PASSWORD.equals(response1[0])) {
						displayErrorAndCancelDialog("Wrong overseer password, please check that you know your exact password, log into the app again and retry.", activity);
						return;
					} else if (ServerHandler.UNDEFINED_CASE.equals(response1[0])) {
						displayErrorAndCancelDialog("Server reports that something undefined went wrong.", activity);
						return;
					} else {
						displayErrorAndCancelDialog("Something went wrong inside the app.", activity);
						return;
					}

					if (TrackedUsersHandler.freeSlots() < 1) {
						displayErrorAndCancelDialog("Could not add user. Maximum number of tracked users reached.", activity);
						return;
					}
					Socket socket2 = ServerHandler.getUser(response1[1]);
					String[] response2 = ServerHandler.receive(socket2).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

					response2[0] = response2[0].trim();
					if (ServerHandler.GOT_USER.equals(response2[0])) {
						// do nothing, code will continue after switch
					} else if (ServerHandler.NOT_FOUND.equals(response2[0])) {
						displayErrorAndCancelDialog("Could not find you or the user that you want to track in database.", activity);
						return;
					} else if (ServerHandler.WRONG_PASSWORD.equals(response2[0])) {
						displayErrorAndCancelDialog("Wrong overseer password, please check that you know your exact password, log into the app again and retry.", activity);
						return;
					} else if (ServerHandler.UNDEFINED_CASE.equals(response2[0])) {
						displayErrorAndCancelDialog("Server reports that something undefined went wrong.", activity);
						return;
					} else {
						displayErrorAndCancelDialog("Something went wrong inside the app.", activity);
						return;
					}

					TrackedUsersHandler.addUserFromString(response2[1]);

					String[] userStrings = response2[1].split(String.valueOf(OverseerApp.USER_SEPARATOR));
					String lastLocation = LocationHandler.getLastLocation(userStrings[2]);
					String[] lastCoordinates = lastLocation.split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));
					String timeAgoRecordedLocation = DateUtils.getRelativeTimeSpanString(Long.parseLong(lastCoordinates[0])).toString();
					Geocoder geocoder = new Geocoder(activity);
					try {
						Address lastAddress = geocoder.getFromLocation(Double.parseDouble(lastCoordinates[1]), Double.parseDouble(lastCoordinates[2]), 3).get(0);
						String address = lastAddress.getAddressLine(0) + '\n' +
								lastAddress.getAdminArea();
						overseerApp.getMainThreadHandler().post(() -> {
							trackingListL.addView(new UserEntryLayout(activity,
									userStrings[1],
									Integer.parseInt(userStrings[0]),
									address,
									"Location recorded " + timeAgoRecordedLocation));
						});
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
						displayErrorAndCancelDialog("Could not convert location to address, using coordinates instead.", activity);
						overseerApp.getMainThreadHandler().post(() -> {
							trackingListL.addView(new UserEntryLayout(activity,
									userStrings[0],
									Integer.parseInt(userStrings[1]),
									"Latitude: " + lastCoordinates[1] + "; Longitude: " + lastCoordinates[2],
									timeAgoRecordedLocation));
						});
					}
				}
				catch (IOException e) {
					Log.e(TAG, e.getMessage());
					displayErrorAndCancelDialog("In/Out error communicating with server", activity);
				}
			});
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.track_new_user)
				.setView(view)
				.setPositiveButton(R.string.add_tracked_user, listener)
				.setNegativeButton(R.string.cancel, (DialogInterface dialogInterface, int i) -> {
					// cancels dialog by default, no need for code
				});
		return builder.create();
	}
}
