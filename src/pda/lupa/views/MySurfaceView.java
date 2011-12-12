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
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import pda.lupa.MyGestureDetector;
import pda.lupa.Settings;

    /**
     * Callbeck pro surface
     */
    public final class MySurfaceView extends SurfaceView 
    implements SurfaceHolder.Callback, SurfaceView.OnTouchListener,
    Camera.PreviewCallback {
	Lupa lupa;
	Camera camera;
	SurfaceHolder prevHolder;
	Context c;
	
	/** ukazatel na gl  */
	MyGLSurfaceView glCallback;
	
	/** buffer, do ktereho se nacita obrazek nahledu */
	private byte[] cameraFrame;
	
	/** buffer pro nacteni dat z kamery*/
        private byte[] previewBuffer1;
	
	/**  pamatuje si 3 posledni framy, aby to vzalo po kliku nejaky starsi,
	 * nez si s mobilem hneme tim kliknutim
	 */
	private List<byte[]> previewFrames = new ArrayList<byte[]>(3);

	/** velikost zobrazovaneho nahledu,
	 * nastavuje se v SetPreviewSize */
	private int prevY, prevX = 0;
	
	/** detektor na gesta.. */
	private GestureDetector gestureDetector;
  	

	public MySurfaceView(Context c, AttributeSet s) {
	    super(c, s);
	    this.c = c;
	    
	    //listener na dotyky displeje
	    this.gestureDetector = new GestureDetector(new MyGestureDetector());
	    this.setOnTouchListener(this);
	    
	    previewFrames.add(new byte[] {});
	    previewFrames.add(new byte[] {});
	    
	}
	
	/**
	 * Inicializace, konstruktor se vola z layoutu
	 * @param lupa 
	 */
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
		this.glCallback.setPreviewSize(size);
		this.setPreviewSize(size);
		camera.setParameters(parameters);
	    }
	    
	    this.changeView(Settings.isGlView());
	    
	    camera.startPreview();
	    lupa.zoom();
	}
	
	public void changeView(boolean glView) {
	    try {
		lupa.setInPreview(false);
		this.setWillNotDraw(true);
		if(glView) { //zobrazujeme pres OpenGL
		    glCallback.setVisibility(View.VISIBLE);
		    camera.setPreviewDisplay(null);
		}
		else { //zobrazujeme pres surfaceview
		    glCallback.setVisibility(View.INVISIBLE);
		    
		    if(Settings.isStopView()) {  //zastavena obrazovka
			camera.setPreviewDisplay(null);
			this.setWillNotDraw(false);
			lupa.getLightButton().setVisibility(View.INVISIBLE);
			return;
		    }
		    camera.stopPreview();
		    Settings.setFocusing(false);
		    camera.setPreviewDisplay(prevHolder);
		    camera.startPreview();
		    lupa.light(Settings.isLightOn());
		}
		lupa.getLightButton().setVisibility(View.VISIBLE);
		camera.setPreviewCallbackWithBuffer(this);
		lupa.setInPreview(true);
		if(!Settings.isFocusing()) lupa.focus();
	    }
	    catch(Throwable t) {
		Log.e("onSurfaceChanged", "stala se smula v setPreviewDisplay: " 
			+ t.getMessage());
	    }
	}
	

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    lupa.close();
	}
	
	/**
	 * callback na data z kamery
	 * @param yuvsSource
	 * @param camera 
	 */
	public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	    if(Settings.isGlView()) {
		glCallback.onPreviewFrame(yuvsSource, camera);
		this.camera.addCallbackBuffer(yuvsSource);
		return;
	    }
	    
	    this.cameraFrame = yuvsSource;
	    camera.addCallbackBuffer(yuvsSource);
	    previewFrames.remove(0);
	    previewFrames.add(cameraFrame);
	}
	
	
	Bitmap bmp;
	Paint paint = new Paint();
	ColorMatrixColorFilter filter;
	@Override
	protected void onDraw(Canvas canvas) {
	    if(bmp == null) {
		invalidate();
		return;
	    }
	    
	    filter = setFilter(Settings.getContrast());
	    paint.setColorFilter(filter);
	    canvas.drawBitmap(bmp , 0, 0, paint);
	    
	    invalidate();
	}
	
	/**
	 * vytvari bitmapu z obrazovky o 3 snimky zpet
	 */
	public void createBitmap() {
	    YuvImage yuvimage;
	    ByteArrayOutputStream baos;
	    
	    yuvimage=new YuvImage(this.previewFrames.get(0), ImageFormat.NV21, prevX, prevY, null);
	    
	    baos = new ByteArrayOutputStream();
	    yuvimage.compressToJpeg(new Rect(0, 0, prevX, prevY), 95, baos);

	    // Convert to Bitmap
	    bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
	    isInverted = Settings.isInverted();
	}
	
	private boolean isInverted;
	/**
	 * Nastavuje filtr statickeho zobrazeni
	 * @param contrast
	 * @return 
	 */
	private ColorMatrixColorFilter setFilter(float contrast) {
	    ColorMatrix output = new ColorMatrix();
	    
	    ColorMatrix bw = new ColorMatrix();
	    bw.setSaturation(0);  //greyscale
	    
	    ColorMatrix invert = new ColorMatrix(new float[] {
		-1, 0,  0, 1, 0,
		0, -1,  0, 1, 0,
		0,  0, -1, 1, 0,
		0,  0,  0, 1, 0});
	    
	    ColorMatrix blueYellow = new ColorMatrix(new float[] {
		0.5f, 0.5f, 0.5f, 0, 0,
		0.5f, 0.5f, 0.5f, 0, 0,
		-0.5f, -0.5f, -0.5f, 0, 255,
		0, 0, 0, 1, 0});
	    
	    float scale = contrast + 1.f;
	    float translate = (-.5f * scale + .5f) * 255.f;
	    ColorMatrix contr = new ColorMatrix(new float[] {
		scale, 0, 0, 0, translate,
		0, scale, 0, 0, translate,
		0, 0, scale, 0, translate,
		0, 0, 0, 1, 0});
	    
	    
	    output.setConcat(output, contr);	    

	    if(isInverted != Settings.isInverted())
		output.setConcat(output, invert);	        	    
	    
	    switch(Settings.getViewType()) {
		case 1:
		    output.setConcat(output, bw);
		case 2:
		    output.setConcat(output, blueYellow);
		    break; 
	    }
	    
	    filter = new ColorMatrixColorFilter(output);
	    return filter;
	}
	

	/**
	 * Nastavime si aktualni rozmery nahledu
	 * @param size 
	 */
	public void setPreviewSize(Camera.Size size) {
	    //zjistime rozmery nahledu
	    this.prevX = size.width;
	    this.prevY = size.height;

	    // vytvorime buffer pro obrazek o velikosti sirka x vyska
	    this.cameraFrame = new byte[prevX*prevY*3/2];

	    this.previewBuffer1 = new byte[prevX*prevY*3/2];
	    this.camera.addCallbackBuffer(this.previewBuffer1);
	}

	/**
	 * Ziskame optimalni roozmery obrazku
	 * @param width
	 * @param height
	 * @param parameters
	 * @return 
	 */
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
	    Camera.Size result = null;	    
	    
	    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
		//Log.d("tag", size.width + "x" + size.height);
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

	/**
	 * Callback pro dotyk displee, vola e pri kazde akci
	 * @param view
	 * @param me
	 * @return 
	 */
	public boolean onTouch(View view, MotionEvent me) {
	    gestureDetector.onTouchEvent(me);
	    return true;
	}

    }