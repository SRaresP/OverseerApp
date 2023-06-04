package com.example.overseerapp.ui.auth;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.overseerapp.R;
import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.tracking.TrackerService;
import com.example.overseerapp.ui.PrimaryActivity;
import com.example.overseerapp.ui.custom.LoadingView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.net.Socket;

public class AuthActivity extends AppCompatActivity {
	private static final String TAG = "AuthActivity";

	private TextInputEditText emailTIET;
	private TextInputEditText passwordTIET;
	private AppCompatButton loginB;
	private AppCompatButton registerB;

	private void setUpForManualLogin(final @Nullable AlertDialog alertDialog, final @Nullable String toToast) {
		OverseerApp.getInstance().getMainThreadHandler().post(() -> {
			setContentView(R.layout.activity_auth);

			loginB = findViewById(R.id.logLoginB);
			registerB = findViewById(R.id.logRegisterB);
			emailTIET = findViewById(R.id.logEmailTIET);
			passwordTIET = findViewById(R.id.logPasswordTIET);

			loginB.setOnClickListener(view -> {
				String email = emailTIET.getText().toString();
				String password = passwordTIET.getText().toString();
				try {
					CurrentUser.setCurrentUser(email, "", password, "");
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

				//LoadingView loadingView = new LoadingView(innerRelLayout, this, "Logging in", null, new AppCompatButton[] {loginB, registerB}, false).show();
				AlertDialog alertDialogClassicLogin = new AlertDialog.Builder(this)
						.setView(new LoadingView(this, "Logging in", false))
						.setCancelable(false)
						.create();
				alertDialogClassicLogin.show();
				loginAndContinueAsync(alertDialogClassicLogin);
			});

			registerB.setOnClickListener(view -> {
				Intent intent = new Intent(this, RegisterActivity.class);
				startActivity(intent);
			});


			emailTIET.setEnabled(true);
			passwordTIET.setEnabled(true);
			loginB.setEnabled(true);
			registerB.setEnabled(true);

			if (toToast != null) {
				Toast.makeText(this, toToast, Toast.LENGTH_SHORT).show();
			}
			if (alertDialog != null) {
				alertDialog.dismiss();
			}
		});
	}

	private void loginAndContinueAsync(@NonNull final AlertDialog alertDialog) {
		Toast.makeText(this, "Connecting to server", Toast.LENGTH_SHORT).show();
		OverseerApp overseerApp = OverseerApp.getInstance();
		overseerApp.getExecutorService().execute(() -> {
			try {
				Socket socket = ServerHandler.login();
				String response = ServerHandler.receive(socket).trim();
				if (response.contains(ServerHandler.LOGGED_IN)) {
					try {
						CurrentUser.saveToDisk();
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
						overseerApp.getMainThreadHandler().post(() -> {
							setUpForManualLogin(alertDialog, "Could not save user data to disk, you will have to log in again next time");
						});
					}
					//get the user part of the response as an array which contains the name and the tracked user id's in order
					String[] userDetails = response.split(String.valueOf(OverseerApp.COMM_SEPARATOR))[1].split(String.valueOf(OverseerApp.USER_SEPARATOR));
					//set those user details
					CurrentUser.name = userDetails[0];
					try {
						CurrentUser.setTrackedUserIDs(userDetails[1]);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
					CurrentUser.addTrackedUsersFromIds();
					overseerApp.getMainThreadHandler().post(() -> {
						Intent intent = new Intent(this, PrimaryActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						finish();
						alertDialog.dismiss();
					});
				} else {
					overseerApp.getMainThreadHandler().post(() -> {
						if (response.contains(ServerHandler.NOT_FOUND)) {
							setUpForManualLogin(alertDialog, "Server could not find an account with that email address");
						} else if (response.contains(ServerHandler.WRONG_PASSWORD)) {
							setUpForManualLogin(alertDialog, "Entered password does not match");
						} else {
							Log.e(TAG, "Unexpected server reply: " + response);
							setUpForManualLogin(alertDialog, "Server sent an unexpected reply");
						}
					});
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				overseerApp.getMainThreadHandler().post(() -> {
					setUpForManualLogin(alertDialog, "Error communicating with server");
				});
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlertDialog alertDialogAutoLogin = new AlertDialog.Builder(this)
				.setView(new LoadingView(this, "Attempting to log in using stored credentials", true))
				.setCancelable(false)
				.create();
		alertDialogAutoLogin.show();

		try {
			CurrentUser.setCurrentUserFromDisk();
			loginAndContinueAsync(alertDialogAutoLogin);
		} catch (IOException e) {
			//only setCurrentUserFromDisk can make the thread reach this path by throwing an exception
			Log.i(TAG, e.getMessage() + "; user is not logged in", e);
			setUpForManualLogin(alertDialogAutoLogin, null);
		}
	}
}