package com.kth.ev.vizsample;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.kth.ev.graphviz.CanvasRenderer;
import com.kth.ev.routedata.APIDataTypes.Step;

/**
 * This class shows how to use the CanvasRenderer interface to draw something on
 * a Canvas. An instance of RouteBoxes will draw onto a given Canvas by being called
 * through the draw(Canvas c) method. A RouteBoxes will only draw on the canvas
 * when it has both route data and an estimation.
 * 
 * @author marothon
 * 
 */
class RouteBoxes implements CanvasRenderer {
//	private static final String TAG = "RouteBoxes";
	private List<Step> route;
	private double[] est_c;
	private double[] dist;
	private double y_max;
	private double y_min;
	private double x_max;
	private int c_width;
	private int c_height;
	private float margin;
	private float origy;
	private float origx;
	private Paint p;

	/**
	 * Constructor.
	 * 
	 * @param route
	 * @param est
	 */
	public RouteBoxes(List<Step> route, double[] est) {
		this.route = route;
		this.est_c = est;
		p = new Paint();
		updateData(route, est);
	}

	/**
	 * Default constructor. Invalid for drawing.
	 */
	public RouteBoxes() {
		p = new Paint();
	}

	/**
	 * This method is where the main drawing onto the canvas takes place. The
	 * Canvas class provides the different means of drawing basic geometry and
	 * images on an Android SurfaceView object.
	 */
	@Override
	public synchronized void draw(Canvas c) {
		// Draw estimation
		if (hasData()) {
			float x_coord = x(dist[0]), x_coord_p = x_coord;
			float y_coord = y(est_c[0]);// , y_coord_p = y_coord;
			float y_mid = c_height / 2;
			p.setColor(Color.rgb(95, 95, 95));
			p.setStyle(Style.FILL);
			c.drawRect(origx, y_mid, x_coord, y_coord, p);
			for (int i = 1; i < est_c.length; i++) {// For each step, draw a
													// box.
				x_coord = x(dist[i]);
				y_coord = y(est_c[i]);

				c.drawRect(x_coord_p, y_mid, x_coord, y_coord, p);
				x_coord_p = x_coord;
				// y_coord_p = y_coord;
			}
		}
	}

	/**
	 * Rescales a y value to a y screen coordinate.
	 * 
	 * In this case, we will regard the middle of the screen as origo. We also
	 * use the value furthest away from 0 in order to make the scaling even on
	 * both sides.
	 * 
	 * On an android screen, the y scale grows downwards.
	 * 
	 * @param y
	 *            Y-value to rescale.
	 * @return
	 */
	private float y(double y) {
		double y_sum = Math.max(Math.abs(y_min), y_max);
		float rangedFraction = (float) ((y + y_sum) / (2 * y_sum));
		float range = (float) c_height - 2.0f * margin;
		return origy - rangedFraction * range;
	}

	/**
	 * Rescales a x value to a x screen coordinate.
	 * 
	 * On an Android screen, the x coordinate grows to the right.
	 * 
	 * @param x
	 *            X-value to rescale
	 * @return
	 */
	private float x(double x) {
		float rangedFraction = (float) (x / x_max);// 0.0 <= y <= 1.0
		float range = (float) c_width - 2.0f * margin;
		return rangedFraction * range + origx;
	}

	/**
	 * This method is called when the canvas used has changed been changed in
	 * some way.
	 */
	@Override
	public void updateDimensions(Canvas c) {
		c_width = c.getWidth();
		c_height = c.getHeight();
		 margin = 0.05f * (c.getWidth() < c.getHeight() ? c.getWidth() : c
		 .getHeight());
		//margin = 0;
		// origy = c_height - 2.0f * margin;
		// origx = 2.0f * margin;
		origy = c_height - margin;
		origx = margin;
	}

	/**
	 * Updates the current estimation values.
	 * 
	 * @param est
	 *            The new estimation values.
	 */
	public synchronized void updateEstimation(double[] est) {
		this.est_c = est;
		Step s = route.get(0);
		dist[0] = s.distance.value;
		y_min = y_max = est[0];
		for (int i = 1; i < route.size(); i++) {
			// Find maximum and minimum consumption estimations
			y_min = y_min < est[i] ? y_min : est[i];
			y_max = y_max > est[i] ? y_max : est[i];
		}
	}

	/**
	 * Updates the data set.
	 * 
	 * @param route
	 *            New route data.
	 * @param est
	 *            New estimation.
	 */
	public synchronized void updateData(List<Step> route, double[] est) {
		this.route = route;
		this.est_c = est;
		// Process a distance array for the estimation
		dist = new double[est.length];
		Step s = route.get(0);
		dist[0] = s.distance.value;
		y_min = y_max = est[0];
		for (int i = 1; i < route.size(); i++) {
			dist[i] += dist[i - 1] + route.get(i).distance.value;
			// Find maximum and minimum consumption estimations
			y_min = y_min < est[i] ? y_min : est[i];
			y_max = y_max > est[i] ? y_max : est[i];
		}
		x_max = dist[dist.length - 1];
	}

	/**
	 * @return True if we have both a route and an estimation.
	 */
	public boolean hasData() {
		return route != null && est_c != null;
	}

}