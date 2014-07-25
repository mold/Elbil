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
	DataGraph speedGraph;
	DataGraph ampGraph;
	DataGraph ampStateGraph;
	int prevAmpChange = 0;
	
	long ampStartTime;
	int ampState;
	
	public AudioGame(Context context) {
		this.context = context;
		speedData = new DataAnalyzer(DATA_SIZE, 0);
		ampData = new DataAnalyzer(DATA_SIZE, 0.5);
	}
	
	public DataGraph getSpeedGraph() {
		speedGraph = new DataGraph(context, "speed", 0, 255);
		speedGraph.setColor(Color.GREEN);
		return speedGraph;
	}
	
	public DataGraph getAmpGraph() {
		ampGraph = new DataGraph(context, "amp", -100, 100);
		ampGraph.setColor(Color.CYAN);
		return ampGraph;
	}
	
	public DataGraph getAmpStateGraph() {
		ampStateGraph = new DataGraph(context, "amp state of change", -1, 1);
		ampStateGraph.setColor(Color.CYAN);
		return ampStateGraph;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData carData = (CarData) observable;
			speedData.pushData(carData.getSpeed(false));
			ampData.pushData(carData.getAmp(false));
			
			float average = (float) speedData.getAverage();
			if (speedGraph != null) {
				speedGraph.addDataPoint(average);
			}
			
			int change = ampData.getStateOfChange();
			if (ampGraph != null) {
				ampGraph.addDataPoint((float) ampData.getAverage());
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
			
			// entering a new state
			if (ampState == 0 && change != 0) {
				ampStartTime = System.currentTimeMillis();
				ampState = change;
			}
			// leaving a state
			if(ampState != 0 && change != ampState) {
				if (ampState == 1) {
					long time = System.currentTimeMillis() - ampStartTime;
					PdBase.sendFloat("amp_gain_time", time);
				}
				ampState = 0;
			}
		}
	}
	
}
