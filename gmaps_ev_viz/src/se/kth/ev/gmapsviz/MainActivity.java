package se.kth.ev.gmapsviz;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.APIDataTypes.DirectionsResult;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.json.JsonParser;
import com.google.maps.android.PolyUtil;
import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.CarDataFetcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements Observer {
	GoogleMap gmap;
	CarData cd;
	CarDataFetcher cdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String key = getString(R.string.google_browser_api_key);
		GoogleAPIQueries.setKey(key);
		
		//Create CarData container
		cd = new CarData();
		
		//Makes this activity listen for change in the activity
		cd.addObserver(this);
		
		//Starts a separate thread for fetching the data
		cdf = new CarDataFetcher(cd, false);
		new Thread(cdf).start();
		
		try {
			String api_data = GoogleAPIQueries.requestDirections("KTH, Sweden",
					"Sundbyberg, Sweden").get();

			//Parsing example
			JsonParser parser = APIRequest.JSON_FACTORY
					.createJsonParser(api_data);
			DirectionsResult directionsResult = parser
					.parse(DirectionsResult.class);
			String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
			List<LatLng> points = PolyUtil.decode(encodedPoints);
			MapFragment fm = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);

			gmap = fm.getMap();
			PolylineOptions line = new PolylineOptions();
			line.addAll(points);
			line.width(4);
			line.color(Color.BLUE);
			gmap.addPolyline(line);
			CircleOptions circleOptionsA = new CircleOptions()
			.center(new LatLng(59.347488, 18.073494))
			.radius(1000)
			.fillColor(Color.BLUE);
			gmap.addCircle(circleOptionsA);
			CircleOptions circleOptionsB = new CircleOptions()
			.center(new LatLng(59.36898, 17.966210))
			.radius(1000)
			.fillColor(Color.GREEN);
			gmap.addCircle(circleOptionsB);
			PolylineOptions borderOptions = new PolylineOptions()
					.add(new LatLng(59.4107, 17.8367))
					.add(new LatLng(59.444, 17.940))
					.add(new LatLng(59.4077, 18.019))
					.add(new LatLng(59.455, 18.13))
					.color(Color.RED);
		   gmap.addPolyline(borderOptions);
			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10.0f));
			
			APIRequest a = GoogleAPIQueries.requestElevation(points.get(0));
			APIRequest b = GoogleAPIQueries.requestElevation(points);
			APIRequest c = GoogleAPIQueries.requestElevation(encodedPoints);
			APIRequest d = GoogleAPIQueries.requestSampledElevation(points, 10);
			APIRequest e = GoogleAPIQueries.requestSampledElevation(encodedPoints, 10);
			
			String one = a.get();
			String encoded = c.get();
			String list = b.get();
			String sampled = d.get();
			String sampled_encoded = e.get();
			
			Log.d("resultsA", "one: "+one);
			Log.d("resultsB", "list: "+list);
			Log.d("resultsC", "encoded: "+encoded);
			Log.d("resultsD", "sampled: "+sampled);
			Log.d("resultsE", "sampled_encoded: "+sampled_encoded);
			//for (LatLng l : points) {
			//	Log.d("po", l.toString());
			//}
			// Log.d("encodedPoints", encodedPoints);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Simply prints the current car battery level every time
	 * there is a change in the CarData object.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof CarData){
			CarData cd = (CarData) observable;
			String battery = String.valueOf(cd.getSoc(true));
			Log.d("Energy", battery);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_main, container,
	// false);
	// return rootView;
	// }
	// }

}
