package com.kth.ev.audiobahn.gamification;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.kth.ev.cardata.CarData;
import com.kth.ev.cardata.EVEnergy;
import com.kth.ev.graphviz.DataGraph;
import com.kth.ev.routedata.RouteDataFetcher;
import com.kth.ev.routedata.APIDataTypes.Step;

public class PdDataController implements Observer {

	Context context;
	RouteDataFetcher routeData;
	/* The distance traveled at the start of a route */
	double distanceTraveledStart;
	double routeDistanceTraveled;
	double prevConsumption;
	double[] routeConsumptions;
	int routeStepIndex;

	public FineDriver fineDriver;

	DataAnalyzer speedData;
	DataAnalyzer ampData;
	DataAnalyzer accData;
	DataGraph speedGraph;
	DataGraph ampGraph;
	DataGraph ampSpeedGraph;
	DataGraph ampAccGraph;
	DataGraph accGraph;
	DataGraph consumptionGraph;

	TextToSpeech speech;
	boolean ttsLoaded;
	boolean gameRunning;

	long ampStartTime;
	long intervalTime;
	int ampState;

	public PdDataController(Context context) {
		this.context = context;

		speedData = new DataAnalyzer(10, 0);
		ampData = new DataAnalyzer(2, 0.5);
		accData = new DataAnalyzer(2, 0);

		ttsLoaded = false;
		gameRunning = false;

		fineDriver = new FineDriver();

		speech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				speech.setLanguage(Locale.US);
				ttsLoaded = true;
				Log.v("puredata", "tts loaded");

				if (gameRunning) {
					start();
				}
			}
		});

		intervalTime = System.currentTimeMillis();
	}

	public void start() {
		gameRunning = true;

		fineDriver.reset();
	}

	public void stop() {
		gameRunning = false;
	}

	public boolean isRunning() {
		return gameRunning;
	}

	public DataGraph getSpeedGraph() {
		speedGraph = new DataGraph(context, "speed (km/h)", 0, 80);
		speedGraph.setColor(Color.GREEN);
		return speedGraph;
	}

	public DataGraph getAmpGraph() {
		ampGraph = new DataGraph(context, "amp", -120, 80);
		ampGraph.setColor(Color.CYAN);
		return ampGraph;
	}

	public DataGraph getAmpSpeedGraph() {
		ampSpeedGraph = new DataGraph(context, "amp/speed", -2, 2);
		ampSpeedGraph.setColor(Color.MAGENTA);
		return ampSpeedGraph;
	}

	public DataGraph getAccelerationGraph() {
		accGraph = new DataGraph(context, "acceleration (m/s^2)", -1, 1);
		accGraph.setColor(Color.RED);
		return accGraph;
	}

	public DataGraph getAmpAccelerationGraph() {
		ampAccGraph = new DataGraph(context, "amp/acc", -40, 40);
		ampAccGraph.setColor(Color.RED);
		return ampAccGraph;
	}

	public DataGraph getConsumptionGraph() {
		consumptionGraph = new DataGraph(context, "consumption diff", -1, 1);
		consumptionGraph.setColor(Color.YELLOW);
		return consumptionGraph;
	}

