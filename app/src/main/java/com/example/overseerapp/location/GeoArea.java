package com.example.overseerapp.location;

import android.nfc.FormatException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class GeoArea {
	private static final String TAG = "GeoArea";
	private static final char AREA_FENCE_SEPARATOR = '·';
	private static final char AREA_DETAILS_SEPARATOR = '°';
	private static final char FENCES_SEPARATOR = '≈';
	private static final char AREAS_SEPARATOR = '÷';
	public static final int DEFAULT_COLOR = 0xffffff;
	public static final int DEFAULT_STROKE_COLOR = 0xff000000;

	public enum GeoAreaMode {
		ALERT_WHEN_INSIDE,
		ALERT_WHEN_OUTSIDE
	}

	private int id;
	private int overseerId;
	private int targetId;
	public int color;
	public GeoAreaMode mode;
	public String triggerMessage;
	public ArrayList<GeoFence> geoFences;

	@NonNull
	public static HashMap<Integer, GeoArea> GetGeoAreaMap(@NonNull String geoAreas)
	{
		String[] input = geoAreas.split(String.valueOf(AREAS_SEPARATOR));
		HashMap<Integer, GeoArea> geoMap = new HashMap<>(1);
		for (String areaStr : input) {
			try {
				GeoArea area = new GeoArea(areaStr);
				geoMap.put(area.id, area);
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
		}
		return geoMap;
	}

	public GeoArea(int overseerId, int targetId, int color, GeoAreaMode mode, String triggerMessage)
	{
		this.overseerId = overseerId;
		this.targetId = targetId;
		this.color = color;
		this.mode = mode;
		this.triggerMessage = triggerMessage;
		geoFences = new ArrayList<GeoFence>(1);
	}

	public GeoArea(int overseerId, int targetId, int color, GeoAreaMode mode)
	{
		this.overseerId = overseerId;
		this.targetId = targetId;
		this.color = color;
		this.mode = mode;
		this.triggerMessage = "";
		geoFences = new ArrayList<GeoFence>(1);
	}

	public GeoArea(int overseerId, int targetId, int color)
	{
		this.overseerId = overseerId;
		this.targetId = targetId;
		this.color = color;
		this.mode = GeoAreaMode.ALERT_WHEN_INSIDE;
		this.triggerMessage = "";
		geoFences = new ArrayList<GeoFence>(1);
	}

	public GeoArea(int overseerId, int targetId)
	{
		this.overseerId = overseerId;
		this.targetId = targetId;
		color = DEFAULT_COLOR;
		this.mode = GeoAreaMode.ALERT_WHEN_INSIDE;
		this.triggerMessage = "";
		geoFences = new ArrayList<GeoFence>(1);
	}

	public GeoArea()
	{
		overseerId = -1;
		targetId = -1;
		color = DEFAULT_COLOR;
		this.mode = GeoAreaMode.ALERT_WHEN_INSIDE;
		this.triggerMessage = "";
		geoFences = new ArrayList<GeoFence>(0);
	}

	public GeoArea(String geoArea) throws FormatException, IllegalArgumentException {
		if (!geoArea.contains(String.valueOf(AREA_FENCE_SEPARATOR))) throw new FormatException("GeoArea is not parsable. Passed GeoArea string: " + geoArea);
		String[] input = geoArea.split(String.valueOf(AREA_FENCE_SEPARATOR));
		String[] areaDetails = input[0].split(String.valueOf(AREA_DETAILS_SEPARATOR));
		if (areaDetails.length < 6) throw new FormatException("GeoArea is not parsable. Passed GeoArea string: " + geoArea);
		String[] geoFences = new String[0];
		if (input.length > 1) {
			geoFences = input[1].split(String.valueOf(FENCES_SEPARATOR));
		}
		id = Integer.parseInt(areaDetails[0]);
		overseerId = Integer.parseInt(areaDetails[1]);
		targetId = Integer.parseInt(areaDetails[2]);
		color = Integer.parseInt(areaDetails[3]);
		mode = GeoAreaMode.valueOf(areaDetails[4]);
		triggerMessage = areaDetails[5];
		this.geoFences = new ArrayList<GeoFence>(geoFences.length);
		for (String geofence : geoFences)
		{
			try {
				this.geoFences.add(new GeoFence(geofence));
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public int getId() {
		return id;
	}

	public int getOverseerId() {
		return overseerId;
	}

	public int getTargetId() {
		return targetId;
	}

	public int getColor() {
		return color;
	}

	public GeoAreaMode getMode() {
		return mode;
	}

	public ArrayList<GeoFence> getGeoFences() {
		return geoFences;
	}

	@NonNull
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder(String.valueOf(id) +
				AREA_DETAILS_SEPARATOR +
				overseerId +
				AREA_DETAILS_SEPARATOR +
				targetId +
				AREA_DETAILS_SEPARATOR +
				color +
				AREA_DETAILS_SEPARATOR +
				mode +
				AREA_DETAILS_SEPARATOR +
				triggerMessage +
				AREA_FENCE_SEPARATOR);
		for (GeoFence geoFence : geoFences)
		{
			output.append(geoFence.toString()).append(FENCES_SEPARATOR);
		}
		return output.toString();
	}
}
