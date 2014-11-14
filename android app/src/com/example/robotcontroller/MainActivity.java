package com.example.robotcontroller;


import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;

public class MainActivity extends Activity{
		private MainView gameView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gameView = new MainView(this);
		gameView.setKeepScreenOn(true);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(gameView);
	}


}