//	private String getTimeString(long time) {
//		long real_time = time;
//		String str = "";
//		// hours
//		long tmp = time / 360;
//		if (tmp >= 1) {
//			str += tmp + " hours ";
//			time -= tmp * 360;
//		}
//		// hours
//		tmp = time / 60;
//		if (tmp >= 1) {
//			str += tmp + " minutes ";
//			time -= tmp * 60;
//		}
//		if (real_time != time) {
//			str += "and ";
//		}
//		// seconds
//		str += time + " seconds";
//		return str;
//	}

	public void setRouteData(RouteDataFetcher routeData) {
		this.routeData = routeData;
		// set the distance travelled to be initialized at the next CarData
		// update
		routeDistanceTraveled = 0;
		distanceTraveledStart = -1;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			long time = System.currentTimeMillis();
			double delta = (time - intervalTime) / 3600000.0;
			intervalTime = time;

			CarData carData = (CarData) observable;
			double speed = carData.getSpeed(false);
			double amp = carData.getAmp(false);
			double acceleration = carData.getAcceleration(false);
			double amp_speed = 0;
			if (speed != 0.0) {
				amp_speed = amp / speed;
			}

			accData.pushData(acceleration);
//			double accelerationAverage = accData.getAverage();
			speedData.pushData(speed);
			double speedAverage = speedData.getAverage();
			double speedRegression = speedData.getRegression(delta);
			// convert to m/s^2
			speedRegression = Math.pow(3600.0, 2) * speedRegression / 1000.0;

			PdBase.sendFloat("speed", (float) speed);
			PdBase.sendFloat("amp", (float) amp);
			PdBase.sendFloat("acceleration", (float) acceleration);

			if (speedGraph != null) {
				speedGraph.addDataPoint((float) speedAverage);
			}

			if (accGraph != null) {
				accGraph.addDataPoint((float) speedRegression);
			}

			if (ampGraph != null) {
				ampGraph.addDataPoint((float) amp);
			}

			if (ampSpeedGraph != null) {
				if (speed != 0.0) {
					ampSpeedGraph.addDataPoint((float) amp_speed);
				} else {
					ampSpeedGraph.addDataPoint(0);
				}
			}

			if (ampAccGraph != null) {
				if (acceleration != 0.0) {
					ampAccGraph.addDataPoint((float) (amp / acceleration));
				} else {
					ampAccGraph.addDataPoint(0);
				}
			}

			// RouteDataFetcher
			double distance = carData.getDistanceTravelled(false);
			float consumptionDifference = 0;
			if (gameRunning) {
				if (routeData != null) {
					if (distanceTraveledStart == -1) {
						distanceTraveledStart = distance;
						routeConsumptions = carData.getEvEnergy().consumptionOnRoute(routeData.getCombinedRoute(), EVEnergy.SPEED | EVEnergy.TIME
										| EVEnergy.SLOPE, carData.getCurrentClimateConsumption(true));
						prevConsumption = carData.getTotalConsumption();
						Log.v("pdgame",
								"route started: " + routeData.getCombinedRoute().size());
					} else if (routeStepIndex < routeData.getCombinedRoute().size()) {
						Step step = routeData.getCombinedRoute().get(routeStepIndex);
						if (step != null) {
							double distanceSum = routeDistanceTraveled
									+ step.distance.value;
							// Log.v("pdgame", "current step: " + distanceSum +
							// "/" + (distance - distanceTravelledStart));
							// get the next step
							// Log.v("pdgame", "distance travelled: " +
							// (distance - distanceTravelledStart));
							if (distance - distanceTraveledStart > distanceSum
									&& routeStepIndex < routeData.getCombinedRoute().size()) {
								routeStepIndex++;
								step = routeData.getCombinedRoute().get(routeStepIndex);
								routeDistanceTraveled = distanceSum;
								distanceSum += step.distance.value;
								prevConsumption = carData.getTotalConsumption();
								Log.v("pdgame", "reached new step: "
										+ routeDistanceTraveled + "/"
										+ (distance - distanceTraveledStart));
							}
							if (routeStepIndex >= routeData.getCombinedRoute().size() - 1) {
								// interrupt drive
							} else if (routeStepIndex >= 0) {
								double prevRouteConsumption = 0;
								if (routeStepIndex > 0) {
									prevRouteConsumption = routeConsumptions[routeStepIndex - 1];
								}
								double relativeDistance = (distance - routeDistanceTraveled)
										/ step.distance.value;
								double currentConsumption = prevRouteConsumption
										+ relativeDistance
										* (routeConsumptions[routeStepIndex] - prevRouteConsumption);
								// PdBase.sendFloat("consumption",
								// (float) currentConsumption);
								double carConsumption = carData.getConsumption(false) - prevConsumption;
								// Log.v("pdgame", "car consumption: " +
								// carConsumption + ", consumption: "
								// + currentConsumption + ", distance: " +
								// relativeDistance);
								consumptionDifference = (float) (carConsumption - currentConsumption);
							}
						}
					}
				}

				PdBase.sendFloat("consumption_diff", consumptionDifference);
				Log.v("consumption", "" + carData.getConsumption(false));
				if (consumptionGraph != null) {
					// consumptionGraph.addDataPoint(consumptionDifference);
					consumptionGraph.addDataPoint((float) carData
							.getConsumption(false));
				}
				fineDriver.setConsumptionDifference(consumptionDifference);
			}
		}
	}

}
