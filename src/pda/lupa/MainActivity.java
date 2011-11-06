package pda.lupa;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity
{
    private static Lupa lupa = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	//fullscreen
	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	setContentView(R.layout.main); 
    }   
    
    
    @Override
    public void onResume() {
	super.onResume();
	lupa = new Lupa(this);
	lupa.open();    
    }
    
    @Override
    public void onPause() {
	super.onPause();
	lupa.close();
	System.exit(0);
    }
    
    

}