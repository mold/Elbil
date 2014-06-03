package com.kth.ev.differentiatedrange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DiffRangeSurfaceView extends SurfaceView implements Runnable {
	Thread t = null;
	SurfaceHolder holder;
	boolean running = false;
	
	float soc = (float) 0.0;
	float socTmp = 0;
	float socDx = (float) 0.05;
	float speed = 0;
	float speedTmp = 0;
	float speedDx =(float)0.1;
	float[] rangeArray = new float[17]; //0 is current
	float[] rangeMaxArray = new float[16]; //0 is current
	float[] rangeTmpArray = new float[16];
	float rangeDx = (float) 0.1;
	float speedOneMinMean = (float) 0.0;
	float speedFiveMinMean = (float) 0.0;
	float speed10SecMean = (float) 0.0;
	
	float speedOneMinMeanTmp = (float) 0.0;
	float speedFiveMinMeanTmp = (float) 0.0;
	float speed10SecMeanTmp = (float) 0.0;
	
	float currentClimateConsumption = (float)3.0;
	
	float topGraphMargin = (float)85.0;
	float bottommargin = (float)65.0;
	
	int fan = 112;
	int climate = 7;
	

	//HeaterSetting heaterSetting = HeaterSetting.LEVEL3;
	ZoomMode zoomMode = ZoomMode.DETAIL;	
	
	Paint maxBar, tmpBar, txt,speedBar;
	
	Typeface font;
	
	Bitmap meterbg, meterarrow, overlay, speedneedle, speedometer,underlay,purplearrow,bluearrow,battery;
	
	public DiffRangeSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		holder=getHolder();
		meterarrow = BitmapFactory.decodeResource(getResources(), R.drawable.meterall340);
		meterbg = BitmapFactory.decodeResource(getResources(), R.drawable.meterbg340);
		speedneedle = BitmapFactory.decodeResource(getResources(), R.drawable.speedneedleh);
		speedneedle = BitmapFactory.decodeResource(getResources(), R.drawable.speedarrowpink);
		speedometer = BitmapFactory.decodeResource(getResources(), R.drawable.speedometer);
		//overlay = BitmapFactory.decodeResource(getResources(), R.drawable.overlay);
		overlay = BitmapFactory.decodeResource(getResources(), R.drawable.overlay3);
		underlay = BitmapFactory.decodeResource(getResources(), R.drawable.texturewhitedirt);
		purplearrow = BitmapFactory.decodeResource(getResources(), R.drawable.purplearrow);
		bluearrow = BitmapFactory.decodeResource(getResources(), R.drawable.bluearrow);
		battery = BitmapFactory.decodeResource(getResources(), R.drawable.battery);
		
		maxBar = new Paint();
		maxBar.setARGB(150, 150, 150, 150);
		maxBar.setStyle(Paint.Style.FILL);

		tmpBar = new Paint();
		tmpBar.setARGB(255, 100, 180, 90);
		tmpBar.setStyle(Paint.Style.FILL);
		
		txt = new Paint();
		txt.setARGB(255, 198, 156, 109);
		txt.setStyle(Paint.Style.FILL);
		
		speedBar = new Paint();
		speedBar.setARGB(255, 221, 166, 166);
		//speedBar.setARGB(200, 198, 156, 109);
		speedBar.setStyle(Paint.Style.FILL);
		
		

	}
	
	public void run() {
		while (running){
			
			if(holder ==null || !holder.getSurface().isValid()){
				continue;
			}
			//if(holder==null)
			//	return;
			//draw our stuff
			Canvas c = null;
			//draw(c);

			//holder.unlockCanvasAndPost(c);
			
			
			try {
                c = holder.lockCanvas();
                synchronized (holder) {
                	Update();
                	if (c != null)
                		draw(c);
                	
                    
                }                                   
            } 

            finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
            	if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
		}
	}
	
	//Update the variables
	public void Update(){
		//SocTmp
		socTmp += (soc-socTmp)*socDx;
		//SpeedTmp
		speedTmp += (speed-speedTmp)*speedDx;
		
		//meanSpeed values
		speedOneMinMeanTmp += (speedOneMinMean-speedOneMinMeanTmp)*speedDx;
		speedFiveMinMeanTmp += (speedFiveMinMean-speedFiveMinMeanTmp)*speedDx;
		speed10SecMeanTmp += (speed10SecMean-speed10SecMeanTmp)*speedDx;
		
		//Recaclulate distances
		for (int i = 0; i < 15; i++) {
			rangeTmpArray[i] += (rangeArray[i]-rangeTmpArray[i])*rangeDx;
		}
		
		//update climate power
		climatePower();
	}
	
	public void climatePower()
	{
		//heater1=112 -> fan off -> no power
	    //113-120 fan on levels 120 maximum
		float fanImpact = (float)0.0; 
		
		if(fan>=113 && fan <=120)
			fanImpact = (float)1.0-((float)120-(float)fan)/(float)7.0; 
		if(fan>=81 && fan <=88)
			fanImpact = (float)1.0-((float)88-(float)fan)/(float)7.0; 
		if(fan>=97 && fan <=103)
			fanImpact = (float)1.0-((float)103-(float)fan)/(float)7.0; 
			
		try
		{
		//if(fan!=112)
		if(fanImpact>=0.0)
		{
			//float fanImpact = (float)1.0-((float)120-(float)fan)/(float)7.0; // 0-1kW, i don't know about this
			//Log.i("CLIMATEfi", Float.toString(fanImpact));
			//8-13 heater on
			if(climate==129 || (climate >=103 && climate <=109)) //103 pushmax heat
			{
				currentClimateConsumption = (float)3.5+fanImpact;
				return;
			}
			if((climate>=65 && climate<=71) || climate==1)
			{
				currentClimateConsumption = fanImpact;
				return;
			}
			if(climate >=8 && climate <=13)
			{
				Log.i("CLIMATEhe", Float.toString((float)(((float)climate-(float)7.0)/(float)6.0)*(float)3.0));
				currentClimateConsumption = (float)((((float)climate-(float)7.0)/(float)6.0)*(float)3.0)+fanImpact;
				return;
				
			}
			
			if(climate >=72 && climate <=77)
			{
				Log.i("CLIMATEhe", Float.toString((float)(((float)climate-(float)72.0)/(float)6.0)*(float)3.0));
				currentClimateConsumption = (float)((((float)climate-(float)72.0)/(float)6.0)*(float)3.0)+fanImpact;
				return;
				
			}
			
			if(climate >=136 && climate <=141)
			{
				Log.i("CLIMATEhe", Float.toString((float)(((float)climate-(float)136.0)/(float)6.0)*(float)3.0));
				currentClimateConsumption = (float)((((float)climate-(float)136.0)/(float)6.0)*(float)3.0)+fanImpact;
				return;
				
			}
			if(climate >=200 && climate <=205)
			{
				//Log.i("CLIMATEhe", Float.toString((float)(((float)climate-(float)200.0)/(float)6.0)*(float)3.0));
				currentClimateConsumption = (float)((((float)climate-(float)200.0)/(float)6.0)*(float)3.0)+fanImpact;
				return;
				
			}

			//2-6 coolAC off
			if((climate >=2 && climate <=7) || climate ==135 || climate==199)
			{
				currentClimateConsumption = fanImpact;
				return;
			}
			//2-6 coolAC off
			//130-134 AC on
			if(climate >=130 && climate <=134)
			{
				currentClimateConsumption = (float)((((float)134.0-(float)climate)/(float)6.0)*(float)3.2)+fanImpact;
				return;
			}
			
			if(climate>=193 && climate<=198)
			{
				currentClimateConsumption = (float)((((float)198.0-(float)climate)/(float)6.0)*(float)3.0)+fanImpact;
				return;
			}

			

		}
		}
		catch(Exception e)
		{
			return;
		}
		

		currentClimateConsumption =  (float)-10.0;
		return;

	}
	
	public void draw(Canvas c){
		//Background
		
		Paint p = new Paint();
		//Set Blue Color
		p.setColor(Color.WHITE);
		
		//Touchscreen boundaries.
		RectF fullCanvas = new RectF(0, 0, c.getWidth(), c.getHeight());
		c.drawBitmap(underlay,null, fullCanvas, p);
		
		c.drawARGB(230, 35, 20, 21);
		//speedometer background
		RectF xLabel = new RectF(fullCanvas);
		Log.d("CANVAS", xLabel.top + " "+xLabel.bottom);
		xLabel.top = xLabel.bottom-75.0f;
		c.drawBitmap(speedometer,null, xLabel, new Paint());
		
		c.drawBitmap(battery,30, 31, new Paint());
		
		p = new Paint();
		p.setARGB(255, 100, 180, 90);
		p.setStyle(Paint.Style.FILL);
			
		c.drawBitmap(speedneedle,(int)(speedTmp*(((float)c.getWidth()-64.0)/140.0)-(float)speedneedle.getWidth()/2.0+34.0), c.getHeight()-bottommargin+22, new Paint());
		
		//5 sec mean
		//c.drawBitmap(speedneedle,(int)(speed10SecMean*((c.getWidth()-64)/140)-speedneedle.getWidth()/2+34), c.getHeight()/2-20, new Paint());
		//10 sec mean
		p.setAlpha(200);
		//c.drawBitmap(bluearrow,(int)(speed10SecMeanTmp*(((float)c.getWidth()-64.0)/140.0)-(float)bluearrow.getWidth()/2.0+32.0), c.getHeight()/2-20, p);
		
		//1 min mean
		//c.drawBitmap(purplearrow,(int)(speedOneMinMeanTmp*(((float)c.getWidth()-64.0)/140.0)-(float)purplearrow.getWidth()/2.0+32.0), c.getHeight()/2-20, p);
		
		//draw SOC meter
		
		//c.drawBitmap(meterbg,c.getWidth()-meterbg.getWidth()+35, c.getHeight()-meterbg.getHeight()/2+35, new Paint());
		
		//c.drawBitmap(RotateBitmap(meterarrow,1),c.getWidth()-meterarrow.getWidth(), c.getHeight()-meterarrow.getHeight()/2, new Paint());
		
		p.setTextSize(55);
		p.setTextAlign(Align.LEFT);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		//p.setARGB(255, 100, 180, 90);
		//p.setARGB(255, 221, 166, 166);
		//p.setARGB(255, 76, 53, 53);
		p.setARGB(255, 240, 240, 240);		
		c.drawText(String.format("%.1f", soc*100) + "%", 78, 70, p);
		
		//Matrix matrix = new Matrix();
	    //matrix.setTranslate(c.getWidth()-meterarrow.getWidth()+35, c.getHeight()-meterarrow.getHeight()/2+35);
	    //matrix.preRotate(-35+(socTmp*70), meterarrow.getWidth()/2, meterarrow.getHeight()/2);
		//c.drawBitmap(meterarrow, matrix, new Paint());
		//Graph
		
		//Log.d("CURRANGE", Float.toString(rangeTmpArray[0]));

		//Draw 10km lines in background
		//c.getHeight()/2-(rangeTmpArray[i]/rangeArray[15])*c.getHeight()/2)-20
		
		//int numberOfLines = (int) (rangeArray[15]/10000);
		if(rangeArray[16] >= 20000)
			zoomMode = ZoomMode.OVERVIEW;
		else
			zoomMode = ZoomMode.DETAIL;
		
		float maxYValue = rangeArray[15];
		float mPerLine = (float)10000.0;
		switch (zoomMode) {
		case OVERVIEW:
			//int numberOfLines = (int) (rangeArray[15]/10000);
			break;
		case DETAIL:
			//maxYValue = rangeArray[16];
			maxYValue = 20000;
			mPerLine = (float)1000.0;
			break;
		default:
			break;
		}
		
		int numberOfLines = (int) (maxYValue/mPerLine);

		for (int i = 0; i <= numberOfLines; i++) {
			Rect rg = new Rect();
			float h = (float) ((float)c.getHeight()-bottommargin-((float)i*mPerLine/maxYValue)*(((float)c.getHeight()-bottommargin-topGraphMargin-20.0)))-(float)20.0;
			rg.set(0,(int) h,c.getWidth(),(int) h-1);
			p = new Paint();
			p.setTextSize(24);
			p.setARGB(255, 198, 156, 109);
			p.setTextAlign(Align.RIGHT);
			p.setTypeface(font); 
			p.setTypeface(Typeface.DEFAULT_BOLD);
			
			if(i%10==0) {
				p.setARGB(255, 200, 180, 120);
				if(i!=0)
					c.drawText(Integer.toString(i*(int)mPerLine/1000) + "km", c.getWidth()-20, h+20, p);
			} else if(i%5==0) {
				p.setARGB(255, 100, 180, 90);
				c.drawText(Integer.toString(i*(int)mPerLine/1000) + "km", c.getWidth()-20, h+20, p);
			} else
				p.setARGB(100, 100, 180, 90);
			
			p.setStyle(Paint.Style.FILL);
			c.drawRect(rg, p);
			
			//c.drawText(Integer.toString(i*) + "km", 40, c.getHeight()/2+110, p);
		}
		
		for (int i = 1; i < 14; i++) {
			Rect rg = new Rect();
			int xpos = (int)((float)i*10*(((float)c.getWidth()-73.0)/140.0)+28.0);
			rg.set(xpos,0,xpos+10,c.getHeight()-(int)bottommargin-20);
			
			//Draw maximum
			rg.top = (int) ((float)c.getHeight()-bottommargin-(rangeMaxArray[i]/maxYValue)*(((float)c.getHeight()-bottommargin-topGraphMargin-20.0)))-20;
			c.drawRect(rg, maxBar);
			
			//Draw current
			rg.top = (int) ((float)c.getHeight()-bottommargin-(rangeTmpArray[i]/maxYValue)*(((float)c.getHeight()-bottommargin-topGraphMargin-20.0)))-20;
			c.drawRect(rg, tmpBar);
		}
		
		Rect rg = new Rect();
		int xpos = (int)(speedTmp*((float)((float)c.getWidth()-73.0)/140.0)+28.0);
		int h= (int) ((float)c.getHeight()-bottommargin-(rangeTmpArray[0]/maxYValue)*(((float)c.getHeight()-bottommargin-topGraphMargin-20.0)))-20;
		rg.set(xpos,h,xpos+10,c.getHeight()-(int)bottommargin-20);
		c.drawRect(rg, speedBar);
		
		//Print distance for current speed
		p.setTextSize(55);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		p.setTextAlign(Align.RIGHT);
		c.drawText(String.format("%.0f", rangeTmpArray[0]/1000.0) + " km", c.getWidth()-20, 70, p);
		
		//Draw details (text)
		//paint.setColor(textColor); 
		//p.setARGB(155, 198, 156, 109);
		//p.setTextSize(52);
		
		//Typeface myTypeface = Typeface.createFromAsset(getAsset, path), "fonts/Clockopia.ttf");
		//p.setTypeface(font); 
		//p.setTextAlign(Align.CENTER);
		//p.setTypeface(Typeface.DEFAULT_BOLD);
		//c.drawText(String.format("%.0f", speed) + " km/h", 280, c.getHeight()-110, p);
		//c.getWidth()-20, 70
		//c.drawText("Heater: "+heaterSetting.ordinal(), 280, c.getHeight()-50, p);
		//c.drawText("Heater: "+heaterSetting.ordinal(), c.getWidth()/2, 70, p);
		//p.setTextSize(35);
		//p.setTextAlign(Align.CENTER);
		//p.setARGB(255, 100, 180, 90);
		//p.setARGB(255, 221, 166, 166);
		//p.setARGB(255, 76, 53, 53);
		//c.drawText(String.format("%.0f", soc*100) + "%", c.getWidth()-meterbg.getWidth()/2+35, c.getHeight()/2+152, p);
		//c.drawText("Range: " + String.format("%.0f", rangeTmpArray[0]/1000) +"km" , 40, c.getHeight()/2+190, p);
		
		//String.format("%.0f", speed);
		//Overlay
		c.drawBitmap(overlay, null, fullCanvas, new Paint());
		
		//Take it easy a bit
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle, source.getWidth()/2,source.getHeight()/2 );
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	


	public void resume() {
		running = true;
		t = new Thread(this);
		t.start();
	}
	
	public void pause() {
		running = false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			try{
				t.join();
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
			break;
		}
		t=null;
	}
	
	//@Override
	//public boolean onTouchEvent(MotionEvent event) {
//		switch (heaterSetting) {
//		case NONE:
//			heaterSetting = HeaterSetting.LEVEL1;
//			currentClimateConsumption = (float)1.0;
//			break;
//		case LEVEL1:
//			heaterSetting = HeaterSetting.LEVEL2;
//			currentClimateConsumption = (float)2.0;
//			break;
//		case LEVEL2:
//			heaterSetting = HeaterSetting.LEVEL3;
//			currentClimateConsumption = (float)3.0;
//			break;
//		case LEVEL3:
//			heaterSetting = HeaterSetting.LEVEL4;
//			currentClimateConsumption = (float)4.0;
//			break;
//		case LEVEL4:
//			heaterSetting = HeaterSetting.NONE;
//			currentClimateConsumption = (float)0.0;
//			break;
//		default:
//			heaterSetting = HeaterSetting.NONE;
//			currentClimateConsumption = (float)0.0;
//			break;
//		}
	    //return super.onTouchEvent(event);
	//}
	//public enum HeaterSetting
	//{
	//	NONE,
	//	LEVEL1,
	//	LEVEL2,
	//	LEVEL3,
	//	LEVEL4
	//}
	
	public enum ZoomMode
	{
		OVERVIEW,
		DETAIL
	}
}



