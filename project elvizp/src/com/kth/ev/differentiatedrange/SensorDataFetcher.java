package com.kth.ev.differentiatedrange;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class SensorDataFetcher {

	LocationManager locationManager;
	
	LocationListener locationListener = new LocationListener() {
	    @Override
	    public void onLocationChanged(Location loc) {
	       Log.v("gps", loc.getLatitude()+" : "+loc.getLongitude());
	    }

	    @Override
	    public void onProviderDisabled(String provider) {}

	    @Override
	    public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
	public SensorDataFetcher(Context context) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, locationListener);
	}
	
}
