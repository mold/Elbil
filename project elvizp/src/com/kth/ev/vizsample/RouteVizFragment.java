package com.kth.ev.vizsample;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;

import com.kth.ev.cardata.CarData;
import com.kth.ev.graphviz.CanvasSurface;
import com.kth.ev.routedata.APIDataTypes.Step;
import com.kth.ev.routedata.RouteDataFetcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RouteVizFragment extends Fragment implements Observer {
	private static final String TAG = "RouteVizFragment";
	private List<Step> route;
	private double[] est_c;// estimated consumption
	private double climat = 0;// check for difference in climate;
	private CarData cd;// volatile reference of carData.
	private Drawer cs;
	private RouteBoxes rb;
	private boolean new_data = false;

	/**
	 * ===========================
	 * 
	 * Fragment methods.
	 * 
	 * ===========================
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		cs.stopDrawing();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (rb != null)
			cs.addViz(rb);
		cs.startDrawing();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		View v = inflater.inflate(R.layout.fragment_elviz, container, false);
		
		if (cs == null)
			cs = new Drawer(500,
					(CanvasSurface) v.findViewById(R.id.elviz_surf));
		else
			cs.changeSurface((CanvasSurface) v.findViewById(R.id.elviz_surf));
		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * ===========================
	 * 
	 * Data and visualization methods.
	 * 
	 * ===========================
	 */

	/**
	 * Here, we should update the data we use in the visualization. There can be
	 * a minor synchronization problem here as the android fragment may not have
	 * been fully created before we this method is called. In that case, the
	 * update call will wait until the view has been initialized.
	 */
	@Override
	public void update(Observable observable, Object data) {
		// When we get new car data
		if (observable instanceof CarData) {

			if (cd == null) {
				cd = (CarData) observable;
				climat = cd.getClimate(false);
			}

			if (climat - cd.getClimate(false) > 0.5f) {// Re-estimate energy
														// consumption, if we
														// have a route.
				est_c = cd.consumptionOnRoute(route, CarData.CLIMATE
						| CarData.SLOPE | CarData.TIME | CarData.SPEED);
				rb.updateEstimation(est_c);
			}

		}

		// When we have received a new route to compute an estimation from.
		if (observable instanceof RouteDataFetcher) {
			Log.d(TAG, "NEW DATA");
			new_data = true;
			route = ((RouteDataFetcher) observable).getCombinedRoute();
		}

		// When we have our data, we can create our visualization.
		if (new_data && route != null && cd != null) {
			Log.d(TAG, "VIZ TIME");

			est_c = cd.consumptionOnRoute(route, CarData.SLOPE | CarData.TIME
					| CarData.SPEED);
			if (rb == null)
				rb = new RouteBoxes(route, est_c);
			else
				rb.updateData(route, est_c);
			// If the view has not been initialized yet, we have to wait
			// until it is so. We wait by yielding to other threads.
			while (cs == null) {
				Log.d(TAG, "YIELD");
				Thread.yield();
			}
			cs.addViz(rb);
			new_data = false;
		}

		// When we have received a new gps position.
		// if (observable instanceof GPSHolder) {
		//
		// }

	}
}
