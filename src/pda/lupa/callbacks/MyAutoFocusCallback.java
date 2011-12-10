package pda.lupa.callbacks;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public final class MyAutoFocusCallback implements Camera.AutoFocusCallback {
	
	private final String TAG = Camera.AutoFocusCallback.class.getSimpleName();
	
	private static final long AUTOFOCUS_FAIL_TIME = 2000L;
	private static final long AUTOFOCUS_OK_TIME = 5000L;
	
	private Handler autoFocusHandler;
	private int autoFocusMessage;
	
	public void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
	    this.autoFocusHandler = autoFocusHandler;
	    this.autoFocusMessage = autoFocusMessage;
	}
	
	public void onAutoFocus(boolean success, Camera camera) {
	     
	    if (autoFocusHandler != null) {
		Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
		
		if(success)
		    autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_OK_TIME);
		else
		    autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_FAIL_TIME);
		    
	    } else {
		Log.d(TAG, "Got auto-focus callback, but no handler for it");
	    }
	}
    }
