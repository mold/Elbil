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
 * @author marothon
 *
 */
public class RouteProgress extends Observable implements Observer {
	protected static final String TAG = "RouteProgress";
	List<Step> route;
	int current, samples;
	double travelled_distance;
	double intermediate_distance;
	double consumed_energy;
	double[] energy_per_step;
	Location current_location, last_location;

	private Activity c;
	private LocationManager locationManager;
	private double avg_current_consumption;

	// Simple updates current_location.
	LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			synchronized (this) {
				current_location = loc;
				Log.v(TAG, loc.getLatitude() + ", " + loc.getLongitude());
			}
		}

		// Unused methods
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};

	public RouteProgress(Activity c, List<Step> route) {
		this.route = route;
		this.current = 0;
		this.energy_per_step = new double[route.size()];
		this.travelled_distance = 0;
		this.c = c;
	}  

	public void start() {
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				locationManager = (LocationManager) c
						.getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 500, 15.0f,
						locationListener);
				started = true;
			}
		});
	}

	private boolean started = false;

	@Override
	public void update(Observable observable, Object data) {
		if (!started) {
			return;
		}

		if (observable instanceof CarData) {// then take current consumption and
											// add to current sum.
			consumed_energy += ((CarData) observable).getConsumption(true);
			samples++;
			updateIndex();
		}
	}

	/**
	 * Estimates the current distance traveled using the GPS position to measure the 
	 * distances between the closest step's positional data.
	 */
	private void updateIndex() {
		if (current >= route.size() || current_location == null
				|| current_location == last_location) {
			return;
		}

		synchronized (locationListener) {

			Step currentStep = route.get(current);
			Location a = new Location("");
			Location b = new Location("");

			a.setLatitude(currentStep.start.lat);
			a.setLongitude(currentStep.start.lng);
			b.setLatitude(currentStep.end.lat);
			b.setLongitude(currentStep.end.lng);

			// checks if we should move along the route
			if (current_location.distanceTo(a) > current_location
					.distanceTo((b))) {// then we have moved
										// along the route
				synchronized (this) {
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
			if (current == route.size() - 1) {//We are at the last step
				intermediate_distance = -current_location.distanceTo(b);
				return;
			}
			if (current == 0) {//We are at the first step, so always increment
				intermediate_distance = current_location.distanceTo(a);
				return;
			} else {//We are in the middle of the route
				Step prev_step = route.get(current - 1);
				Location pa = new Location("");
				pa.setLatitude(prev_step.start.lat);
				pa.setLongitude(prev_step.start.lat);

				double cb = current_location.distanceTo(b);
				double cpa = current_location.distanceTo(pa);
				if (cpa < cb) {// Before the current step start
					intermediate_distance = -current_location.distanceTo(a);
				} else {// After the current step start
					intermediate_distance = current_location.distanceTo(a);
				}
			}
			last_location = current_location;
			avg_current_consumption = consumed_energy / samples;
			samples = 0;
			consumed_energy = 0;
			current++;

			notifyObservers();
		}
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
}
