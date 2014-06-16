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
		
		@Key("legs")
		public List<Leg> legs;
	}
	
	public static class Leg{
		@Key("steps")
		public List<Step> steps;
	}
	
	public static class Step{
		@Key("distance")
		public Value distance;
		@Key("duration")
		public Value duration;
		@Key("start_location")
		public Location start;
		@Key("end_location")
		public Location end;
		
		public double slope;
		
		public void updateSlope(double elevA, double elevB){
			slope = (elevB - elevA)/distance.value;
		}
		
		public String toString(){
			return start+"-->"+end+": "+slope+" slope, "+distance.value+" m, "+duration.value+" s";
		}
	}
	
	public static class Value{
		@Key("value")
		public double value;
	}

	public static class OverviewPolyLine {
		@Key("points")
		public String points;
	}
	
	public static class ElevationResult{
		@Key("results")
		public List<ElevationData> elevationpoints;
	}
	
	public static class ElevationData{
		@Key("elevation")
		public double elevation;
		
		@Key("location")
		public Location location;
		
		@Key("resolution")
		public double resolution;
		
		public String toString(){
			return elevation + " " +location+" "+resolution;
		}
	}
	
	public static class Location{
		@Key("lat")
		public double lat;
		
		@Key("lng")
		public double lng;
		
		public String toString(){
			return "("+lat+";"+lng+")";
		}
	}
}
