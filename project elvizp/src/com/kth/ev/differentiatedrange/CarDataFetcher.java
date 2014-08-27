/**
 * 
 */
package com.kth.ev.differentiatedrange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

/**
 * @author dkd
 * 
 */
public class CarDataFetcher implements Runnable{
	private CarData carData;
	private boolean fromCar;

	// For server data getting
	private HttpClient client;
	private HttpResponse httpResponse;
	private String baseUrl = "http://localhost:8080/";
	private BufferedReader in;

	public CarDataFetcher(CarData carData, boolean fromCar) {
		this.carData = carData; 
		this.fromCar = fromCar;

		if (fromCar) { // Gonna get data from car, not from server
			// TODO: Implement
		} else { // Gonna get data from server
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 3000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			client = new DefaultHttpClient(httpParameters);
		}
	}

	/**
	 * Fetches data from system (car or server) and send to CarData
	 */
	public void fetchData() {
		if (fromCar) { // Get data from car, not from server
			// TODO: Implement
		} else { // Get data from server
			try {
				String data = getServerData("all");
				Scanner scanner = new Scanner(data);
				scanner.useDelimiter("(</h1>)?<h1>");
				String d;
				while (scanner.hasNext()) {
					//Log.v("serverdata", d);
					d = scanner.next();
					if (d.startsWith("speed:")) {
						carData.setSpeed(Double.parseDouble(d.substring(6)));
					} else if (d.startsWith("soc:")) {
						carData.setSoc(Double.parseDouble(d.substring(4)));
					} else if (d.startsWith("amp:")) {
						carData.setAmp(Double.parseDouble(d.substring(4)));
					} else if (d.startsWith("volt:")) {
						carData.setVolt(Double.parseDouble(d.substring(5)));
					}
				}
				scanner.close();
				
				/*carData.setSpeed(Double.parseDouble(getServerData("speed")));
				carData.setSoc(Double.parseDouble(getServerData("soc")));
				carData.setAmp(Double.parseDouble(getServerData("amp")));
				carData.setVolt(Double.parseDouble(getServerData("volt")));*/
				double speed = carData.getSpeed(false);
				double amp = carData.getAmp(false);
				Log.v("cardebug", "speed: " + speed + ", amp: " + amp);
				//carData.setClimate(Double.parseDouble(getServerData("heating0")));
				//carData.setFan(Double.parseDouble(getServerData("heating1")));
				carData.calculate();
				Log.v("cardebug", "calculated");
				carData.notifyObservers();
				Log.v("cardebug", "notify");
			} catch (NumberFormatException e) {
				// Got null values or something
				//Log.i("fetch", "Got null from server for some parameter");
				// Calculate and notify (because we probably got SOME data)
				carData.calculate();
				carData.notifyObservers();
				Log.v("cardebug", "wrong value: " + e.getMessage());
				Log.e("cardebug", "wrong value: " + e.getMessage());
			} catch (HttpHostConnectException e) {
				//Log.e("fetch", "Could not connect to server");
				Log.v("cardebug", "http exception" + e.toString());
				
			} catch (SocketTimeoutException e) {
				Log.e("cardebug", "socket timeout: " + e.toString());
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
				//Log.e("fetch", e.toString());
				//Log.v("cardebug", "wrong value?: " + e.getMessage());
				Log.e("cardebug", "wrong value?: " + e.toString());
			}
		}
	}

	private String getServerData(String type) throws Exception {
		HttpGet request = new HttpGet();
		request.setURI(new URI(baseUrl + type));
		httpResponse = client.execute(request);
		in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
		StringBuffer sb = new StringBuffer("");
		String l = "";
		String nl = System.getProperty("line.separator");
		while ((l = in.readLine()) != null) {
			sb.append(l + nl);
		}
		in.close();
		Log.i("fetch", type + ": " + sb.toString());
		return sb.toString();
	}

	public CarData getCarData() {
		return carData;
	}

	/**
	 * Fetches data every 500ms.
	 */
	@Override
	public void run() {
		while(true){
			Log.v("cardebug", "before fetch");
			fetchData();
			Log.v("cardebug", "after fetch");
			try {
				Thread.sleep(50);
				Log.v("cardebug", "after sleep");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
