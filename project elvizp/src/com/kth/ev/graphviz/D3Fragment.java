package com.kth.ev.graphviz;

import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;

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

/**
 * Fragment that contains a WebView for rendering graphs using the d3.js
 * framework.
 * 
 * @author marothon
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class D3Fragment extends Fragment implements Observer {
	private static final String TAG = "D3Fragment";
	private RouteDataFetcher rdf;
	private Thread t_rdf;
	private CarData cd;

	WebView browser;

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
				rdf = new RouteDataFetcher();
				rdf.addObserver(this);
				t_rdf = new Thread(rdf);
				t_rdf.start();
			}else{
				Log.e(TAG, "Cannot start API call without internet access.");
			}
		}

		browser = new WebView(getActivity());

		browser.setVerticalScrollBarEnabled(false);
		browser.setHorizontalScrollBarEnabled(false);
		((ViewGroup) getView()).addView(browser);

		if (getActivity() instanceof ElvizpActivity) {
			Log.d(TAG, "Adding javascript interface");
			browser.addJavascriptInterface(cd, "CarData");
		}

		browser.setWebChromeClient(new WebChromeClient());
		browser.getSettings().setJavaScriptEnabled(true);
		browser.loadUrl("file:///android_asset/linechart.html");

	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof RouteDataFetcher) {
			// Log.d(TAG, "GOT SOME NEW DATA!");
			rdf = (RouteDataFetcher) observable;
			addEstimation(cd, (RouteDataFetcher) observable);
			setRoute(rdf.raw);
		}
	}

	/**
	 * Loads a json encoded step file (fetched from google api) into
	 * the visualization. Interpreted as the current route.
	 * 
	 * @param json
	 */
	private void setRoute(String json) {
		runBrowserCommand("javascript:setRoute(" + json + ")");
	}

	/**
	 * Adds an energy estimation to the visualization based on the 
	 * route data and car data.
	 * 
	 * @param cd2 CarData object.
	 * @param observable Thread which fetched the route data.
	 */
	private void addEstimation(CarData cd2, RouteDataFetcher observable) {
		if (rdf.data.size() < 1)
			return;

		int factors = 0;
		factors |= CarData.SLOPE | CarData.TIME | CarData.SPEED;
		final String consumption = cd.consumptionOnRouteJSON(rdf.data, factors);
		runBrowserCommand("javascript:updateSeries(\"Estimated consumption\","
						+ consumption + ")");
	}
	
	private void runBrowserCommand(final String c){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				browser.loadUrl(c);
			}
		});
	}

}
