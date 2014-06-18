package com.kth.ev.graphviz;
import java.io.IOException;

import android.os.AsyncTask;

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

/**
 * Class to handle a HTTP call to any HTTP based API.
 * 
 * @author marothon
 *
 */
public class APIRequestTask extends AsyncTask<URLParameter, Integer, String> {
	final String url_string;
	static String api_key;
	static final HttpTransport HTTP_TRANSPORT = AndroidHttp
			.newCompatibleTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * Constructs task
	 * 
	 * @param url The base url of the API to request data from.
	 */
	public APIRequestTask(String url) {
		url_string = url;
	}

	/**
	 * 
	 * Sets the API key for this call. Is sent along the request
	 * as the URL parameter "key". 
	 * 
	 * @param key
	 */
	public static void setKey(String key) {
		api_key = key;
	}
	
	/**
	 * 
	 * Method that sends the HTTP request to the given url using the parameters
	 * given..
	 * 
	 * @param params URLParameters that accompany the API request.
	 * @return	Returns the HTTPResponse object.
	 * @throws IOException Exception when building the request failed.
	 */
	private HttpResponse getResponse(URLParameter... params) throws IOException {
		HttpRequestFactory requestFactory = HTTP_TRANSPORT
				.createRequestFactory(new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});

		GenericUrl url = new GenericUrl(url_string);
		for (URLParameter urlParam : params)
			url.put(urlParam.name, urlParam.value);

		url.put("key", api_key);

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		return httpResponse;
	}

	/**
	 * Performs the api call. If the API call fails the .get() method
	 * will return null.
	 */
	@Override
	protected String doInBackground(URLParameter... urlParams) {
		String httpResponse = null;
		try {
			httpResponse = getResponse(urlParams).parseAsString();
		} catch (IOException ex) {
			ex.printStackTrace();//We don't care about the error.
		}
		return httpResponse;
	}


}