package com.kth.ev.differentiatedrange;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class DataGraph extends SurfaceView implements SurfaceHolder.Callback, Observer {

	static enum DATA {
		SPEED, AMP, SOC, FAN, CLIMATE
	}
	
	SurfaceHolder holder;
	DATA data;
	Paint paint;
	Paint textPaint;
	
	int width;
	int height;
	int textSize = 40;
	double prevDataPoint = 0;
	double lastDataPoint = 0;
	int dataPoints = 0;
	String typeString;
	int textWidth;
	float min;
	float max;
	double[] dataValues = new double[200];
	
	public DataGraph(Context context, CarData cd, DATA data) {
		super(context);
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(metrics.widthPixels, 200);
		setLayoutParams(params);
		setPadding(0, 0, 0, 10);
		
		holder = getHolder();
		holder.addCallback(this);
		
		this.data = data;
		switch(this.data) {
		case SPEED:
			min = 0;
			max = 255;
			typeString = "speed";
			break;
		case AMP:
			min = -100;
			max = 100;
			typeString = "amp";
			break;
		case SOC:
			min = 0;
			max = 100;
			typeString = "soc";
			break;
		case FAN:
			min = 0;
			max = 255;
			typeString = "fan";
			break;
		case CLIMATE:
			min = 0;
			max = 255;
			typeString = "climate";
			break;
		}
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(4);
		paint.setStyle(Paint.Style.STROKE);
		Random r = new Random();
		paint.setARGB(255, 100+r.nextInt(155), 100+r.nextInt(155), 100+r.nextInt(155));
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextSize(textSize);
		textPaint.setColor(paint.getColor());
		
		Rect bounds = new Rect();
		textPaint.getTextBounds(typeString, 0, typeString.length(), bounds);
		textWidth = bounds.width();
		
		cd.addObserver(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		if (holder == null || !holder.getSurface().isValid()){
			return;
		}
		Canvas c = null;
		CarData cd = (CarData) observable;
		
		double value = 0;
		switch(this.data) {
		case SPEED:
			value = cd.getSpeed(false);
			break;
		case AMP:
			value = cd.getAmp(false);
			break;
		case SOC:
			value = cd.getSoc(false);
			break;
		case FAN:
			value = cd.getFan(false);
			break;
		case CLIMATE:
			value = cd.getClimate(false);
			break;
		}
		if (dataPoints >= dataValues.length) {
			dataPoints = 0;
		}
		dataValues[dataPoints] = value;
		dataPoints++;
		
		try {
            c = holder.lockCanvas();
            synchronized (holder) {
            	if (c != null) {
            		draw(c);
            	}
            }                                   
        } finally {
        	if (c != null) {
                holder.unlockCanvasAndPost(c);
        	}
        }
	}
	
	@Override
	public void draw(Canvas c) {
		// clear canvas
		c.drawColor(Color.BLACK);
		
		float dx = (float)width/dataValues.length;
		float yScale = (height - 30)/(max - min);
		Paint white = new Paint();
		white.setColor(Color.WHITE);
		white.setTextSize(40);
		c.drawLine(0, height - textSize/2 + min*yScale, width, height - textSize/2 + min*yScale, white);
		c.drawText(""+max, 0, textSize, textPaint);
		c.drawText(""+min, 0, height-5, textPaint);
		c.drawText(typeString, width-textWidth-5, textSize, textPaint);
		c.drawText(""+dataValues[dataPoints-1], (dataPoints-1)*dx, (float)(height - textSize/2 - (dataValues[dataPoints-1] - min)*yScale), textPaint);
		for (int i = 0; i < dataPoints-1; i++) {
			c.drawLine(i*dx, (float)(height - textSize/2 - (dataValues[i] - min)*yScale),
					(i+1)*dx, (float)(height - textSize/2 - (dataValues[i+1] - min)*yScale),
					paint);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
		holder.setFixedSize(this.width, 200);
		Log.v("datagraph", width+", "+height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

}
