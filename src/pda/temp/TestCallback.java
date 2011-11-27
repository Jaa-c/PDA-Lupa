package pda.temp;

import android.hardware.Camera;
import android.util.Log;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestCallback implements Camera.PreviewCallback {
    
    Date start;
    int fcount = 0;
    byte[] buffer;
    
    
    Camera camera;
    public TestCallback(Camera camera) {
	this.camera = camera;
	init();
    }
    
    public void init() {
	buffer = new byte[120000];
	camera.addCallbackBuffer(buffer);
	
    }
    
    
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	if(start == null){
		start = new Date();
	}
	fcount++;
	if(fcount % 50 == 0){
		double ms = (new Date()).getTime() - start.getTime();
		Log.i("AR","fps:" + fcount/(ms/1000.0));
	}
	camera.addCallbackBuffer(buffer);
    }
    
}
