package com.kth.ev.application;

import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;
import com.kth.ev.cardata.CarData;
import com.kth.ev.cardata.CarDataFetcher;
import com.kth.ev.routedata.GPSHolder;
import com.kth.ev.routedata.GoogleAPIQueries;
import com.kth.ev.routedata.RoutePickFragment;
import com.kth.ev.vizsample.RouteVizFragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main activity for the electric car visualizations.
 * 
 * @author marothon
 *  
 */             
public class ElvizpActivity extends FragmentActivity{
	protected static final String TAG = "ElvizpActivity";
	public CarData cd;
	CarDataFetcher cdf;
	ViewPager vp;
	ElvizpAdapter mp;
	GPSHolder gps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Create CarData container
		cd = new CarData();
		
		//Makes this activity listen for change in the activity
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
		
		//Starts listening for GPS location.
		gps = new GPSHolder(this);
		gps.start();
		
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
        		RouteVizFragment e = new RouteVizFragment();
        		cd.addObserver(e);
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
    
    /**
     * Adds all fragments (which are observers) as an observer
     * to the given observable
     */ 
    public void relayObservable(Observable o){
    	for(Fragment f :getSupportFragmentManager().getFragments()){
    		if(f instanceof Observer){
    			o.addObserver((Observer) f);
    		}
    	}
    }
    
    /**
     * Finds fragment by the relative position in the swipe layout.
     * 
     * @param pos The id of the fragment.
     * @return The requested fragment.
     */
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

	public GPSHolder gps() {
		return gps;
	}
	
}
