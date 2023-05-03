package com.example.overseerapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.R;
import com.example.overseerapp.location.LocationHandler;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.tracking.TrackedUser;
import com.example.overseerapp.ui.custom.CodeDialogFragment;

import java.io.IOException;

public class TrackingListFragment extends Fragment {
	private static final String TAG = "TrackingListFragment";
	private OverseerApp overseerApp;

	private AppCompatButton addNewTrackedUserB;

	public TrackingListFragment() {
		// Required empty public constructor
	}

	public static TrackingListFragment newInstance(String param1, String param2) {
		return new TrackingListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		overseerApp = OverseerApp.getInstance();

		addNewTrackedUserB = requireActivity().findViewById(R.id.trackAddNewB);
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

		ViewGroup trackingListL = requireActivity().findViewById(R.id.trackTrackingListL);
		trackingListL.removeAllViews();

		addNewTrackedUserB.setEnabled(false);

		overseerApp.getExecutorService().execute(() -> {
			CurrentUser.resetTrackedUsers();
			CurrentUser.addTrackedUsersFromIds();

			TrackedUser[] users = CurrentUser.getTrackedUsers();
			for (TrackedUser user : users) {
				if (user == null) continue;
				String lastLocation = LocationHandler.getLastLocation(user.getLocationHistory());

				//get the last latitude-long from the location history
				String[] lastCoordinates = lastLocation.split(String.valueOf(OverseerApp.DATE_LAT_LONG_SEPARATOR));

				//format the epoch time to time ago format
				String timeAgoRecordedLocation = DateUtils.getRelativeTimeSpanString(Long.parseLong(lastCoordinates[0])).toString();

				//convert the coordinates into an address
				Geocoder geocoder = new Geocoder(activity);
				try {
					Address lastAddress = geocoder.getFromLocation(Double.parseDouble(lastCoordinates[1]), Double.parseDouble(lastCoordinates[2]), 3).get(0);
					String address = lastAddress.getAddressLine(0) + '\n' +
							lastAddress.getAdminArea();

					//add a new view with all the information
					overseerApp.getMainThreadHandler().post(() -> {
						trackingListL.addView(new UserEntryLayout(activity,
								this,
								user.getName(),
								user.getId(),
								address,
								"Location recorded " + timeAgoRecordedLocation));
					});
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			if (CurrentUser.freeTrackingSlots() < 1) {
				overseerApp.getMainThreadHandler().post(() -> {
					addNewTrackedUserB.setText(R.string.tracking_list_already_full);
					addNewTrackedUserB.setEnabled(false);
				});
			} else {
				overseerApp.getMainThreadHandler().post(() -> {
					addNewTrackedUserB.setEnabled(true);
				});
			}
			overseerApp.getMainThreadHandler().post(() -> {
				addNewTrackedUserB.setOnClickListener((view) -> {
					CodeDialogFragment codeDialogFragment = new CodeDialogFragment(trackingListL, this);
					codeDialogFragment.show(requireActivity().getSupportFragmentManager(), "CodeDialog");
				});
			});
		});
	}
}