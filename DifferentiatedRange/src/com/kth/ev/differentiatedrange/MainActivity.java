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

public class MainActivity extends Activity implements OnClickListener, Runnable {
	
	final int THREAD_SLEEP = 50;
	final int DATA_SLEEP = 500;
	
	DiffRangeSurfaceView v;
	GetData gd;
	PureDataHandler pdHandler;
	CarData carData;
	
	// View
	Button soundToggle;
	TextView dataText;
	
	// PureData
	Patch test;
	String[] patches = {"sine.pd", "test.pd"};
	Patch[] loadedPatches;
	
	Runnable viewUpdater = new Runnable() {
		@Override
		public void run() {
			dataText.setText("soc: " + cd.getSoc(true) + "\nspeed: "+ cd.getSpeed(true) + "\nfan: " + cd.getFan(true) + "\nclimate: " + cd.getClimate(true));
		}
	};
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cd = new CarData(false, DATA_SLEEP);
        
        pd = new PureDataHandler(this, cd);
        pd.addReadyListener(new PureDataHandler.ReadyListener() {
			@Override
			public void ready() {
				initPd();
			}
		});
        
        initView();
        
        Thread t = new Thread(this);
        t.start();
    }

    private void initView() {
    	setContentView(R.layout.activity_main);
    	soundToggle = (Button) findViewById(R.id.toggle_sound);
    	soundToggle.setOnClickListener(this);
    	dataText = (TextView) findViewById(R.id.data_text);
    	// init the patch list
    	TextView item;
    	LinearLayout list = (LinearLayout) findViewById(R.id.patch_list);
    	for(int i = 0; i < patches.length; i++) {
    		item = new TextView(this);
    		item.setText(patches[i]);
    		item.setTag(patches[i]);
    		item.setOnClickListener(this);
    		// TODO: add this to a style
    		item.setBackgroundColor(getResources().getColor(R.color.white));
    		item.setTextColor(getResources().getColor(R.color.black));
    		item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    		item.setPadding(10, 10, 10, 10);
    		list.addView(item);
    	}
    }
    
    private void initPd() {
    	loadedPatches = new Patch[patches.length];
    	for(int i = 0; i < patches.length; i++) {
    		loadedPatches[i] = new Patch(patches[i]);
    	}
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
			TextView text = (TextView) v;
	        String info = (String) v.getTag();
	        int i = 0;
	        if (info != null) {
		        while(i < patches.length) {
					if (info.equals(patches[i])) {
						if (loadedPatches[i].isOpen()) {
							text.setText(patches[i]);
							loadedPatches[i].close();
						} else {
							text.setText("[open] " + patches[i]);
							loadedPatches[i].open();
						}
						break;
					}
					i++;
				}
	        }
	    }
		
		switch(v.getId()) {
		case R.id.toggle_sound:
			if (pd.isPlaying()) {
				pd.stopAudio();
				soundToggle.setText(R.string.start_sound);
			} else {
				pd.startAudio();
				soundToggle.setText(R.string.stop_sound);
			}
			break;
		}
	}
	
	@Override
	public void run() {
		while(true) {
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(viewUpdater);
			
			try {
				Thread.sleep(THREAD_SLEEP);
			} catch (InterruptedException e) {
				
			}
		}
	}
    
}
