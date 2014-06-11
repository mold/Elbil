package se.kth.ev.gmapsviz;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

public class GoogleAPIQueries {
	public static final String elevation_url = "https://maps.googleapis.com/maps/api/elevation/json";
	public static final String directions_url = "https://maps.googleapis.com/maps/api/directions/json";

	public static void setKey(String key) {
		APIRequest.setKey(key);
	}

	public static APIRequest requestDirections(String from,
			String to) {
		URLParameter _from = new URLParameter("origin", from);
		URLParameter _to = new URLParameter("destination", to);
		URLParameter _sensor = new URLParameter("sensor", "false");

		APIRequest task = new APIRequest(directions_url);
		task.execute(_from, _to, _sensor);
		return task;
	}

	/**
	 * Request the elevation for a given set of locations.
	 * 
	 * @param locs
	 *            An encoded path or a string with locations.
	 * @return APIRequest task which contains the data.
	 */
	public static APIRequest requestElevation(String locs) {
		URLParameter locations = new URLParameter("locations", "enc:" + locs);
		APIRequest task = new APIRequest(elevation_url);
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
	public static APIRequest requestElevation(LatLng point) {
		URLParameter locations = new URLParameter("locations", point.latitude
				+ "," + point.longitude);
		APIRequest task = new APIRequest(elevation_url);
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
	public static APIRequest requestElevation(List<LatLng> points) {
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
	public static APIRequest requestSampledElevation(List<LatLng> points,
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
	public static APIRequest requestSampledElevation(String points, int samples) {
		URLParameter locations = new URLParameter("path", "enc:" + points);
		URLParameter samperi = new URLParameter("samples",
				Integer.toString(samples));
		APIRequest task = new APIRequest(elevation_url);
		task.execute(locations, samperi);
		return task;
	}

}
