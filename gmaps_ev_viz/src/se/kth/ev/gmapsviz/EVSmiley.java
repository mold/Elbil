package se.kth.ev.gmapsviz;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;

public class EVSmiley {
	Color facecolor;
	float radius;
	float centerX;
	float centerY;
	
	public static class Mood{
		final static int HAPPY = 0, WORRIED = 1, SAD = 2, ANGRY = 3;
	}
	
	public void setMood(int mood){
		switch(mood){
		case Mood.HAPPY:
			happyFace();
			break;
		case Mood.WORRIED:
			worriedFace();
			break;
		case Mood.SAD:
			sadFace();
			break;
		case Mood.ANGRY:
			angryFace();
			break;
		}
	}
	
	public EVSmiley(){
		radius = 0.25f;
		centerX = 0.65f;
		centerY = 0.65f;
	}
	
	private void angryFace() {
		// TODO Auto-generated method stub
	}

	private void sadFace() {
		// TODO Auto-generated method stub
	}

	private void worriedFace() {
		// TODO Auto-generated method stub
	}

	private void happyFace() {
		// TODO Auto-generated method stub
	}

	public void draw(Canvas c){
		float radius = c.getWidth()*0.15f;
		float xCenter = this.centerX * c.getWidth();
		float yCenter = this.centerY * c.getHeight();
		RectF face = new RectF(xCenter-radius, yCenter-radius, xCenter+radius, yCenter+radius);
		RectF lEye = new RectF(xCenter+radius*0.5f, yCenter-radius*0.5f, xCenter+radius*0.3f, yCenter-radius*0.3f);
		RectF rEye = new RectF(xCenter+radius*0.5f, yCenter+radius*0.5f, xCenter+radius*0.3f, yCenter+radius*0.3f);

		RectF smile = new RectF(face);
		Paint facepaint = new Paint();
		smile.top += radius * 0.15;
		smile.bottom -= radius * 0.15f;
		smile.left += radius * 0.25f;
		facepaint.setStyle(Style.FILL);
		facepaint.setColor(Color.YELLOW);
		c.drawOval(face, facepaint);
		facepaint.setColor(Color.BLACK);
		c.drawOval(lEye, facepaint);
		c.drawOval(rEye, facepaint);
//		facepaint.setColor(Color.BLACK);
		facepaint.setStyle(Style.STROKE);
		facepaint.setStrokeWidth(2);
		c.drawArc(smile, 90, 180, false, facepaint);
	}
	
}
