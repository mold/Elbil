package se.kth.ev.gmapsviz;

import java.util.List;
import se.kth.ev.gmapsviz.APIDataTypes.DirectionsResult;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.json.JsonParser;
import com.google.maps.android.PolyUtil;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	GoogleMap gmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String key = getString(R.string.google_browser_api_key);
		GoogleAPIQueries.setKey(key);

		try {
			String api_data = GoogleAPIQueries.requestDirections("Chicago,IL",
					"Los Angeles,CA").get();

			JsonParser fact = APIRequest.JSON_FACTORY
					.createJsonParser(api_data);
			DirectionsResult directionsResult = fact
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
			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10.0f));
			
			//for (LatLng l : points) {
			//	Log.d("po", l.toString());
			//}
			// Log.d("encodedPoints", encodedPoints);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// GoogleAPIQueries.requestElevation("39.7391536,-104.9847034");

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
