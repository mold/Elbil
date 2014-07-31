package com.kth.ev.differentiatedrange.gamification;

import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.graphviz.DataGraph;

public class AudioGame implements Observer {

	final int DATA_SIZE = 10;
	
	Context context;
	
	DataAnalyzer speedData;
	DataAnalyzer ampData;
	DataAnalyzer accData;
	DataGraph speedGraph;
	DataGraph ampGraph;
	DataGraph ampStateGraph;
	DataGraph ampSpeedGraph;
	DataGraph accGraph;
	int prevAmpChange = 0;
	
	long ampStartTime;
	int ampState;
	
	public AudioGame(Context context) {
		this.context = context;
		speedData = new DataAnalyzer(DATA_SIZE, 0);
		ampData = new DataAnalyzer(DATA_SIZE, 0.5);
		accData = new DataAnalyzer(2, 0);
	}
	
	public DataGraph getSpeedGraph() {
		speedGraph = new DataGraph(context, "speed", 0, 80);
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
		ampSpeedGraph = new DataGraph(context, "speed/amp", -30, 30);
		ampSpeedGraph.setColor(Color.MAGENTA);
		return ampSpeedGraph;
	}
	
	public DataGraph getAccelerationGraph() {
		accGraph = new DataGraph(context, "acceleration", -20, 20);
		accGraph.setColor(Color.RED);
		return accGraph;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
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
			PdBase.sendFloat("acceleration", (float) accelerationAvg);
			
			if (speedGraph != null) {
				speedGraph.addDataPoint((float) speed);
			}
			
			if (accGraph != null) {
				accGraph.addDataPoint((float) accelerationAvg);
			}
			
			int change = ampData.getStateOfChange();
			if (ampGraph != null) {
				//ampGraph.addDataPoint((float) ampData.getAverage());
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
				ampSpeedGraph.addDataPoint((float) (speed / amp));
			}
			
			// entering a new state
			if (ampState == 0 && change != 0) {
				ampStartTime = System.currentTimeMillis();
				ampState = change;
			}
			// leaving a state
			if(ampState != 0 && change != ampState) {
				if (ampState == 1) {
					long time = System.currentTimeMillis() - ampStartTime;
					Log.v("pdgame", "gain streak: " + time);
					PdBase.sendFloat("amp_gain_time", time);
				}
				ampState = 0;
			}
		}
	}
	
}
