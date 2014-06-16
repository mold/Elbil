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
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof CarData) {
			CarData cd = (CarData) observable;

			// Perform update to UI
			Canvas c = null;
			try {
				//synchronized (this) {
					c = getHolder().getSurface().lockCanvas(null);
					float r = (float) cd.getSoc(true) / 100.0f;
					//Log.d("RADIUS", r + ", "+cd.getFan(true)+", "+cd.getSpeed(true));
					pc.setRadius(r);
					//pc.addSlice("FAN", (float) cd.getFan(true), Color.RED);
					//pc.addSlice("CLIMATE", (float) cd.getClimate(true),
					//		Color.RED);
					pc.addSlice("WOOP", (float) 0.50f, Color.GREEN);

					pc.addSlice("SPEED", (float) cd.getSpeed(true), Color.GREEN);
					pc.drawChart(c);
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
