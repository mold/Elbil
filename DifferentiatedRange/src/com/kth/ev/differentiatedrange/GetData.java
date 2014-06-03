package com.kth.ev.differentiatedrange;

import java.util.Observable;
import java.util.Observer;

import android.util.Log;

public class GetData implements Runnable, Observer {
	DiffRangeSurfaceView v = null;
	Thread t = null;
	boolean running = false;
	EVEnergy evEnergy;
	float batterySize = (float) 15.0; // kwh
	int threadSleep = 500;
	int timesPerMin, timesPer5Min, timesPer10Sec;
	float currentClimateConsumption = (float) 3.0;
	CarDataFetcher cdfetch = new CarDataFetcher(false);

	public GetData(DiffRangeSurfaceView surfaceView) {
		v = surfaceView;
		evEnergy = new EVEnergy((float) 1521, (float) 0.012, (float) 0.29, (float) 2.7435);
		evEnergy.efficiency = (float) 0.88;

		// for (int i = 1; i < 15; i++) {
		// float res =
		// (float)evEnergy.EstimatedDistance((float)(10.0*i*1000/3600),
		// (float)0.0, (float)0.0, (float) (v.soc*batterySize),(float)3.0);
		// Log.i("DISTTABLE: " + Integer.toString(i*10), Float.toString(res));
		// }

		timesPer10Sec = 10 * 1000 / threadSleep;
		timesPerMin = 60000 / threadSleep;
		timesPer5Min = 5 * 60000 / threadSleep;

		cdfetch.getCarData().addObserver(this);
	}

