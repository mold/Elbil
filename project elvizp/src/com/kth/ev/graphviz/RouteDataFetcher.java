package com.kth.ev.graphviz;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.json.JsonParser;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.kth.ev.graphviz.APIDataTypes.DirectionsResult;
import com.kth.ev.graphviz.APIDataTypes.ElevationData;
import com.kth.ev.graphviz.APIDataTypes.ElevationResult;
import com.kth.ev.graphviz.APIDataTypes.Leg;
import com.kth.ev.graphviz.APIDataTypes.Step;
/**
 * A Runnable implementation that performs some calls to the
 * google API to fetch information about a given route.
 *  
 * @author marothon
 *
 */
public class RouteDataFetcher extends Observable implements Runnable {
	private static final String TAG = "RouteDataFetcher";
	String pointA, pointB;
	public List<Step> data;
	public String raw, rawextra;

	/**
	 * Constructor
	 * 
	 * Sets some random locations for the route.
	 */
	public RouteDataFetcher() {
		pointA = "KTH, Sweden";
		pointB = "Sundbyberg, Sweden";
		data = new ArrayList<Step>();
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
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				raw = GoogleAPIQueries.requestDirections(pointA,
						pointB).get();
				if (raw == null) {
					throw new Exception("Google Direction API call failed!");
				}
				JsonParser parser = APIRequestTask.JSON_FACTORY
						.createJsonParser(raw);
				DirectionsResult dRes = parser.parse(DirectionsResult.class);
				
				List<LatLng> step_locas = new ArrayList<LatLng>(20);
				// Push all LatLng from the parsed data.
				List<Leg> legs = dRes.routes.get(0).legs;
				List<Step> steps = new ArrayList<Step>();
				for (Leg l : legs) {
					for (Step s : l.steps) {
						steps.add(s);
						step_locas.add(new LatLng(s.start.lat, s.start.lng));
					}
				}
				int groan = legs.get(legs.size() - 1).steps.size() - 1;
				Step last = legs.get(legs.size() - 1).steps.get(groan);
				step_locas.add(new LatLng(last.end.lat, last.end.lng));

				String enc = PolyUtil.encode(step_locas);
				String elevation_data = GoogleAPIQueries.requestElevation(enc)
						.get();
				if (elevation_data == null) {
					throw new Exception("Google Elevation API call failed!");
				}
				parser = APIRequestTask.JSON_FACTORY
						.createJsonParser(elevation_data);
				ElevationResult eres = parser.parse(ElevationResult.class);
				List<ElevationData> ed = eres.elevationpoints;

				int i = 0;
				double d = 0;
				for (Step s : steps) {
					d += s.distance.value;
					ElevationData a = ed.get(i), b = ed.get(i + 1);
					s.updateSlope(a.elevation, b.elevation);
				}
				Log.d(TAG, "from: "+pointA+", to: "+pointB);
				
				Log.d(TAG, "distance: "+d);
				data = steps;
				Gson gson = new Gson();
				rawextra = gson.toJson(steps);
				Log.v(TAG, rawextra);
			}
		} catch (Exception e) {
			Log.d(TAG, "Something went wrong");
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}
}
