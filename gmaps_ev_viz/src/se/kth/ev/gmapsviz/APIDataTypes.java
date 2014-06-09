package se.kth.ev.gmapsviz;

import java.util.List;

import com.google.api.client.util.Key;

public class APIDataTypes {
	public static class DirectionsResult {
		@Key("routes")
		public List<Route> routes;
	}

	public static class Route {
		@Key("overview_polyline")
		public OverviewPolyLine overviewPolyLine;
	}

	public static class OverviewPolyLine {
		@Key("points")
		public String points;
	}
}
