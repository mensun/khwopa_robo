package com.example.robotcontroller;


import com.example.game1.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.util.Log;

public class MainView extends View implements SensorEventListener, OnTouchListener{
		
	private Bitmap accelBmp;
	private Bitmap brakeBmp;
	private Bitmap rotateclockBmp;
	private Bitmap rotateantiBmp;
	private Bitmap leftrightBmp;
	
	private boolean forward;
	private boolean reverse;
	private boolean brake;
	
	private boolean left;
	private boolean right;
	
	

	
	float leftright;
	
	float[] tx = new float[2];
	float[] ty = new float[2];
	boolean[] touched = new boolean[2];
	int[] tid = new int[2];
	
	

	private tcpclient client;

	
	public MainView(Context context) {
		super(context);
		
		//Load BitMap Resources		
		accelBmp=BitmapFactory.decodeResource(getResources(), R.drawable.up);
		brakeBmp=BitmapFactory.decodeResource(getResources(), R.drawable.brake);
		rotateclockBmp=BitmapFactory.decodeResource(getResources(), R.drawable.rotateclock);
		rotateantiBmp=BitmapFactory.decodeResource(getResources(), R.drawable.rotateanti);
		leftrightBmp= BitmapFactory.decodeResource(getResources(), R.drawable.leftright);
		
		SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
            Log.d("ACCEL","No accelerometer installed");
        } else {
            Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            if (!manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
                Log.d("ACCEL","Couldn't register sensor listener");
            }
        }
        
        this.setOnTouchListener(this);
        
