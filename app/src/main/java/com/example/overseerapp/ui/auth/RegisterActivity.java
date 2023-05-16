package com.example.overseerapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.overseerapp.R;
import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.server_comm.CurrentUser;
import com.example.overseerapp.server_comm.ServerHandler;
import com.example.overseerapp.ui.PrimaryActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.net.Socket;

public class RegisterActivity extends AppCompatActivity {
	private static final String TAG = "RegisterActivity";

	private TextInputEditText emailTIET;
	private TextInputEditText nameTIET;
	private TextInputEditText passwordTIET;
	private AppCompatButton registerB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		OverseerApp overseerApp = OverseerApp.getInstance();

		emailTIET = findViewById(R.id.regEmailRegisterTIET);
		nameTIET = findViewById(R.id.regNameRegisterTIET);
		passwordTIET = findViewById(R.id.regPasswordRegisterTIET);
		registerB = findViewById(R.id.regRegisterB);

		registerB.setOnClickListener(view -> {
			String email = emailTIET.getText().toString();
			if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				Toast.makeText(this, "Email address entered appears to be invalid", Toast.LENGTH_SHORT).show();
				return;
			}
			String name = nameTIET.getText().toString();
			String password = passwordTIET.getText().toString();
			try {
				CurrentUser.setCurrentUser(email, name, password, "");
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}

			Toast.makeText(this, "Connecting to server", Toast.LENGTH_SHORT).show();
			overseerApp.getExecutorService().execute(() -> {
				try {
					Socket socket = ServerHandler.register();
					String response = ServerHandler.receive(socket);
					overseerApp.getMainThreadHandler().post(() -> {
						if (response.contains(ServerHandler.REGISTERED)) {
							Intent intent = new Intent(this, PrimaryActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							finish();
						} else if (response.contains(ServerHandler.EMAIL_ALREADY_TAKEN)) {
							Toast.makeText(this, "That email is taken", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(this, "Server sent an unexpected reply", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
					overseerApp.getMainThreadHandler().post(() -> {
						Toast.makeText(this, "Error communicating with server", Toast.LENGTH_SHORT).show();
					});
				}
			});
		});
	}
}