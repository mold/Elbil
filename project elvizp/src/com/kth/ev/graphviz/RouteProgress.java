package com.kth.ev.graphviz;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.graphviz.APIDataTypes.Step;

/**
 * An observable that will notify observers when the current gps position is
 * passing one of the step endpoints of a given route. This effectively
 * estimates the current traveled distance. Note that this class also assumes
 * that the car will travel along the original plotted route.
 * 
 * Another important thing to remember is that the energy consumption is
 * averaged over the number of samples received from CarData while the Car was
 * driving along the current step.
 * 
 * The energy value is fetched from a CarData using the Observer implementation,
 * while the estimated distance & energy consumption of every step is relayed
 * further using the Observable extension.
 * 
 * Will only setChanged when the car has actually moved forward (ie.
 * currentDistance() is greater than before.
 * 
 * @author marothon
 *
 */
public class RouteProgress extends Observable implements Observer {
	protected static final String TAG = "RouteProgress";
	private List<Step> route;
	private int current, samples;
	private double travelled_distance;
	private double intermediate_distance;
	private double consumed_energy;
	private double[] energy_per_step;
	private Location last_location;
	private double avg_current_consumption;
	private GPSHolder gps;

	public RouteProgress(GPSHolder gps, List<Step> route) {
		this.route = route;
		this.current = 0;
		this.energy_per_step = new double[route.size()];
		this.travelled_distance = 0;
		this.gps = gps;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {// then take current consumption and
											// add to current sum.
			consumed_energy += ((CarData) observable).getConsumption(true);
			samples++;
			if(gps.hasLocation()){
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
			if (gps_location.distanceTo(a) > gps_location.distanceTo((b))) {// then
																			// we
																			// have
																			// moved
																			// along
																			// the
																			// route
				synchronized (this) {
					current++;
					Log.v(TAG, "TIME TO MOVE");
					travelled_distance += currentStep.distance.value;
					currentStep = route.get(current);
					a.setLatitude(currentStep.start.lat);
					a.setLongitude(currentStep.start.lng);
					b.setLatitude(currentStep.end.lat);
					b.setLongitude(currentStep.end.lng);
				}
			}

			// Determine current distance using step GPS positions
			// Special cases
			if (current == route.size() - 1) {// We are at the last step, so
												// assume we are behind
				intermediate_distance = -gps_location.distanceTo(b);
			} else if (current == 0) {// We are at the first step, so assume
										// we are ahead
				intermediate_distance = gps_location.distanceTo(b);
			} else {// We are in the middle of the route
				Step prev_step = route.get(current - 1);
				Location pa = new Location("");
				pa.setLatitude(prev_step.start.lat);
				pa.setLongitude(prev_step.start.lat);

				double cb = gps_location.distanceTo(b);
				double cpa = gps_location.distanceTo(pa);
				if (cpa < cb) {// Before the current step start
					intermediate_distance = -gps_location.distanceTo(a);
				} else {// After the current step start
					intermediate_distance = gps_location.distanceTo(a);
				}
			}
			last_location = gps_location;

			// We didn't move forward, thus use the previous intermediate
			// distance
			// and don't call setChanged.
			if (od > currentDistance()) {
				intermediate_distance = oid;
				return;
			}

			// Log.d(TAG, "Current consumption: "+consumed_energy);
			if (consumed_energy > 0 && samples > 1)
				avg_current_consumption = consumed_energy / samples;

			samples = 0;
			consumed_energy = 0;
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
