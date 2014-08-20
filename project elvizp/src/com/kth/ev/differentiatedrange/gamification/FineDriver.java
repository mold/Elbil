package com.kth.ev.differentiatedrange.gamification;

import org.puredata.core.PdBase;

import se.kth.ev.gmapsviz.R;

import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class FineDriver {

	private enum Mode {
		NORMAL("NORMAL"), GOOD("GOOD"), BAD("BAD");

		private final String name;

		private Mode(String s) {
			name = s;
		}

		public String toString() {
			return name;
		}
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

	// the interval of loosing points in the different modes
	private final long NORMAL_POINTS_INTERVAL = 4000;
	private final long GOOD_POINTS_INTERVAL = 2000;
	private final long BAD_POINTS_INTERVAL = 2000;
	// a value to see how long time is spent in a mode
	private final long MODE_OVERRIDE_TIME = 30000;

	private long modeStartTimeStamp;
	private long modeTimeStamp;
	private long pointsTimeStamp;
	private double consumptionDifference;
	private double consumptionOverflow;
	private Mode mode;
	private Mode currentMode;
	private double transitionTime;
	private double modeOverrideTime;

	// View
	TextView pointsText;
	TextView consumptionText;
	TextView modeText;
	TextView debugText;
	TextView levelText;
	TextView scoringText;
	Handler uiHandler;
	Resources resources;
	boolean viewInitialized;
	
	// scoring
	private int points;
	private int level;
	private int prevLevelPoints;
	private int nextLevelPoints;
	private final double LOOSE_LEVEL_LIMIT = 0.9;

	public FineDriver() {
		uiHandler = new Handler();
		viewInitialized = false;
		reset();
	}

	public void reset() {
		consumptionDifference = 0;
		points = 0;
		level = 0;
		prevLevelPoints = 0;
		nextLevelPoints = 50;
		mode = Mode.NORMAL;
		currentMode = Mode.NORMAL;
		modeStartTimeStamp = System.currentTimeMillis();
	}

	public void setConsumptionDifference(double difference) {
		long time = System.currentTimeMillis();

		if (difference >= POSITIVE_LIMIT) {
			if (currentMode != Mode.GOOD) {
				currentMode = Mode.GOOD;
			}
			// check if the consumption just passed the limit
			if (consumptionDifference < POSITIVE_LIMIT) {
				modeTimeStamp = time;
				PdBase.sendBang(R_POS_BANG);
			}
			if (mode != Mode.GOOD) {
				if (time - modeTimeStamp > ACTIVATION_TIME) {
					mode = Mode.GOOD;
					modeStartTimeStamp = time;
					Log.v(TAG, "Switched to GOOD mode");
					PdBase.sendBang(R_GOOD_BANG);
					PdBase.sendBang(R_STATE_BANG);
				} else {
					transitionTime = (time - modeTimeStamp) / (float) ACTIVATION_TIME;
					PdBase.sendFloat(R_TRANSITION, (float) transitionTime);
				}
			}
		} else if (difference <= NEGATIVE_LIMIT) {
			if (currentMode != Mode.BAD) {
				currentMode = Mode.BAD;
			}
			// check if the consumption just passed the limit
			if (consumptionDifference > NEGATIVE_LIMIT) {
				modeTimeStamp = time;
				PdBase.sendBang(R_NEG_BANG);
			}
			if (mode != Mode.BAD) {
				if (time - modeTimeStamp > ACTIVATION_TIME) {
					mode = Mode.BAD;
					modeStartTimeStamp = time;
					Log.v(TAG, "Switched to BAD mode");
					PdBase.sendBang(R_BAD_BANG);
					PdBase.sendBang(R_STATE_BANG);
				} else {
					transitionTime = (time - modeTimeStamp) / (float) ACTIVATION_TIME;
					PdBase.sendFloat(R_TRANSITION, (float) transitionTime);
				}
			}
		} else {
			if (currentMode != Mode.NORMAL) {
				currentMode = Mode.NORMAL;
			}
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
					modeStartTimeStamp = time;
					Log.v(TAG, "Switched to NORMAL mode");
					PdBase.sendBang(R_NORMAL_BANG);
					PdBase.sendBang(R_STATE_BANG);
				} else {
					transitionTime = (time - modeTimeStamp) / (float) DEACTIVATION_TIME;
					PdBase.sendFloat(R_TRANSITION, (float) transitionTime);
				}
			}
		}

		// calculate points
		modeOverrideTime = (time - modeStartTimeStamp) / (double) MODE_OVERRIDE_TIME;
		modeOverrideTime = modeOverrideTime > 1 ? 1 : modeOverrideTime;
		switch (mode) {
		case NORMAL:
			if (time - pointsTimeStamp > NORMAL_POINTS_INTERVAL * (1 - modeOverrideTime * 0.5)) {
				points -= 1;
				managePoints();
				Log.v(TAG, "points: " + points);
				pointsTimeStamp = time;
			}
			break;
		case GOOD:
			if (time - pointsTimeStamp > GOOD_POINTS_INTERVAL * (1 - modeOverrideTime * 0.8)) {
				points += 1;
				managePoints();
				pointsTimeStamp = time;
			}
			consumptionOverflow = (float) (difference - POSITIVE_LIMIT);
			consumptionOverflow = consumptionOverflow < 0 ? 0
					: consumptionOverflow;
			PdBase.sendFloat(R_CONSUMPTION, (float) consumptionOverflow);
			break;
		case BAD:
			if (time - pointsTimeStamp > BAD_POINTS_INTERVAL * (1 - modeOverrideTime * 0.8)) {
				points -= 1;
				managePoints();
				pointsTimeStamp = time;
			}
			consumptionOverflow = (float) (difference - NEGATIVE_LIMIT);
			consumptionOverflow = consumptionOverflow > 0 ? 0
					: consumptionOverflow;
			PdBase.sendFloat(R_CONSUMPTION, (float) consumptionOverflow);
			break;
		}

		updateView();

		consumptionDifference = difference;
	}

	public void initView(View v) {
		resources = v.getResources();
		pointsText = (TextView) v.findViewById(R.id.points_text);
		consumptionText = (TextView) v.findViewById(R.id.consumption_text);
		modeText = (TextView) v.findViewById(R.id.mode_text);
		debugText = (TextView) v.findViewById(R.id.debug_text);
		levelText = (TextView) v.findViewById(R.id.level_text);
		scoringText = (TextView) v.findViewById(R.id.scoring_text);

		viewInitialized = true;

		updateView();
	}
	
	private void managePoints() {
		if (points > 0 && points <= prevLevelPoints * LOOSE_LEVEL_LIMIT) {
			if (level > 0) {
				level -= 1;
				nextLevelPoints = prevLevelPoints;
				prevLevelPoints /= 2;
			}
		} else if (points >= nextLevelPoints) {
			level += 1;
			prevLevelPoints = nextLevelPoints;
			nextLevelPoints *= 2;
		}
	}

	private class UIThread implements Runnable {

		@Override
		public void run() {
			levelText.setText("Level: " + level);
			pointsText.setText("Points: " + points);
			consumptionText.setText("Consumption overflow: "
					+ Math.round(consumptionOverflow * 1000) / 1000.0
					+ " (consumption difference: "
					+ Math.round(consumptionDifference * 1000) / 1000.0 + ")");
			modeText.setText(mode.toString() + " mode (current state: " + currentMode.toString() + ")");
			switch(mode) {
			case NORMAL:
				modeText.setTextColor(resources.getColor(R.color.black));
				break;
			case GOOD:
				modeText.setTextColor(resources.getColor(R.color.green));
				break;
			case BAD:
				modeText.setTextColor(resources.getColor(R.color.red));
				break;
			}
			debugText.setText("Transition time: " + Math.round(1000*transitionTime) / 1000.0 + ", Mode override time: " + modeOverrideTime);
			scoringText.setText("Next level: " + nextLevelPoints + " (previous level: " + Math.round(prevLevelPoints * LOOSE_LEVEL_LIMIT) + ")");
		}

	}

	private void updateView() {
		if (viewInitialized) {
			uiHandler.post(new UIThread());
		}
	}

}
