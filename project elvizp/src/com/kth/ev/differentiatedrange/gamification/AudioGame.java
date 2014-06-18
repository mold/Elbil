package com.kth.ev.differentiatedrange.gamification;

import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import android.content.Context;
import android.util.Log;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.graphviz.DataGraph;

public class AudioGame implements Observer {

	final int DATA_SIZE = 10;
	
	DataAnalyzer speedData;
	DataAnalyzer ampData;
	DataGraph speedGraph;
	DataGraph ampGraph;
	DataGraph ampStateGraph;
	int prevAmpChange = 0;
	
	public AudioGame(Context context) {
		speedData = new DataAnalyzer(DATA_SIZE, 0);
		ampData = new DataAnalyzer(DATA_SIZE, 0.5);
		speedGraph = new DataGraph(context, "speed average", 0, 255);
		ampGraph = new DataGraph(context, "amp average", -100, 100);
		ampStateGraph = new DataGraph(context, "amp state of change", -1, 1);
	}
	
	public DataGraph getSpeedGraph() {
		return speedGraph;
	}
	
	public DataGraph getAmpGraph() {
		return ampGraph;
	}
	
	public DataGraph getAmpStateGraph() {
		return ampStateGraph;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData carData = (CarData) observable;
			speedData.pushData(carData.getSpeed(false));
			ampData.pushData(carData.getAmp(false));
			
			float average;
			average = (float) speedData.getAverage();
			//PdBase.sendFloat("speed_average", average);
			speedGraph.addDataPoint(average);
			ampGraph.addDataPoint((float) ampData.getAverage());
			int change = ampData.getStateOfChange();
			if (change > 0 && prevAmpChange <= 0) {
				PdBase.sendBang("amp_high");
			}
			if (change < 0 && prevAmpChange >= 0) {
				PdBase.sendBang("amp_low");
			}
			prevAmpChange = change;
			ampStateGraph.addDataPoint(change);
		}
	}
	
}
