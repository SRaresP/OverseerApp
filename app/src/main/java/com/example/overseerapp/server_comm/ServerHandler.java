package com.example.overseerapp.server_comm;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;
import com.example.overseerapp.location.GeoArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler {
	private static final String IP = "192.168.100.2";
	private static final int PORT = 8000;

	private static final String END = "<_-END-_>";

	//server request types
	private static final String LOGIN_OVERSEER = "LOGIN_OVERSEER";
	private static final String REGISTER_OVERSEER = "REGISTER_OVERSEER";
	private static final String EDIT_OVERSEER = "EDIT_OVERSEER";
	private static final String ADD_TARGET = "ADD_TARGET";
	private static final String GET_USER = "GET_USER";
	private static final String REMOVE_TARGET = "REMOVE_TARGET";
	private static final String GET_SETTINGS = "GET_SETTINGS";
	private static final String CHANGE_SETTINGS = "CHANGE_SETTINGS";
	private static final String REMOVE_SETTINGS = "REMOVE_SETTINGS";
	private static final String GET_TARGET_LOCATION_AND_INTERVAL = "GET_TARGET_LOCATION_AND_INTERVAL";
	private static final String GET_GEOAREAS = "GET_GEOAREAS";
	private static final String ADD_GEOAREA = "ADD_GEOAREA";

	//replies from server
	//positive
	public static final String LOGGED_IN = "LOGGED_IN";
	public static final String REGISTERED = "REGISTERED";
	public static final String EDITED = "EDITED";
	public static final String GOT_USER = "GOT_USER";
	public static final String ADDED_TARGET = "ADDED_TARGET";
	public static final String REMOVED_TARGET = "REMOVED_TARGET";
	public static final String GOT_SETTINGS = "GOT_SETTINGS";
	public static final String CHANGED_SETTINGS = "CHANGED_SETTINGS";
	public static final String REMOVED_SETTINGS = "REMOVED_SETTINGS";
	public static final String GOT_TARGET_LOCATION_AND_INTERVAL = "GOT_TARGET_LOCATION_AND_INTERVAL";
	public static final String GOT_GEOAREAS = "GOT_GEOAREAS";
	public static final String ADDED_GEOAREA = "ADDED_GEOAREA";
	//negative
	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String WRONG_PASSWORD = "WRONG_PASSWORD";
	public static final String EMAIL_ALREADY_TAKEN = "EMAIL_ALREADY_TAKEN";
	public static final String COULD_NOT_REMOVE_TARGET = "COULD_NOT_REMOVE_TARGET";
	public static final String NOT_A_TARGET_ID = "NOT_A_TARGET_ID";
	public static final String NOT_AN_INTERVAL = "NOT_AN_INTERVAL";
	public static final String NOT_A_GEOAREA = "NOT_A_GEOAREA";
	public static final String ALREADY_TRACKING = "ALREADY_TRACKING";
	//code problem
	public static final String UNDEFINED_CASE = "UNDEFINED_CASE";

	//Do not call constructor, use static methods
	private ServerHandler() {}

	public static Socket login() throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(LOGIN_OVERSEER)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.toText())
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket register() throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(REGISTER_OVERSEER)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.toText())
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket getUser(String trackedUserId) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(GET_USER)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(trackedUserId)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket addTarget(String code) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(ADD_TARGET)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(code)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket removeTarget(int id) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(REMOVE_TARGET)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(id)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket getSettings(int targetId) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(GET_SETTINGS)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket changeSettings(int targetId, int interval) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(CHANGE_SETTINGS)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(interval)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket removeSettings(int targetId) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(REMOVE_SETTINGS)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket getTargetLocationAndInterval(int targetId) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(GET_TARGET_LOCATION_AND_INTERVAL)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket getGeoAreas(int targetId) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(GET_GEOAREAS)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket addGeoArea(int targetId, String geoArea) throws IOException {
		Socket socket = new Socket(IP, PORT);
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

		StringBuilder stringBuilder = new StringBuilder()
				.append(ADD_GEOAREA)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(CurrentUser.email)
				.append(OverseerApp.USER_SEPARATOR)
				.append("")
				.append(OverseerApp.USER_SEPARATOR)
				.append(CurrentUser.password)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(targetId)
				.append(OverseerApp.COMM_SEPARATOR)
				.append(geoArea)
				.append(END);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;
	}

	public static Socket addGeoArea(int targetId, GeoArea geoArea) throws IOException {
		return addGeoArea(targetId, geoArea.toString());
	}

	public static String receive(final @NonNull Socket socket) throws IOException {
		char[] response = new char[500];
		String finalResponse = "";

		InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		while (true) {
			bufferedReader.read(response);
			finalResponse += String.valueOf(response);
			if (finalResponse.contains(END)) {
				break;
			}
		}
		socket.close();

		finalResponse = finalResponse.trim();
		return finalResponse.split(END)[0];
	}
}
