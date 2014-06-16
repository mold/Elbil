package se.kth.ev.gmapsviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class PieChart {
	private RectF bounds;
	private float total;
	private HashMap<String, Float> slices; 
	private HashMap<String, Integer> colors;
	private Random rand;
	private float radius;
	
	public PieChart(float r){
		rand = new Random();
		radius = r;
		total = 0;
		slices = new HashMap<String, Float>(10);
		colors = new HashMap<String, Integer>(10);
	}
	
	/**
	 * Sets the scale of the radius, where a radius of 1 = half of the canvas width.
	 * 
	 * @param r Scale of the radius. Must hold condition 0 < r < 1
	 */
	public void setRadius(float r){
		if(r < 0 || r > 1)
			throw new IllegalArgumentException("Error: Radius "+radius+" not between 0 and 1!");
		radius = r;
	}
	
	public void addSlice(String name, float amount){
		int color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		total += amount;
		slices.put(name, amount);
		colors.put(name, color);
	}
	
	public void removeSlice(String name){
		float amount = slices.remove(name);
		total -= amount;
		colors.remove(name);
	}
		
	public void addSlice(String name, float amount, int color){
		total += amount;
		slices.put(name, amount);
		colors.put(name, color);
	}
	
	public void changeSliceColor(String name, int color){
		if(colors.containsKey(name)){
			colors.put(name, color);
		}else{
			System.err.println("Warning: No slice with that name.");
		}
	}
	
	public void drawChart(Canvas c){
		Paint p = new Paint();
		
		//float radius = 0.5f*c.getWidth();
		float radius = this.radius*0.5f*c.getWidth();
		RectF chart = new RectF(c.getWidth()/2-radius, c.getHeight()/2-radius, c.getWidth()/2+radius, c.getHeight()/2+radius);
		p.setColor(Color.LTGRAY);
		c.drawOval(chart, p);

			
		float sofar = 0;
		for(Entry<String, Float> am : slices.entrySet()){
			if(am.getValue() < 0.0f) continue;
			float angle = (am.getValue()/total)*360;
			p.setColor(colors.get(am.getKey()));
			c.drawArc(chart, sofar, angle, true, p);
			sofar += angle;
		}
		c.drawArc(chart, sofar, 360.0f-sofar, true, p);
		
	
	}

}
