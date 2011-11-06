package pda.lupa.callbacks;

import pda.lupa.MyGLSurfaceView;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

final public class MyPreviewCallback implements Camera.PreviewCallback {
    MyGLSurfaceView glCallback;

    MyPreviewCallback(MyGLSurfaceView glCallback) {
	this.glCallback = glCallback;
    }

    public void onPreviewFrame(byte[] bytes, Camera camera) {
	if(glCallback != null) 
	    glCallback.onPreviewFrame(bytes, camera);
    }
   
}
