package com.kth.ev.graphviz;

import java.util.Observable;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSHolder extends Observable{
	protected static final String TAG = "GPSHolder";
	private Activity act;
	private Location current_location;
	private LocationManager locationManager;
	private long time;
	
	// Simple updates current_location.
	LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location loc) {
			time = System.currentTimeMillis();
			synchronized (this) {
				current_location = loc;
				Log.v(TAG, loc.getLatitude() + ", " + loc.getLongitude());
			}
		}

		// Unused methods
		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	public GPSHolder(Activity act){
		this.act = act;
	}
	
	/**
	 * Creates a copy of the current location. It's a copy to prevent
	 * the location from being changed after it has been fetched.
	 * 
	 * @return A copy of the current GPS location.
	 */
	public Location getCurrentLocation(){
		Location copy = new Location(current_location);
		return copy;
	}
	
	/**
	 * Returns true if we have a location
	 * 
	 * @return
	 */
	public boolean hasLocation(){
		return current_location != null;
	}
	
	/**
	 * Returns the age (in ms) of the current
	 * location.
	 * 
	 * @return time in ms
	 */
	public long locationAge(){
		return System.currentTimeMillis() - time;
	}

	public void start() {
		time = 0;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				locationManager = (LocationManager) act
						.getSystemService(Context.LOCATION_SERVICE);
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 100, 15.0f,
						locationListener);
			}
		});
	}
}
