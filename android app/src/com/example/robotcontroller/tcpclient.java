package com.example.robotcontroller;


import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


import android.util.Log;

public class tcpclient implements Runnable {
	
	private Socket s;
	private OutputStream os;
	private byte command;
	private boolean connected=false;
	private byte prevcommand;
	
	public tcpclient(){
		
		
		command=0x00;
	}
	
	@Override
	public void run() {
		

		while(true)
		{
			try {
			
				s = new Socket("192.168.43.5", 23);  //Set robot ip address here
				os = s.getOutputStream();
			}catch (Exception e) {
				Log.e("TPCCLIENT","Connection Error",e);
				connected=false;
			}
			connected=true;
			while(connected)
			{
				
			    if(command != prevcommand)
			    {
			    	try{
			
					os.write(command);
					os.flush();
					
			    	}
			    	catch(Exception e)
			    	{
			    		
			    		Log.d("Command","Command Written failed");
			    		break;
			    		
			    	}
			    	prevcommand=command;	
			    }
			    
			    try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   	
				
			    
			
			}
			try {
				os.close();
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	public void setCommand(final byte command){
		this.command = command;
	
	}
	


}
