package com.kth.ev.routedata;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.kth.ev.routedata.APIDataTypes.Step;

import android.location.Location;
import android.util.Log;

/**
 * An observable that will notify observers when the current gps position is
 * passing one of the step endpoints of a given route. This effectively
 * estimates the current traveled distance. Note that this class also assumes
 * that the car will travel along the original plotted route.
 * 
 * @author marothon
 *
 */
public class RouteProgress extends Observable implements Observer {
	protected static final String TAG = "RouteProgress";
	private List<Step> route;
	private int current;
	private double travelled_distance;
	private double intermediate_distance;
	private Location last_location;
	private double avg_current_consumption;
	private boolean finished;

	public RouteProgress(List<Step> route) {
		this.route = route;
		this.current = 0;
		this.travelled_distance = 0;
		finished = false;
	}
	
	public RouteProgress(List<Step> route, GPSHolder gps) {
		this.route = route;
		this.current = 0;
		this.travelled_distance = 0;
		finished = false;
		gps.addObserver(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		if (finished) {// Stop doing stuff
			Log.d(TAG, "finished");
			observable.deleteObserver(this);
			return;
		}

		if (observable instanceof GPSHolder) {
			Log.d(TAG, "got a gps position");
			GPSHolder gps = (GPSHolder) observable;
			if (gps.hasLocation() && route != null) {
				updateDistance(gps.getCurrentLocation());
			}
		}
	}

	/**
	 * Estimates the current distance traveled using the GPS position to measure
	 * the distances between the closest step's positional data.
	 */
	public void updateDistance(Location gps_location) {
		synchronized (this) {
			double od = currentDistance();
			double oid = intermediate_distance;

			if (current >= route.size() || gps_location == null
					|| gps_location == last_location) {
				Log.e(TAG, "Cannot compute new index.");
				return;
			}

			Step currentStep = route.get(current);
			Location a = new Location("");
			Location b = new Location("");

			a.setLatitude(currentStep.start.lat);
			a.setLongitude(currentStep.start.lng);
			b.setLatitude(currentStep.end.lat);
			b.setLongitude(currentStep.end.lng);

			// checks if we should move along the route
			Location c = new Location("");
			Step loop_step;
			double d = Double.MAX_VALUE, nd;
			int nc = current;
			for (int i = current; i < route.size(); i++) {//Compares current location to gps locations from the google api data.
				loop_step = route.get(i);
				c.setLatitude(loop_step.start.lat);
				c.setLongitude(loop_step.start.lng);
				nd = c.distanceTo(gps_location);
				if (d > nd) {//We have are closer to this point, thus moved past the previous step index.
					d = nd;
					nc = i;
				}
			}
			if (nc > current) {
				synchronized (this) {
					if (current >= route.size()) {// No more route points to
						// base anything on.
						finished = true;
						return;
					}

					Log.v(TAG, "TIME TO MOVE, "+(nc-current)+" steps!");
					
					for (int i = current; i < nc; i++) {
						travelled_distance += route.get(i).distance.value;
					};
					current = nc;
					currentStep = route.get(current);
					a.setLatitude(currentStep.start.lat);
					a.setLongitude(currentStep.start.lng);
					b.setLatitude(currentStep.end.lat);
					b.setLongitude(currentStep.end.lng);
				}
			}
			intermediate_distance = gps_location.distanceTo(a);
			last_location = gps_location;

			// We didn't move closer to the next point, thus use the previous
			// intermediate
			// distance
			// and don't call setChanged.
			if (od > currentDistance()) {
				intermediate_distance = oid;
				return;
			}

			// Log.d(TAG, "Current consumption: "+consumed_energy);
			setChanged();
			notifyObservers();
		}
	}

	public int currentRouteIndex() {
		return current;
	}

	/**
	 * Returns the averaged consumption for the current step.
	 * 
	 * @return
	 */
	public double currentConsumption() {
		return avg_current_consumption;
	}

	public double currentDistance() {
		return travelled_distance + intermediate_distance;
	}

	public List<Step> getRoute() {
		return route;
	}
}
