package com.example.overseerapp.tracking;

public class TooManyTrackedUsersException extends Exception {
	public TooManyTrackedUsersException() {
		super();
	}

	public TooManyTrackedUsersException(String message) {
		super(message);
	}

	public TooManyTrackedUsersException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyTrackedUsersException(Throwable cause) {
		super(cause);
	}

	protected TooManyTrackedUsersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}