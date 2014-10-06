package com.kth.ev.application;

import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import com.kth.ev.application.R;

import com.kth.ev.audiobahn.gamification.PdDataController;
import com.kth.ev.audiobahn.puredata.Patch;
import com.kth.ev.audiobahn.puredata.PureDataHandler;
import com.kth.ev.cardata.CarData;
import com.kth.ev.routedata.RouteDataFetcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Fragment responsible for audio sonification.
 * 
 * @author johntu, dmol
 * 
 */
public class AudiobahnFragment extends Fragment implements OnClickListener,
		Observer, OnSeekBarChangeListener {
	// private static final String TAG = "AudiobahnFragment";
	private static PureDataHandler pdHandler;
	private static CarData carData;

	PdDataController pdDataController;

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
		super.onActivityCreated(savedInstanceState);
		// Log.v(TAG, "onCreate");
		pdDataController = new PdDataController(getActivity());
		if (getActivity() instanceof ElvizpActivity) {
			carData = ((ElvizpActivity) getActivity()).cd;
			carData.addObserver(pdDataController);
			pdHandler = new PureDataHandler(getActivity(), carData);
			pdHandler.addReadyListener(new PureDataHandler.ReadyListener() {
				@Override
				public void ready() {
					Log.v("audiobahn", "pd initialized");
					if (audiobahnView != null) {
						initView();
					}
				}
			});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		// Log.v(TAG, "onCreateView");
		audiobahnView = inflater.inflate(R.layout.fragment_audiobahn,
				container, false);
		return audiobahnView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (pdHandler.ready()) {
			// Log.v(TAG, "initView (onActivityCreated)");
			initView();
		}
	}

	private void initView() {
		LinearLayout container = (LinearLayout) getView().findViewById(
				R.id.container);
		View v = getView();
		startGame = (Button) v.findViewById(R.id.start_game);
		startGame.setOnClickListener(this);
		soundToggle = (Button) v.findViewById(R.id.toggle_sound);
		soundToggle.setOnClickListener(this);
		buttonReload = (Button) v.findViewById(R.id.reload_folder);
		buttonReload.setOnClickListener(this);
		seekbar1 = (SeekBar) v.findViewById(R.id.seekBar1);
		seekbar1.setOnSeekBarChangeListener(this);
		seekbar2 = (SeekBar) v.findViewById(R.id.seekBar2);
		seekbar2.setOnSeekBarChangeListener(this);
		seekbar1Text = (TextView) v.findViewById(R.id.seekBar1Text);
		seekbar2Text = (TextView) v.findViewById(R.id.seekBar2Text);

		// load the patches and init the patch list view
		loadPatches();

		// add some data graphs
		container.addView(pdDataController.getSpeedGraph());
		container.addView(pdDataController.getAccelerationGraph());
		container.addView(pdDataController.getAmpGraph());
		container.addView(pdDataController.getConsumptionGraph());

		// DataGraph graph;
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.SPEED);
		// container.addView(graph);
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.AMP);
		// container.addView(graph);
		// graph = new DataGraph(this, carData, DataGraph.DATA.SOC);
		// container.addView(graph);
		
		updateSliderValues();
		
		pdDataController.fineDriver.initView(v);
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
		LinearLayout list = (LinearLayout) getView().findViewById(
				R.id.patch_list);
		list.removeAllViews();
		for (int i = 0; i < loadedPatches.length; i++) {
			item = new TextView(getActivity());
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
				LinearLayout list = (LinearLayout) getView().findViewById(
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
				}
			}
		}

		switch (v.getId()) {
		case R.id.start_game:
			if (pdDataController.isRunning()) {
				startGame.setText(R.string.start_game);
				startGame.setTextColor(getResources().getColor(R.color.black));
				pdDataController.stop();
			} else {
				startGame.setText(R.string.stop_game);
				startGame.setTextColor(getResources().getColor(R.color.red));
				pdDataController.start();
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
			pdDataController.setRouteData((RouteDataFetcher) observable);
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
