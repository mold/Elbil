package se.kth.ev.gmapsviz;

import java.net.URL;
import java.util.List;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.maps.android.PolyUtil;

import android.os.AsyncTask;
import android.util.Log;

public class GoogleAPIQueries {
	static final HttpTransport HTTP_TRANSPORT = AndroidHttp
			.newCompatibleTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	static String api_key;
	
	public static void setKey(String key){
		api_key = key;
	}
	
	public static APIRequest requestDirections(final String from,
			final String to) {
		URLParameter _from = new URLParameter("origin", from);
		URLParameter _to = new URLParameter("destination", to);
		URLParameter _sensor = new URLParameter("sensor", "false");
		
		APIRequest task = new APIRequest("https://maps.googleapis.com/maps/api/directions/json");
		task.execute(_from, _to, _sensor);
		return task;
	}
	
	public static APIRequest requestElevation(String locs){
		URLParameter locations = new URLParameter("locations", locs);
		APIRequest task = new APIRequest("https://maps.googleapis.com/maps/api/elevation/json");
		task.execute(locations);
		return task;
	}

	public static class DirectionsResult {
		@Key("routes")
		public List<Route> routes;
	}

	public static class Route {
		@Key("overview_polyline")
		public OverviewPolyLine overviewPolyLine;
	}

	public static class OverviewPolyLine {
		@Key("points")
		public String points;
	}

	public static class URLParameter {
		public String name;
		public String value;

		public URLParameter(String n, String v) {
			name = n;
			value = v;
		}

		public String toString() {
			return name + "=" + value;
		}
	}

	private static class APIRequest extends
			AsyncTask<URLParameter, Integer, String> {
		final String url_string;
		
		public APIRequest(String url){
			url_string = url;
		}
		
		@Override
		protected String doInBackground(URLParameter... urlParams) {
			try {
				HttpRequestFactory requestFactory = HTTP_TRANSPORT
						.createRequestFactory(new HttpRequestInitializer() {
							@Override
							public void initialize(HttpRequest request) {
								request.setParser(new JsonObjectParser(
										JSON_FACTORY));
							}
						});

				GenericUrl url = new GenericUrl(url_string);
				for (URLParameter urlParam : urlParams)
					url.put(urlParam.name, urlParam.value);
				url.put("key", api_key);
				// url.put("origin", from);
				// url.put("destination", to);
				// url.put("sensor", false);

				com.google.api.client.http.HttpRequest request = requestFactory
						.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				Log.d("httpresponse", httpResponse.parseAsString());
				/*
				DirectionsResult directionsResult = httpResponse
						.parseAs(DirectionsResult.class);
				String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
				Log.d("encodedPoints", encodedPoints);
				*/
				// latLngs = PolyUtil.decode(encodedPoints);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(String result) {
		}

	}
}
