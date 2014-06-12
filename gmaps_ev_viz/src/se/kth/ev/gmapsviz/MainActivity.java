package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;
import com.kth.ev.differentiatedrange.CarData;
import com.kth.ev.differentiatedrange.CarDataFetcher;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends FragmentActivity implements Observer {
	CarData cd;
	CarDataFetcher cdf;
	ViewPager vp;
	MyAdapter mp;
	Audiobahn ab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Create CarData container
		cd = new CarData();
		
		//Makes this activity listen for change in the activity
		cd.addObserver(this);
		Audiobahn ab = new Audiobahn();
		ab.initSelf(cd, this);
        mp = new MyAdapter(getSupportFragmentManager(), cd, this);

        vp = (ViewPager)findViewById(R.id.pager);
        vp.setAdapter(mp);

        vp.setCurrentItem(0);
        
		String key = getString(R.string.google_browser_api_key);
		GoogleAPIQueries.setKey(key);

		
		//Starts a separate thread for fetching the data
		cdf = new CarDataFetcher(cd, false);
		new Thread(cdf).start();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		}
	}
	

    public static class MyAdapter extends FragmentPagerAdapter {
    	CarData cd;
    	Context c;
        public MyAdapter(FragmentManager fm, CarData cd, Context c) {
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
        		return new MyMap();
        	case 1:
        		Audiobahn ab = new Audiobahn();
        		ab.initSelf(cd, c);
        		return ab;
        	case 2:
        	default:
        		return new TextFragment();
        	}
        	
        }
    }

    /** Fragment for loading our google map */
    	
    /** A simple fragment that displays a TextView. */
    public static class TextFragment extends Fragment {
      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_text, container, false);
      }
    }
    



}
