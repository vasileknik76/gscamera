package ru.vasilek.gscamera;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class CameraActivity extends Activity implements OnClickListener, OnCheckedChangeListener{
	
	private static final String TAG = "CameraActivity";
	//private Camera mCamera;
    private TextureView mTextureView;
	private GLSurfaceView glView;
	private CameraPreview mv;
	private ToggleButton[] filters =  new ToggleButton[5];
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    setContentView(R.layout.activity_camera);
	    
	    mv = new CameraPreview(this);
	    mv.getHolder().setFixedSize(1, 1);
	    FrameLayout lc = (FrameLayout) findViewById(R.id.camera_preview);
	    LinearLayout llol = (LinearLayout) findViewById(R.id.cp_lol);
	    llol.addView(mv);
	    lc.addView(mv.sv);
	    View panel = LayoutInflater.from(this).inflate(R.layout.effectspanel, null, false);
	    lc.addView(panel);
	    Button b = (Button) findViewById(R.id.button_capture);
	    b.setOnClickListener(this);
	    ((Button) findViewById(R.id.button_settings)).setOnClickListener(this);
	    filters[0] = ((ToggleButton) findViewById(R.id.buteffect_blue));
	    filters[1] = ((ToggleButton) findViewById(R.id.buteffect_red));
	    filters[2] = ((ToggleButton) findViewById(R.id.buteffect_none));
	    filters[3] = ((ToggleButton) findViewById(R.id.buteffect_negative));
	    filters[4] = ((ToggleButton) findViewById(R.id.buteffect_orange));
	    for (int i = 0; i < 5; i++)
	    	filters[i].setOnCheckedChangeListener(this);
	    
	    EffectsPanel ep = new EffectsPanel(this);
	    lc.addView(ep);
	}	



	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Log.d(TAG, "new orientation: ORIENTATION_LANDSCAPE");
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	Log.d(TAG, "new orientation: ORIENTATION_PORTRAIT");
	    }
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_capture:
			mv.getPic();
			break;
		
		case R.id.button_settings:
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			SettingsActivity.writeCameraInfo(mv.camera, intent);
			startActivity(intent);
			break;
			
		default:
			break;
		}
		
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!isChecked)
			return;
		for (int i = 0; i < 5; i++)
			if (filters[i].getId() != buttonView.getId())
				filters[i].setChecked(false);
		switch (buttonView.getId()){
			case R.id.buteffect_red:				
				mv.activefilter = CameraPreview.redfilter;
				break;
			
			case R.id.buteffect_blue:
				mv.activefilter = CameraPreview.bluefilter;
				break;
			
			case R.id.buteffect_negative:
				mv.activefilter = CameraPreview.negativefilter;
				break;
				
			case R.id.buteffect_none:
				mv.activefilter = CameraPreview.nonefilter;
				break;
				
			case R.id.buteffect_orange:
				mv.activefilter = CameraPreview.orangefilter;
				break;
			
		}

	}


	
}
