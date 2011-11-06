package pda.lupa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView  implements GLSurfaceView.Renderer {
    Context context;
    
    /** buffer, do ktereho se nacita obrazek nahledu */
    private byte[] cameraFrame;
    /** ukazatel na texturu */
    private int[] cameraTexture;
    /** velikost zobrazovaneho nahledu,
     * nastavuje se v SetPreviewSize */
    private int prevY, prevX = 0;
      
    /** bufferm obsahuje mapovani textury */
    private FloatBuffer textureBuffer;
    private float texture[] = {
	0.0f, 1.0f, // 2
	0.0f, 0.0f, // 1
	1.0f, 1.0f, // 4
	1.0f, 0.0f  // 3
    };
    
    /** bufferm obsahuje souradnice vrcholu */
    private FloatBuffer vertexBuffer;
    private float vertices[] = {
	-1.0f, -1.0f,  0.0f, // 1
	-1.0f,  1.0f,  0.0f, // 2
	 1.0f, -1.0f,  0.0f, // 3
	 1.0f,  1.0f,  0.0f  // 4
    };
    
    /**
     * Konstruktor, vola se hned z XML layoutu v main activity
     * @param c globalni kontext
     * @param a atributy (id etc.)
     */
    public MyGLSurfaceView(Context c, AttributeSet a) {
	super(c, a);
	//this.context = c;
	
	//nastaveni pruhlednosti pozadi!
	this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	//nastavime render
	this.setRenderer(this);		
	//prej to je na nekterych zarizeni treba
	this.setZOrderOnTop(true);
	
	// predpripravime si ctverec, vytvorime buffery
	//zda se, ze to takhle zbesile delaj vsichni
	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
	byteBuffer.order(ByteOrder.nativeOrder());
	vertexBuffer = byteBuffer.asFloatBuffer();
	vertexBuffer.put(vertices);
	vertexBuffer.position(0);

	byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
	byteBuffer.order(ByteOrder.nativeOrder());
	textureBuffer = byteBuffer.asFloatBuffer();
	textureBuffer.put(texture);
	textureBuffer.position(0); 
    }
    
    /**
     * Vytvoreni plochy
     * @param gl
     * @param config 
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	// povolime textury
	gl.glEnable(GL10.GL_TEXTURE_2D);
	//povolime Smooth Shading, potreba vyzkouset
	gl.glShadeModel(GL10.GL_SMOOTH);
	//pruhledne pozadi
	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	//depth buffer a depth testing, zjistit...
	gl.glClearDepthf(1.0f);
	gl.glEnable(GL10.GL_DEPTH_TEST);
	gl.glDepthFunc(GL10.GL_LEQUAL); 
	
	//perspektiva, casem asi zbytecny
	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }
    
    /**
     * Vola se pri zmene povrchu a pri jeho vytvoreni...
     * @param gl
     * @param width
     * @param height 
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) { 
	if(height == 0) return;
	//reset viewport
	gl.glViewport(0, 0, width, height);
	//vybrat projection matrix
	gl.glMatrixMode(GL10.GL_PROJECTION);
	// a matici vyresetovat
	gl.glLoadIdentity();

	//tohle je treba nejak predelat!! TODO
	GLU.gluPerspective(gl, 25.0f, (float)height / (float)width, 0.1f, 10.0f);

	//vybrat modelview matrix
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	gl.glLoadIdentity(); 
    }

    /**
     * Vykresleni framu
     * @param gl 
     */
    public void onDrawFrame(GL10 gl) {
	if(cameraFrame == null) return;
	
	//vymazat buffery
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	// porad modelview matrix
	gl.glLoadIdentity();
	// posunme TODO
	gl.glTranslatef(0.0f, 0.0f, -5.0f);
	
	//nabindujeme aktualni texturu
	bindCameraTexture(gl);
	//normala povrchu
	gl.glNormal3f(0,0,1);
	// povolime buffery
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	// nastavit face rotation
	gl.glFrontFace(GL10.GL_CW);
	// ukazatel na vertex buffer
	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	
	// vykrelime ctverec
	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

	//vymazat stav
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	
	//gl.glBindTexture(GL10.GL_TEXTURE_2D, cameraTexture[0]);
    }
 
  
    /**
     * 90% ukradeno
     * @see http://nhenze.net/?p=107
     * Volana pri kazdem novem framu
     * @param bytes data z kamery v divnym formatu yuvs :)
     */
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	if(this.prevX == 0) return;	
	int bwCounter=0;
	int yuvsCounter=0;
	for (int x=0; x < this.prevX; x++) {
		System.arraycopy(yuvsSource, yuvsCounter, this.cameraFrame, bwCounter, this.prevY);
		yuvsCounter=yuvsCounter+this.prevY;
		bwCounter=bwCounter+this.prevY;
	}
    }
    
    /**
     * 100% ukradeno
     * @see http://nhenze.net/?p=172
     */
    void bindCameraTexture(GL10 gl) {
	if(cameraFrame == null) return;
	    try {
		if (cameraTexture==null)
			cameraTexture=new int[1];
		else
			gl.glDeleteTextures(1, cameraTexture, 0);

		gl.glGenTextures(1, cameraTexture, 0);
		int tex = cameraTexture[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, 
			this.prevX, this.prevY, 0, GL10.GL_LUMINANCE, 
			GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(this.cameraFrame));

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    }
	    catch (Exception e) {
		Log.d("bindcameratexture", "" + e.getMessage());
	    }
    }
    
    /**
     * Nastavi aktualni velikost zobrazovaneho nahledu
     * @param size Velikost aktualniho nahledu!
     */
    public void setPreviewSize(Camera.Size size) {
	//zjistime rozmery nahledu
	this.prevX = size.width;
	this.prevY = size.height;
	// vytvorime buffer pro obrazek o velikosti sirka x vyska
	cameraFrame = new byte[prevX*prevY];
    }
}