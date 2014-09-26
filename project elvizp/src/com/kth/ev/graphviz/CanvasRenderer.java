package com.kth.ev.graphviz;

import android.graphics.Canvas;

/**
 * Interface for creating rendering classes 
 * to canvases.
 * 
 * @author marothon
 *
 */
public interface CanvasRenderer {

	/**
	 * Draws whatever onto the given canvas. The method may not 
	 * release or lock the canvas in any way.
	 * 	
	 * @param c The canvas to render onto.
	 */
	public void draw(Canvas c);
	
	/**
	 * Updates the internal dimensions of the renderer.
	 *  
	 * @param c The canvas to render onto.
	 */
	public void updateDimensions(Canvas c);
	

}
