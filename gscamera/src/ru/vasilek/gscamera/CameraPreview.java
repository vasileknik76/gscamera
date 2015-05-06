package ru.vasilek.gscamera;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	private static final String TAG = "CameraPreview";
	public Camera camera;
	public SurfaceView sv;
	int[] rgb;
	private Context ctx;
	private Size max;
	private Size prevsz;
	private Bitmap buffer;
	
	
	
	public static String getPhotoDirectory(Context context)
	{
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/Camera/";
//	    return context.;
	}
	
	private ShutterCallback shutter = new ShutterCallback() {
		
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			
		}
	};
	private PictureCallback jpeg = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Options opts = new Options();
	    	opts.inMutable = true;
			Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

			picture = filter(picture, true);
			Calendar c = Calendar.getInstance();
			Date d = c.getTime();
			String fname = String.format("%d%02d%02d_%02d%02d%02d.jpg", c.get(Calendar.YEAR), d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds());
			FileOutputStream out = null;
			try {
				File f = new File(getPhotoDirectory(ctx));
				f.mkdir();
				out = new FileOutputStream(getPhotoDirectory(ctx)+fname);
			    
			    picture.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
			    // PNG is a lossless format, the compression factor (100) is ignored
			} catch (Exception e) {
			    e.printStackTrace();
			} finally {
			    try {
			        if (out != null) {
			            out.close();
			        }
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			}
	            
			camera.startPreview();
	            
		}
	};
	
	private PictureCallback raw = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}
	};
	private SharedPreferences sPrefs;
	private DrawThread dThread;
	
	
    
    public CameraPreview( Context context ) {
        super(context);
        ctx = context;
        // We're implementing the Callback interface and want to get notified
        // about certain surface events.
        sv = new SurfaceView(context);
        getHolder().addCallback( this );
        // We're changing the surface to a PUSH surface, meaning we're receiving
        // all buffer data from another component - the camera, in this case.
        getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
    }
    
    protected Bitmap filter(Bitmap tmp) {
		for (int i = 0; i<tmp.getWidth(); i++)
			for (int j = 0; j<tmp.getHeight(); j++){
				int c = tmp.getPixel(i, j);
				byte b =  (byte) (c & 0xFF); c = c >>> 8;
				byte g =  (byte) (c & 0xFF); c = c >>> 8;
				byte r =  (byte) (c & 0xFF); c = c >>> 8;
				byte a =  (byte) (c & 0xFF);
				
				byte r1, g1, b1 , a1;
				r1 = (byte) (activefilter[0]*r+activefilter[1]*g+activefilter[2]*b+activefilter[3]*a+activefilter[4]);
				g1 = (byte) (activefilter[5]*r+activefilter[6]*g+activefilter[7]*b+activefilter[8]*a+activefilter[9]);
				b1 = (byte) (activefilter[10]*r+activefilter[11]*g+activefilter[12]*b+activefilter[13]*a+activefilter[14]);
				//a1 = (byte) (activefilter[15]*r+activefilter[16]*g+activefilter[17]*b+activefilter[18]*a+activefilter[19]);
				c = (0xFF000000) + (r1 << 16) + (g1 << 8) + b1;
				tmp.setPixel(i, j, c);
			}
		return tmp;
	}
    
    protected Bitmap filter(Bitmap picture, boolean rotate) {
    	Matrix matrix = new Matrix();
    	matrix.postRotate(90);
    	if (rotate) {
    		Bitmap rotb = Bitmap.createBitmap(picture , 0, 0, picture.getWidth(), picture.getHeight(), matrix, true);
			return filter(rotb);
    	}
    	else
    		return filter(picture);
	}

	public void getPic(){
    	camera.takePicture(shutter , raw, jpeg);
    }
	
	int getScreenPointCount(){
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
		display.getSize(outSize);
		return outSize.x*outSize.y;
	}
	
	@SuppressWarnings("deprecation")
	void applySettings(){
		sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		Parameters parameters = camera.getParameters();
        parameters.setJpegQuality(100);
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        max = sizes.get(0);
        prevsz = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++)
        	if (sizes.get(i).width*sizes.get(i).height>max.height*max.width) 
        		max = sizes.get(i);    
        
        int scr = getScreenPointCount();
        for (int i = 0; i < sizes.size(); i++)
        	if (sizes.get(i).width*sizes.get(i).height*7<=scr) {
        		prevsz = sizes.get(i); 
        		break;
        	}
        String s = sPrefs.getString(SettingsActivity.KEY_QUALITYPREVIEW, "");
        if (s != ""){
        	prevsz.width = Integer.parseInt(s.split(";")[0]);
        	prevsz.height = Integer.parseInt(s.split(";")[1]);
        }
        
        s = sPrefs.getString(SettingsActivity.KEY_QUALITYPIC, "");
        if (s != ""){
        	max.width = Integer.parseInt(s.split(";")[0]);
        	max.height = Integer.parseInt(s.split(";")[1]);
        }
        
        Log.d(TAG, String.format("pic size: (%d;%d)", max.width, max.height));
        Log.d(TAG, String.format("preview size: (%d;%d)", prevsz.width, prevsz.height));
        parameters.setPreviewSize(prevsz.width, prevsz.height); 
        parameters.setPictureSize(max.width, max.height);
        float aspect = (float)(prevsz.width)/prevsz.height;
        max.width = prevsz.width;
        max.height = prevsz.height;
        prevsz.width = sv.getWidth();
        prevsz.height = (int) (sv.getWidth()*aspect);
        
        rgb = new int[max.width*max.height];
        buffer = Bitmap.createBitmap(max.height, max.width, Config.ARGB_8888);
        //parameters.setPreviewFrameRate(20);
        parameters.setPreviewFormat(ImageFormat.NV21);

        camera.setParameters(parameters);
        camera.setPreviewCallback(this);
	}
     

	public void surfaceCreated( SurfaceHolder holder ) {
        // Once the surface is created, simply open a handle to the camera hardware.
        camera = Camera.open();
        applySettings();
        camera.setDisplayOrientation(90);
//        dThread = new DrawThread(holder);
//        dThread.filter = activefilter;
//        dThread.max = max;
//        dThread.prevsz = prevsz;
//        dThread.rgb = rgb;
//        dThread.buffer = buffer;
//        dThread.setRunning(true);
//        dThread.start();
        
    }
 
    @Override
	public void draw(Canvas canvas) {
    	Log.d(TAG, "draw");
		super.draw(canvas);
	}
    
    @Override
	public void onDraw(Canvas canvas) {
    	Log.d(TAG, "onDraw");
		super.onDraw(canvas);
	}

	public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        // This method is called when the surface changes, e.g. when it's size is set.
        // We use the opportunity to initialize the camera preview display dimensions.
