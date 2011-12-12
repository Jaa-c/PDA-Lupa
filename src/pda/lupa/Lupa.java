package pda.lupa;

import pda.lupa.views.MyGLSurfaceView;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import pda.lupa.views.MySurfaceView;


public final class Lupa {
    /** Kamera */
    private Camera camera = null;
    
    /* SurfaceView a holder */
    private MySurfaceView prev = null;
    private SurfaceHolder prevHolder = null;
    
    /** GL View */
    private MyGLSurfaceView glView;
    
    /** Kontext */
    private Activity activity = null;
    
    /**handler pro akce*/
    private ActionHandler handler = null;
    
    /** Callback na autofocus! */
    private MyAutoFocusCallback autoFocus;
    
    /** jsme zrovna v nahledu? */
    private boolean inPreview = false;
    
    /** tlacitko na svetlo*/
    private final Button light;
    
    /** napoveda, jen jednoduse textview */
    private TextView napoveda;
    
    
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
	
	this.napoveda = (TextView) this.activity.findViewById(R.id.napoveda);
	this.napoveda.setText(Html.fromHtml(this.activity.getString((R.string.napoveda))));
	this.napoveda.setBackgroundColor(Color.BLACK);
	this.napoveda.setTextColor(Color.WHITE);
	this.napoveda.setTextSize(20f);
	this.napoveda.setPadding(10, 10, 10, 10);
	this.napoveda.setMovementMethod(new ScrollingMovementMethod());
	
	this.napoveda.setVisibility(View.INVISIBLE);
	
	
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
	Settings.setFocusing(true);
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
    
    /**
     * Vibruje po dany cas
     * @param ms jak dlouho vibrovat
     */
    public void vibrate(int ms) {
	Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
	v.vibrate(ms);
    }
    
    /**
     * Meni view
     * @param viewType 
     */
    public void changeView(int viewType) {
	if(viewType == 0)
	    prev.changeView(false);
	else
	    prev.changeView(true);
    }
    
    /**
     * nastavuje bitmapu z aktualniho zaberu na displeji
     */
    public void setBitmapData() {
	    prev.createBitmap();//glView.getCameraFrame());
    }
    
    /**
     * Stara se o svetlo
     * @param turnOn zapnout nebo vypnout
     */
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
	//System.exit(0);
	if (camera != null) {
	    camera.stopPreview(); //tady konci ZOBRAZOVANI NAHLEDU
	    camera.setPreviewCallback(null); //smazeme callback na kameru
	    autoFocus.setHandler(null, 0); //smazeme handler
	    this.light(false);
	    Settings.setFocusing(false);
	    camera.release(); //ted muzeme v klidu ukoncit kameru
	    camera = null;
	    glView = null;
	    inPreview = false;
	}
    }
    
    //uz jenom gettery
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
    public TextView getNapoveda() {
	return napoveda;
    }
    
    /**
     * Listener na klik tlacitka baterka
     */
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