package com.kth.ev.graphviz;

import java.util.Observable;
import java.util.Observer;

import se.kth.ev.gmapsviz.R;

import com.google.gson.Gson;
import com.kth.ev.differentiatedrange.CarData;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
	private CarData cd;
	private WebView browser;
	private RouteProgress rp;
	private TestRouteProgress trp;

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

			// Disable scrolling, but also disables javascript events!
			browser.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return (event.getAction() == MotionEvent.ACTION_MOVE);
				}
			});

			if (Build.VERSION.SDK_INT >= 11) {
				browser.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}

			runBrowserCommand("file:///android_asset/viz.html");
		}
		if (cd == null)
			if (getActivity() instanceof ElvizpActivity) {
				cd = ((ElvizpActivity) getActivity()).cd;
				cd.addObserver(this);
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
	 * @param data
	 *            optional data from the observable.
	 * @param observable
	 *            The observable object.
	 */
	@Override
	public void update(Observable observable, Object data) {
		synchronized (this) {
			if (observable instanceof RouteDataFetcher) {
				RouteDataFetcher rdf = (RouteDataFetcher) observable;
				if (rdf.getCombinedRoute() == null) {
					postToast("Unsuccessful data fetch.");
				}
				// runBrowserCommand("file:///sdcard/elvizp/viz.html");
				runBrowserCommand("file:///android_asset/viz.html");
				// reset();
				updateRoute(rdf.data_combined_json);
				updateEstimation(cd, (RouteDataFetcher) observable);
				// Attach RouteProgress
				// For testing with real GPS
				rp = new RouteProgress(((ElvizpActivity) getActivity()).gps, rdf.getCombinedRoute());
				rp.addObserver(this);
				cd.addObserver(rp);
				
				//For testing locally
				//trp = new TestRouteProgress((ElvizpActivity) getActivity(), rdf);
				//rp = trp.getProgress();
				rp.addObserver(this);
				cd.addObserver(rp);
				
				new Thread(trp).start();
				//rp.start();
			}
			// if (observable instanceof CarData) {
			// CarData cd = (CarData) observable;
			// updatePr/**
			// * Updates visualization with current CarData
			// */
			// private void updateProgress(CarData cd) {
			// runBrowserCommand("javascript:updateData(" + cd.toJson(true) +
			// ")");
			// }ogress(cd);
			// Log.v("cardebug", "update vizfragment");
			// }
			if (observable instanceof RouteProgress) {
				synchronized (rp) {
					addProgress(rp.currentDistance(), rp.currentConsumption());
				}
			}
		}
	}

	// /**
	// * Resets the visualization
	// */
	// private void reset() {
	// runBrowserCommand("javascript:reset()");
	// }

	/**
	 * Adds a specific data point to the visualization
	 */
	private void addProgress(double distance, double consumption) {
		runBrowserCommand("javascript:addProgress(" + distance + ", "
				+ consumption + ")");
	}

	// /**
	// * Updates visualization with current CarData
	// */
	// private void updateProgress(CarData cd) {
	// runBrowserCommand("javascript:updateData(" + cd.toJson(true) + ")");
	// }

	/**
	 * Loads a json encoded step file (fetched from google api) into the
	 * visualization. Interpreted as the current route.
	 * 
	 * @param json
	 *            JSON string with data.
	 */
	private void updateRoute(String json) {
		if (!isValidJSON(json)) {
			throw new IllegalArgumentException("Not a valid JSON string.");
		} else {
			runBrowserCommand("javascript:updateRoute(" + json + ")");
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
	private void updateEstimation(CarData cd2, RouteDataFetcher observable) {
		RouteDataFetcher rdf = (RouteDataFetcher) observable;
		if (rdf.getCombinedRoute() == null || rdf.getCombinedRoute().size() < 1 || browser == null)
			return;

		int factors = 0;
		factors |= CarData.SLOPE | CarData.TIME | CarData.SPEED;
		final String consumption = cd.consumptionOnRouteJSON(rdf.getCombinedRoute(), factors);
		runBrowserCommand("javascript:updateEstimation(" + consumption + ")");
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
	 * @param cs
	 *            Message to toast.
	 */
	private void postToast(final CharSequence cs) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(getActivity()
						.getApplicationContext(), cs, Toast.LENGTH_SHORT);
				toast.show();

			}
		});
	}

}
