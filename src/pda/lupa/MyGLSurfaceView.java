package pda.lupa;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView  implements GLSurfaceView.Renderer {
    Context context;
    private PointF surfaceSize;
    private PointF offset;
    private ShortBuffer triangleBuffer; 
    
    
    public MyGLSurfaceView(Context c, AttributeSet a) {
		super(c, a);
		this.context = c;
			
		//nastaveni pruhlednosti pozadi!
		this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		//nastavime render
		this.setRenderer(this);		
		
		//prej to je na nekterych zarizeni treba
		this.setZOrderOnTop(true);

		surfaceSize = new PointF();
		offset = new PointF();	
    }



  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	    gl.glClearColor(0, 0, 0, 0);
	    gl.glShadeModel(GL10.GL_SMOOTH);
	    gl.glEnable(GL10.GL_DEPTH_TEST);
  }

  public void onSurfaceChanged(GL10 gl, int width, int height) {

	surfaceSize.set(width/2, height/2);
	gl.glViewport(0, 0, (int) surfaceSize.x, (int) surfaceSize.y);

	// Create our triangle.
	final int div = 1;
	short[] triangles = {
	    0, 0, 0,
	    0, (short) (surfaceSize.y / div), 0,
	    (short) (surfaceSize.x / div), (short) (surfaceSize.y / div), 0,
	};
	triangleBuffer = ShortBuffer.wrap(triangles);

	//gl.glClearColor(1f, 1f, 1f, 0.5f);
	//gl.glEnable(GL10.GL_CULL_FACE);
	gl.glShadeModel(GL10.GL_SMOOTH);
	//gl.glEnable(GL10.GL_DEPTH_TEST);




	// Set our field of view.
	gl.glMatrixMode(GL10.GL_PROJECTION);
	gl.glLoadIdentity();
	gl.glFrustumf(
	    -surfaceSize.x / 2, surfaceSize.x / 2,
	    -surfaceSize.y / 2, surfaceSize.y / 2,
	    1, 3);

	// Position the camera at (0, 0, -2) looking down the -z axis.
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	gl.glLoadIdentity();
	// Points rendered to z=0 will be exactly at the frustum's
	// (farZ - nearZ) / 2 so the actual dimension of the triangle should be
	// half
	gl.glTranslatef(0, 0, -2);
     
     
  }

  public void onDrawFrame(GL10 gl) {
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	gl.glPushMatrix();


	gl.glTranslatef(offset.x, offset.y, 0);

	gl.glColor4f(1.0f, 0.3f, 0.0f, .5f);
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glVertexPointer(3, GL10.GL_SHORT, 0, triangleBuffer);
	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 3);
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

	gl.glPopMatrix();
       
       
  }

    /**
     * Volana pri kazdem novem framu
     * @param bytes data z kamery v divnym formatu :)
     */
    public void onPreviewFrame(byte[] bytes, Camera camera) {
	//Log.d("callback", "GL callback");
	Camera.Size size = camera.getParameters().getPreviewSize();
    }
}
