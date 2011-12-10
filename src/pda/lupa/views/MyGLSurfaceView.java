package pda.lupa.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Date;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import pda.lupa.Settings;

public class MyGLSurfaceView extends GLSurfaceView  implements GLSurfaceView.Renderer, Camera.PreviewCallback {
    Context context;
    
    /** buffer, do ktereho se nacita obrazek nahledu */
    private byte[] cameraFrame;
    //ByteBuffer cameraFrame = null;
    
    /** buffer pro nacteni dat z kamery*/
    private byte[] previewBuffer1;
    /** ukazatel na texturu */
    private int[] cameraTexture;
    /** velikost zobrazovaneho nahledu,
     * nastavuje se v SetPreviewSize */
    private int prevY, prevX = 0;
    
    /** Kvuli bufferum potrebuju instanci kamery */
    private Camera camera = null;
      
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
	this.context = c;
	
	
	//spustime jako samostatne vlakno
	//new Thread(this).start();
	
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
	//gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
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
	//tady se bude rozhodovat, jestli je zobrazeno gl nebo "ne"
	if(Settings.isGlView())
	    bindCameraTexture(gl);//nabindujeme aktualni texturu
	else {
	    gl.glColor4f(1f, 0f, 0f, 0f);
	    return;
	}

	//vymazat buffery
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	// porad modelview matrix
	gl.glLoadIdentity();
	// posunme TODO
	gl.glTranslatef(0.0f, 0.0f, -5.0f);
	
	
	this.initTexture(gl);
	
	
	
	
	
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
	
	//gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_COLOR);
	switch(Settings.getViewType()) {
	    case 2:
		gl.glColor4f(1f, 1f, 0.4f, 1f);
		break;
	    default:
		gl.glColor4f(0f, 0f, 0f, 1f);
		break;
	}
	
	
	// vykrelime ctverec
	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
	
	
	//gl.glDisable(GL10.GL_COLOR_LOGIC_OP);
	//vymazat stav
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    
    public byte[] createContrast(byte[] src, double value) {
	// get contrast value
	double contrast = Math.pow((100 + value) / 100, 2);
	// scan through all pixels
	for(int x = 0; x < src.length; x++) {
	    src[x] = (byte)(((((Color.alpha(src[x]) / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	    //src[x] = (out > 255) ? (byte) 255 : out;
	    //src[x] = (out < 0) ? (byte) 0 : out;
	}
	return src;
    }

 
 
    /**
     * Volana pri kazdem novem framu
     * YUV funguje tak, ze nejdriv posila pro kazdy pixel jas 1B
     * a pote posila pro 2 pixely 1B barvy... tedy 1. 2/3 souboru
     * jsou ciste BW data, barvu muzu vklidu zahodit
     * @param bytes data z kamery v divnym formatu yuv :)
     */
    Date start;
    int fcount = 0;
    
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	if(this.prevX == 0) return;
	if(start == null){
		start = new Date();
	}
	fcount++;
	if(fcount % 100 == 0){
		double ms = (new Date()).getTime() - start.getTime();
		Log.i("AR","fps:" + fcount/(ms/1000.0));
	}
	
	System.arraycopy(yuvsSource, 0, this.cameraFrame, 0, this.cameraFrame.length-1);
		
	//this.cameraFrame = ByteBuffer.wrap(yuvsSource, 0, prevX*prevY);
	
	camera.addCallbackBuffer(yuvsSource);
    }
    
    /**
     * Po kazde, kdyz se zmeni callback je vymazana fronta bufferu,
     * pro nastaveni callbacku musime tedy pridat buffer zpet do fronty
     */
    public void initBuffer() {
	camera.addCallbackBuffer(this.previewBuffer1);
    }
    
    int tex = -1;
    void initTexture(GL10 gl) {
	if(tex != -1) return; //tohle se provede pouze jednou
	cameraTexture = new int[1];
	gl.glGenTextures(1, cameraTexture, 0);
	tex = cameraTexture[0];
	gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
	gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, 
		this.prevX, this.prevY, 0, GL10.GL_LUMINANCE, 
		GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(this.cameraFrame));

	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    }
    /**
     * insirovano zde, ale uz znacne upraveno
     * @see http://nhenze.net/?p=172
     */
    void bindCameraTexture(GL10 gl) {
	if(cameraFrame == null) return;
	try {
	    if (cameraTexture==null)
		cameraTexture=new int[1];
	    else
		gl.glDeleteTextures(1, cameraTexture, 0);
	    
	    
	    gl.glEnable(GL10.GL_TEXTURE_2D );
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
	    if(Settings.getViewType() > 1) {
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_ADD );

		//gl.glActiveTexture(GL10.GL_TEXTURE1 );
		gl.glEnable(GL10.GL_TEXTURE_2D );
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);    
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE );
	    }
	    else {
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
	    }

	    //gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
	    gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE,
		    this.prevX, this.prevY, 0, GL10.GL_LUMINANCE, 
		    GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(this.cameraFrame)); 
	}
	catch (Exception e) {
	    Log.d("bindcameratexture", "" + e.getMessage());
	}
    }
    
    /**
     * Nastavi aktualni velikost zobrazovaneho nahledu
     * @param size Velikost aktualniho nahledu!
     */
    public void setPreviewSize(Camera.Size size, Camera camera) {
	//zjistime rozmery nahledu
	this.prevX = size.width;
	this.prevY = size.height;
	
	this.camera = camera;
	// vytvorime buffer pro obrazek o velikosti sirka x vyska
	
	this.cameraFrame = new byte[prevX*prevY];
	
	this.previewBuffer1 = new byte[prevX*prevY*12/8];
	this.camera.addCallbackBuffer(this.previewBuffer1);
    }

}