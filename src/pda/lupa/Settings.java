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
	handler = h;
	glView = c.getResources().getBoolean(R.bool.GL_view);
	inverted = false;
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
    
    
}
