package pda.lupa;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import pda.lupa.callbacks.MyAutoFocusCallback;
import pda.temp.MyPreviewCallback;
import pda.lupa.callbacks.MySurfaceCallback;

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
    private MySurfaceCallback prev = null;
    private SurfaceHolder prevHolder = null;
        
    private MyGLSurfaceView glView;
    
    /** Kontext */
    private Activity activity = null;
    
    /**handler pro akce*/
    private ActionHandler handler = null;
    
    /** Callback na autofocus! */
    private MyAutoFocusCallback autoFocus;
    
    private boolean inPreview = false;
    
    
    /**
     * Konstruktor, inicializuje atributy, pripravuje surface
     * pro zobrazeni nahledu z fotaku, vyuziva callback tridu 
     * pro surface
     * @param main aktivita..
     */
    public Lupa(Activity main) {//, SurfaceView prev) {
	this.activity = main;
	this.glView = (MyGLSurfaceView) this.activity.findViewById(R.id.gl_preview);
	this.prev = (MySurfaceCallback) this.activity.findViewById(R.id.preview);
	
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
	}
    }

    /**
     * Spousti auto focus, pokud je dostupny!
     */
    public void focus() {
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
    public void zoom(int zoom) {
	Camera.Parameters param = camera.getParameters();
	
	// Pokud fotak nepodporuje zoom, moc toho nevymyslim.
	if(!param.isZoomSupported()) {
	    Toast.makeText(activity, R.string.err_noZoom , Toast.LENGTH_LONG).show();
	    return;
	}
	//zjistime maximalni zoom zarizeni, pripadne upravime
	int maxZoom = param.getMaxZoom();
	zoom = zoom > maxZoom ? maxZoom : zoom;
	//nastavime hodnotu zoomu
	param.setZoom(zoom);
	camera.setParameters(param);
    }

    /**
     * releasuje kameru a mazu vsechny mozny callbacky etc.
     */
    public void close() {
	System.exit(0);
	if (camera != null) {
	    //prevHolder.addCallback(null); //smazeme klasickej preview 
	    autoFocus.setHandler(null, 0); //smazeme handler
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
}