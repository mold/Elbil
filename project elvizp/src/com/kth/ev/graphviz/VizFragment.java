package com.kth.ev.graphviz;

import java.util.Observable;
import java.util.Observer;
import se.kth.ev.gmapsviz.R;

import com.google.gson.Gson;
import com.kth.ev.differentiatedrange.CarData;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Fragment that contains a WebView for rendering graphs using the d3.js
 * framework.
 * 
 * @author marothon
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class VizFragment extends Fragment implements Observer {
	private static final String TAG = "VizFragment";
	private Thread t_rdf;
	private CarData cd;
	private WebView browser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		View v = inflater.inflate(R.layout.fragment_d3, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (cd == null)
			if (getActivity() instanceof ElvizpActivity) {
				cd = ((ElvizpActivity) getActivity()).cd;
				cd.addObserver(this);
			}
		if (t_rdf == null) {
			if (((ElvizpActivity) getActivity()).isNetworkAvailable()) {
				Log.d("ElvizFragment", "GET DATA");
				RouteDataFetcher rdf = new RouteDataFetcher();
				rdf.addObserver(this);
				t_rdf = new Thread(rdf);
				t_rdf.start();
			} else {
				postToast("This application requires internet."); 
				Log.e(TAG, "Cannot start API call without internet access.");
			}
		}
		if (browser == null) {
			browser = new WebView(getActivity());

			browser.setVerticalScrollBarEnabled(false);
			browser.setHorizontalScrollBarEnabled(false);

			if (getActivity() instanceof ElvizpActivity) {
				Log.d(TAG, "Adding javascript interface");
				browser.addJavascriptInterface(cd, "CarData");
			}

			browser.setWebChromeClient(new WebChromeClient());
			browser.getSettings().setJavaScriptEnabled(true);
			browser.loadUrl("file:///android_asset/viz.html");
		}
		((ViewGroup) getView()).addView(browser);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		((ViewGroup) getView()).removeView(browser);
	};

	/**
	 * Listens for RouteDataFetchers and CarData objects.
	 * 
	 * @param data optional data from the observable.
	 * @param observable The observable object.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof RouteDataFetcher) {
			RouteDataFetcher rdf = (RouteDataFetcher) observable;
			if (rdf.data == null) {
				postToast("Unsuccessful data fetch.");
			}
			setRoute(rdf.json_processed);
			addEstimation(cd, (RouteDataFetcher) observable);
		}
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;
			updateProgress(cd);
		}
	}

	/**
	 * Updates visualisation with current CarData
	 */
	private void updateProgress(CarData cd) {
		runBrowserCommand("javascript:updateData(" + cd.toJson(true) + ")");
	}

	/**
	 * Loads a json encoded step file (fetched from google api) into the
	 * visualization. Interpreted as the current route.
	 * 
	 * @param json
	 *            JSON string with data.
	 */
	private void setRoute(String json) {
		if (!isValidJSON(json)) {
			throw new IllegalArgumentException("Not a valid JSON string.");
		} else {
			runBrowserCommand("javascript:setRoute(" + json + ")");
		}
	}

	/**
	 * Adds an energy estimation to the visualization based on the route data
	 * and car data.
	 * 
	 * @param cd2
	 *            CarData object.
	 * @param observable
	 *            Thread which fetched the route data.
	 */
	private void addEstimation(CarData cd2, RouteDataFetcher observable) {
		RouteDataFetcher rdf = (RouteDataFetcher) observable;
		if (rdf.data == null || rdf.data.size() < 1 || browser == null)
			return;
 
		int factors = 0;
		factors |= CarData.SLOPE | CarData.TIME | CarData.SPEED;
		final String consumption = cd.consumptionOnRouteJSON(rdf.data, factors);
		runBrowserCommand("javascript:updateSeries(\"Estimated consumption\","
				+ consumption + ")");
	}

	/**
	 * Runs a browser command in the fragments WebView.
	 * 
	 * @param c
	 *            The browser command.
	 */
	private void runBrowserCommand(final String c) {
		if (browser == null || getActivity() == null)
			return;

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				browser.loadUrl(c);
			}
		});
	}

	private static final Gson gson = new Gson();

	/**
	 * Verifies that a given String is in JSON formatting.
	 * 
	 * @param JSON_STRING
	 *            String to check.
	 * @return True if valid.
	 */
	private boolean isValidJSON(String JSON_STRING) {
		try {
			gson.fromJson(JSON_STRING, Object.class);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}
	
	/**
	 * Posts a toast message on the main UI thread.
	 * 
	 * @param cs Message to toast.
	 */
	private void postToast(final CharSequence cs){
		getActivity().runOnUiThread(new Runnable(){
		@Override
		public void run() {
			Toast toast = Toast.makeText(getActivity()
					.getApplicationContext(), cs, Toast.LENGTH_SHORT);
			toast.show();  

		}});
	}

}
