package pda.lupa;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * detekuje uzivatelsky vstup
 */
public class MyGestureDetector extends SimpleOnGestureListener {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    public MyGestureDetector() {
	super();
    }
    
    /**
     * potvrzene kliknuti na obrazovku
     * @param e
     * @return 
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
	if(Settings.isStopView()) {
	    Settings.setStopView(false);
	    return true;
	}
	Settings.setStopView(true);

	return true;
    }


    /**
     * tahnuti na nejakou stranu
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return 
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	try {
	    //left
	    if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		Settings.setInverted(false);
	    } 
	    //right
	    else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		Settings.setInverted(true);
	    }
	    //top
	    else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		if(Settings.isStopView())
		    Settings.setContrast(+2.5f);
		else
		    Settings.setZoom(Settings.getZoom()+1);
	    } 
	    //bottom
	    else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		if(Settings.isStopView())
		    Settings.setContrast(-3f);
		else
		    Settings.setZoom(Settings.getZoom()-1);
	    }
	} catch (Exception e) {
	    // nic..
	}
	return true;
    }

}
