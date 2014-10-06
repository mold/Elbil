package com.kth.ev.graphviz;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface.OutOfResourcesException;

public class CanvasSurface extends SurfaceView implements
		SurfaceHolder.Callback {
	@SuppressWarnings("unused")
	private static final String TAG = "CanvasSurface";
	List<CanvasRenderer> crl;

	public CanvasSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		crl = new ArrayList<CanvasRenderer>();
	}

	public CanvasSurface(Context context, AttributeSet set) {
		super(context, set);
		crl = new ArrayList<CanvasRenderer>();
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

	public synchronized void addRenderer(CanvasRenderer c) {
		if (!crl.contains(c)) {
			crl.add(c);
			updateDimensions(getHolder());
		}
	}

	public synchronized void redraw() {
		// Perform update to UI
		Canvas c = null;
		// Log.d("ElvizSurface",
		// "Is holder null? "+(getHolder()==null)+". Is the surface valid? "+getHolder().getSurface().isValid());
		if (getHolder() == null || !getHolder().getSurface().isValid())
			return;
		try {
			c = getHolder().getSurface().lockCanvas(null);
			c.drawColor(Color.BLACK);
			for (CanvasRenderer cr : crl)
				cr.draw(c);

		} catch (OutOfResourcesException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null)
				getHolder().getSurface().unlockCanvasAndPost(c);
		}
	}

	/**
	 * Internal method to update the dimensions for each CanvasRenderer.
	 * 
	 * @param holder
	 */
	private void updateDimensions(SurfaceHolder holder) {
		Canvas c = null;
		if (!holder.getSurface().isValid())
			return;
		try {
			c = holder.lockCanvas(null);
			for (CanvasRenderer cr : crl)
				cr.updateDimensions(c);

		} catch (OutOfResourcesException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null)
				getHolder().getSurface().unlockCanvasAndPost(c);
		}
	}

}
