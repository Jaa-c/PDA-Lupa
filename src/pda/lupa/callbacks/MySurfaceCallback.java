package pda.lupa.callbacks;

import pda.lupa.MyGLSurfaceView;
import pda.lupa.Lupa;
import android.R;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

    /**
     * Callbeck pro surface
     */
    public final class MySurfaceCallback implements SurfaceHolder.Callback {
	Activity activity;
	Lupa lupa;
	
	Camera camera;
	SurfaceHolder prevHolder;
	
	MyGLSurfaceView glCallback;
	

	public MySurfaceCallback(Activity activity, Lupa lupa) {
	    this.lupa = lupa;
	    this.activity = activity;
	    
	    this.glCallback = lupa.getGlCallback();
	    
	    camera = lupa.getCamera();
	    prevHolder = lupa.getPrevHolder();
	    

	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    try {
		if(true) {//nejaka podminka kdyz ho budu chtit zobrazit
		    camera.setPreviewCallback(new MyPreviewCallback(glCallback));
		}
		//else
		camera.setPreviewDisplay(prevHolder);
	    }
	    catch (Throwable t) {
		Log.e("PreviewDemo-surfaceCallback",
			"Exception in setPreviewDisplay()", t);
		Toast.makeText(activity, t.getMessage(), Toast.LENGTH_LONG).show();
	  }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    Camera.Parameters parameters = camera.getParameters();
	    Camera.Size size = getBestPreviewSize(width, height, parameters);  
	
	    if (size!=null) {
		parameters.setPreviewSize(size.width, size.height);
		
		//musime nastavit danou velikost do GLSurfaceView
		this.glCallback.setPreviewSize(size);
		
		camera.setParameters(parameters);
		camera.startPreview(); // tady ZACINA ZOBRAZENI NAHLEDU
		
		lupa.setInPreview(true);
		lupa.zoom(5);
		lupa.focus();
	    }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    lupa.close();
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
    }
