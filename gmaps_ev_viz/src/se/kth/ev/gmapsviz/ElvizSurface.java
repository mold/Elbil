package se.kth.ev.gmapsviz;

import java.util.Observable;
import java.util.Observer;

import com.kth.ev.differentiatedrange.CarData;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ElvizSurface extends SurfaceView implements SurfaceHolder.Callback, Observer {
	SurfaceHolder sh;
	
	public ElvizSurface(Context context) {
		super(context);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		sh = holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		sh = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof CarData){
			CarData cd = (CarData) observable;
			
			//Perform update to UI
			Canvas s;
			try {
				s = sh.getSurface().lockCanvas(null);
				
				//Draw things here
				
				sh.getSurface().unlockCanvasAndPost(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
