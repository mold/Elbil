package unused;

import java.util.Observable;
import java.util.Observer;

import com.kth.ev.application.ElvizpActivity;
import com.kth.ev.application.R;
import com.kth.ev.cardata.CarData;
import com.kth.ev.cardata.EVEnergy;
import com.kth.ev.graphviz.CanvasSurface;
import com.kth.ev.routedata.RouteDataFetcher;
import com.kth.ev.routedata.APIDataTypes.Step;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
  
public class EVVizFragment extends Fragment implements Observer {
	//private static final String TAG = "EVVizFragment";
	private CanvasSurface canvas;
	private CarData cd;
	private RouteDataFetcher rdf;
	private Thread t_rdf;
	private Thread t_consump;
	private XYPlot evg;
	float[] distance_for_steps;

	private Runnable consumptionProgressUpdater = new Runnable() {
		EVEnergy ee;

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			double last_speed = 200.0f * 33.0f / 3.6f;
			float travelled_distance = 0;
			int route_index = 0;
			Step current_step;
			ee = cd.getEvEnergy();
			while (route_index < rdf.getCombinedRoute().size()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				double dt = (System.currentTimeMillis() - time) / 1000.0f;// seconds
				double speed = 200.0f * 33.0f / 3.6f;// m/s
				// double speed = cd.getSpeed(false)/3.6f; // m/s
				double lin_acc = 0;
				// double lin_acc = (speed - last_speed)/dt;
				double dist = (last_speed + speed) / 2.0f * dt;// meters
				travelled_distance += dist;
				current_step = rdf.getCombinedRoute().get(route_index);

				if (travelled_distance / 1000.0f > distance_for_steps[route_index]) {
					route_index++;
				}
				float consumption = consumptionOnStep(current_step,
						speed / 200.0f, lin_acc, dist, dt * 200.0f);
				evg.add_data_point("Realtime consumption",
						travelled_distance / 1000.0f, consumption);
				// evg.add_data_point("Realtime consumption",
				// travelled_distance/1000.0f, 0.1f);

				canvas.redraw();
				last_speed = speed;
				time = System.currentTimeMillis();
			}
		}

		private float consumptionOnStep(Step s, double speed,
				double acceleration, double distance, double dt) {
			return (float) ee.kWhPerKm(distance, speed, acceleration, s.slope,
					0, dt);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		evg = new XYPlot("km", "kWh/km", 8, 4, 0, 0, 1, 2.0f);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (cd == null)
			if (getActivity() instanceof ElvizpActivity) {
				cd = ((ElvizpActivity) getActivity()).cd;
				cd.addObserver(this);
			}
		if (t_rdf == null) {
			Log.d("ElvizFragment", "GET DATA");
			rdf = new RouteDataFetcher();
			rdf.addObserver(this);
			t_rdf = new Thread(rdf);
			t_rdf.start();
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
      View v = inflater.inflate(R.layout.fragment_elviz, container, false);
      canvas = (CanvasSurface) v.findViewById(R.id.elviz_surf);
  	  canvas.addRenderer(evg);
      return v;
    }

	@Override
	public void update(Observable observable, Object data) {
		if(canvas != null){
			if(observable instanceof RouteDataFetcher){
				//Log.d(TAG, "GOT SOME NEW DATA!");
				evg.reset();
				rdf = (RouteDataFetcher) observable;
				addEvData(cd, (RouteDataFetcher) observable);
				if(t_consump == null || !t_consump.isAlive()){
					t_consump = new Thread(consumptionProgressUpdater);
					t_consump.start();
				}
				canvas.redraw();
			}
		}
	}

	/**
	 * Adds estimated consumption along a route to the XYPlot object.
	 * 
	 * @param cd CarData object.
	 * @param rdf RouteDataFetcher object, containing the route data.
	 */
	public void addEvData(CarData cd, RouteDataFetcher rdf) {
		if(rdf.getCombinedRoute().size() < 1)
			return;
		
		int factors = 0;
		factors |= EVEnergy.SLOPE | EVEnergy.TIME | EVEnergy.SPEED;
		double[] consumption = cd.getEvEnergy().consumptionOnRoute(rdf.getCombinedRoute(), factors, 0.7 + cd.getCurrentClimateConsumption(true));

		distance_for_steps = new float[consumption.length];
		float[] consumption_per_step = new float[consumption.length];

		if (rdf.getCombinedRoute().size() > 0) {
			double xval = rdf.getCombinedRoute().get(0).distance.value;
			for (int i = 0; i < consumption.length; i++) {
				distance_for_steps[i] = (float) (xval / 1000.0f);
				consumption_per_step[i] = (float) consumption[i];
				xval += rdf.getCombinedRoute().get(i).distance.value;
			}
			evg.add_data("Consumption", distance_for_steps, consumption_per_step);
		}
	}

}