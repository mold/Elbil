package se.kth.ev.gmapsviz;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import se.kth.ev.gmapsviz.APIDataTypes.DirectionsResult;
import se.kth.ev.gmapsviz.APIDataTypes.ElevationData;
import se.kth.ev.gmapsviz.APIDataTypes.ElevationResult;
import se.kth.ev.gmapsviz.APIDataTypes.Leg;
import se.kth.ev.gmapsviz.APIDataTypes.Step;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.json.JsonParser;
import com.google.maps.android.PolyUtil;

public class EnergyEstimator extends Observable implements Runnable {
	String pointA, pointB;
	List<Step> data;

	public EnergyEstimator() {
		pointA = "KTH, Sweden";
		pointB = "Sundbyberg, Sweden";
	}

	public EnergyEstimator(String a, String b) {
		pointA = a;
		pointB = b;
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				String routes_data = GoogleAPIQueries.requestDirections(pointA,
						pointB).get();

				JsonParser parser = APIRequest.JSON_FACTORY
						.createJsonParser(routes_data);
				DirectionsResult dRes = parser.parse(DirectionsResult.class);

				List<LatLng> step_locas = new ArrayList<LatLng>(20);
				// Push all LatLng from the parsed data.
				List<Leg> legs = dRes.routes.get(0).legs;
				List<Step> steps = new ArrayList<Step>();
				for (Leg l : legs) {
					for (Step s : l.steps) {
						steps.add(s);
						step_locas.add(new LatLng(s.start.lat, s.start.lng));
					}
				}
				int groan = legs.get(legs.size() - 1).steps.size() - 1;
				Step last = legs.get(legs.size() - 1).steps.get(groan);
				step_locas.add(new LatLng(last.end.lat, last.end.lng));

				String enc = PolyUtil.encode(step_locas);
				// for(Step e :
				// directionsResult.routes.get(0).legs.get(0).steps){
				// Log.d("steps", e.toString());
				// }
				String elevation_data = GoogleAPIQueries.requestElevation(enc)
						.get();
				Log.d("elevation", elevation_data);
				parser = APIRequest.JSON_FACTORY
						.createJsonParser(elevation_data);
				ElevationResult eres = parser.parse(ElevationResult.class);
				// for(ElevationData e : eres.elevationpoints){
				// Log.d("elevation", e.toString());
				// }

				Log.d("compare", "Elevation");
				Log.d("compare", "" + eres.elevationpoints.size());
				Log.d("compare", eres.elevationpoints.get(0).toString());
				Log.d("compare", eres.elevationpoints.get(1).toString());

				Log.d("compare", "Step");
				Log.d("compare", "" + steps.size());
				Log.d("compare", steps.get(0).toString());
				List<ElevationData> ed = eres.elevationpoints;

				int i = 0;
				for (Step s : steps) {
					ElevationData a = ed.get(i), b = ed.get(i + 1);
					s.updateSlope(a.elevation, b.elevation);
					Log.d("result", s.toString());
				}

				data = steps;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
		Log.d("IM DONE!","WOOP");
	}
}
