package com.example.overseerapp.server_comm.exceptions;

import androidx.annotation.NonNull;

public class TrackingSlotsAmountException extends Exception{
	public TrackingSlotsAmountException() {
		super();
	}

	public TrackingSlotsAmountException(final @NonNull String message) {
		super(message);
	}

	public TrackingSlotsAmountException(final @NonNull String message, final @NonNull Throwable cause) {
		super(message, cause);
	}

	public TrackingSlotsAmountException(final @NonNull Throwable cause) {
		super(cause);
	}

	protected TrackingSlotsAmountException(final String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
