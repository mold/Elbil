package com.kth.ev.graphviz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.json.JsonParser;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.kth.ev.graphviz.APIDataTypes.DirectionsResult;
import com.kth.ev.graphviz.APIDataTypes.ElevationData;
import com.kth.ev.graphviz.APIDataTypes.ElevationResult;
import com.kth.ev.graphviz.APIDataTypes.Leg;
import com.kth.ev.graphviz.APIDataTypes.Location;
import com.kth.ev.graphviz.APIDataTypes.Step;
import com.kth.ev.graphviz.APIDataTypes.Value;
/**
 * A Runnable implementation that performs some calls to the
 * google API to fetch information about a given route.
 *  
 * @author marothon
 *    
 */
public class RouteDataFetcher extends Observable implements Runnable {
	private static final String TAG = "RouteDataFetcher";
	private String pointA, pointB;
	private List<Step> data_directions, data_combined;
	private String json;
	public String data_combined_json;
	public List<LatLng> route, elevation;
	                 
	private static int sample_size = 100;   

	/**
	 * Constructor
	 * 
	 * Sets some random locations for the route.
	 */
	public RouteDataFetcher() {
		pointA = "Tåsjöberget, Sweden";
		pointB = "Tåsjön, Strömsund, Sweden";
		data_combined = new ArrayList<Step>();
	}    

	/**
	 * Constructor
	 * 
	 * @param a Start position of the route.
	 * @param b End position of the route.
	 */
	public RouteDataFetcher(String a, String b) {
		pointA = a;
		pointB = b;
		data_combined = new ArrayList<Step>();
	}
	
	public List<Step> getDirectionsRoute(){
		if(data_directions == null){
			Log.e(TAG, "No directions data available.");
			return null;
		}
		return data_directions;
	}
	
	public List<Step> getCombinedRoute(){
		if(data_combined == null){
			Log.e(TAG, "No combined data available.");
			return null;
		}
		return data_combined;
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				json = GoogleAPIQueries.requestDirections(pointA,
						pointB).get();
				if (json == null) {
					throw new Exception("Google Direction API call failed!");
				}
				JsonParser parser = APIRequestTask.JSON_FACTORY
						.createJsonParser(json);
				DirectionsResult dRes = parser.parse(DirectionsResult.class);
				
				List<LatLng> step_locas = new ArrayList<LatLng>(20);
				// Push all LatLng from the parsed data.
				List<Leg> legs = dRes.routes.get(0).legs;
				data_directions = new ArrayList<Step>();
				double dist = 0;
				for (Leg l : legs) {
					for (Step s : l.steps) {
						dist += s.distance.value;
						data_directions.add(s);
						step_locas.add(new LatLng(s.start.lat, s.start.lng));
					}
				}
				
				//Create new step list which will be filled
				//with slope estimations from the elevation
				//data.
				List<Step> ret = new ArrayList<Step>(sample_size);
				double[] speeds = new double[sample_size];
				double avg = dist / sample_size;
				int dist2 = 1;
				speeds[0] = legs.get(0).steps.get(0).distance.value / legs.get(0).steps.get(0).duration.value;
				for (Leg l : legs) {
					for (Step s : l.steps) { 
						speeds[dist2] = s.distance.value / s.duration.value;
						dist2 += (s.distance.value/dist) * sample_size;
					}
				} 
				
				//Fill speed array with approximative speed.
				double approx = speeds[0];
				for(int i=1; i<speeds.length; i++){
					if(speeds[i] == 0){
						speeds[i] = approx;
					}else{
						approx = speeds[i];
					}
					Log.v(TAG, "speed"+i+":"+speeds[i]);
				}
				
				//Fetch sample_size elevation samples
				int groan = legs.get(legs.size() - 1).steps.size() - 1;
				Step last = legs.get(legs.size() - 1).steps.get(groan);
				step_locas.add(new LatLng(last.end.lat, last.end.lng));
				String enc = PolyUtil.encode(step_locas);
				String elevation_data = GoogleAPIQueries.requestSampledElevation(enc, sample_size)
						.get();
				
				route = step_locas;
				
				if (elevation_data == null) {
					throw new Exception("Google Elevation API call failed!");
				} 
				
				//Parse elevation data
				parser = APIRequestTask.JSON_FACTORY
						.createJsonParser(elevation_data);
				ElevationResult eres = parser.parse(ElevationResult.class);
				List<ElevationData> ed = eres.elevationpoints;
				
				//Log.d(TAG, ed.size()+" elevation");
				
				//Rebuild steps using flimsy speed
				//estimations.
				Iterator<ElevationData> it = ed.iterator();
				int c_dist = 0;
				elevation = new ArrayList<LatLng>();
				ElevationData ed_p = it.next();
				while(it.hasNext()){
					ElevationData ed_c = it.next();
					
					Step s = new Step();
					s.start = new Location(); s.end = new Location();
					s.start = ed_p.location; s.end = ed_c.location;
					s.start.elevation = ed_p.elevation;
					s.end.elevation = ed_c.elevation;
					 
					elevation.add(new LatLng(s.start.lat, s.start.lng));
					
					s.distance = new Value(); s.duration = new Value();
					s.distance.value = avg;
					s.duration.value = avg / speeds[c_dist];
					
					s.updateSlope(ed_p.elevation, ed_c.elevation);
					ret.add(s);
					c_dist++;
					ed_p = ed_c;
				}
//				int i = 0;
//				for (Step s : steps) {
//					ElevationData a = ed.get(i), b = ed.get(i + 1);
//					s.updateSlope(a.elevation, b.elevation);
//				}
				data_combined = ret;
				Gson gson = new Gson();
				data_combined_json = gson.toJson(ret);
				//json_processed = gson.toJson(steps);
				//Log.v(TAG, ret.size()+"");
				Log.v(TAG, data_combined_json);
				//Log.v(TAG, rawextra);
			}
		} catch (Exception e) {
			data_combined = null;
			Log.d(TAG, e.toString());
		}
		setChanged();
		notifyObservers();
	}
}
