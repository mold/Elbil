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
	Button soundToggle;
	TextView dataText;

	// PureData
	Patch test;
	Patch[] loadedPatches;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		return inflater.inflate(R.layout.fragment_audiobahn, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (carData == null) {
			if (getActivity() instanceof ElvizpActivity) {
				carData = ((ElvizpActivity) getActivity()).cd;
			}
		}
		if (pdHandler == null) {
			pdHandler = new PureDataHandler(getActivity(), carData);
			pdHandler.addReadyListener(new PureDataHandler.ReadyListener() {
				@Override
				public void ready() {
					init();
				}
			});
		} else {
			init();
		}
	}

	private void initView() {
		LinearLayout container = (LinearLayout) getView().findViewById(
				R.id.container);
		soundToggle = (Button) getView().findViewById(R.id.toggle_sound);
		soundToggle.setOnClickListener(this);
		// init the patch list
		TextView item;
		LinearLayout list = (LinearLayout) getView().findViewById(
				R.id.patch_list);
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
		// add some data graphs
		game = new AudioGame(getActivity());
		game.addToView(container);
		carData.addObserver(game);

		DataGraph graph;
		graph = new DataGraph(getActivity(), carData, DataGraph.DATA.SPEED);
		container.addView(graph);
		graph = new DataGraph(getActivity(), carData, DataGraph.DATA.AMP);
		container.addView(graph);
		// graph = new DataGraph(this, carData, DataGraph.DATA.SOC);
		// container.addView(graph);
	}

	private void init() {
		loadedPatches = pdHandler.loadPatchesFromDirectory(Environment
				.getExternalStorageDirectory() + "/puredata/");
		Log.v("puredata", "patches: " + loadedPatches.length);
		initView();
	}

	@Override
	public void onClick(View v) {
		// check for patch list clicks
		if (v instanceof TextView) {
			Object tag = v.getTag();
			if (tag != null && tag instanceof Patch) {
				TextView text = (TextView) v;
				Patch patch = (Patch) tag;
				if (patch.isOpen()) {
					text.setText(patch.getFileName());
					text.setBackgroundColor(getResources().getColor(
							R.color.white));
					patch.close();
				} else {
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
		}
	}
}
