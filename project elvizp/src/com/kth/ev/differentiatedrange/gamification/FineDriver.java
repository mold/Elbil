package com.kth.ev.differentiatedrange.gamification;

import org.puredata.core.PdBase;

import se.kth.ev.gmapsviz.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FineDriver {

	private enum Mode {
		NORMAL, GOOD, BAD
	}

	private final String TAG = "finedriver";
	private final String R_CONSUMPTION = "c_overflow";
	private final String R_TRANSITION = "c_transition_time";
	private final String R_POS_BANG = "c_pos_bang";
	private final String R_NEG_BANG = "c_neg_bang";
	private final String R_GOOD_BANG = "c_good_bang";
	private final String R_BAD_BANG = "c_bad_bang";
	private final String R_NORMAL_BANG = "c_normal_bang";
	private final String R_STATE_BANG = "c_state_bang";

	// time before a good/bad mode kicks in
	private final long ACTIVATION_TIME = 5000;
	// time before a good/bad mode is left
	private final long DEACTIVATION_TIME = 5000;
	// values above this value are considered good
	private final double POSITIVE_LIMIT = 0.3;
	// values below this value are considered bad
	private final double NEGATIVE_LIMIT = -0.2;

	// the interval of loosing points in NORMAL mode
	private final long NORMAL_POINTS_INTERVAL = 4000;
	private final long GOOD_POINTS_INTERVAL = 1000;
	private final long BAD_POINTS_INTERVAL = 1000;

	private long modeTimeStamp;
	private long pointsTimeStamp;
	private double consumptionDifference;
	private Mode mode;
	private int points;

	// View
	LinearLayout view;
	TextView pointsText;

	public FineDriver() {

	}

	public void reset() {
		consumptionDifference = 0;
		points = 0;
		mode = Mode.NORMAL;
	}

	public void setConsumptionDifference(double difference) {
		long time = System.currentTimeMillis();

		if (difference >= POSITIVE_LIMIT) {
			// check if the consumption just passed the limit
			if (consumptionDifference < POSITIVE_LIMIT) {
				modeTimeStamp = time;
				PdBase.sendBang(R_POS_BANG);
			}
			if (mode != Mode.GOOD) {
				if (time - modeTimeStamp > ACTIVATION_TIME) {
					mode = Mode.GOOD;
					Log.v(TAG, "Switched to GOOD mode");
					PdBase.sendBang(R_GOOD_BANG);
					PdBase.sendBang(R_STATE_BANG);
				}
			}
		} else if (difference <= NEGATIVE_LIMIT) {
			// check if the consumption just passed the limit
			if (consumptionDifference > NEGATIVE_LIMIT) {
				modeTimeStamp = time;
				PdBase.sendBang(R_NEG_BANG);
			}
			if (mode != Mode.BAD) {
				if (time - modeTimeStamp > ACTIVATION_TIME) {
					mode = Mode.BAD;
					Log.v(TAG, "Switched to BAD mode");
					PdBase.sendBang(R_BAD_BANG);
					PdBase.sendBang(R_STATE_BANG);
				}
			}
		} else {
			if (mode != Mode.NORMAL) {
				// check if the consumption just passed a limit
				if (consumptionDifference <= NEGATIVE_LIMIT
						|| consumptionDifference >= POSITIVE_LIMIT) {
					modeTimeStamp = time;
					switch (mode) {
					case GOOD:
						PdBase.sendBang(R_NEG_BANG);
						break;
					case BAD:
						PdBase.sendBang(R_POS_BANG);
						break;
					default:
						break;
					}
				}
				if (time - modeTimeStamp > DEACTIVATION_TIME) {
					mode = Mode.NORMAL;
					Log.v(TAG, "Switched to NORMAL mode");
					PdBase.sendBang(R_NORMAL_BANG);
					PdBase.sendBang(R_STATE_BANG);
				} else {
					float transitionTime = (float) ((time - modeTimeStamp) / DEACTIVATION_TIME);
					PdBase.sendFloat(R_TRANSITION, transitionTime);
				}
			}
		}

		// calculate points
		float rConsumption;
		switch (mode) {
		case NORMAL:
			if (time - pointsTimeStamp > NORMAL_POINTS_INTERVAL) {
				points -= 1;
				Log.v(TAG, "points: " + points);
				pointsTimeStamp = time;
				updateView();
			}
			break;
		case GOOD:
			if (time - pointsTimeStamp > GOOD_POINTS_INTERVAL) {
				points += 1;
				pointsTimeStamp = time;
				updateView();
			}
			rConsumption = (float) (difference - POSITIVE_LIMIT);
			PdBase.sendFloat(R_CONSUMPTION, rConsumption < 0 ? 0 : rConsumption);
			break;
		case BAD:
			if (time - pointsTimeStamp > BAD_POINTS_INTERVAL) {
				points -= 1;
				pointsTimeStamp = time;
				updateView();
			}
			rConsumption = (float) (difference - NEGATIVE_LIMIT);
			PdBase.sendFloat(R_CONSUMPTION, rConsumption > 0 ? 0 : rConsumption);
			break;
		}

		consumptionDifference = difference;
	}

	public View getView(Activity activity) {
		LayoutInflater inflater = activity.getLayoutInflater();
		view = (LinearLayout) inflater.inflate(R.layout.fine_driver, null);
		pointsText = (TextView) view.findViewById(R.id.points_text);
		/*
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		view = new LinearLayout(context);
		view.setLayoutParams(params);
		pointsText = new TextView(context);
		pointsText.setLayoutParams(params);
		view.addView(pointsText);
		*/
		updateView();
		return view;
	}
	
	private class ViewThread implements Runnable {

		@Override
		public void run() {
			pointsText.setText("Points: " + points);
		}
		
	}

	private void updateView() {
		if (view != null) {
			//Looper.prepare();
			//new Handler().post(new ViewThread());
		}
	}
}
