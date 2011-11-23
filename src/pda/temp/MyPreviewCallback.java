package pda.temp;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Date;

final public class MyPreviewCallback extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    
    /** buffer, do ktereho se nacita obrazek nahledu */
    private byte[] cameraFrame;
    
    /** buffer pro nacteni dat z kamery*/
    private byte[] previewBuffer1;
    /** velikost zobrazovaneho nahledu,
     * nastavuje se v SetPreviewSize */
    private int prevY, prevX = 0;
    
    /** Kvuli bufferum potrebuju instanci kamery */
    private Camera camera = null;
    
    SurfaceHolder myHolder;

    
    MyPreviewCallback(Context c) {
	super(c);
	
	
	myHolder = this.getHolder();
	myHolder.addCallback(this);
    }
    
    public void surfaceCreated(SurfaceHolder sh) {
	
    }

    public void surfaceChanged(SurfaceHolder sh, int i, int i1, int i2) {
	
    }

    public void surfaceDestroyed(SurfaceHolder sh) {
	
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
	
	System.arraycopy(yuvsSource, 0, this.cameraFrame, 0, this.cameraFrame.length-1);
	
	camera.addCallbackBuffer(yuvsSource);
    }
    
    public void setPreviewSize(Camera.Size size, Camera camera) {
	//zjistime rozmery nahledu
	this.prevX = size.width;
	this.prevY = size.height;
	
	this.camera = camera;
	// vytvorime buffer pro obrazek o velikosti sirka x vyska
	
	this.cameraFrame = new byte[prevX*prevY];
	
	this.previewBuffer1 = new byte[prevX*prevY*12/8];
	this.camera.addCallbackBuffer(this.previewBuffer1);
    }


   
}
