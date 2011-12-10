package pda.lupa.views;

import android.view.MotionEvent;
import pda.lupa.Lupa;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import pda.lupa.MyGestureDetector;
import pda.lupa.Settings;

    /**
     * Callbeck pro surface
     */
    public final class MySurfaceView extends SurfaceView 
    implements SurfaceHolder.Callback, Camera.PreviewCallback,
    SurfaceView.OnTouchListener {
	Lupa lupa;
	
	Camera camera;
	SurfaceHolder prevHolder;
	
	MyGLSurfaceView glCallback;
	
	/** buffer, do ktereho se nacita obrazek nahledu */
	private byte[] cameraFrame;

	/** buffer pro nacteni dat z kamery*/
	private byte[] previewBuffer1;
	/** velikost zobrazovaneho nahledu,
	 * nastavuje se v SetPreviewSize */
	private int prevY, prevX = 0;
	
	Context c;
	
	private GestureDetector gestureDetector;
        
	

	public MySurfaceView(Context c, AttributeSet s) {
	    super(c, s);
	    this.c = c;
	    
	    this.gestureDetector = new GestureDetector(new MyGestureDetector());
	    
	    this.setWillNotDraw(true);
	    this.setOnTouchListener(this);
	    
	    //this.setZOrderOnTop(true);
	    
	}
	
	public void init(Lupa lupa) {
	    this.lupa = lupa;
	    
	    this.glCallback = lupa.getGlCallback();
	    
	    camera = lupa.getCamera();
	    
	    prevHolder = this.getHolder();
	    prevHolder.addCallback(this);
	    prevHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    //nic
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    Camera.Parameters parameters = camera.getParameters();
	    Camera.Size size = getBestPreviewSize(width, height, parameters);  
	    	    
	    
	    if (size!=null) {
		parameters.setPreviewSize(size.width, size.height);
		//musime nastavit danou velikost do GLSurfaceView
		this.glCallback.setPreviewSize(size, this.camera);
		camera.setParameters(parameters);
	    }
	    
	    this.changeView(Settings.isGlView());

	    //camera.startPreview(); // tady ZACINA ZOBRAZENI NAHLEDU
	    lupa.setInPreview(true);
	    lupa.zoom();
	    lupa.focus();
	}
	
	public void changeView(boolean glView) {
	    try {
		
		camera.stopPreview();
		if(glView) {
		    glCallback.setVisibility(View.VISIBLE);
		    glCallback.initBuffer();
		    camera.setPreviewCallbackWithBuffer(glCallback);
		    camera.setPreviewDisplay(null);
		}
		else {
		    glCallback.setVisibility(View.INVISIBLE);
		    camera.setPreviewDisplay(prevHolder);
		    camera.setPreviewCallbackWithBuffer(null);
		    //camera.setPreviewCallbackWithBuffer(this);
		    //camera.setPreviewDisplay(null);
		}
		camera.startPreview();
	    }
	    catch(Throwable t) {
		Log.e("onSurfaceChanged", "stala se smula v setPreviewDisplay");
	    }
	}
	

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    lupa.close();
	}
	
	
	YuvImage yuvimage;
	ByteArrayOutputStream baos;
	byte[] jdata;
	int[] out = new int[320*240];
	
	IntBuffer rgb;
	
	Paint paint = new Paint();
	Bitmap bmp;
	ColorMatrixColorFilter filter = setContrast(10f);
	
	@Override
	protected void onDraw(Canvas canvas) {
	    if(true || cameraFrame == null) {
		invalidate();
		return;
	    }
	   
	    yuvimage=new YuvImage(cameraFrame, ImageFormat.NV21, prevX, prevY, null);
	    
	    baos = new ByteArrayOutputStream();
	    yuvimage.compressToJpeg(new Rect(0, 0, prevX, prevY), 80, baos);
	    jdata = baos.toByteArray();

	    // Convert to Bitmap
	    bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

	    
	    paint.setColorFilter(filter);
	    canvas.drawBitmap(bmp , 0, 0, paint);
	    
	    //canvas.drawPaint(paint);
	    invalidate();
	}
	
	private ColorMatrixColorFilter setContrast(float contrast) {
	    ColorMatrix output = new ColorMatrix();
	    
	    
	    ColorMatrix bw = new ColorMatrix();
	    bw.setSaturation(0);  //greyscale
	    
	    ColorMatrix invert = new ColorMatrix(new float[] {
		-1, 0,  0, 1, 0,
		0, -1,  0, 1, 0,
		0,  0, -1, 1, 0,
		1,  1,  1, 1, 0});
	    
	    ColorMatrix chageColor = new ColorMatrix(new float[] {
		1, 1, 1, 0, 0,
		0, 1, 0, 0, 0,
		0, 0, 1, 0, 0,
		0, 0, 0, 1, 0});
	    
	    float scale = contrast + 1.f;
	    float translate = (-.5f * scale + .5f) * 255.f;
	    ColorMatrix contr = new ColorMatrix(new float[] {
		scale, 0, 0, 0, translate,
		0, scale, 0, 0, translate,
		0, 0, scale, 0, translate,
		0, 0, 0, 1, 0});
	    
	    
	    output.setConcat(output, bw);
	    //output.setConcat(output, invert);	    
	    //output.setConcat(output, contr);
	    //output.setConcat(output, chageColor);
	    
	    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(output);
	    return filter;
	}

	
	Date start;
	int fcount = 0;
	public void onPreviewFrame(byte[] yuvsSource, Camera camera) {

	    if(this.prevX == 0) return;
	    if(start == null){
		    start = new Date();
	    }
	    fcount++;
	    if(fcount % 100 == 0){
		    double ms = (new Date()).getTime() - start.getTime();
		    Log.i("AR","fps:" + fcount/(ms/1000.0));
	    }

	    this.cameraFrame = yuvsSource;

	    camera.addCallbackBuffer(yuvsSource);
	}

	public void setPreviewSize(Camera.Size size, Camera camera) {
	    //zjistime rozmery nahledu
	    this.prevX = size.width;
	    this.prevY = size.height;

	    this.camera = camera;
	    // vytvorime buffer pro obrazek o velikosti sirka x vyska

	    this.cameraFrame = new byte[prevX*prevY*3/2];

	    this.previewBuffer1 = new byte[prevX*prevY*3/2];
	    this.camera.addCallbackBuffer(this.previewBuffer1);
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param parameters
	 * @return 
	 */
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
	    Camera.Size result = null;	    
	    
	    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
		Log.d("tag", size.width + "x" + size.height);
		if (size.width <= width && size.height <= height) {
		    if (result == null) {
			result = size;
			continue;
		    }
		    else {
			int resultArea = result.width * result.height;
			int newArea = size.width * size.height;

			if (newArea > resultArea) {
			    result = size;
			}
		    }
		}
	    }	    
	    return result;
	}

	public boolean onTouch(View view, MotionEvent me) {
	    gestureDetector.onTouchEvent(me);
	    return true;
	}

    }