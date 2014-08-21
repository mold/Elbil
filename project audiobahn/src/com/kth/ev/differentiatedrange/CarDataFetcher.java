/**
 * 
 */
package com.kth.ev.differentiatedrange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

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
	private String baseUrl = "http://localhost:8080/";
	private BufferedReader in;

	public CarDataFetcher(CarData carData, boolean fromCar) {
		this.carData = carData; 
		this.fromCar = fromCar;

		if (fromCar) { // Gonna get data from car, not from server
			// TODO: Implement
		} else { // Gonna get data from server
			client = new DefaultHttpClient();
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
				Log.v("cardebug", data);
				carData.setSpeed(Double.parseDouble(getServerData("speed")));
				carData.setSoc(Double.parseDouble(getServerData("soc")));
				carData.setAmp(Double.parseDouble(getServerData("amp")));
				
				//double speed = carData.getSpeed(false);
				//double amp = carData.getAmp(false);
				//Log.v("cardebug", "speed: " + speed + ", amp: " + amp);
				//carData.setClimate(Double.parseDouble(getServerData("heating0")));
				//carData.setFan(Double.parseDouble(getServerData("heating1")));
				carData.calculate();
				carData.notifyObservers();
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
			} catch (Exception e) {
				// Auto-generated catch block
				e.printStackTrace();
				//Log.e("fetch", e.toString());
				//Log.v("cardebug", "wrong value?: " + e.getMessage());
				//Log.e("cardebug", "wrong value?: " + e.getMessage());
			}
		}
	}

	private String getServerData(String type) throws Exception {
		HttpGet request = new HttpGet();
		request.setURI(new URI(baseUrl + type));
		HttpResponse response = client.execute(request);
		Log.v("cardebug", "request executed");
		in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
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
				Thread.sleep(500);
				Log.v("cardebug", "after sleep");
			} catch (InterruptedException e) {
				e.getMessage();
			}
		}
	}
}
