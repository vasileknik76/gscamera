package ru.vasilek.gscamera;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

public final class Effects {
	
	public static float[] redfilter = {
		1, 0, 0, 0, 0,
		1, 0, 0, 0, 0,
		1, 0, 0, 0, 0,
		0, 0, 0, 1, 0	};

	public static float[] bluefilter = {
			0,	 0, 	1, 	0, 	0,
			0,	 0, 	1, 	0, 	0,
			0, 	 0, 	1, 	0, 	0,
			0, 	 0, 	0, 	1, 	0	};
	
	public static float[] nonefilter = {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 1, 0	};
	
	public static float[] nightfilter = {
		0.393f,		0.769f,		0.189f,		0,		0,
		0.349f,		0.686f,		0.168f,		0,		0,
		0.272f,		0.534f,		0.1310f,	0,		0,
		0,			0,			0,			1,		0	};
	
	public static float[] negativefilter = {
			-1,		0,		0,		0,		255,
			0,		-1,		0,		0,		255,
			0,		0,		-1,		0,		255,
			0,		0,		0,		1,		0		};
	
	public static float[] orangefilter = {
		0.502f, 	0.748f, 	0, 	0, 	0,
		0.502f, 	0.748f, 	0, 	0, 	0,
		0.502f, 	0.748f, 	0, 	0, 	0,
		0, 			0, 			0, 	1, 	0		};
		
	}
	
	class CameraEffect {
		float[] mtx;
		private static List<CameraEffect> fcameffects = null;
		
		public static List<CameraEffect> CameraEffects(){
			if (fcameffects != null)
				return fcameffects;
			
			fcameffects = new ArrayList<CameraEffect>();
			Effects e = new Effects();
			for (Class<?> c = Effects.class; c != null; c = c.getSuperclass())
		    {
		        Field[] fields = c.getDeclaredFields();
		        for (Field classField : fields)
		        {
		        	try {
		        		if (classField.getName().contains("filter")){
		        			float[] f = (float[]) classField.get(e);
		        			fcameffects.add(new CameraEffect(f));		        			
		        		}
						
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
		    }
			return fcameffects;
			
		}
		
		public CameraEffect(float[] amtx){
			mtx = amtx;
		}
		
		public void draw(Canvas c, Bitmap b){
			Paint paint = new Paint();
			paint.setColorFilter(new ColorMatrixColorFilter(mtx));
			c.drawBitmap(b, 0, 0, paint);
		}
		
		

}
