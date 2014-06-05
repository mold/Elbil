/**
 * 
 * @author John Brynte Turesson
 * 
 */

package com.example.audiobahn;

import java.util.Random;
import java.util.Observable;

import org.puredata.core.PdBase;

import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AudioBahnActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {

	private static final String TAG = "pd";

	private Button play;
	private TextView logs;
	private SeekBar frequencySlider;
	private TextView frequencyText;
	private Button togglePatchButton;
	private TextView patchText;
	
	private AudioBahn ab;
	
	/**
	 * A dummy data object.
	 */
	private class Data {
		public float frequency = 0;
	}
	
	/**
	 * A dummy sender to test data sending.
	 */
	private class DataSender extends Observable {
		
		public void sendData(Data data) {
			setChanged();
			notifyObservers(data);
			clearChanged();
		}
	}
	
	private DataSender sender;
	
	private void init() {
		sender = new DataSender();
		Patch patch = ab.createPatch(R.raw.sine);
		sender.addObserver(patch);
		patch.addDataListener(new DataListener() {
			@Override
			public void onDataUpdate(Object object) {
				Data data = (Data) object;
				PdBase.sendFloat("frequency", data.frequency);
			}
		});
		patch.open();
	}

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ab = new AudioBahn(this);
		ab.addReadyListener(new AudioBahn.ReadyListener() {
			@Override
			public void ready() {
				Log.v(TAG, "Pd initialized");
				init();
			}
		});
		
		initGui();
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	private void initGui() {
		setContentView(R.layout.activity_audiobahn);
		frequencySlider = (SeekBar) findViewById(R.id.frequency_slider);
		frequencySlider.setOnSeekBarChangeListener(this);
		frequencyText = (TextView) findViewById(R.id.frequency_text);
		play = (Button) findViewById(R.id.play_button);
		play.setOnClickListener(this);
		togglePatchButton = (Button) findViewById(R.id.toggle_patch_button);
		togglePatchButton.setOnClickListener(this);
		patchText = (TextView) findViewById(R.id.loaded_patch_text);
		logs = (TextView) findViewById(R.id.log_box);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	private void cleanup() {
		ab.closeConnection();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_button:
			if (ab.isPlaying()) {
				ab.stopAudio();
			} else {
				ab.startAudio();
			}
			break;
		case R.id.toggle_patch_button:
			// TODO
			break;
		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		Data data = new Data();
		data.frequency = progress;
		sender.sendData(data);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}
