package com.kth.ev.cardata;

import java.util.List;

import com.kth.ev.routedata.APIDataTypes.Step;

public class EVEnergy {

	// speed = undefined,
	// time = undefined,
	// soc = undefined,
	// acceleration = 0, // acceleration
	public double mass = 1521; // vehicle mass
	public double cr = 0.012; // vehicle roll resistance coefficient
	public double cd = 0.29; // vehicle drag coefficient
	public double area = 2.7435; // vehicle area
	public double r = 1.225; // air density
	public double g = 9.82; // gravity
	public double efficiency = 0.87; // efficiency
	public double heating = 3.0; // kW Level 1, Level 2 heater is 4.2 kW, and
									// A/C is 3.5kW. Both use variable amounts
									// of power.
	public double batterySize = 15.0; // kwh

	public double EstimatedDistance(double speed, double acceleration,
			double slope, double soc, double heating) {
		return speed * (soc * 3600)
				/ Power(speed, acceleration, slope, heating);
		// return evenergy.speed() * ((evenergy.soc() * 3600)/ evenergy.power())
		// ;
	}

	public double Power(double speed, double acceleration, double slope,
			double heating) {
		return heating + TotalForce(speed, acceleration, slope) * speed / 1000
				/ efficiency; // kW
	}

	public double TotalForce(double speed, double acceleration, double slope) {
		// Acceleration resistance
		double fa = acceleration * mass; // kg m/s^2

		// Slope resistance
		// double fs = 0;
		double fs = slope * g * mass; // kg m/s^2
		// if(slope > 0.0f)
		// Log.d("SLOPE EFFECT", fs+"");

		// Roll resistance
		double fr = cr * g * mass; // kg m/s^2

		// Wind resistance a.k.a. drag
		double fd = (double) 0.5 * r * cd * area * speed * speed; // kg m/s^2

		double total = fa + fs + fr + fd;

		return total; // kg m/s^2

	}

	public double Energy(double speed, double acceleration, double slope,
			double heating, double dt) {
		return Power(speed, acceleration, slope, heating) * dt / 3600;
	}

	public double kmPerKWh(double distance, double speed, double acceleration,
			double slope, double heating, double dt) {
		return distance / 1000.0f
				/ Energy(speed, acceleration, slope, heating, dt);
	}

	public double kWhPerKm(double distance, double speed, double acceleration,
			double slope, double heating, double dt) {
		return Energy(speed, acceleration, slope, heating, dt) / distance
				* 1000.0f;
	}

	// Factors for the method consumption on route.
	public static final int SPEED = 1, SLOPE = 2, ACCELERATION = 8, TIME = 16;

	/**
	 * Calculates the consumption rate of the given route, consisting of steps
	 * fetched from the Google API Directions & Elevation API
	 * 
	 * @param steps
	 *            A list of Step
	 * @param factors
	 *            A list of which parameters to take into account, coded into an
	 *            int.
	 * @return An array of doubles which contains the energy consumption for
	 *         every step.
	 */
	public double[] consumptionOnRoute(List<Step> steps, int factors,
			double climate) {
		if (steps == null) {
			return null;
		}
		double[] ret = new double[steps.size()];
		int i = 0;
		for (Step s : steps) {
			ret[i] = kWhPerKm(s.distance.value,
					(factors & SPEED) > 0 ? s.distance.value / s.duration.value
							: 0, 0, (factors & SLOPE) > 0 ? s.slope : 0,
					climate, (factors & TIME) > 0 ? s.duration.value : 1);

			i++;
		}
		return ret;
	}

}
