package com.kth.ev.differentiatedrange.gamification;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.graphviz.APIDataTypes.Step;
import com.kth.ev.graphviz.DataGraph;
import com.kth.ev.graphviz.RouteDataFetcher;

public class AudioGame implements Observer {

	final String GAME_START = "The audio game has been started. . ";

	Context context;
	RouteDataFetcher routeData;
	/* The distance travelled at the start of a route */
	double distanceTravelledStart;
	double routeDistanceTravelled;
	double prevConsumption;
	double[] routeConsumptions;
	int routeStepIndex;
	int smoothDrivePoints;

	/* game variables */
	final double ACC_D = 2; // time above acc threshold (s)
	final double ACC_T = 3; // acc threshold
	final double BRK_D = 2; // time below break threshold (s)
	final double BRK_T = -3; // break threshold
	final double AMP_ACC_T = -2;
	final double AMP_BRK_T = 1.5;
	final double AMP_GAIN_T = 1;

	long accThresholdTime;
	boolean accThresholdCrossing;
	long brkThresholdTime;
	boolean brkThresholdCrossing;
	long ampGainTimeStart;
	double ampGainMax = 10;
	double ampGainLevelMeter;
	double ampGainTotal;
	
	public FineDriver fineDriver;

	/* game data */
	long longestSmoothDrive;
	long smoothDriveTime;

	DataAnalyzer speedData;
	DataAnalyzer ampData;
	DataAnalyzer accData;
	DataGraph speedGraph;
	DataGraph ampGraph;
	DataGraph ampStateGraph;
	DataGraph ampSpeedGraph;
	DataGraph ampAccGraph;
	DataGraph accGraph;
	DataGraph consumptionGraph;
	int prevAmpChange = 0;

	TextToSpeech speech;
	boolean ttsLoaded;
	boolean gameRunning;

	long ampStartTime;
	long intervalTime;
	int ampState;

	public AudioGame(Context context) {
		this.context = context;
		
		speedData = new DataAnalyzer(2, 0);
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
		smoothDriveTime = System.currentTimeMillis();
		accThresholdTime = 0;
		accThresholdCrossing = false;
		brkThresholdTime = 0;
		brkThresholdCrossing = false;

		if (ttsLoaded) {
			Log.v("puredata", "speech");
			//speech.speak(GAME_START, TextToSpeech.QUEUE_ADD, null);
			//readRecords();
		}
	}

	public void stop() {
		gameRunning = false;
	}

	public boolean isRunning() {
		return gameRunning;
	}

