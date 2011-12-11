package pda.lupa;

import pda.lupa.views.MyGLSurfaceView;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Vibrator;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import pda.lupa.callbacks.MyAutoFocusCallback;
import pda.lupa.views.MySurfaceView;

/**
 * ToDo:
 *  - barevna bude standardne vykreslovana pres surfaceview
 *  - cernobila, invertovana atp se bude muset delat pres openGL
 *  -> do openGL se budou predavat jen cernobila data!! (max 25ms)
 *    -> see http://nhenze.net/?p=107
 *  - a nebo NDK a udělat to v C... ffs!!
 *  -- openGL bude asi muset mít vlastní aktivitu!
 */


public final class Lupa {
    /** Kamera */
    private Camera camera = null;
    
    /* Blbosti aby to fungovalo... */
    private MySurfaceView prev = null;
    private SurfaceHolder prevHolder = null;
        
    private MyGLSurfaceView glView;
    
    /** Kontext */
    private Activity activity = null;
    
    /**handler pro akce*/
    private ActionHandler handler = null;
    
    /** Callback na autofocus! */
    private MyAutoFocusCallback autoFocus;
    
    private boolean inPreview = false;
    
    private final Button light;
    
    
    /**
     * Konstruktor, inicializuje atributy, pripravuje surface
     * pro zobrazeni nahledu z fotaku, vyuziva callback tridu 
     * pro surface
     * @param main aktivita..
     */
    public Lupa(Activity main) {
	this.activity = main;
	this.glView = (MyGLSurfaceView) this.activity.findViewById(R.id.gl_preview);
	this.prev = (MySurfaceView) this.activity.findViewById(R.id.preview);
		
	light = (Button) this.activity.findViewById(R.id.button_light);
	light.setOnClickListener(new LightOnClickListener());
	
	if(!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
	    light.setVisibility(View.INVISIBLE);
	}
	
	
	//je to jeste potreba?
	Display display = activity.getWindowManager().getDefaultDisplay();
	glView.getHolder().setFixedSize(display.getWidth(), display.getHeight());
	
	this.handler = new ActionHandler(this);
	this.open();
	
	this.prev.init(this);	
	
    }
    
    

    /**
     * Otevira kameru
     */
    protected void open() {
	if(camera == null) {
	    camera = Camera.open();
	    
	    //glView.setCamera(camera);
	    //Autofocus:
	    autoFocus = new MyAutoFocusCallback(); //vytvorime callback
	    autoFocus.setHandler(this.handler, R.id.auto_focus); //nastavime handle
	    Settings.init(activity, camera.getParameters(), this.handler);
	}
    }

    /**
     * Spousti auto focus, pokud je dostupny!
     */
    public void focus() {
	if(!inPreview) return;
	String focus = camera.getParameters().getFocusMode();
	if(focus.equals("auto") || focus.equals("macro")) {
	    camera.autoFocus(autoFocus);
	}
    }
    
    /**
     * Nastavuje zoom kamery
     * 
     * @param zoom hodnota zoomu, pokud je vetsi nez 
     * Camera.Parameters.getMaxZoom() tak se snizi
     */
    public void zoom() {
	Camera.Parameters param = camera.getParameters();

	if(!param.isZoomSupported()) {
	    Toast.makeText(activity, R.string.err_noZoom , Toast.LENGTH_LONG).show();
	    return;
	}

	param.setZoom(Settings.getZoom());
	camera.setParameters(param);
    }
    
    /**
     * Nastavuje effekt kamery
     * @param effect 
     */
    public void invert(boolean inverted) {
	Camera.Parameters param = camera.getParameters();
	if(inverted)
	    param.setColorEffect(Camera.Parameters.EFFECT_NONE);
	else 
	    param.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
	camera.setParameters(param);
    }
    
    public void vibrate(int ms) {
	Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
	v.vibrate(ms);
    }
    
    public void changeView(int viewType) {
	if(viewType == 0)
	    prev.changeView(false);
	else
	    prev.changeView(true);
    }
    
    public void setBitmapData() {
	    prev.createBitmap();//glView.getCameraFrame());
    }
    
    public void light(boolean turnOn) {
	Camera.Parameters param = camera.getParameters();
	if(turnOn)
	    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
	else
	    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
	camera.setParameters(param); 
    }

    /**
     * releasuje kameru a mazu vsechny mozny callbacky etc.
     */
    public void close() {
	System.exit(0);
	if (camera != null) {
	    prevHolder.addCallback(null); //smazeme klasickej preview 
	    autoFocus.setHandler(null, 0); //smazeme handler
	    this.light(false);
	    camera.setPreviewCallback(null); //smazeme callback na kameru
	    camera.stopPreview(); //tady konci ZOBRAZOVANI NAHLEDU
	    camera.release(); //ted muzeme v klidu ukoncit kameru
	    camera = null;
	    glView = null;
	    inPreview = false;
	}
    }
    
    public MyGLSurfaceView getGlCallback() {
	return this.glView;
    
    }
    
    
    public boolean inPreview() {
	return inPreview;
    }
    public void setInPreview(boolean prev) {
	this.inPreview = prev;
    }
    public Camera getCamera() {
	return camera;
    }  
    public Button getLightButton() {
	return light;
    }
    
    
    class LightOnClickListener implements OnClickListener {
	public void onClick(View v) {
		if(Settings.isLightOn()) {
		    light.setBackgroundResource(R.drawable.button_64_2);
		    Settings.setLightOn(false);
		}
		else {
		    light.setBackgroundResource(R.drawable.button_64);
		    Settings.setLightOn(true);
		}
	    }
    }
}