	@Override
	public void run() {
		while (running) {
			cdfetch.fetchData();
			// try {
			// soc = test.getInternetData("http://localhost:8080/soc").trim();
			// // Log.i("SOC", soc);
			// if (soc != null && !soc.equals((String) "null")) {
			// // Log.i("SOC in IF", soc);
			//
			// v.soc = Float.parseFloat(soc) / 100 > 0 ? Float.parseFloat(soc) /
			// 100 : 0;
			// // v.soc = (float)0.10;
			// }
			//
			// speed =
			// test.getInternetData("http://localhost:8080/speed").trim();
			// // Log.i("SPEED", speed);
			// if (speed != null && !speed.equals((String) "null")) {
			// // Log.i("SPEED in IF", speed);
			// if (speed.equals((String) "255"))
			// speed = "0";
			// v.speed = Float.parseFloat(speed);
			//
			// v.speed10SecMean = (v.speed10SecMean * (timesPer10Sec - 1) +
			// Float.parseFloat(speed))
			// / timesPer10Sec;
			// v.speedOneMinMean = (v.speedOneMinMean * (timesPerMin - 1) +
			// Float.parseFloat(speed)) / timesPerMin;
			// v.speedFiveMinMean = (v.speedFiveMinMean * (timesPer5Min - 1) +
			// Float.parseFloat(speed))
			// / timesPer5Min;
			// }
			//
			// fan =
			// test.getInternetData("http://localhost:8080/heating1").trim();
			// // Log.i("fan", fan);
			// if (fan != null && !fan.equals((String) "null")) {
			// // Log.i("Fan:", fan);
			// v.fan = Integer.parseInt(fan);
			// }
			//
			// climate =
			// test.getInternetData("http://localhost:8080/heating0").trim();
			// // Log.i("SOC", soc);
			// if (climate != null && !climate.equals((String) "null")) {
			// // Log.i("Climate:", climate);
			// v.climate = Integer.parseInt(climate);
			// }
			//
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//

			Log.i("Speed: ", Float.toString(v.speed));
			// Recaclulate distances
			float tmp = v.speed >= (float) 0.1 ? v.speed : (float) 0.1;
			if (v.speed >= (float) 0.1) {
				v.rangeArray[0] = (float) evEnergy.EstimatedDistance((float) ((float) tmp * 1000.0 / 3600.0),
						(float) 0.0, (float) 0.0, (float) (v.soc * batterySize), (float) 0.7
								+ v.currentClimateConsumption);
			} else {
				v.rangeArray[0] = (float) 0.0;
			}

			if (v.soc <= 0) {
				v.rangeArray[15] = (float) 0.0;
				v.rangeArray[16] = (float) 0.0;
			} else {
				// v.rangeArray[0] =
				// (float)evEnergy.EstimatedDistance((float)((float)tmp*1000.0/3600.0),
				// (float)0.0, (float)0.0, (float)(v.soc*batterySize),
				// (float)0.7+ v.currentClimateConsumption);
				v.rangeArray[15] = (float) evEnergy.EstimatedDistance((float) (30.0 * 1000.0 / 3600.0), (float) 0.0,
						(float) 0.0, (float) (batterySize), (float) 0.7);
				v.rangeArray[16] = (float) evEnergy.EstimatedDistance((float) (30.0 * 1000.0 / 3600.0), (float) 0.0,
						(float) 0.0, (float) (v.soc * batterySize), (float) 0.7);
				// v.rangeArray[15] =
				// (float)evEnergy.EstimatedDistance((float)(40.0*1000/3600),
				// (float)0.0, (float)0.0, (float)(v.soc*batterySize),
				// (float)0.0);
				// v.rangeArray[0] =
				// evEnergy.EstimatedDistance(v.speed*(1000/3600), 0, 0,
				// v.soc*21);
			}
			for (int i = 1; i < 15; i++) {
				if (v.soc <= 0)
					v.rangeArray[i] = (float) 0.0;
				else
					v.rangeArray[i] = (float) evEnergy.EstimatedDistance((float) (10.0 * (float) i * 1000.0 / 3600.0),
							(float) 0.0, (float) 0.0, (float) (v.soc * batterySize), (float) 0.7
									+ v.currentClimateConsumption);
				// if(v.rangeArray[i]>v.rangeArray[15])
				// v.rangeArray[15] = v.rangeArray[i];
				// Log.i("DistTABLE: " + Integer.toString(i*10),
				// Float.toString(v.rangeArray[i]));
			}

			for (int i = 1; i < 15; i++) {
				if (v.soc <= 0)
					v.rangeMaxArray[i] = (float) 0.0;
				else
					v.rangeMaxArray[i] = (float) evEnergy.EstimatedDistance(
							(float) (10.0 * (float) i * 1000.0 / 3600.0), (float) 0.0, (float) 0.0,
							(float) (v.soc * batterySize), (float) 0.7);
				// if(v.rangeArray[i]>v.rangeArray[15])
				// v.rangeArray[15] = v.rangeArray[i];
				// Log.i("MaxTABLE: " + Integer.toString(i*10),
				// Float.toString(v.rangeMaxArray[i]));
			}

			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			try {
				Thread.sleep(threadSleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void resume() {
		running = true;
		t = new Thread(this);
		t.start();
	}

	public void pause() {
		running = false;
		while (true) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		t = null;

	}

	@Override
	public void update(Observable observable, Object data) {
		CarData cd = (CarData) observable;

		double soc = cd.getSoc();
		v.soc = (float) (soc / 100 > 0 ? soc / 100 : 0);

		double speed = cd.getSpeed();
		if (speed == 255.0)
			speed = 0;
		v.speed = (float) (speed);
		v.speed10SecMean = (float)((v.speed10SecMean * (timesPer10Sec - 1) + speed) / timesPer10Sec);
		v.speedOneMinMean = (float)((v.speedOneMinMean * (timesPerMin - 1) + speed) / timesPerMin);
		v.speedFiveMinMean = (float)((v.speedFiveMinMean * (timesPer5Min - 1) + speed) / timesPer5Min);
		
		Log.i("update",v.soc+" "+v.speed+" "+v.speed10SecMean+" "+v.speedOneMinMean+" "+v.speedFiveMinMean);

		double fan = cd.getFan();
		v.fan = (int) Math.round(fan);

		double climate = cd.getClimate();
		v.climate = (int) Math.round(climate);

	}
}
