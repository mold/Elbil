package se.kth.ev.gmapsviz;
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

public class APIRequest extends AsyncTask<URLParameter, Integer, String> {
	final String url_string;
	static String api_key;
	static final HttpTransport HTTP_TRANSPORT = AndroidHttp
			.newCompatibleTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	public APIRequest(String url) {
		url_string = url;
	}

	public static void setKey(String key) {
		api_key = key;
	}
	
	private HttpResponse getResponse(URLParameter... params) throws Exception {
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

	@Override
	protected String doInBackground(URLParameter... urlParams) {
		String httpResponse = null;
		try {
			httpResponse = getResponse(urlParams).parseAsString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return httpResponse;
	}


}