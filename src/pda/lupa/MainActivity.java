package pda.lupa;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity
{
    private static Lupa lupa = null;
    private MyGLSurfaceView glView;
    
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
	//glView = (MyGLSurfaceView) this.findViewById(R.id.gl_preview);
	lupa = new Lupa(this);
	lupa.open();    
    }
    
    @Override
    public void onPause() {
	super.onPause();
	lupa.close();
    }
    
    

}