//        Camera.Parameters p = camera.getParameters();
//        p.setPreviewSize( width, height );
//        camera.setParameters(p);
    	camera.setDisplayOrientation(90);
    	applySettings();
        // We also assign the preview display to this surface...
        try {
            camera.setPreviewDisplay( holder );
        } catch( IOException e ) {
            e.printStackTrace();
        }
        // ...and start previewing. From now on, the camera keeps pushing preview
        // images to the surface.
        camera.startPreview();
    }
 
    public void surfaceDestroyed( SurfaceHolder holder ) {
        // Once the surface gets destroyed, we stop the preview mode and release
        // the whole camera since we no longer need it.
//    	dThread.setRunning(false);
        try {
			camera.setPreviewDisplay(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        camera.setPreviewCallback(null);
    	camera.stopPreview();
        camera.release();
        camera = null;
    }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		Log.d(TAG, "dispatchDraw");
		doDraw();
		super.dispatchDraw(canvas);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.d(TAG, "onPreviewFrame");
		//Log.d(TAG, String.format("data.length=%d; rgb.length=%d", data.length, rgb.length));
//		dThread.data = data.clone();
		this.invalidate();
		YUV_NV21_TO_RGB(rgb, data, max.width, max.height);	
		
//		doDraw();
		
	}
	
	public void doDraw(){
		SurfaceHolder h = sv.getHolder();
		Canvas c = h.lockCanvas();
		
		Paint p = new Paint();	
		synchronized (rgb) {
			for (int i = 0; i<max.height; i++)
				for (int j = 0; j<max.width; j++){
					int col = rgb[i*max.width+j];
					col = col | 0xFF000000;
					buffer.setPixel(max.height-i-1, j, col);
				}
			ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(activefilter);
			p.setColorFilter(cmcf);
			c.drawBitmap(Bitmap.createScaledBitmap(buffer, prevsz.width, prevsz.height, true), 0, 0, p);			
		}		
		h.unlockCanvasAndPost(c);
	}
	
	public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
	    final int frameSize = width * height;

	    final int ii = 0;
	    final int ij = 0;
	    final int di = +1;
	    final int dj = +1;

	    int a = 0;
	    for (int i = 0, ci = ii; i < height; ++i, ci += di) {
	        for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
	            int y = (0xff & ((int) yuv[ci * width + cj]));
	            int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
	            int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
	            y = y < 16 ? 16 : y;

	            int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
	            int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	            int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

	            r = r < 0 ? 0 : (r > 255 ? 255 : r);
	            g = g < 0 ? 0 : (g > 255 ? 255 : g);
	            b = b < 0 ? 0 : (b > 255 ? 255 : b);

	            argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
	        }
	    }
	}
	
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
	
	public float[] activefilter = nonefilter;
	
}
