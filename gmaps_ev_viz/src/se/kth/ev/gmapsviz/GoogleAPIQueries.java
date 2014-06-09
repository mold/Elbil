package se.kth.ev.gmapsviz;


public class GoogleAPIQueries {

	public static void setKey(String key) {
		APIRequest.setKey(key);
	}

	public static APIRequest requestDirections(final String from,
			final String to) {
		URLParameter _from = new URLParameter("origin", from);
		URLParameter _to = new URLParameter("destination", to);
		URLParameter _sensor = new URLParameter("sensor", "false");

		APIRequest task = new APIRequest(
				"https://maps.googleapis.com/maps/api/directions/json");
		task.execute(_from, _to, _sensor);
		return task;
	}

	public static APIRequest requestElevation(String locs) {
		URLParameter locations = new URLParameter("locations", locs);
		APIRequest task = new APIRequest(
				"https://maps.googleapis.com/maps/api/elevation/json");
		task.execute(locations);
		return task;
	}


	
}
