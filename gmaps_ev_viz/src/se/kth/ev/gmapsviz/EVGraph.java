package se.kth.ev.gmapsviz;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

public class EVGraph {
	EVSmiley smiley;
	Path x_axis, y_axis;
	String x_label, y_label;
	int x_steps, y_steps;
	int label_frequency;
	float label_stroke_width;
	float x_scale, y_scale;
	float margin = 20;
	HashMap<String, DataPair> data_series;

	public EVGraph() {
		x_label = "Length of tour (km)";
		y_label = "Energy consumption rate per km (kWh/km)";
		x_steps = 10;
		y_steps = 10;
		x_scale = 100;
		y_scale = 100;
		label_frequency = 2;
		label_stroke_width = 5;
		data_series = new HashMap<String,DataPair>(2);
		smiley = new EVSmiley();
	}
	
	private static class DataPair{
		float[] X;
		float[] Y;
		
		DataPair(float[] X, float[] Y){
			this.X = X;
			this.Y = Y;
		}
	}
	
	public EVGraph(String x_label, String y_label, int x_steps, int y_steps, float x_scale, float y_scale, int label_frequency, float label_stroke_width){
		this.x_label = x_label;
		this.y_label = y_label;
		this.x_steps = x_steps;
		this.y_steps = y_steps;
		this.x_scale = x_scale;
		this.y_scale = y_scale;
		this.label_frequency = label_frequency;
		this.label_stroke_width = label_stroke_width;
		data_series = new HashMap<String,DataPair>(2);
		smiley = new EVSmiley();

	}
	
	public void add_data(String name, float[] X, float[] Y){
		if(X.length != Y.length){
			System.err.println("Error: Cannot add dataseries with inequal amount of elements.");
			return;
		}
		for(int i=0; i<X.length; i++){
			if(X[i] > x_scale){
				x_scale = X[i] + X[i]*0.25f;
			}
			if(Y[i] > y_scale){
				y_scale = Y[i]+ Y[i]*0.25f;
			}
		}
		data_series.put(name, new DataPair(X, Y));
	}


	public void draw(Canvas c) {
		drawAxes(c);
		drawData(c);
		smiley.draw(c);
	}
	
	private void drawData(Canvas c){
		Set<Entry<String, DataPair>> set = data_series.entrySet();
		
		for(Entry<String, DataPair> entry : set){
			float[] X= entry.getValue().X;
			float[] Y= entry.getValue().Y;
			Path data_path = new Path();
			Path data_path2 = new Path();
			
			data_path.moveTo(2.0f*margin, 2.0f*margin);
			data_path2.moveTo(2.0f*margin, 2.0f*margin);

			float prev_x = 2.0f*margin, prev_y = 2.0f*margin;
			for(int i=0; i<entry.getValue().X.length; i++){
				float x = X[i];
				float y = Y[i];
				float x_coord = scaleYcoord(y, c);
				float y_coord = scaleXcoord(x, c);
				data_path.cubicTo(prev_x, (y_coord+prev_y)/2.0f, x_coord, (y_coord+prev_y)/2.0f, x_coord, y_coord);
				//data_path.quadTo((x_coord+prev_x)/2.0f, (y_coord+prev_y)/2.0f, x_coord, y_coord);
				data_path2.lineTo(x_coord, y_coord);
				prev_x = x_coord;
				prev_y = y_coord;
			}
			Paint line = new Paint();
			line.setStyle(Style.STROKE);
			line.setColor(Color.GREEN);
			c.drawPath(data_path, line);
			//line.setColor(Color.RED);
			//c.drawPath(data_path2, line);
		}
	}
	
	private float scaleYcoord(float y, Canvas c) {
		float rangedFraction = y / y_scale;//0.0 <= y <= 1.0
		float range = c.getWidth() - 3*margin;
		return rangedFraction*range + 2*margin;
	}

	private float scaleXcoord(float x, Canvas c) {
		float rangedFraction = x / x_scale;//0.0 <= y <= 1.0
		float range = c.getHeight() - 3*margin;
		return rangedFraction*range + 2*margin;
	}

	private void drawAxes(Canvas c){
		x_axis = new Path();
		y_axis = new Path();

		x_axis.moveTo(2.0f * margin, 2.0f * margin);
		x_axis.lineTo(2.0f * margin, c.getHeight() - margin);

		y_axis.moveTo(2.0f * margin, 2.0f * margin);
		y_axis.lineTo(c.getWidth() - margin, 2.0f * margin);

		Paint axis_paint = new Paint();
		axis_paint.setColor(Color.WHITE);

		float text_size = margin * 0.75f;
		axis_paint.setTextSize(text_size);
		axis_paint.setStyle(Style.FILL);
		axis_paint.setTextAlign(Align.CENTER);
		c.drawTextOnPath(x_label, x_axis, 0, 2.0f * margin, axis_paint);
		c.drawTextOnPath(y_label, y_axis, 0, -2.0f * margin + text_size,
				axis_paint);

		float x_size = c.getHeight() - 3.0f * margin;
		float x_step = (x_size - label_stroke_width) / x_steps;
		float x_val = (float) (x_scale / x_steps);

		float y_size = c.getWidth() - 3.0f * margin;
		float y_step = (y_size - label_stroke_width) / y_steps;
		float y_val = (float) (y_scale / y_steps);

		// float current_x = x_step+2.0f*margin;
		float current_x = x_step + 2.0f * margin;
		float current_text = -x_size / 2.0f + x_step;

		x_axis.moveTo(2.0f * margin, current_x);

		float current_val = x_val;
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		for (int i = 0; i < x_steps; i++) {
			if ((i+1) % label_frequency == 0) {
				x_axis.lineTo(2.5f * margin, current_x);
				String val = df.format(current_val);
				c.drawTextOnPath(val, x_axis, current_text,
						margin, axis_paint);
			}
			current_val += x_val;
			current_x += x_step;
			current_text += x_step;
			x_axis.moveTo(2.0f * margin, current_x);
		}
		float current_y = y_step + 2.0f*margin;;
		float y_text = y_step;
		current_text = -x_size / 2.0f + 1.5f * margin;
		current_val = y_val;
		y_axis.moveTo(current_y, 2.0f * margin);
		for (int i = 0; i < y_steps; i++) {
			if ((i+1) % label_frequency == 0) {
				y_axis.lineTo(current_y, 2.5f * margin);
				String val = df.format(current_val);
				c.drawTextOnPath(val, x_axis, current_text,
						-y_text, axis_paint);
			}
			current_val += y_val;
			current_y += y_step;
			y_text += y_step;
			y_axis.moveTo(current_y, 2.0f * margin);
		}
		axis_paint.setStyle(Style.STROKE);
		axis_paint.setStrokeWidth(label_stroke_width);
		c.drawPath(x_axis, axis_paint);
		c.drawPath(y_axis, axis_paint);
	}

}
