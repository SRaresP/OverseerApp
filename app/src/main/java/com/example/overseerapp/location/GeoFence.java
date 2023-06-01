package com.example.overseerapp.location;

import android.nfc.FormatException;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

public class GeoFence {
	private static final char FENCE_DETAILS_SEPARATOR = '÷';

	private int id;
	private int geoAreaId;
	private double latitude;
	private double longitude;
	private int radiusMeters;

	public GeoFence(int geoAreaId, double latitude, double longitude, int radiusMeters)
	{
		this.geoAreaId = geoAreaId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radiusMeters = radiusMeters;
	}

	public GeoFence(Circle circle)
	{
		this.radiusMeters = (int)circle.getRadius();
		LatLng position = circle.getCenter();
		this.latitude = position.latitude;
		this.longitude = position.longitude;
	}

	// Shouldn't be used, values are set so that it's as obvious on a map as possible
	// This constructor is only kept to respect the server GeoFence definition as well as possible
	public GeoFence()
	{
		geoAreaId = -1;
		latitude = 0;
		longitude = 0;
		// 10k kilometers
		radiusMeters = 10000000;
	}

	public GeoFence(String geofence) throws FormatException, NumberFormatException {
		String[] input = geofence.split(String.valueOf(FENCE_DETAILS_SEPARATOR));
		if (input.length < 5)
		{
			throw new FormatException("GeoFence is not parsable. Passed GeoFence string: " + geofence);
		}
		id = Integer.parseInt(input[0]);
		geoAreaId = Integer.parseInt(input[1]);
		latitude = Double.parseDouble(input[2]);
		longitude = Double.parseDouble(input[3]);
		radiusMeters = Integer.parseInt(input[4]);
	}

	public int getId() {
		return id;
	}

	public int getGeoAreaId() {
		return geoAreaId;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getRadiusMeters() {
		return radiusMeters;
	}

	@NonNull
	@Override
	public String toString()
	{
		return String.valueOf(id) +
				FENCE_DETAILS_SEPARATOR +
				geoAreaId +
				FENCE_DETAILS_SEPARATOR +
				latitude +
				FENCE_DETAILS_SEPARATOR +
				longitude +
				FENCE_DETAILS_SEPARATOR +
				radiusMeters;
	}
}
