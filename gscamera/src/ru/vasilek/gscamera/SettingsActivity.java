package ru.vasilek.gscamera;

import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity {

	public static final String KEY_QUALITYPREVIEW = "qualitypreview";
	public static final String KEY_QUALITYPIC = "qualitypic";


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    // создаем экран
		setTheme(R.style.AppThemeLight);
	    PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(this);
	    setPreferenceScreen(rootScreen);
	    
	    Intent intent = getIntent();
	    CameraInfo data = new CameraInfo(intent);
	    
	    
	    
	    
	    String[] entries = new String[data.previewSizes.length];
	    String[] entriesValues = new String[data.previewSizes.length];
	    for (int i = 0; i < data.previewSizes.length; i++){
	    	entries[i] = String.format("%.1fM (%dx%d)", (float)(data.previewSizes[i].x*data.previewSizes[i].y)/1000000f,data.previewSizes[i].x, data.previewSizes[i].y);
	    	entriesValues[i] = String.format("%d;%d", data.previewSizes[i].x, data.previewSizes[i].y);
	    }
	    
	    ListPreference qualityPrev = new ListPreference(this);
	    qualityPrev.setKey(KEY_QUALITYPREVIEW);
	    qualityPrev.setTitle("Качество превью");
	    qualityPrev.setEntries(entries);
	    qualityPrev.setEntryValues(entriesValues);
	    qualityPrev.setDefaultValue(entriesValues[data.previewSizes.length-1]);
	    rootScreen.addPreference(qualityPrev);
	    
	    
	    
	    entries = new String[data.picSizes.length];
	    entriesValues = new String[data.picSizes.length];
	    for (int i = 0; i < data.picSizes.length; i++){
	    	entries[i] = String.format("%.1fM (%dx%d)", (float)(data.picSizes[i].x*data.picSizes[i].y)/1000000f,data.picSizes[i].x, data.picSizes[i].y);
	    	entriesValues[i] = String.format("%d;%d", data.picSizes[i].x, data.picSizes[i].y);
	    }
	    
	    ListPreference qualitypic = new ListPreference(this);
	    qualitypic.setKey(KEY_QUALITYPIC);
	    qualitypic.setTitle("Качество изображения");
	    qualitypic.setDefaultValue(entriesValues[0]);
	    qualitypic.setEntries(entries);
	    qualitypic.setEntryValues(entriesValues);
	    rootScreen.addPreference(qualitypic);
	    
	}
	
	/**
     * Write data from camera to intent. 
     * Call this method before show SettingsActivity
     */
	public static void writeCameraInfo(Camera camera, Intent intent){
		Parameters params = camera.getParameters();
		List<android.hardware.Camera.Size> szs =  params.getSupportedPreviewSizes();
		String[] szss = new String[szs.size()];
		for (int i = 0; i < szss.length; i++){
			szss[i] = String.format("%d;%d", szs.get(i).width, szs.get(i).height);
		}		
		intent.putExtra("previewsizes", szss);
		
		szs =  params.getSupportedPictureSizes();
		szss = new String[szs.size()];
		for (int i = 0; i < szss.length; i++){
			szss[i] = String.format("%d;%d", szs.get(i).width, szs.get(i).height);
		}		
		intent.putExtra("picsizes", szss);
	}
	
	
	static class CameraInfo {
		Point[] previewSizes;
		Point[] picSizes;
		
		public CameraInfo(Intent intent){
			String[] szss = intent.getStringArrayExtra("previewsizes");
			previewSizes = new Point[szss.length];
			for (int i = 0; i < szss.length; i++){
				String[] substr = szss[i].split(";");
				previewSizes[i] = new Point();
				previewSizes[i].x = Integer.parseInt(substr[0]);
				previewSizes[i].y = Integer.parseInt(substr[1]);
			}
			
			szss = intent.getStringArrayExtra("picsizes");
			picSizes = new Point[szss.length];
			for (int i = 0; i < szss.length; i++){
				String[] substr = szss[i].split(";");
				picSizes[i] = new Point();
				picSizes[i].x = Integer.parseInt(substr[0]);
				picSizes[i].y = Integer.parseInt(substr[1]);
			}
		}
		
	}
	
	
}
