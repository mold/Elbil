package com.kth.ev.graphviz;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

/**
 * Collection of short-hand API requests to the google API.
 * 
 * @author marothon
 * 
 */
public class GoogleAPIQueries {
	//private static final String TAG = "GoogleAPIQueries";
	public static final String elevation_url = "https://maps.googleapis.com/maps/api/elevation/json";
	public static final String directions_url = "https://maps.googleapis.com/maps/api/directions/json";
	public static final String places_url = "https://maps.googleapis.com/maps/api/place";

	public static void setKey(String key) {
		APIRequestTask.setKey(key);
	}

	/**
	 * Asks the google api for directions.
	 * 
	 * @param from
	 *            The start position, given in human readable form.
	 * @param to
	 *            The end position, given in human readable form.
	 * @return The APIRequestTask associated with this call. Use the .get()
	 *         method to retrieve the result.
	 */
	public static APIRequestTask requestDirections(String from, String to) {
		URLParameter _from = new URLParameter("origin", from);
		URLParameter _to = new URLParameter("destination", to);
		URLParameter _sensor = new URLParameter("sensor", "false");

		APIRequestTask task = new APIRequestTask(directions_url);
		task.execute(_from, _to, _sensor);
		return task;
	}

	/**
	 * Request the elevation for a given set of locations.
	 * 
	 * @param locs
	 *            An encoded path or a string with locations.
	 * @return APIRequestTask from which the data can be retrieved.
	 */
	public static APIRequestTask requestElevation(String locs) {
		URLParameter locations = new URLParameter("locations", "enc:" + locs);
		APIRequestTask task = new APIRequestTask(elevation_url);
		task.execute(locations);
		return task;
	}

	/**
	 * Request the elevation for a given location.
	 * 
	 * @param locs
	 *            Location in LatLng object.
	 * @return APIRequest task which contains the data.
	 */
	public static APIRequestTask requestElevation(LatLng point) {
		URLParameter locations = new URLParameter("locations", point.latitude
				+ "," + point.longitude);
		APIRequestTask task = new APIRequestTask(elevation_url);
		task.execute(locations);
		return task;
	}

	/**
	 * Request a sample of the elevation along a given path.
	 * 
	 * @param locs
	 *            Locations in plaintext
	 * @return APIRequest task which contains the data.
	 */
	public static APIRequestTask requestElevation(List<LatLng> points) {
		String encoded = PolyUtil.encode(points);
		return requestElevation(encoded);
	}

	/**
	 * Request a sample of the elevation along a given path.
	 * 
	 * @param locs
	 *            Locations
	 * @return APIRequest task which contains the data.
	 */
	public static APIRequestTask requestSampledElevation(List<LatLng> points,
			int samples) {
		String encoded = PolyUtil.encode(points);
		return requestSampledElevation(encoded, samples);
	}

	/**
	 * Request a sample of the elevation along a given path.
	 * 
	 * @param points
	 *            Encoded locations
	 * @param samples
	 *            The number of elevation samples to be take along the path.
	 * @return
	 */
	public static APIRequestTask requestSampledElevation(String points,
			int samples) {
		URLParameter locations = new URLParameter("path", "enc:" + points);
		URLParameter samperi = new URLParameter("samples",
				Integer.toString(samples));
		APIRequestTask task = new APIRequestTask(elevation_url);
		task.execute(locations, samperi);
		return task;
	}

	public static APIRequestTask requestAutocomplete(String input) {
		String url = places_url + "/autocomplete" + "/json";
		URLParameter input_param = null;

		input_param = new URLParameter("input", input);
		APIRequestTask task = new APIRequestTask(url);
		task.execute(input_param);
		return task;
	}

}
