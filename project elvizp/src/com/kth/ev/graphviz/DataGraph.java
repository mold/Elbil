package com.kth.ev.graphviz;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.kth.ev.electriccar.CarData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public class DataGraph extends SurfaceView implements SurfaceHolder.Callback,
		Observer {

	static enum DATA {
		SPEED, AMP, SOC, FAN, CLIMATE
	}

	SurfaceHolder holder;
	DATA data;
	Paint paint;
	Paint textPaint;
	Paint white;
	Path path;

	int lineSegments = 200;
	int dataPoints = lineSegments;
	int width;
	int height;
	int textSize = 40;
	boolean invalidDataPoint;
	float dataPoint;
	String typeString;
	int textWidth;
	float min;
	float max;

	public DataGraph(Context context, String typeString, float min, float max) {
		super(context);

		this.typeString = typeString;
		this.min = min;
		this.max = max;

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		width = metrics.widthPixels;
		height = 200;
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,
				height);
		setLayoutParams(params);
		setPadding(0, 0, 0, 10);

		holder = getHolder();
		holder.addCallback(this);

		path = new Path();

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(4);
		paint.setStyle(Paint.Style.STROKE);
		Random r = new Random();
		paint.setARGB(255, 100 + r.nextInt(155), 100 + r.nextInt(155),
				100 + r.nextInt(155));
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextSize(textSize);
		textPaint.setColor(paint.getColor());
		white = new Paint();
		white.setColor(Color.WHITE);

		Rect bounds = new Rect();
		textPaint.getTextBounds(typeString, 0, typeString.length(), bounds);
		textWidth = bounds.width();
	}

	public DataGraph(Context context, CarData cd, DATA data) {
		this(context, "", 0, 0);

		this.data = data;
		switch (this.data) {
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

		Rect bounds = new Rect();
		textPaint.getTextBounds(typeString, 0, typeString.length(), bounds);
		textWidth = bounds.width();

		cd.addObserver(this);
	}

	public void addDataPoint(float value) {
		dataPoints++;
		if (dataPoints >= lineSegments) {
			dataPoints = 0;
			path = new Path();
		}
		dataPoint = value;

		if (holder == null || !holder.getSurface().isValid()) {
			return;
		}
		Canvas c = null;

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

	public void addDataPoint() {
		invalidDataPoint = true;
		addDataPoint(0);
	}

	public void setColor(int color) {
		paint.setColor(color);
		textPaint.setColor(color);
	}

	@Override
	public void update(Observable observable, Object data) {
		CarData cd = (CarData) observable;

		double value = 0;
		switch (this.data) {
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
		default:
			return;
		}

		this.addDataPoint((float) value);
	}

	@Override
	public void draw(Canvas c) {
		// clear canvas
		c.drawColor(Color.BLACK);

		float dx = (float) width / lineSegments;
		float yScale = (height - textSize) / (max - min);
		float x = dataPoints * dx;
		float y = height - textSize / 2 - (dataPoint - min) * yScale;
		c.drawLine(0, height - textSize / 2 + min * yScale, width, height
				- textSize / 2 + min * yScale, white);
		c.drawText("" + max, 0, textSize, textPaint);
		c.drawText("" + min, 0, height - 5, textPaint);
		c.drawText(typeString, width - textWidth - 5, textSize, textPaint);
		if (invalidDataPoint) {
			invalidDataPoint = false;
			c.drawText("undefined", dataPoints * dx + 5, height - textSize / 2
					+ min * yScale, textPaint);
			path.moveTo(x, y);
		} else {
			c.drawText("" + dataPoint, dataPoints * dx + 5, y + textSize / 2
					- 5, textPaint);
			if (dataPoints > 0) {
				path.lineTo(x, y);
			} else {
				path.moveTo(x, y);
			}
		}
		c.drawPath(path, paint);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
		holder.setFixedSize(this.width, 200);
		Log.v("datagraph", width + ", " + height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

}
