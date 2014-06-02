package com.kth.ev.differentiatedrange;

public class EVEnergy {

	//speed = undefined, 
	//time = undefined, 
	//distance = undefined,
	//soc = undefined,
	//acceleration = 0, // acceleration
	//slope = 0, // slope
	public float mass = (float) 1521; // vehicle mass
	public float cr = (float) 0.012; // vehicle roll resistance coefficient
	public float cd = (float) 0.29; // vehicle drag coefficient
	public float area = (float) 2.7435; // vehicle area
	public float r = (float) 1.225; // air density
	public float g = (float) 9.82; // gravity
	public float efficiency = (float) 0.87; // efficiency
	public float heating = (float)3.0; //kW Level 1, Level 2 heater is 4.2 kW, and A/C is 3.5kW. Both use variable amounts of power.

	public EVEnergy(){

	}
	
	public EVEnergy(float mass, float cr, float cd, float area){
		mass = this.mass;
		cr = this.cr;
		cd = this.cd;
		area = this.area;
	}

	public float EstimatedDistance(float speed,float acceleration, float slope, float soc, float heating){
		return speed * (soc*3600)/ Power(speed, acceleration,slope,efficiency,heating);
		//return evenergy.speed() * ((evenergy.soc() * 3600)/ evenergy.power()) ;
	}
	
	public float Power(float speed, float acceleration, float slope, float efficiency, float heating) {
		return heating + TotalForce(speed, acceleration, slope) * speed / 1000 / efficiency; // kW
	}
	
	public float TotalForce(float speed, float acceleration, float slope){
		// Acceleration resistance
		float fa = acceleration * mass; // kg m/s^2
	
		// Slope resistance
		float fs = slope * g * mass; // kg m/s^2
	
		// Roll resistance
		float fr = cr * g * mass; // kg m/s^2
	
		// Wind resistance a.k.a. drag
		float fd = (float)0.5 * r * cd * area * speed * speed; // kg m/s^2
	
		float total = fa + fs + fr + fd;
	
		return total;  // kg m/s^2
	
	}

}