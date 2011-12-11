package pda.lupa;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;

public class Settings {
    private static Handler handler;
    public static void init(Context c, Camera.Parameters p, Handler h) {
	maxZoom = p.getMaxZoom();
	zoom = 5;
	contrast = 0.1f;
	handler = h;
	glView = c.getResources().getBoolean(R.bool.GL_view);
	inverted = false;
	stopView = false;
	lightOn = false;
    }
    
    private static int maxZoom;
    private static int zoom;
    public static void setZoom(int zoom) {
	if(zoom > maxZoom || zoom < 0) {
	    zoom = zoom < 0 ? 0 : maxZoom;
	    handler.dispatchMessage(handler.obtainMessage(R.id.vibrate_limit));
	}

	Settings.zoom = zoom;
	handler.dispatchMessage(handler.obtainMessage(R.id.zoom));
    }
    public static int getZoom() {
	return zoom;
    }
    
    
    private static boolean inverted;
    public static boolean isInverted() {
	return inverted;
    }
    public static void setInverted(boolean inverted) {
	if(Settings.inverted == inverted)
	    return;
	Settings.inverted = inverted;
	if(Settings.inverted)
	    handler.dispatchMessage(handler.obtainMessage(R.id.invert, false));
	else
	    handler.dispatchMessage(handler.obtainMessage(R.id.invert, true));
	
    }
    
    
    private static boolean glView;
    public static boolean isGlView() {
	return glView;
    }
    /**
     * 0 - klasicke
     * 1 - cernobile
     * 2 - zlutocerne
     */
    private static int viewType;
    public static void setViewType(int vt) {
	if(vt > 2 || vt < 0) vt = 0;
	if(vt >= 1) glView = true;
	else glView = false;
	viewType = vt;
		
	handler.dispatchMessage(handler.obtainMessage(R.id.view_type, viewType, 0));
    }
    public static int getViewType() {
	return viewType;
    }
    
    private static boolean stopView;
    public static void setStopView(boolean stop) {
	stopView = stop;
	
	handler.dispatchMessage(handler.obtainMessage(R.id.view_stop));
    }
    public static boolean isStopView() {
	return stopView;
    }
    
    
    private static float contrast;
    public static void setContrast(float add) {
	if(add > 0)
	    contrast *= add;
	else
	    contrast /= -add;
	
	if(contrast < 0.1f) {
	    contrast = 0.1f;
	    handler.dispatchMessage(handler.obtainMessage(R.id.vibrate_limit));
	}
	if(contrast > 200) {
	    contrast = 200;
	     handler.dispatchMessage(handler.obtainMessage(R.id.vibrate_limit));
	}
    }
    public static float getContrast() {
	return contrast;
    }
    
    
    private static boolean lightOn;
    public static void setLightOn(boolean b) {
	lightOn = b;
	
	handler.dispatchMessage(handler.obtainMessage(R.id.light));
    }
    public static boolean isLightOn() {
	return lightOn;
    }
    
    
}
