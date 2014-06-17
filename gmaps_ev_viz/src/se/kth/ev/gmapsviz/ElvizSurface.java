package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;

import com.kth.ev.differentiatedrange.CarData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface.OutOfResourcesException;

public class ElvizSurface extends SurfaceView implements
		SurfaceHolder.Callback, Observer {
	private PieChart pc;
	private EVGraph evg;

	public ElvizSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);

	}

	public ElvizSurface(Context context, AttributeSet set) {
		super(context, set);
		getHolder().addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		pc = new PieChart(20.0f);
		//evg = new EVGraph();
		evg = new EVGraph("km", "kWh/km", 5, 6, 0, 0, 1, 2.0f);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	private float[] graph_kWhPkm;
	private float[] graph_km;

	public void addEvData(CarData cd, RouteDataFetcher rdf) {
		double[] consumption = cd.determineConsumption(rdf.data);
		float[] data = new float[consumption.length];
		float[] data2 = new float[consumption.length];

		double xval = rdf.data.get(0).distance.value;
		for (int i = 0; i < consumption.length; i++) {
			data[i] = (float) (xval/1000.0f);
			data2[i] = (float) consumption[i]; 
			xval += rdf.data.get(i).distance.value;
		}
		graph_kWhPkm = data2;
		graph_km = data;

		evg.add_data("Consumption", graph_km, graph_kWhPkm);
	}
	
	public void redraw(){
		// Perform update to UI
		Canvas c = null;
		if(getHolder() == null || !getHolder().getSurface().isValid())
			return;
		try {
				c = getHolder().getSurface().lockCanvas(null);
				evg.draw(c);
		}catch(OutOfResourcesException e){
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null)
				getHolder().getSurface().unlockCanvasAndPost(c);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;

			// Perform update to UI
			Canvas c = null;
			if(getHolder() == null || !getHolder().getSurface().isValid())
				return;
			try {
				//synchronized (this) {
					c = getHolder().getSurface().lockCanvas(null);
					//float r = (float) cd.getSpeed(true) / 50.0f;
//					//Log.d("RADIUS", r + ", "+cd.getFan(true)+", "+cd.getSpeed(true));
					//pc.setRadius(r);
//					//pc.addSlice("FAN", (float) cd.getFan(true), Color.RED);
//					//pc.addSlice("CLIMATE", (float) cd.getClimate(true),
//					//		Color.RED);
//					pc.addSlice("WOOP", (float) 0.50f, Color.GREEN);
//					//pc.addSlice("SPEED", (float) cd.getSpeed(true), Color.GREEN);
					evg.draw(c);
//					pc.drawChart(c);
				//}
			}catch(OutOfResourcesException e){
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null)
					getHolder().getSurface().unlockCanvasAndPost(c);
			}
		}
	}
}
