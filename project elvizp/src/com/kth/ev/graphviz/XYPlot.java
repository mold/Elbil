package com.kth.ev.graphviz;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;

/**
 * 
 * Class for rendering a Graph onto a given canvas.
 * 
 * @author marothon
 * 
 */
public class XYPlot implements CanvasRenderer {
	Path x_axis, y_axis;
	String x_label, y_label;
	int x_steps, y_steps;
	int label_frequency;
	int c_width, c_height;
	float label_stroke_width;
	float x_scale, y_scale;
	float margin;
	HashMap<String, DataPair> data_series;
	private float origy;
	private float origx;

	/**
	 * Constructor
	 */
	public XYPlot() {
		x_label = "Length of tour (km)";
		y_label = "Energy consumption rate per km (kWh/km)";
		x_steps = 10;
		y_steps = 10;
		x_scale = 100;
		y_scale = 100;
		label_frequency = 2;
		label_stroke_width = 5;
		margin = 20;
		data_series = new HashMap<String, DataPair>(2);
	}

	/**
	 * Constructor.
	 * 
	 * @param x_label
	 *            The label String for the x-axis.
	 * @param y_label
	 *            -||- y-axis.
	 * @param x_steps
	 *            Number of indents on the x-axis
	 * @param y_steps
	 *            -||- y-axis
	 * @param x_scale
	 *            The value-range of the x-axis which ( 0 -> x-scale )
	 * @param y_scale
	 *            -||- y-axis
	 * @param label_frequency
	 *            How frequent the indents on the axes appear
	 * @param label_stroke_width
	 *            How thick the label lines should be drawn.
	 */
	public XYPlot(String x_label, String y_label, int x_steps, int y_steps,
			float x_scale, float y_scale, int label_frequency,
			float label_stroke_width) {
		this.x_label = x_label;
		this.y_label = y_label;
		this.x_steps = x_steps;
		this.y_steps = y_steps;
		this.x_scale = x_scale;
		this.y_scale = y_scale;
		this.label_frequency = label_frequency;
		this.label_stroke_width = label_stroke_width;
		data_series = new HashMap<String, DataPair>(2);

	}
	

	/**
	 * Adds a data-set to the graph. Requires that the dataset is even, i.e.
	 * X.length == Y.length.
	 * 
	 * @param name
	 *            Name of the data-set.
	 * @param X
	 *            The x-axis values.
	 * @param Y
	 *            The y-axis values.
	 */
	public void add_data(String name, float[] X, float[] Y) {
		if (X.length != Y.length) {
			System.err
					.println("Error: Cannot add dataseries with inequal amount of elements.");
			return;
		}
		for (int i = 0; i < X.length; i++) {
			if (X[i] > x_scale) {
				x_scale = X[i] + X[i] * 0.25f;
			}
			if (Y[i] > y_scale) {
				y_scale = Y[i] + Y[i] * 0.25f;
			}
		}
		data_series.put(name, new DataPair(X, Y));
	}

	/**
	 * Same as other add_data, but provides the option to set the color of the
	 * plotted data right away.
	 * 
	 * @param name
	 *            Name of the data-set.
	 * @param X
	 *            The x-axis values.
	 * @param Y
	 *            The y-axis values.
	 * @param color
	 *            Color of the plot.
	 */
	public void add_data(String name, float[] X, float[] Y, int color) {
		add_data(name, X, Y);
		data_series.get(name).color = color;
	}

	public void add_data_point(String name, float x, float y) {
		DataPair dp = data_series.get(name);
		if (dp == null) {
			dp = new DataPair(new float[100], new float[100], 0);
			dp.color = Color.WHITE;
			data_series.put(name, dp);
		}
		if (x > x_scale) {
			x_scale = x + x * 0.25f;
		}
		if (y > y_scale) {
			y_scale = y + y * 0.25f;
		}
		dp.add(x, y);
	}

	/**
	 * Draws this XYPlot onto the given canvas.
	 * 
	 */
	public void draw(Canvas c) {
		drawAxes(c);
		drawData(c);
	}

	/**
	 * Draws the data saved in this plot onto the canvas.
	 * 
	 * @param c
	 */
	private void drawData(Canvas c) {
		Set<Entry<String, DataPair>> set = data_series.entrySet();

		for (Entry<String, DataPair> entry : set) {
			float[] X = entry.getValue().X;
			float[] Y = entry.getValue().Y;
			Path data_path = new Path();
			Path data_path2 = new Path();

			float x_coord = scaleXcoord(X[0]);
			float y_coord = scaleYcoord(Y[0]);
			data_path.moveTo(x_coord, y_coord);
			data_path2.moveTo(x_coord, y_coord);

			float prev_x = x_coord, prev_y = y_coord;
			for (int i = 1; i < entry.getValue().data_length; i++) {
				x_coord = scaleXcoord(X[i]);
				y_coord = scaleYcoord(Y[i]);
				//data_path.cubicTo(prev_x, (y_coord + prev_y) / 2.0f, x_coord,
				//		(y_coord + prev_y) / 2.0f, x_coord, y_coord);

				data_path.cubicTo((x_coord + prev_x) / 2.0f, prev_y, (x_coord + prev_x) / 2.0f,
						y_coord, x_coord, y_coord);
				// data_path.quadTo((x_coord+prev_x)/2.0f,
				// (y_coord+prev_y)/2.0f, x_coord, y_coord);
				// data_path2.lineTo(x_coord, y_coord);
				prev_x = x_coord;
				prev_y = y_coord;
			}
			Paint line = new Paint();
			line.setStyle(Style.STROKE);
			line.setColor(entry.getValue().color);
			c.drawPath(data_path, line);
			// line.setColor(Color.RED);
			// c.drawPath(data_path2, line);
		}
	}

