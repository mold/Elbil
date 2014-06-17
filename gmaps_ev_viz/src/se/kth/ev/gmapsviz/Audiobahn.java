package se.kth.ev.gmapsviz;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Audiobahn extends Fragment implements OnClickListener {
	private static PureDataHandler pdHandler;
	private static CarData carData;
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
		if (carData == null)
			if (getActivity() instanceof MainActivity)
				carData = ((MainActivity) getActivity()).cd;
	}

	@Override
	public void onResume() {
		super.onResume();
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
		soundToggle = (Button) getView().findViewById(R.id.toggle_sound);
		soundToggle.setOnClickListener(this);
		dataText = (TextView) getView().findViewById(R.id.data_text);
		// init the patch list
		TextView item;
		LinearLayout list = (LinearLayout) getView().findViewById(
				R.id.patch_list);
		for (int i = 0; i < loadedPatches.length; i++) {
			item = new TextView(getActivity());
			item.setTag(i);
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
			TextView text = (TextView) v;
			if (v.getTag() != null) {
				int i = (Integer) v.getTag();
				if (loadedPatches[i].isOpen()) {
					text.setText(loadedPatches[i].getFileName());
					text.setBackgroundColor(getResources().getColor(
							R.color.white));
					loadedPatches[i].close();
				} else {
					text.setText("[open] " + loadedPatches[i].getFileName());
					text.setBackgroundColor(getResources().getColor(
							R.color.green));
					loadedPatches[i].open();
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
