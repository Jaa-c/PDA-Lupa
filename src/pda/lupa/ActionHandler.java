package pda.lupa;

import android.os.Handler;
import android.os.Message;

public class ActionHandler extends Handler {
    Lupa lupa;

    public ActionHandler(Lupa lupa) {
	this.lupa = lupa;
    }
    
    @Override
    public void handleMessage(Message message) {
	switch (message.what) {
	    case R.id.auto_focus:
		if (lupa.inPreview()) {
		    lupa.focus();
		    //lupa.focus(this, R.id.auto_focus);
		}
		break;
      
	 }
    }
    
}
