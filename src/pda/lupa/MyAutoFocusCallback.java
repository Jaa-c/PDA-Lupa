package pda.lupa;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/**
 * Pokus o kontinualni autofocus... Asi se nic lepsiho vymyslet neda
 */
public final class MyAutoFocusCallback implements Camera.AutoFocusCallback {
	
	
	private static final long AUTOFOCUS_FAIL_TIME = 2000L; //pri chybe
	private static final long AUTOFOCUS_OK_TIME = 5000L; //kdyz je to ok
	
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
		Log.d("MyAutoFocusCallback", "chybi handler pro callback");
	    }
	}
    }
