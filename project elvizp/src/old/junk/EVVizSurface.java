package old.junk;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface.OutOfResourcesException;

public class EVVizSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	List<CanvasRenderer> crl;
	
	public EVVizSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);

	}

	public EVVizSurface(Context context, AttributeSet set) {
		super(context, set);
		getHolder().addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		updateDimensions(holder);
		redraw();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		updateDimensions(holder);
		redraw();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	
	public void addRenderer(CanvasRenderer c){
		if(crl == null){
			crl = new ArrayList<CanvasRenderer>();
		}
		crl.add(c);
	}
	
	public void addRendererList(List<CanvasRenderer> l){
		if(crl == null)
			crl = l;
		else
			crl.addAll(l);
	}
	
	public void redraw(){
		// Perform update to UI
		Canvas c = null;
		//Log.d("ElvizSurface", "Is holder null? "+(getHolder()==null)+". Is the surface valid? "+getHolder().getSurface().isValid());
		if(getHolder() == null || !getHolder().getSurface().isValid())
			return;
		try {
				c = getHolder().getSurface().lockCanvas(null);
				Log.d("ElvizSurface", "DOODLE!");
				c.drawColor(Color.BLACK);
				for(CanvasRenderer cr : crl)
					cr.draw(c);
				
		}catch(OutOfResourcesException e){
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null)
				getHolder().getSurface().unlockCanvasAndPost(c);
		}
	}

	/**
	 * Internal method to update the dimensions for each canvasrenderer.
	 * @param holder
	 */
	private void updateDimensions(SurfaceHolder holder) {
		Canvas c = null;
		if(!holder.getSurface().isValid())
			return;
		try {
				c = holder.lockCanvas(null);
				for(CanvasRenderer cr : crl)
					cr.updateDimensions(c);
				
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
