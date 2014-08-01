package com.kth.ev.differentiatedrange.gamification;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.drive.query.internal.InFilter;
import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.graphviz.DataGraph;

public class AudioGame implements Observer {

	final String GAME_START = "The audio game has been started. . "; 
	
	Context context;
	
	/* game variables */
	final double ACC_D = 2; // time above acc threshold (s)
	final double ACC_T = 3; // acc threshold
	final double BRK_D = 2; // time below break threshold (s)
	final double BRK_T = -3; // break threshold
	
	long accThresholdTime;
	boolean accThresholdCrossing;
	long brkThresholdTime;
	boolean brkThresholdCrossing;
	
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
		
		smoothDriveTime = System.currentTimeMillis();
		accThresholdTime = 0;
		accThresholdCrossing = false;
		brkThresholdTime = 0;
		brkThresholdCrossing = false;
		
		if (ttsLoaded) {
			Log.v("puredata", "speech");
			speech.speak(GAME_START, TextToSpeech.QUEUE_ADD, null);
			readRecords();
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
		str = "your record for longest smooth drive is " + longestSmoothDrive + " seconds.";
		speech.speak(str, TextToSpeech.QUEUE_ADD, null);
	}

	public DataGraph getSpeedGraph() {
		speedGraph = new DataGraph(context, "speed (km/h)", 0, 80);
		speedGraph.setColor(Color.GREEN);
		return speedGraph;
	}

	public DataGraph getAmpGraph() {
		ampGraph = new DataGraph(context, "amp", -40, 40);
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
	
	private void interruptSmoothDrive() {
		long time = System.currentTimeMillis();
		long smoothDrive = (time - smoothDriveTime) / 1000;
		
		if (smoothDrive > longestSmoothDrive) {
			longestSmoothDrive = smoothDrive;
			readRecords();
		}
		
		smoothDriveTime = time;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			long time = System.currentTimeMillis();
			double delta = (time - intervalTime) / 1000.0;
			intervalTime = time;

			CarData carData = (CarData) observable;
			double speed = carData.getSpeed(false);
			double amp = carData.getAmp(false);
			double acceleration = carData.getAcceleration(false);

			speedData.pushData(speed);
			ampData.pushData(amp);
			accData.pushData(acceleration);
			double accelerationAvg = accData.getAverage();

			PdBase.sendFloat("speed", (float) speed);
			PdBase.sendFloat("amp", (float) amp);
			PdBase.sendFloat("acceleration", (float) acceleration);

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
				PdBase.sendBang("amp_high");
			}
			if (change < 0 && prevAmpChange >= 0) {
				PdBase.sendBang("amp_low");
			}
			prevAmpChange = change;

			if (ampSpeedGraph != null) {
				if (speed != 0.0) {
					ampSpeedGraph.addDataPoint((float) (amp / speed));
				} else {
					ampSpeedGraph.addDataPoint();
				}
			}

			if (speed != 0.0) {
				PdBase.sendFloat("amp_speed", (float) (amp / speed));
			} else {
				PdBase.sendFloat("amp_speed", 0);
			}

			if (accelerationAvg != 0.0) {
				PdBase.sendFloat("amp_acc", (float) (amp / acceleration));
			} else {
				PdBase.sendFloat("amp_acc", 0);
			}

			if (ampAccGraph != null) {
				if (accelerationAvg != 0.0) {
					ampAccGraph.addDataPoint((float) (amp / acceleration));
				} else {
					ampAccGraph.addDataPoint();
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
					Log.v("pdgame", "gain streak: " + time);
					PdBase.sendFloat("amp_gain_time", time);
				}
				ampState = 0;
			}
			
			/* data threshold checking */
			if (acceleration > ACC_T) {
				if (!accThresholdCrossing) {
					accThresholdTime = System.currentTimeMillis();
					accThresholdCrossing = true;
				}
			} else {
				if (accThresholdCrossing) {
					time = System.currentTimeMillis() - accThresholdTime;
					if (time > ACC_D) {
						interruptSmoothDrive();
					}
					Log.v("audiogame", "Acc time: " + time);
				}
			}
			if (acceleration < BRK_T) {
				if (!brkThresholdCrossing) {
					brkThresholdTime = System.currentTimeMillis();
					brkThresholdCrossing = true;
				}
			} else {
				if (brkThresholdCrossing) {
					time = System.currentTimeMillis() - brkThresholdTime;
					if (time > BRK_D) {
						interruptSmoothDrive();
					}
					Log.v("audiogame", "Brk time: " + time);
				}
			}
		}
	}

}
