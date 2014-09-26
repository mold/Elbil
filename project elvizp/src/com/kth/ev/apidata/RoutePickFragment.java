package com.kth.ev.apidata;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kth.ev.application.ElvizpActivity;

import se.kth.ev.gmapsviz.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

/**
 * Fragment for picking a route. Uses auto completion to help the user. When a
 * route has been picked, the fragment pushes the RouteDataFetched observable to
 * the first fragment (with id 0) of the main activity.
 * 
 * @author marothon
 * 
 */
public class RoutePickFragment extends Fragment {
	protected static final String TAG = "RoutePickFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		View v = inflater
				.inflate(R.layout.fragment_routepick, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final AutoCompleteTextView from = (AutoCompleteTextView) getView()
				.findViewById(R.id.from);
		final AutoCompleteTextView to = (AutoCompleteTextView) getView()
				.findViewById(R.id.to);

		from.setText("Lindstedtsvägen 3, Stockholm, Sweden");
		to.setText("Blackeberg, Sweden");

		OnKeyListener okl = new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(
									ElvizpActivity.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		};

		from.setOnKeyListener(okl);
		to.setOnKeyListener(okl);
		from.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line));
		to.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line));

		// Connect route picker fragment with viz fragment
		Button butt = (Button) getView().findViewById(R.id.load_directions);
		butt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Log.d(TAG, "GET ROUTE DATA");
				AutoCompleteTextView from = (AutoCompleteTextView) getView()
						.findViewById(R.id.from);
				AutoCompleteTextView to = (AutoCompleteTextView) getView()
						.findViewById(R.id.to);
				// Log.d(TAG,
				// "Clicked for query "+from.getText().toString()+" to "+to.getText().toString());
				RouteDataFetcher rdf = new RouteDataFetcher(from.getText()
						.toString(), to.getText().toString());
				ElvizpActivity a = (ElvizpActivity) getActivity();
				if (a.isNetworkAvailable()) {
					a.relayObservable(rdf);
					Thread t_rdf = new Thread(rdf);
					t_rdf.start();
				} else {
					CharSequence text = "Cannot fetch route data without internet connection.";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(getActivity()
							.getApplicationContext(), text, duration);
					toast.show();
					Log.e(TAG, text.toString());
				}
			}
		});

		Button use_gps = (Button) getView().findViewById(R.id.use_gps);
		use_gps.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Check that we have a location to use
				AutoCompleteTextView from = (AutoCompleteTextView) getView()
						.findViewById(R.id.from);
				ElvizpActivity act = (ElvizpActivity) getActivity();

				if (!act.gps().hasLocation()) {
					postToast("No GPS location available");
					return;
				}

				try {
					String georevdata = GoogleAPIQueries
							.requestReverseGeolocation(
									act.gps().getCurrentLocation()).get();
					// JsonParser parser = APIRequestTask.JSON_FACTORY
					// .createJsonParser(georevdata);
					JSONObject obj = new JSONObject(georevdata);
					// parser.parse(obj);

					String address = obj.getJSONArray("results")
							.getJSONObject(0).getString("formatted_address");
					Log.d(TAG, address);
					from.setText(address);
				} catch (InterruptedException e) {
					Log.d(TAG,
							"Something stopped us from getting the geolocation");
					e.printStackTrace();
				} catch (ExecutionException e) {
					Log.d(TAG, "Something went wrong, using raw gps location.");
					from.setText(act.gps().getCurrentLocation().getLatitude()
							+ "," + act.gps().getCurrentLocation().getLongitude());
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG,
							"Something went wrong with the json parsing. Using raw gps location");
					from.setText(act.gps().getCurrentLocation().getLatitude()
							+ "," + act.gps().getCurrentLocation().getLongitude());
					e.printStackTrace();
				}
			}
		});

		Button show_route = (Button) getView().findViewById(R.id.show_gmaps);
		show_route.setText("Show route in Google Maps");
		show_route.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String sf = from.getText().toString();
				sf.replace(" ", "+");
				String st = to.getText().toString();
				st.replace(" ", "+");
				ElvizpActivity a = (ElvizpActivity) getActivity();
				if (a.isNetworkAvailable()) {
					Intent intent = new Intent(
							android.content.Intent.ACTION_VIEW, Uri
									.parse("http://www.google.se/maps/dir/"
											+ sf + "/" + st));
					startActivity(intent);
				} else {
					CharSequence text = "Cannot show route without internet access.";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(getActivity()
							.getApplicationContext(), text, duration);
					toast.show();
					Log.e(TAG, text.toString());
				}
			}
		});
	}

	private void postToast(String toast_text) {
		CharSequence text = toast_text;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(getActivity().getApplicationContext(),
				text, duration);
		toast.show();
	}

	/**
	 * Autocompletion class.
	 * 
	 */
	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
			implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			if (resultList.size() <= index)
				return "";
			return resultList.get(index);
		}

		APIRequestTask task;

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					Log.d(TAG, "FILTERING!");
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						String result = null;
						try {
							Log.d(TAG, "Starting request");
							if (task == null)
								task = GoogleAPIQueries
										.requestAutocomplete(constraint
												.toString());
							result = task.get();
						} catch (InterruptedException e) {
							Log.d(TAG, "Autocomplete task interrupted.");
							e.printStackTrace();
						} catch (ExecutionException e) {
							Log.d(TAG, "Autocomplete task execution error.");
							e.printStackTrace();
						}
						if (result != null) {
							resultList = parseAutocomplete(result);
						}
					}
					task = null;
					return filterResults;
				}

				private ArrayList<String> parseAutocomplete(String result) {
					ArrayList<String> resultList = null;
					try {
						// Create a JSON object hierarchy from the results
						JSONObject jsonObj = new JSONObject(result);
						JSONArray predsJsonArray = jsonObj
								.getJSONArray("predictions");

						// Extract the Place descriptions from the results
						resultList = new ArrayList<String>(
								predsJsonArray.length());
						for (int i = 0; i < predsJsonArray.length(); i++) {
							resultList.add(predsJsonArray.getJSONObject(i)
									.getString("description"));
						}
					} catch (JSONException e) {
						Log.e(TAG, "Cannot process JSON results", e);
					}
					return resultList;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					// Assign the data to the FilterResults
					results.values = resultList;
					results.count = resultList.size();

					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}

	}

}
