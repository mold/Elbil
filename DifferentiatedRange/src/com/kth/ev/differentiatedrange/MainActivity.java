package com.kth.ev.differentiatedrange;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.Audio;
import android.app.Activity;
import android.content.Context;
import android.graphics.LinearGradient;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	final int DATA_SLEEP = 100; // update data 10 times per second
	
	DiffRangeSurfaceView v;
	GetData gd;
	PureDataHandler pdHandler;
	CarData carData;
	
	// View
	Button soundToggle;
	
	// PureData
	Patch test;
	Patch[] loadedPatches;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        carData = new CarData(false, DATA_SLEEP);
        
        pdHandler = new PureDataHandler(this, carData);
        pdHandler.addReadyListener(new PureDataHandler.ReadyListener() {
			@Override
			public void ready() {
				init();
			}
		});
    }

    private void initView() {
    	setContentView(R.layout.activity_main);
    	LinearLayout container = (LinearLayout) findViewById(R.id.container);
    	soundToggle = (Button) findViewById(R.id.toggle_sound);
    	soundToggle.setOnClickListener(this);
    	// init the patch list
    	TextView item;
    	LinearLayout list = (LinearLayout) findViewById(R.id.patch_list);
    	for(int i = 0; i < loadedPatches.length; i++) {
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
    	// add some data graphs
    	DataGraph graph;
    	graph = new DataGraph(this, carData, DataGraph.DATA.SPEED);
    	container.addView(graph);
    	graph = new DataGraph(this, carData, DataGraph.DATA.AMP);
    	container.addView(graph);
    	graph = new DataGraph(this, carData, DataGraph.DATA.SOC);
    	container.addView(graph);
    }
    
    private void init() {
    	loadedPatches = pdHandler.loadPatchesFromDirectory(Environment.getExternalStorageDirectory() + "/puredata/");
    	Log.v("puredata", "patches: " + loadedPatches.length);

    	initView();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		// check for patch list clicks
		if(v instanceof TextView) {
			Object tag = v.getTag();
			if (tag != null && tag instanceof Patch) {
				TextView text = (TextView) v;
		        Patch patch = (Patch) tag;
				if (patch.isOpen()) {
					text.setText(patch.getFileName());
					text.setBackgroundColor(getResources().getColor(R.color.white));
					patch.close();
				} else {
					text.setText("[open] " + patch.getFileName());
					text.setBackgroundColor(getResources().getColor(R.color.green));
					patch.open();
				}
			}
	    }
		
		switch(v.getId()) {
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
