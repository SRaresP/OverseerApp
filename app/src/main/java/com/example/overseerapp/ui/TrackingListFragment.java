package com.example.overseerapp.ui;

import android.app.Activity;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.tracking.TrackedUser;
import com.example.overseerapp.tracking.TrackedUsersHandler;

import java.io.IOException;
import java.net.Socket;

public class TrackingListFragment extends Fragment {
	private static final String TAG = "TrackingListFragment";
	private OverseerApp overseerApp;

	public TrackingListFragment() {
		// Required empty public constructor
	}

	public static TrackingListFragment newInstance(String param1, String param2) {
		return new TrackingListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overseerApp = OverseerApp.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_tracking_list, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		Activity activity = requireActivity();

		ViewGroup trackingListL = getView().findViewById(R.id.trackTrackingListL);
		trackingListL.removeAllViews();

		overseerApp.getExecutorService().execute(() -> {
			String[] IDs = CurrentUser.trackedUserIDs.split(String.valueOf(OverseerApp.TRACKED_USER_SEPARATOR));
			for (String id : IDs) {
				try {
					Socket socket = ServerHandler.getLocationHistory(id);
					String[] response = ServerHandler.receive(socket).split(String.valueOf(OverseerApp.COMM_SEPARATOR));

					//didn't receive location history
					if (!response[0].equals(ServerHandler.GOT_LOCATION_HISTORY)) {
						overseerApp.getMainThreadHandler().post(() -> {
							Toast.makeText(requireActivity(), "Could not get location histories for id: " + id + ". Received response : " + response[0], Toast.LENGTH_LONG);
						});
						continue;
					}

					//received location history
					TrackedUsersHandler.addUserFromString(response[1]);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});

		TrackedUser[] users = TrackedUsersHandler.getTrackedUsers();
		for (TrackedUser user : users) {
			//get the last latitude-long from the location history
			String[] lastLocation = user.getLocationHistory().split(String.valueOf(OverseerApp.LOC_HISTORY_SEPARATOR))[0].split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));

			//format the epoch time to time ago format
			String timeAgoRecordedLocation = DateUtils.getRelativeTimeSpanString(Long.parseLong(lastLocation[0])).toString();

			//convert the coordinates into an address
			Geocoder geocoder = new Geocoder(activity);
			try {
				String lastLocationStr = geocoder.getFromLocation(Double.parseDouble(lastLocation[1]), Double.parseDouble(lastLocation[2]), 3).get(0).toString();

				//add a new view with all the information
				trackingListL.addView(new UserEntryLayout(activity, user.getName(), user.getId(), lastLocationStr, timeAgoRecordedLocation));
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
}