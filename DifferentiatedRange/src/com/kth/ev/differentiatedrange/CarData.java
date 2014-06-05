package com.kth.ev.differentiatedrange;

import java.util.Observable;

import android.util.Log;

/**
 * @author dkd
 * 
 */
public class CarData extends Observable {
	private double soc, speed, fan, climate;

	private EVEnergy evEnergy;
	private double[] rangeArray = new double[17]; // 0 is current
	private double[] rangeMaxArray = new double[16]; // 0 is current
	private double speedOneMinMean, speedFiveMinMean, speed10SecMean;

	private double currentClimateConsumption = 3.0;

	private long lastUpdateTime = System.currentTimeMillis();

	public CarData() {
		evEnergy = new EVEnergy((float) 1521, (float) 0.012, (float) 0.29, (float) 2.7435);
		evEnergy.efficiency = (float) 0.88;
	}

	public EVEnergy getEvEnergy() {
		return evEnergy;
	}

	public void setEvEnergy(EVEnergy evEnergy) {
		this.evEnergy = evEnergy;
	}

	public void calculate() {
		/** Calculate average speeds **/
		long timeSinceLast = System.currentTimeMillis() - lastUpdateTime;
		lastUpdateTime = System.currentTimeMillis();
		double updatesPerSecond = 1.0 / (timeSinceLast / 1000.0);
		double timesPer10Sec = updatesPerSecond * 10;
		double timesPerMin = updatesPerSecond * 60;
		double timesPer5Min = updatesPerSecond * 300;
		speed10SecMean = (speed10SecMean * (timesPer10Sec - 1) + speed) / timesPer10Sec;
		speedOneMinMean = (speedOneMinMean * (timesPerMin - 1) + speed) / timesPerMin;
		speedFiveMinMean = (speedFiveMinMean * (timesPer5Min - 1) + speed) / timesPer5Min;

		/** Calculate distances **/
		if (speed >= 0.1) {
			double tmp = speed >= 0.1 ? speed : 0.1;
			rangeArray[0] = evEnergy.EstimatedDistance((tmp * 1000.0 / 3600.0), 0.0, 0.0, (soc * evEnergy.batterySize),
					0.7 + currentClimateConsumption);
		} else {
			rangeArray[0] = 0.0;
		}

		if (soc <= 0) {
			rangeArray[15] = 0.0;
			rangeArray[16] = 0.0;
		} else {
			rangeArray[15] = evEnergy
					.EstimatedDistance((30.0 * 1000.0 / 3600.0), 0.0, 0.0, (evEnergy.batterySize), 0.7);
			rangeArray[16] = evEnergy.EstimatedDistance((30.0 * 1000.0 / 3600.0), 0.0, 0.0,
					(soc * evEnergy.batterySize), 0.7);
		}
		for (int i = 1; i < 15; i++) {
			if (soc <= 0)
				rangeArray[i] = 0.0;
			else
				rangeArray[i] = evEnergy.EstimatedDistance((10.0 * i * 1000.0 / 3600.0), 0.0, 0.0,
						(soc * evEnergy.batterySize), 0.7 + currentClimateConsumption);
		}

		for (int i = 1; i < 15; i++) {
			if (soc <= 0)
				rangeMaxArray[i] = 0.0;
			else
				rangeMaxArray[i] = evEnergy.EstimatedDistance((10.0 * i * 1000.0 / 3600.0), 0.0, 0.0,
						(soc * evEnergy.batterySize), 0.7);
		}

		calculateClimatePower();

		setChanged();
	}

	/**
	 * Returns soc (state of charge)
	 * 
	 * @return soc (0-100)
	 */
	public double getSoc() {
		return soc;
	}

	/**
	 * Set SOC (State Of Charge)
	 * 
	 * @param soc
	 *            State of charge (0-100)
	 */
	public void setSoc(double soc) {
		this.soc = soc;
		setChanged();
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		if (speed >= 255)
			speed = 0;
		this.speed = speed;
		setChanged();
	}

