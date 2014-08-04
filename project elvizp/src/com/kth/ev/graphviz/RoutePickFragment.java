package com.kth.ev.graphviz;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.ev.gmapsviz.R;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Fragment for picking a route. Uses auto completion
 * to help the user. When a route has been picked, the
 * fragment pushes the RouteDataFetched observable to
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

		AutoCompleteTextView from = (AutoCompleteTextView) getView()
				.findViewById(R.id.from);
		AutoCompleteTextView to = (AutoCompleteTextView) getView()
				.findViewById(R.id.to);

		from.setText("Lindstedtsvägen 9, Stockholm, Sweden");
		to.setText("Blackeberg, Sweden");       
        
		from.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line));
		to.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line));
		  
		//Connect route picker fragment with viz fragment
		Button butt = (Button) getView().findViewById(R.id.load_directions);
		butt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.d(TAG, "GET ROUTE DATA");
				AutoCompleteTextView from = (AutoCompleteTextView) getView()
						.findViewById(R.id.from);
				AutoCompleteTextView to = (AutoCompleteTextView) getView()
						.findViewById(R.id.to);
				//Log.d(TAG, "Clicked for query "+from.getText().toString()+" to "+to.getText().toString());
				RouteDataFetcher rdf = new RouteDataFetcher(from.getText().toString(), to.getText().toString());
				ElvizpActivity a = (ElvizpActivity) getActivity();
				a.relayObservable(rdf, 0);
				Thread t_rdf = new Thread(rdf);
				t_rdf.start();
			}
		});
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

							// Assign the data to the FilterResults
							filterResults.values = resultList;
							filterResults.count = resultList.size();
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