	private void readRecords() {
		String str;
		str = "your record for longest smooth drive is "
				+ getTimeString(longestSmoothDrive);
		speech.speak(str, TextToSpeech.QUEUE_ADD, null);
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

	public DataGraph getAmpStateGraph() {
		ampStateGraph = new DataGraph(context, "amp state of change", -1, 1);
		ampStateGraph.setColor(Color.CYAN);
		return ampStateGraph;
	}

	public DataGraph getAmpSpeedGraph() {
		ampSpeedGraph = new DataGraph(context, "amp/speed", -2, 2);
		ampSpeedGraph.setColor(Color.MAGENTA);
		return ampSpeedGraph;
	}

	public DataGraph getAccelerationGraph() {
		accGraph = new DataGraph(context, "acceleration (m/s^2)", -10, 10);
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

	private String getTimeString(long time) {
		long real_time = time;
		String str = "";
		// hours
		long tmp = time / 360;
		if (tmp >= 1) {
			str += tmp + " hours ";
			time -= tmp * 360;
		}
		// hours
		tmp = time / 60;
		if (tmp >= 1) {
			str += tmp + " minutes ";
			time -= tmp * 60;
		}
		if (real_time != time) {
			str += "and ";
		}
		// seconds
		str += time + " seconds";
		return str;
	}

	public void setRouteData(RouteDataFetcher routeData) {
		this.routeData = routeData;
		// set the distance travelled to be initialized at the next CarData
		// update
		routeDistanceTravelled = 0;
		distanceTravelledStart = -1;
	}

	private void interruptSmoothDrive() {
		long time = System.currentTimeMillis();
		long smoothDrive = (time - smoothDriveTime) / 1000;

		if (smoothDrive > longestSmoothDrive) {
			longestSmoothDrive = smoothDrive;
			//readRecords();
		}

		smoothDriveTime = time;
		
		smoothDrivePoints--;
		//PdBase.sendFloat("drive_points", smoothDrivePoints);
		//PdBase.sendBang("drive_points_loss");
		Log.v("pdgame", "Smooth drive points: " + smoothDrivePoints + " (-1)");
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			long time = System.currentTimeMillis();
			// double delta = (time - intervalTime) / 1000.0;
			intervalTime = time;

			CarData carData = (CarData) observable;
			double speed = carData.getSpeed(false);
			double amp = carData.getAmp(false);
			double acceleration = carData.getAcceleration(false);
			double amp_speed = 0;
			if (speed != 0.0) {
				amp_speed = amp / speed;
			}

			speedData.pushData(speed);
			ampData.pushData(amp);
			accData.pushData(acceleration);
			double accelerationAvg = accData.getAverage();

			Log.v("pddebug", "speed: " + speed + ", amp: " + amp);
			//PdBase.sendFloat("speed", (float) speed);
			PdBase.sendFloat("amp", (float) amp);
			//PdBase.sendFloat("acceleration", (float) acceleration);

			if (speedGraph != null) {
				speedGraph.addDataPoint((float) speed);
			}

			if (accGraph != null) {
				accGraph.addDataPoint((float) acceleration);
			}

			int change = ampData.getStateOfChange();
			if (ampGraph != null) {
				// ampGraph.addDataPoint((float) ampData.getAverage());
				ampGraph.addDataPoint((float) amp);
			}
			if (ampStateGraph != null) {
				ampStateGraph.addDataPoint(change);
			}
			if (change > 0 && prevAmpChange <= 0) {
				//PdBase.sendBang("amp_high");
			}
			if (change < 0 && prevAmpChange >= 0) {
				//PdBase.sendBang("amp_low");
			}
			prevAmpChange = change;

			if (ampSpeedGraph != null) {
				if (speed != 0.0) {
					ampSpeedGraph.addDataPoint((float) amp_speed);
				} else {
					ampSpeedGraph.addDataPoint(0);
				}
			}

			if (speed != 0.0) {
				//PdBase.sendFloat("amp_speed", (float) amp_speed);
			} else {
				//PdBase.sendFloat("amp_speed", 0);
			}

			if (accelerationAvg != 0.0) {
				//PdBase.sendFloat("amp_acc", (float) (amp / acceleration));
			} else {
				//PdBase.sendFloat("amp_acc", 0);
			}

			if (ampAccGraph != null) {
				if (accelerationAvg != 0.0) {
					ampAccGraph.addDataPoint((float) (amp / acceleration));
				} else {
					ampAccGraph.addDataPoint(0);
				}
			}

			// entering a new state
			if (ampState == 0 && change != 0) {
				ampStartTime = System.currentTimeMillis();
				ampState = change;
			}
			// leaving a state
			if (ampState != 0 && change != ampState) {
				if (ampState == 1) {
					time = System.currentTimeMillis() - ampStartTime;
					//Log.v("pdgame", "gain streak: " + time);
					//PdBase.sendFloat("amp_gain_time", time);
				}
				ampState = 0;
			}

			if (acceleration > 0) {
				//PdBase.sendFloat("acc_threshold",
						//(float) (acceleration / ACC_T));
			} else {
				//PdBase.sendFloat("acc_threshold",
						//(float) (acceleration / BRK_T));
			}
			if (amp_speed > 0) {
				//PdBase.sendFloat("amp_speed_threshold",
					//	(float) (amp_speed / AMP_BRK_T));
			} else {
				//PdBase.sendFloat("amp_speed_threshold",
					//	(float) (amp_speed / AMP_ACC_T));
			}

			if (gameRunning) {
				/* data threshold checking */
				// acceleration
				if (speed > 1
						&& (acceleration > ACC_T || amp_speed < AMP_ACC_T)) {
					Log.v("pdgame", "acc: " + acceleration + ", amp/speed: "
							+ amp_speed);
					if (!accThresholdCrossing) {
						accThresholdTime = System.currentTimeMillis();
						accThresholdCrossing = true;
					}
				} else {
					if (accThresholdCrossing) {
						accThresholdCrossing = false;
						time = System.currentTimeMillis() - accThresholdTime;
						if (time > ACC_D) {
							interruptSmoothDrive();
						}
						Log.v("pdgame", "Acc time: " + time);
					}
				}
				// break
				if (speed > 1
						&& (acceleration < BRK_T || amp_speed > AMP_BRK_T)) {
					Log.v("pdgame", "acc: " + acceleration + ", amp/speed: "
							+ amp_speed);
					if (!brkThresholdCrossing) {
						brkThresholdTime = System.currentTimeMillis();
						brkThresholdCrossing = true;
					}
				} else {
					if (brkThresholdCrossing) {
						brkThresholdCrossing = false;
						time = System.currentTimeMillis() - brkThresholdTime;
						if (time > BRK_D) {
							// dont interrupt on breaks
							//interruptSmoothDrive();
						}
						Log.v("pdgame", "Brk time: " + time);
					}
				}
			}
			
			// amp gain (game)
			if (amp > 0) {
				if (ampGainTotal == 0.0) {
					ampGainTimeStart = System.currentTimeMillis();
				}
				ampGainTotal += amp;
			} else {
				if (ampGainTotal != 0.0) {
					time = System.currentTimeMillis() - ampGainTimeStart;
					double gain = 0;
					if (time > 0) {
						gain = ampGainTotal / (time / 1000);
					}
					int newPoints = 0;
					Log.v("pdgame", "gain: " + gain);
					ampGainLevelMeter += gain;
					// add gain to the level meter
					while (ampGainLevelMeter > ampGainMax) {
						// recieve points!
						smoothDrivePoints++;
						newPoints++;
						
						ampGainLevelMeter -= ampGainMax;
						if (ampGainLevelMeter < 0) {
							ampGainLevelMeter = 0;
						}
					}
					// recieved points
					if (newPoints > 0) {
						//PdBase.sendFloat("drive_points", smoothDrivePoints);
						//PdBase.sendBang("drive_points_gain");
						Log.v("pdgame", "Smooth drive points: " + smoothDrivePoints + " (+" + newPoints + ")");
					}
					if (gain > ampGainMax) {
						ampGainMax = gain;
					}
					ampGainTotal = 0;
				}
			}

			// RouteDataFetcher
			//Log.v("pdgame", "routeData step index: " + routeStepIndex);
			double distance = carData.getDistanceTravelled(false);
			float consumptionDifference = 0;
			if (routeData != null) {
				if (distanceTravelledStart == -1) {
					distanceTravelledStart = distance;
					routeConsumptions = carData.consumptionOnRoute(
							routeData.data, CarData.SPEED | CarData.TIME | CarData.SLOPE | CarData.CLIMATE);
					prevConsumption = carData.getTotalConsumption();
					Log.v("pdgame", "route started: " + routeData.data.size());
				} else if(routeStepIndex < routeData.data.size()) {
					Step step = routeData.data.get(routeStepIndex);
					if (step != null) {
						double distanceSum = routeDistanceTravelled
								+ step.distance.value;
						//Log.v("pdgame", "current step: " + distanceSum + "/" + (distance - distanceTravelledStart));
						// get the next step
						//Log.v("pdgame", "distance travelled: " + (distance - distanceTravelledStart));
						while (distance - distanceTravelledStart > distanceSum
								&& routeStepIndex < routeData.data.size()) {
							routeStepIndex++;
							step = routeData.data.get(routeStepIndex);
							routeDistanceTravelled = distanceSum;
							distanceSum += step.distance.value;
							prevConsumption = carData.getTotalConsumption();
							Log.v("pdgame", "reached new step: " + routeDistanceTravelled + "/" + (distance - distanceTravelledStart));
						}
						if (routeStepIndex >= routeData.data.size() - 1) {
							// interrupt drive
						} else if(routeStepIndex >= 0) {
							double prevRouteConsumption = 0;
							if (routeStepIndex > 0) {
								prevRouteConsumption = routeConsumptions[routeStepIndex - 1];
							}
							double relativeDistance = (distance - routeDistanceTravelled)
									/ step.distance.value;
							double currentConsumption = prevRouteConsumption
									+ relativeDistance
									* (routeConsumptions[routeStepIndex] - prevRouteConsumption);
							//PdBase.sendFloat("consumption",
								//	(float) currentConsumption);
							double carConsumption = carData.getTotalConsumption() - prevConsumption;
							//Log.v("pdgame", "car consumption: " + carConsumption + ", consumption: "
							//		+ currentConsumption + ", distance: " + relativeDistance);
							consumptionDifference = (float) (carConsumption - currentConsumption);
						}
					}
				}
			}
			
			PdBase.sendFloat("consumption_diff", consumptionDifference);
			if (consumptionGraph != null) {
				consumptionGraph.addDataPoint(consumptionDifference);
			}
			fineDriver.setConsumptionDifference(consumptionDifference);
		}
	}

}
