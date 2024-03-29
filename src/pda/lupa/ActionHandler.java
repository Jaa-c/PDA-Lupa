package pda.lupa;

import android.os.Handler;
import android.os.Message;

public class ActionHandler extends Handler {
    private Lupa lupa;

    public ActionHandler(Lupa lupa) {
	this.lupa = lupa;
    }
    
    /**
     * Stara se o jednotlive zpravy a uruje, co se bude delat
     * @param message 
     */
    @Override
    public void handleMessage(Message message) {	
	switch (message.what) {
	    case R.id.auto_focus:
		if(!lupa.inPreview()) return;
		lupa.focus();
		break;
	    case R.id.zoom:
		if(!lupa.inPreview()) return;
		lupa.zoom();
		break;
 	    case R.id.vibrate_limit:
		lupa.vibrate(100);
		break;
	    case R.id.invert:
		lupa.invert((Boolean)message.obj);
		break;
	    case R.id.view_type:
		if(!Settings.isStopView()) 
		    lupa.changeView(message.arg1);
		else
		    lupa.changeView(0);
		break;	
	    case R.id.view_stop:
		if(Settings.isStopView()) {
		    lupa.setBitmapData();
		    lupa.changeView(0);
		}
		else
		    lupa.changeView(Settings.getViewType());
		break;
	    case R.id.light:
		if(!Settings.isLightOn())
		    lupa.light(false);
		else
		    lupa.light(true);
		break;
	 }
    }   
}