package com.kth.ev.graphviz;

import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;

import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.CarDataFetcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main activity for the electric car visualizations.
 * 
 * @author marothon
 *
 */
public class ElvizpActivity extends FragmentActivity implements Observer {
	protected static final String TAG = "ElvizpActivity";
	public CarData cd;
	CarDataFetcher cdf;
	RouteDataFetcher ee;
	ViewPager vp;
	ElvizpAdapter mp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Create CarData container
		cd = new CarData();
		
		//Makes this activity listen for change in the activity
		cd.addObserver(this);
        mp = new ElvizpAdapter(getSupportFragmentManager(), cd, this);

        //Setup the swipe interaction.
        vp = (ViewPager)findViewById(R.id.pager);
        vp.setAdapter(mp);

        vp.setCurrentItem(0);
        
		String key = getString(R.string.google_browser_api_key);
		GoogleAPIQueries.setKey(key);

		//Starts a separate thread for fetching the data
		cdf = new CarDataFetcher(cd, false);
		new Thread(cdf).start();
		
	}

	/**
	 * Simply prints the current car battery level every time
	 * there is a change in the CarData object.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof CarData){
			CarData cd = (CarData) observable;
			String battery = String.valueOf(cd.getSoc(true));
			Log.d("Energy", battery);
		}else if(observable instanceof RouteDataFetcher){
			/*
			EnergyEstimator ee = (EnergyEstimator) observable;
			double[] consumption = cd.determineConsumption(ee.data);
			for(int i=0; i<consumption.length; i++)
			Log.d("consump", consumption[i]+"");
			*/
		}
	}
	
	/**
	 * Subclass to handle the different visualization/sonification fragments.
	 * 
	 * @author marothon
	 *
	 */
    public static class ElvizpAdapter extends FragmentPagerAdapter {
    	CarData cd;
    	Context c;
        public ElvizpAdapter(FragmentManager fm, CarData cd, Context c) {
            super(fm);
            this.cd = cd;
            this.c = c;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
        	switch(position){
        	case 0:
        		VizFragment e = new VizFragment();
        		//EVVizFragment e = new EVVizFragment();
        		//cd.addObserver(e);
        		return e;
        	case 1:
        		return new RoutePickFragment();
        	default:
        		AudiobahnFragment ab = new AudiobahnFragment();
        		return ab;
        	}
        }
        
    }
    /**
     * 
     * A template fragment used in default cases of the Adapter. 
     * 
     * @author marothon
     *
     */
    public static class TextFragment extends Fragment {
      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_text, container, false);
      }
    }
    
    /**
     * Adds the given fragment as an observer to the given observable object.
     * 
     * @param frag_index Fragments index in the ViewPager.
     */
    public void relayObservable(Observable o, int frag_id){
    	if(getFragmentByPosition(frag_id) instanceof Observer)
    		o.addObserver((Observer) getFragmentByPosition(frag_id));
    }
    
    public Fragment getFragmentByPosition(int pos) {
            String tag = "android:switcher:" + vp.getId() + ":" + pos;
            return getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * Checks if the application has internet access. Returns true if so.
     * 
     * @return True if internet is available.
     */
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


}
