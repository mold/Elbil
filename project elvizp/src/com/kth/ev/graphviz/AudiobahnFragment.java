package com.kth.ev.graphviz;

import se.kth.ev.gmapsviz.R;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.gamification.AudioGame;
import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fragment responsible for audio sonification.
 * 
 * @author johntu, dmol
 * 
 */
public class AudiobahnFragment extends Fragment implements OnClickListener {
	private static PureDataHandler pdHandler;
	private static CarData carData;

	AudioGame game;

	// View
	View audiobahnView;
	Button soundToggle;
	Button buttonReload;
	TextView dataText;

	// PureData
	Patch test;
	Patch[] loadedPatches;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v("audiobahn", "onCreate");
		game = new AudioGame(getActivity());
		if (getActivity() instanceof ElvizpActivity) {
			carData = ((ElvizpActivity) getActivity()).cd;
			carData.addObserver(game);
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
		Log.v("audiobahn", "onCreateView");
		audiobahnView = inflater.inflate(R.layout.fragment_audiobahn,
				container, false);
		return audiobahnView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (pdHandler.ready()) {
			Log.v("audiobahn", "initView (onActivityCreated)");
			initView();
		}
	}

	private void initView() {
		LinearLayout container = (LinearLayout) getView().findViewById(
				R.id.container);
		soundToggle = (Button) getView().findViewById(R.id.toggle_sound);
		soundToggle.setOnClickListener(this);
		buttonReload = (Button) getView().findViewById(R.id.reload_folder);
		buttonReload.setOnClickListener(this);
		
		// load the patches and init the patch list view
		loadPatches();
		
		// add some data graphs
		container.addView(game.getSpeedGraph());
		container.addView(game.getAmpGraph());
		container.addView(game.getAmpStateGraph());

		// DataGraph graph;
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.SPEED);
		// container.addView(graph);
		// graph = new DataGraph(getActivity(), carData, DataGraph.DATA.AMP);
		// container.addView(graph);
		// graph = new DataGraph(this, carData, DataGraph.DATA.SOC);
		// container.addView(graph);
	}
	
	/**
	 * Load the patches in the 'puredata' directory.
	 * This method also updates the list view.
	 */
	private void loadPatches() {
		// TODO: check if the directory exists
		// load patches from the local puredata directory
		loadedPatches = pdHandler.loadPatchesFromDirectory(Environment.getExternalStorageDirectory() + "/puredata/");
		
		// update the list view
		TextView item;
		LinearLayout list = (LinearLayout) getView().findViewById(R.id.patch_list);
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
}
