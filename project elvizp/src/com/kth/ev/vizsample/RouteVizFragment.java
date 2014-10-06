package com.kth.ev.vizsample;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.kth.ev.application.R;
import com.kth.ev.cardata.CarData;
import com.kth.ev.cardata.EVEnergy;
import com.kth.ev.graphviz.CanvasSurface;
import com.kth.ev.routedata.APIDataTypes.Step;
import com.kth.ev.routedata.RouteDataFetcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Sample fragment to handle the visualization classes.
 * 
 * The fragment will retain the data so the visualizations
 * can be reproduced when the fragment comes into view.
 * 
 * The fragment implements the Observer pattern to receive
 * data updates. Depending on what data that this
 * observer has been attached to it can do different things.
 * In this sample, the fragment will listen for RouteDataFetcher
 * and CarData. When CarData is received, the climate consumption
 * is recorded and the reference to its EVEnergy instance is updated.
 * EVEnergy is later used for the consumptionOnRoute method. 
 * 
 * The classes Drawer and RouteBoxes takes care of the visualization 
 * part.
 * 
 * @author marothon
 *
 */
public class RouteVizFragment extends Fragment implements Observer {
//	private static final String TAG = "RouteVizFragment";
	private List<Step> route;
	private double[] est_c;// estimated consumption
	private double climat = 0;// check for difference in climate;
	private EVEnergy ev;
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
		cs = new Drawer(500);
		rb = new RouteBoxes();
	}

	@Override
	public void onPause() {
		super.onPause();
		cs.stopDrawing();
	}

	@Override
	public void onResume() {
		super.onResume();
		cs.addViz(rb);
		// cs.drawOnce();
		cs.startDrawing();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		View v = inflater.inflate(R.layout.fragment_elviz, container, false);
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
	 * Data handling methods.
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
		// For when we get new car data
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;
			ev = cd.getEvEnergy();

			// Re-estimate energy consumption, if we have a route.
			if (climat != cd.getCurrentClimateConsumption(true)
					&& route != null) {
				est_c = ev.consumptionOnRoute(route, EVEnergy.SLOPE
						| EVEnergy.TIME | EVEnergy.SPEED,
						cd.getCurrentClimateConsumption(true));
				rb.updateEstimation(est_c);
			}
			climat = cd.getCurrentClimateConsumption(true);
		}

		// For when we have received a new route to compute an estimation from.
		if (observable instanceof RouteDataFetcher) {
			new_data = true;
			route = ((RouteDataFetcher) observable).getCombinedRoute();
		}

		// For when we have our data, where we create our visualization.
		if (new_data && ev != null) {
			est_c = ev.consumptionOnRoute(route, EVEnergy.SLOPE | EVEnergy.TIME
					| EVEnergy.SPEED, climat);
			rb.updateData(route, est_c);
			new_data = false;
		}
	}
}
