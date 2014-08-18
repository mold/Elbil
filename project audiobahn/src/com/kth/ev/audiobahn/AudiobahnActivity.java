package com.kth.ev.audiobahn;

import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import se.kth.ev.gmapsviz.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.CarDataFetcher;
import com.kth.ev.differentiatedrange.gamification.AudioGame;
import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;
import com.kth.ev.graphviz.RouteDataFetcher;

/**
 * Fragment responsible for audio sonification.
 * 
 * @author johntu, dmol
 * 
 */
public class AudiobahnActivity extends Activity implements OnClickListener,
		Observer, OnSeekBarChangeListener {
	// private static final String TAG = "AudiobahnFragment";
	private static PureDataHandler pdHandler;
	private static CarDataFetcher cdFetcher;
	private static CarData carData;

	AudioGame audioGame;

	// View
	View audiobahnView;
	Button startGame;
	Button soundToggle;
	Button buttonReload;
	TextView dataText;
	SeekBar seekbar1;
	SeekBar seekbar2;
	TextView seekbar1Text;
	TextView seekbar2Text;
	float lastSeekbar1;
	float lastSeekbar2;
	// PureData
	Patch test;
	Patch[] loadedPatches;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fragment_audiobahn);
		
		// Log.v(TAG, "onCreate");
		audioGame = new AudioGame(this);
		
		carData = new CarData();
		carData.addObserver(audioGame);
		pdHandler = new PureDataHandler(this, carData);
		pdHandler.addReadyListener(new PureDataHandler.ReadyListener() {
			@Override
			public void ready() {
				Log.v("audiobahn", "pd initialized");
				initView();
			}
		});
		
		cdFetcher = new CarDataFetcher(carData, false);
		new Thread(cdFetcher).start();
	}

	private void initView() {
		LinearLayout container = (LinearLayout) findViewById(
				R.id.container);
		startGame = (Button) findViewById(R.id.start_game);
		startGame.setOnClickListener(this);
		soundToggle = (Button) findViewById(R.id.toggle_sound);
		soundToggle.setOnClickListener(this);
		buttonReload = (Button) findViewById(R.id.reload_folder);
		buttonReload.setOnClickListener(this);
		seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		seekbar1.setOnSeekBarChangeListener(this);
		seekbar2 = (SeekBar) findViewById(R.id.seekBar2);
		seekbar2.setOnSeekBarChangeListener(this);
		seekbar1Text = (TextView) findViewById(R.id.seekBar1Text);
		seekbar2Text = (TextView) findViewById(R.id.seekBar2Text);

		// load the patches and init the patch list view
		loadPatches();

		// add some data graphs
		container.addView(audioGame.getSpeedGraph());
		container.addView(audioGame.getAccelerationGraph());
		container.addView(audioGame.getAmpGraph());
		container.addView(audioGame.getAmpSpeedGraph());
		container.addView(audioGame.getAmpAccelerationGraph());
		container.addView(audioGame.getAmpStateGraph());

		// DataGraph graph;
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.SPEED);
		// container.addView(graph);
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.AMP);
		// container.addView(graph);
		// graph = new DataGraph(this, carData, DataGraph.DATA.SOC);
		// container.addView(graph);
		
		updateSliderValues();
	}

	/**
	 * Load the patches in the 'puredata' directory. This method also updates
	 * the list view.
	 */
	private void loadPatches() {
		// TODO: check if the directory exists
		// load patches from the local puredata directory
		if (loadedPatches != null) {
			for (int i = 0; i < loadedPatches.length; i++) {
				loadedPatches[i].close();
			}
		}
		loadedPatches = pdHandler.loadPatches();

		// update the list view
		TextView item;
		LinearLayout list = (LinearLayout) findViewById(
				R.id.patch_list);
		list.removeAllViews();
		for (int i = 0; i < loadedPatches.length; i++) {
			item = new TextView(this);
			item.setTag(loadedPatches[i]);
			item.setText(loadedPatches[i].getFileName());
			item.setOnClickListener(this);
			// TODO: add this to a style
			item.setBackgroundColor(getResources().getColor(R.color.white));
			item.setTextColor(getResources().getColor(R.color.black));
			item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			item.setPadding(10, 10, 10, 10);
			list.addView(item);
		}
	}
	
	private void updateSliderValues() {
		seekbar1Text.setText("slider1: " + lastSeekbar1);
		PdBase.sendFloat("slider1", lastSeekbar1);
		seekbar2Text.setText("slider2: " + lastSeekbar2);
		PdBase.sendFloat("slider2", lastSeekbar2);
	}

	@Override
	public void onClick(View v) {
		// check for patch list clicks
		if (v instanceof TextView) {
			Object tag = v.getTag();
			if (tag != null && tag instanceof Patch) {
				LinearLayout list = (LinearLayout) findViewById(
						R.id.patch_list);
				Patch patch = (Patch) tag;
				boolean open = patch.isOpen();
				TextView text;
				// close all patches
				for (int i = 0; i < loadedPatches.length; i++) {
					patch = loadedPatches[i];
					if (patch.isOpen()) {
						text = (TextView) list.getChildAt(i);
						text.setText(patch.getFileName());
						text.setBackgroundColor(getResources().getColor(
								R.color.white));
						patch.close();
					}
				}
				// open the patch if it was closed
				if (!open) {
					text = (TextView) v;
					patch = (Patch) tag;
					text.setText("[open] " + patch.getFileName());
					text.setBackgroundColor(getResources().getColor(
							R.color.green));
					patch.open();
					
					// send the current values of the seek bars
					updateSliderValues();
				}
			}
		}

		switch (v.getId()) {
		case R.id.start_game:
			if (audioGame.isRunning()) {
				startGame.setText(R.string.start_game);
				startGame.setTextColor(getResources().getColor(R.color.black));
				audioGame.stop();
			} else {
				startGame.setText(R.string.stop_game);
				startGame.setTextColor(getResources().getColor(R.color.red));
				audioGame.start();
			}
			break;
		case R.id.toggle_sound:
			if (pdHandler.isPlaying()) {
				pdHandler.stopAudio();
				soundToggle.setText(R.string.start_sound);
			} else {
				pdHandler.startAudio();
				soundToggle.setText(R.string.stop_sound);
			}
			break;
		case R.id.reload_folder:
			loadPatches();
			break;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof RouteDataFetcher) {
			//audioGame.setRouteData((RouteDataFetcher) observable);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar == seekbar1) {
			lastSeekbar1 = progress / 1000.0f;
		} else if (seekBar == seekbar2) {
			lastSeekbar2 = progress / 1000.0f;
		}
		updateSliderValues();
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
