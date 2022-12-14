package com.example.overseerapp.server_comm;

import androidx.annotation.NonNull;

import com.example.overseerapp.OverseerApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler {
	private static final String IP = "192.168.100.2";
	private static final int PORT = 8000;

	//server request types
	private static final String LOGIN_OVERSEER = "LOGIN_OVERSEER";
	private static final String REGISTER_OVERSEER = "REGISTER_OVERSEER";
	private static final String EDIT_OVERSEER = "EDIT_OVERSEER";
	private static final String ADD_TARGET = "ADD_TARGET";
	private static final String GET_USER = "GET_USER";

	//replies from server
	//positive
	public static final String LOGGED_IN = "LOGGED_IN";
	public static final String REGISTERED = "REGISTERED";
	public static final String EDITED = "EDITED";
	public static final String GOT_USER = "GOT_USER";
	public static final String ADDED_TARGET = "ADDED_TARGET";
	//negative
	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String WRONG_PASSWORD = "WRONG_PASSWORD";
	public static final String EMAIL_ALREADY_TAKEN = "EMAIL_ALREADY_TAKEN";
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
				.append(CurrentUser.toText());
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
				.append(CurrentUser.toText());
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
				.append(trackedUserId);
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
				.append(code);
		printWriter.write(stringBuilder.toString());
		printWriter.flush();
		return socket;

	}

	public static String receive(final @NonNull Socket socket) throws IOException {

		char[] response = new char[500];

		InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		bufferedReader.read(response);
		socket.close();

		return String.valueOf(response);
	}
}
