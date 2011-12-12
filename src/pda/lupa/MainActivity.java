package pda.lupa;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
	getWindow().setFormat(PixelFormat.RGBA_8888);
		
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.layout.menu, menu);
	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	switch (item.getItemId()) {
	    case R.id.menu_nastaveni:
		return true;
	    case R.id.menu_barevny:
		Settings.setViewType(0);
		return true;
	    case R.id.menu_cernobily:
		    Settings.setViewType(1);
		    return true;
	    case R.id.menu_zluty:
		    Settings.setViewType(2);
		return true;
	    case R.id.menu_napoveda:
		lupa.getNapoveda().setVisibility(View.VISIBLE);
	    default:
		return super.onOptionsItemSelected(item);
	}
    }
    
    @Override
    public void onBackPressed() {
	if(lupa.getNapoveda().getVisibility() == View.VISIBLE) {
	    lupa.getNapoveda().setVisibility(View.INVISIBLE);
	    lupa.getLightButton().setVisibility(View.VISIBLE);
	    return;
	}
	this.onPause();
    }
    

}