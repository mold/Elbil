package se.kth.ev.gmapsviz;

import java.util.List;

import se.kth.ev.gmapsviz.APIDataTypes.DirectionsResult;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.json.JsonParser;
import com.google.maps.android.PolyUtil;

public class MyMap extends SupportMapFragment {
	GoogleMap gmap;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//doStuff();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	private void doStuff() {
		try {
			String api_data = GoogleAPIQueries.requestDirections("KTH, Sweden",
					"Sundbyberg, Sweden").get();

			// Parsing example
			JsonParser parser = APIRequest.JSON_FACTORY
					.createJsonParser(api_data);
			DirectionsResult directionsResult = parser
					.parse(DirectionsResult.class);
			String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
			List<LatLng> points = PolyUtil.decode(encodedPoints);

			// MapFragment fm = (MapFragment) getFragmentManager()
			// .findFragmentById(R.id.map);

			gmap = getMap();
			PolylineOptions line = new PolylineOptions();
			line.addAll(points);
			line.width(4);
			line.color(Color.BLUE);
			gmap.addPolyline(line);
			CircleOptions circleOptionsA = new CircleOptions()
					.center(new LatLng(59.347488, 18.073494)).radius(1000)
					.fillColor(Color.BLUE);
			gmap.addCircle(circleOptionsA);
			CircleOptions circleOptionsB = new CircleOptions()
					.center(new LatLng(59.36898, 17.966210)).radius(1000)
					.fillColor(Color.GREEN);
			gmap.addCircle(circleOptionsB);
			PolylineOptions borderOptions = new PolylineOptions()
					.add(new LatLng(59.4107, 17.8367))
					.add(new LatLng(59.444, 17.940))
					.add(new LatLng(59.4077, 18.019))
					.add(new LatLng(59.455, 18.13)).color(Color.RED);
			gmap.addPolyline(borderOptions);
			gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0),
					10.0f));

			APIRequest a = GoogleAPIQueries.requestElevation(points.get(0));
			APIRequest b = GoogleAPIQueries.requestElevation(points);
			APIRequest c = GoogleAPIQueries.requestElevation(encodedPoints);
			APIRequest d = GoogleAPIQueries.requestSampledElevation(points, 10);
			APIRequest e = GoogleAPIQueries.requestSampledElevation(
					encodedPoints, 10);

			String one = a.get();
			String encoded = c.get();
			String list = b.get();
			String sampled = d.get();
			String sampled_encoded = e.get();

			Log.d("resultsA", "one: " + one);
			Log.d("resultsB", "list: " + list);
			Log.d("resultsC", "encoded: " + encoded);
			Log.d("resultsD", "sampled: " + sampled);
			Log.d("resultsE", "sampled_encoded: " + sampled_encoded);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
