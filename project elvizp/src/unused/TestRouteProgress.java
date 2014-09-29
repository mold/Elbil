package unused;

import java.util.List;

import android.location.Location;
import android.util.Log;

import com.kth.ev.application.ElvizpActivity;
import com.kth.ev.routedata.RouteDataFetcher;
import com.kth.ev.routedata.RouteProgress;
import com.kth.ev.routedata.APIDataTypes.Step;

public class TestRouteProgress implements Runnable {

	private static final String TAG = "TestRouteProgress";
	private RouteProgress rp;
	private List<Step> route;
	
	public TestRouteProgress(ElvizpActivity act, RouteDataFetcher in) {
		route = in.getCombinedRoute();
		rp = new RouteProgress(in.getCombinedRoute());
	} 
	
	@Override
	public void run() {
		for(int i = 0; i < route.size(); i++){
			sleepie();
			Location fake = new Location("");
			fake.setLatitude(route.get(i).start.lat);
			fake.setLongitude(route.get(i).start.lng);
			rp.updateDistance(fake);
			double dist = rp.currentDistance();
			double cons = rp.currentConsumption();
			Log.d(TAG, dist + " m, "+cons+" kWh");
		}
	}
	
	public RouteProgress getProgress(){
		return rp;
	}
	
	private void sleepie(){
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
