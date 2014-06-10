package com.kth.ev.differentiatedrange;

import com.kth.ev.differentiatedrange.puredata.Patch;
import com.kth.ev.differentiatedrange.puredata.PureDataHandler;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements Runnable {
	
	final int THREAD_SLEEP = 500;
	
	DiffRangeSurfaceView v;
	GetData gd;
	PureDataHandler pd;
	CarDataFetcher cdfetch;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        v = new DiffRangeSurfaceView(this);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(v);
        v.resume();
        
        gd = new GetData(v);
        gd.resume();
        cdfetch = new CarDataFetcher(false);
        
        pd = new PureDataHandler(this);
        pd.addReadyListener(new PureDataHandler.ReadyListener() {
			@Override
			public void ready() {
				initPd();
			}
		});
        
        Thread t = new Thread(this);
        t.start();
        
        //String s = GetData.connect("http://localhost:8080/soc");
        //Log.d("TAG", "MSG");
        //float s = Float.parseFloat(GetData.connect("http://localhost:8080/soc"));
//		GetDataMethod test = new GetDataMethod();
//		String soc, speed;
//		try {
//			soc = test.getInternetData("http://localhost:8080/soc").trim();
//			Log.i("SOC", soc);
//			if( soc != "null")
//			{
//				v.soc = Float.parseFloat(soc)/100;
//			}
//
//			speed = test.getInternetData("http://localhost:8080/speed").trim();
//			Log.i("SPEED", speed);
//			if( speed != "null")
//			{
//				v.speed = Integer.parseInt(speed);
//			}
//		  
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    private void initPd() {
    	// TODO: make some music
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void run() {
		while(true) {
			cdfetch.fetchData();
			
			try {
				Thread.sleep(THREAD_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
