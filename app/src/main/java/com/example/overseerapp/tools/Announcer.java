package com.example.overseerapp.tools;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

// Thought MutableLiveData doesn't work, this is an unused workaround
// TODO: remove on next commit
public class Announcer<T> {
	private static final String TAG = "Announcer";

	public interface Callback {
		void respond();
	}

	private T storage;
	private ArrayList<Announcer.Callback> callbacks;

	private void init() {
		callbacks = new ArrayList<>(1);
	}

	public Announcer() {
		init();
	}

	public Announcer(@NonNull final T value) {
		init();
		this.storage = value;
	}

	public Announcer(@NonNull final Callback callback) {
		init();
		callbacks.add(callback);
	}

	public Announcer(@NonNull final T value, @NonNull final Callback callback) {
		init();
		this.storage = value;
		callbacks.add(callback);
	}

	public void setValue(@NonNull final T value) {
		this.storage = value;
		for (Callback callback : callbacks) {
			callback.respond();
		}
	}

	public T getValue() {
		return storage;
	}

	public boolean addCallback(@NonNull final Announcer.Callback callback) {
		try {
			return callbacks.add(callback);
		} catch	(Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

	public boolean removeCallback(@NonNull final Announcer.Callback callback) {
		try {
			return callbacks.remove(callback);
		} catch	(Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

	public void clearCallbacks() {
		try {
			callbacks.clear();
		} catch	(Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
}
