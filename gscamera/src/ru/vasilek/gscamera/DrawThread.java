package ru.vasilek.gscamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;

public class DrawThread extends Thread {
	
	private SurfaceHolder holder;
	private boolean runFlag;
	private long prevTime;
	public int[] rgb;
	public byte[] data;
	public float[] filter;
	public Size max;
	public Size prevsz;
	public Bitmap buffer;
	
	public DrawThread(SurfaceHolder holder) {
		this.holder = holder;
	}
	
	public void setRunning(boolean run) {
        runFlag = run;
    }
	
	@Override
	public void run() {
		Canvas canvas;
		while (runFlag){
			if (data != null)
				CameraPreview.YUV_NV21_TO_RGB(rgb, data, max.width, max.height);
			
			SurfaceHolder h = holder;
			Canvas c = h.lockCanvas();
			if (c == null)
				continue;
			Paint p = new Paint();	
			synchronized (rgb) {
				for (int i = 0; i<max.height; i++)
					for (int j = 0; j<max.width; j++){
						int col = rgb[i*max.width+j];
						col = col | 0xFF000000;
						buffer.setPixel(max.height-i-1, j, col);
					}
				ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(filter);
				p.setColorFilter(cmcf);
				c.drawBitmap(Bitmap.createScaledBitmap(buffer, prevsz.width, prevsz.height, true), 0, 0, p);			
			}		
			h.unlockCanvasAndPost(c);
		}
		
		super.run();
	}

}