	public double getFan() {
		return fan;
	}

	public void setFan(double fan) {
		this.fan = fan;
		setChanged();
	}

	public double getClimate() {
		return climate;
	}

	public void setClimate(double climate) {
		this.climate = climate;
		setChanged();
	}

	private void calculateClimatePower() {
		// heater1=112 -> fan off -> no power
		// 113-120 fan on levels 120 maximum
		float fanImpact = (float) 0.0;

		if (fan >= 113 && fan <= 120)
			fanImpact = (float) 1.0 - ((float) 120 - (float) fan) / (float) 7.0;
		if (fan >= 81 && fan <= 88)
			fanImpact = (float) 1.0 - ((float) 88 - (float) fan) / (float) 7.0;
		if (fan >= 97 && fan <= 103)
			fanImpact = (float) 1.0 - ((float) 103 - (float) fan) / (float) 7.0;

		try {
			// if(fan!=112)
			if (fanImpact >= 0.0) {
				// float fanImpact =
				// (float)1.0-((float)120-(float)fan)/(float)7.0; // 0-1kW, i
				// don't know about this
				// Log.i("CLIMATEfi", Float.toString(fanImpact));
				// 8-13 heater on
				if (climate == 129 || (climate >= 103 && climate <= 109)) // 103
																			// pushmax
																			// heat
				{
					currentClimateConsumption = (float) 3.5 + fanImpact;
					return;
				}
				if ((climate >= 65 && climate <= 71) || climate == 1) {
					currentClimateConsumption = fanImpact;
					return;
				}
				if (climate >= 8 && climate <= 13) {
					Log.i("CLIMATEhe",
							Float.toString((float) (((float) climate - (float) 7.0) / (float) 6.0) * (float) 3.0));
					currentClimateConsumption = (float) ((((float) climate - (float) 7.0) / (float) 6.0) * (float) 3.0)
							+ fanImpact;
					return;

				}

				if (climate >= 72 && climate <= 77) {
					Log.i("CLIMATEhe",
							Float.toString((float) (((float) climate - (float) 72.0) / (float) 6.0) * (float) 3.0));
					currentClimateConsumption = (float) ((((float) climate - (float) 72.0) / (float) 6.0) * (float) 3.0)
							+ fanImpact;
					return;

				}

				if (climate >= 136 && climate <= 141) {
					Log.i("CLIMATEhe",
							Float.toString((float) (((float) climate - (float) 136.0) / (float) 6.0) * (float) 3.0));
					currentClimateConsumption = (float) ((((float) climate - (float) 136.0) / (float) 6.0) * (float) 3.0)
							+ fanImpact;
					return;

				}
				if (climate >= 200 && climate <= 205) {
					// Log.i("CLIMATEhe",
					// Float.toString((float)(((float)climate-(float)200.0)/(float)6.0)*(float)3.0));
					currentClimateConsumption = (float) ((((float) climate - (float) 200.0) / (float) 6.0) * (float) 3.0)
							+ fanImpact;
					return;

				}

				// 2-6 coolAC off
				if ((climate >= 2 && climate <= 7) || climate == 135 || climate == 199) {
					currentClimateConsumption = fanImpact;
					return;
				}
				// 2-6 coolAC off
				// 130-134 AC on
				if (climate >= 130 && climate <= 134) {
					currentClimateConsumption = (float) ((((float) 134.0 - (float) climate) / (float) 6.0) * (float) 3.2)
							+ fanImpact;
					return;
				}

				if (climate >= 193 && climate <= 198) {
					currentClimateConsumption = (float) ((((float) 198.0 - (float) climate) / (float) 6.0) * (float) 3.0)
							+ fanImpact;
					return;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		currentClimateConsumption = -10.0;
		return;

	}

	public double getSpeedOneMinMean() {
		return speedOneMinMean;
	}

	public double getSpeedFiveMinMean() {
		return speedFiveMinMean;
	}

	public double getSpeed10SecMean() {
		return speed10SecMean;
	}
}
