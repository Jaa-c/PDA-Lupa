package pda.lupa;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

}