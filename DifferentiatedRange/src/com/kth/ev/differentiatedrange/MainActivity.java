package com.kth.ev.differentiatedrange;

import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;

import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, Observer {
	
	final int THREAD_SLEEP = 1000;
	
	DiffRangeSurfaceView v;
	GetData gd;
	PureDataHandler pd;
	CarData cd;
	
	// View
	TextView dataText = null;
	
	// PureData
	Patch test;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        cd = new CarData(false, THREAD_SLEEP);
        
        pd = new PureDataHandler(this);
        pd.addReadyListener(new PureDataHandler.ReadyListener() {
			@Override
			public void ready() {
				initPd();
			}
		});
        
        initView();
        
        //cd.addObserver(this);
    }

    private void initView() {
    	setContentView(R.layout.activity_main);
    	Button reloadPatch = (Button) findViewById(R.id.reload_patch);
    	reloadPatch.setOnClickListener(this);
    	dataText = (TextView) findViewById(R.id.data_text);
    }
    
    private void initPd() {
    	// Observe data
    	cd.addObserver(pd);
    	// Open a test patch
    	test = new Patch("test.pd");
    	test.open();
    	// Start playing
    	pd.startAudio();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.reload_patch:
			test.close();
			test.open();
			break;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;
			if (dataText != null)
				dataText.setText("soc: " + cd.getSoc(false) + "\nspeed: "+ cd.getSpeed(false) + "\nfan: " + cd.getFan(false) + "\nclimate: " + cd.getClimate(false));
		}
	}
    
}
