package com.kth.ev.vizsample;

import com.kth.ev.graphviz.CanvasRenderer;
import com.kth.ev.graphviz.CanvasSurface;

/**
 * An instance of this class will refresh a given canvas at a set amount of
 * time.
 * 
 * @author marothon
 * 
 */
class Drawer implements Runnable {
	private int refresh_rate;
	private boolean draw;
	private CanvasSurface canvas;
	private Thread t;

	public Drawer(int refresh_rate, CanvasSurface canvas) {
		this.refresh_rate = refresh_rate;
		this.canvas = canvas;
		draw = true;
	}

	public void addViz(CanvasRenderer cr) {
		canvas.addRenderer(cr);
	}

	@Override
	public void run() {
		while (draw) {
			synchronized (this) {
				canvas.redraw();
			}
			try {
				Thread.sleep(refresh_rate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawOnce() {
		if (t == null)
			canvas.redraw();
	}

	public void stopDrawing() {
		draw = false;
		t.interrupt();
	}

	public void startDrawing() {
		draw = true;
		t = new Thread(this, "DrawerThread for " + this);
		t.start();
	}

	public synchronized void changeSurface(CanvasSurface canvasSurface) {
		canvas = canvasSurface;
	}

}