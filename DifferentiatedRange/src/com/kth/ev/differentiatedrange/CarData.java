package com.kth.ev.differentiatedrange;

import java.util.Observable;

/**
 * @author dkd
 * 
 */
public class CarData extends Observable {
	private double soc, speed, fan, climate;

	// TODO: Add special fields (e.g. climateConsumptionYadaYada)

	public CarData() {

	}

	public void calculate() {
		// TODO: Calculate special fields
		setChanged();
	}

	public double getSoc() {
		return soc;
	}

	public void setSoc(double soc) {
		setChanged();
		this.soc = soc;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		setChanged();
		this.speed = speed;
	}

	public double getFan() {
		return fan;
	}

	public void setFan(double fan) {
		setChanged();
		this.fan = fan;
	}

	public double getClimate() {
		return climate;
	}

	public void setClimate(double climate) {
		setChanged();
		this.climate = climate;
	}
}
