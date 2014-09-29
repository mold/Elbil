package com.kth.ev.vizsample;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;

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

public class RouteVizFragment extends Fragment implements Observer {
	@SuppressWarnings("unused")
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
		if (rb.hasData())
			cs.addViz(rb);
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
				est_c = cd.getEvEnergy().consumptionOnRoute(route,
						EVEnergy.SLOPE | EVEnergy.TIME | EVEnergy.SPEED, cd.getCurrentClimateConsumption(true));
				rb.updateEstimation(est_c);
				climat = cd.getClimate(false);
			}

		}

		// When we have received a new route to compute an estimation from.
		if (observable instanceof RouteDataFetcher) {
			new_data = true;
			route = ((RouteDataFetcher) observable).getCombinedRoute();
		}

		// When we have our data, we can create our visualization.
		if (new_data && cd != null) {
			est_c = cd.getEvEnergy().consumptionOnRoute(route, EVEnergy.SLOPE | EVEnergy.TIME
					| EVEnergy.SPEED, cd.getCurrentClimateConsumption(true));
			rb.updateData(route, est_c);
			cs.addViz(rb);
			new_data = false;
		}

		// When we have received a new gps position.
		// if (observable instanceof GPSHolder) {
		//
		// }

	}
}
