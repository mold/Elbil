package com.kth.ev.differentiatedrange;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class AudioView implements Runnable {

	CarData cd;
	TextView dataText;
	
	public AudioView(Activity context, CarData cd) {
		dataText = (TextView) context.findViewById(R.id.data_text);
		this.cd = cd;
	}
	
	/*@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;
	    	dataText.setText("allrighty then");
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
			    public void run()
			    {

			    	//dataText.setText("soc: " + cd.getSoc(false) + "\nspeed: "+ cd.getSpeed(false) + "\nfan: " + cd.getFan(false) + "\nclimate: " + cd.getClimate(false));
			    }
			});
		}
	}*/

	@Override
	public void run() {
		dataText.setText("soc: " + cd.getSoc(true) + "\nspeed: "+ cd.getSpeed(true) + "\nfan: " + cd.getFan(true) + "\nclimate: " + cd.getClimate(true));
	}
	
}