	private float scaleYcoord(float y) {
		float rangedFraction = y / y_scale;// 0.0 <= y <= 1.0
		float range = (float) c_height - 3.0f * margin;
		return origy - rangedFraction * range;
	}

	private float scaleXcoord(float x) {
		float rangedFraction = x / x_scale;// 0.0 <= y <= 1.0
		float range = (float) c_width - 3 * margin;
		return rangedFraction * range + 2 * margin;
	}

	private void drawAxes(Canvas c) {
		if (x_axis == null) {
			x_axis = new Path();
			y_axis = new Path();

			x_axis.moveTo(origx, origy);
			x_axis.lineTo(c_width - margin, origy);

			y_axis.moveTo(origx, origy);
			y_axis.lineTo(origx, origy-(c_height-3.0f*margin));
		}

		Paint axis_paint = new Paint();
		axis_paint.setColor(Color.WHITE);

		float text_size = margin * 0.75f;
		axis_paint.setTextSize(text_size);
		axis_paint.setStyle(Style.FILL);
		axis_paint.setTextAlign(Align.CENTER);
		c.drawTextOnPath(x_label, x_axis, 0, 2.0f * margin, axis_paint);
		axis_paint.setTextAlign(Align.LEFT);
		c.drawTextOnPath(y_label, y_axis, 0, -2.0f * margin + text_size,
				axis_paint);
		axis_paint.setTextAlign(Align.CENTER);
		float x_size = c_width - 4.0f * margin;
		float fraction = x_size / (c_width - 3.0f * margin);
		float x_step = (x_size * fraction) / x_steps;
		float x_val = (float) (x_scale * fraction / x_steps);

		float y_size = c_height - 3.0f * margin;
		fraction = y_size / (c_height - 3.0f * margin);
		float y_step = (y_size * fraction) / y_steps;
		float y_val = (float) (y_scale / y_steps);

		float current_x = x_step + 2.0f * margin;
		float current_text = -x_size / 2.0f + x_step - text_size * 0.75f	;

		x_axis.moveTo(current_x, origy);

		float current_val = x_val;
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		for (int i = 0; i < x_steps; i++) {
			if ((i + 1) % label_frequency == 0) {
				x_axis.lineTo(current_x, origy-0.5f * margin);
				String val = df.format(current_val);
				c.drawTextOnPath(val, x_axis, current_text, margin, axis_paint);
			}
			current_val += x_val;
			current_x += x_step;
			current_text += x_step;
			x_axis.moveTo(current_x, origy);
		}
		float current_y = origy - y_step;

		float y_text = y_step-text_size/2.0f;
		Path helper_path = new Path();
		helper_path.moveTo(origx-2.0f*margin, origy);
		helper_path.lineTo(origx, origy);
		current_val = y_val;
		
		y_axis.moveTo(origx, current_y);
		for (int i = 0; i < y_steps; i++) {
			if ((i + 1) % label_frequency == 0) {
				y_axis.lineTo(origx+0.5f*margin, current_y);
				String val = df.format(current_val);
				c.drawTextOnPath(val, helper_path, 0, -y_text, axis_paint);
			}
			current_val += y_val;
			current_y -= y_step;
			y_text += y_step;
			y_axis.moveTo(origx, current_y);
		}
		axis_paint.setStyle(Style.STROKE);
		axis_paint.setStrokeWidth(label_stroke_width);
		c.drawPath(x_axis, axis_paint);
		c.drawPath(y_axis, axis_paint);
	}

	private static class DataPair {
		int data_length;
		float[] X;
		float[] Y;
		int color;

		DataPair(float[] X, float[] Y) {
			this.X = X;
			this.Y = Y;
			data_length = X.length;
			color = Color.GREEN;
		}

		DataPair(float[] X, float[] Y, int length) {
			this(X, Y);
			data_length = length;
		}

		public void add(float x, float y) {
			synchronized (this) {
				if (data_length >= X.length) {
					X = Arrays.copyOf(X, X.length * 2);
					Y = Arrays.copyOf(Y, Y.length * 2);
				}
				X[data_length] = x;
				Y[data_length] = y;
				data_length++;
			}
		}
	}

	@Override
	public void updateDimensions(Canvas c) {
		c_width = c.getWidth();
		c_height = c.getHeight();
		margin = 0.05f * (c.getWidth() < c.getHeight() ? c.getWidth() : c
				.getHeight());
		origy = c_height-2.0f * margin;
		origx = 2.0f*margin;
	}

	/**
	 * Removes all data and resets the scales.
	 */
	public void reset() {
		x_scale = 0;
		y_scale = 0;
		data_series.clear();
	}

}