        client = new tcpclient();
        new Thread(client).start();
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		canvas.drawColor(Color.BLACK);
		canvas.drawBitmap(accelBmp,20,10,null);
		canvas.drawBitmap(brakeBmp,getWidth()-brakeBmp.getWidth()-20,20,null);
		canvas.drawBitmap(rotateclockBmp, getWidth()-rotateclockBmp.getWidth()-20,getHeight()/2-rotateclockBmp.getHeight()/2 , null);
		canvas.drawBitmap(rotateantiBmp, getWidth()-rotateantiBmp.getWidth()*2-80,getHeight()/2-rotateantiBmp.getHeight()/2 , null);
		canvas.drawBitmap(leftrightBmp, getWidth()-leftrightBmp.getWidth()-20,getHeight()-leftrightBmp.getHeight() , null);
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) { //for accelerometer
		final float alpha = (float) 0.8;
		float gravity=(float) 0.0;
		gravity=alpha*gravity+(1-alpha)*event.values[1];
		leftright = event.values[1]-gravity;
		
		/*if(touched[0]==true || touched[1] ==true)
		{
		   	if(leftright>2)
        	{
        		int c=(int)map(leftright,2f,5.0f,0.0f,10.0f) +30;
        		if(c<=40)
        		{
        			client.setCommand((byte)c);
        			Log.d("ACCEL","Right"+String.valueOf(c));
        			right=true;
        			left=false;
        		}
        	}
        	else if(leftright<-2)
        	{
        		int d=(int)map(leftright,-2f,-5.0f,0.0f,10.0f) + 40;
        		if(d<=50)
        		{
        			right=false;
        			left=true;
        			client.setCommand((byte)d);
        			Log.d("ACCEL","Left"+String.valueOf(d));
        		}
        	
        	}
        	else
        	{
        		left=false;
        		right=false;
        		client.setCommand((byte)0x04);
        	}
		} 
		
		else
		{
			left=false;
    		right=false;
    		client.setCommand((byte)0x04);
		}*/
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerCount = event.getPointerCount();
		
		
		for (int i = 0; i < 2; i++) {
			if (i >= pointerCount) {
				touched[i] = false;
				tid[i] = -1;
				continue;
			}
			if (event.getAction() != MotionEvent.ACTION_MOVE && i != pointerIndex) {
				// if it's an up/down/cancel/out event, mask the id to see if we should process it for this touch point
				continue;
			}
	
			if(touched[i]==false)
			{
				client.setCommand((byte)0x00);
			}
			
			if(touched[i]==true && inBounds((int)tx[i],(int)ty[i],getWidth()-brakeBmp.getWidth()-20,0,brakeBmp.getWidth(),brakeBmp.getHeight()))
			{
				Log.d("TOUCH","touched for brake");
				
				client.setCommand((byte)0x03);
				brake=true;
			}
			else
				brake=false;
			
			if(brake==false)
			{
				if(touched[i]==true && inBounds((int)tx[i],(int)ty[i],0,0,30+accelBmp.getWidth(),getHeight()))
				{
					
					if(ty[i]>getHeight()/2 -10 && ty[i]<getHeight()/2+10)
					{
						//Log.d("TOUCH","Speed 0");
						client.setCommand((byte)0x00);
						forward=false;
						reverse=false;
						left=false;
						right=false;
					}
					
					if(ty[i]>0 && ty[i]<getHeight()/2-10)
					{	
						forward=true;
						reverse=false;
						if(left==false || right==false)
						{
						int a = (int) map((int)ty[i],0,getHeight()/2-10,10,0) + 10;	
						Log.d("TOUCH","Speed forward"+String.valueOf(a));
						
						client.setCommand((byte)a);
						}
					}
					
					if(ty[i]>getHeight()/2 + 10 && ty[i]<getHeight())
					{
						forward=false;
						reverse=true;
						if(left==false || right==false)
						{
						int b = (int) map((int)ty[i],getHeight()/2 + 10,getHeight(),0,10) + 20;
						Log.d("TOUCH","Speed reverse"+String.valueOf(b));
						client.setCommand((byte)b);
						}
						
					}
				}
				
				
				if(touched[i]==true && inBounds((int)tx[i],(int)ty[i],getWidth()-leftrightBmp.getWidth()-20,getHeight()-leftrightBmp.getHeight(),leftrightBmp.getWidth(),leftrightBmp.getHeight()))
				{
					int a=getWidth()-leftrightBmp.getWidth()/2-20;
					
					//Log.d("TOUCH","LR"+String.valueOf(a));
					//Log.d("TOUCH","LR"+String.valueOf( getWidth()-leftrightBmp.getWidth()-20));
					
					if(tx[i]>a-5 && tx[i]<a+5)
					{
						Log.d("TOUCH","LeftRight 0");
						client.setCommand((byte)0x04);
						left=false;
						right=false;
					}
					
					if(tx[i]>a-leftrightBmp.getWidth()/2 && tx[i]<a-5)
					{	
						int c=(int)map(tx[i],a-5,getWidth()-leftrightBmp.getWidth()-20,0,10) +40;
		        		if(c<=50)
		        		{
		        			client.setCommand((byte)c);
		        			Log.d("TOUCH","LEFT"+String.valueOf(c));
		        			right=true;
		        			left=false;
		        		}
					}
					
					if(tx[i]>a+5 && tx[i]<a+leftrightBmp.getWidth()/2)
					{
						
						int d=(int)map(tx[i],a+5,getWidth()-20,0,10) + 30;
		        		if(d<=40)
		        		{
		        			right=false;
		        			left=true;
		        			client.setCommand((byte)d);
		        			Log.d("TOUCH","Right"+String.valueOf(d));
		        		}
					}
				}
				
				
				if(touched[i]==true && inBounds((int)tx[i],(int)ty[i],getWidth()-rotateclockBmp.getWidth(),getHeight()/2-rotateantiBmp.getHeight()/2 ,rotateclockBmp.getWidth(),rotateclockBmp.getHeight()))
				{
					
					Log.d("TOUCH","touched for rotateclock" + String.valueOf( tx[i]));
					client.setCommand((byte)0x06);
					
				}
					
				
				if(touched[i]==true && inBounds((int)tx[i],(int)ty[i],getWidth()-rotateantiBmp.getWidth()*2-80,getHeight()/2-rotateantiBmp.getHeight()/2 ,rotateantiBmp.getWidth(),rotateantiBmp.getHeight()))
				{
					client.setCommand((byte)0x05);
					Log.d("TOUCH","touched for rotate anticlockwise");
				}
				
			}
			
			
			
			int pointerId = event.getPointerId(i);
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				touched[i] = true;
				tid[i] = pointerId;
				tx[i] = (int) event.getX(i);
				ty[i] = (int) event.getY(i);
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
	        case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_CANCEL:
				touched[i] = false;
				tid[i] = -1;
				tx[i] = (int) event.getX(i);
				ty[i] = (int) event.getY(i);
				break;

			case MotionEvent.ACTION_MOVE:
				touched[i] = true;
				tid[i] = pointerId;
				tx[i] = (int) event.getX(i);
				ty[i] = (int) event.getY(i);
				break;
			}
		}
		
		if(touched[0] == false && touched[1] == false )
		{
			client.setCommand((byte)0x00);
		}
		//Log.d("TOUCH1",String.valueOf(touched[0])+","+String.valueOf(tx[0])+","+String.valueOf(ty[0])+","+String.valueOf(tid[0]));
		//Log.d("TOUCH2",String.valueOf(touched[1])+","+String.valueOf(tx[1])+","+String.valueOf(ty[1])+","+String.valueOf(tid[1]));
		return true;
	}
	
	private long map(long x, long in_min, long in_max, long out_min, long out_max)
	{
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	private float map(float x, float in_min, float in_max, float out_min, float out_max)
	{
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	private boolean inBounds(int tx, int ty, int x, int y, int width, int height)
	{
		if(tx > x && tx < x + width - 1 && ty > y && ty < y + height -1 )
			return true;
		else
			return false;
		
	}
	

	
	
}
