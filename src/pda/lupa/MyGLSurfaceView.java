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
    
    private byte[] cameraFrame;
    private int[] cameraTexture;
    
    private int prevY, prevX = 0;
    
    FloatBuffer cubeBuff;
    FloatBuffer texBuff;
    
    private Square square; 
    
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
	
	 this.square = new Square();
    }
    
    public void setPreviewSize(Camera.Size size) {
	
	//zjistime rozmery nahledu!
	this.prevX = size.width;
	this.prevY = size.height;
	int s = prevX*prevY;
	Log.d("negative size", " " + s);
	
	cameraFrame = null;//new byte[s];
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	// Load the texture for the square

	gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
	gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
	gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
	gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
	gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
	

	//Really Nice Perspective Calculations
	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    
    }
    
    public void onSurfaceChanged(GL10 gl, int width, int height) { 
	if(height == 0) { 						//Prevent A Divide By Zero By
		height = 1; 						//Making Height Equal One
	}
	

	gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
	gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
	gl.glLoadIdentity(); 					//Reset The Projection Matrix

	//Calculate The Aspect Ratio Of The Window
	GLU.gluPerspective(gl, 25.0f, (float)height / (float)width, 0.1f, 10.0f);
	
	//square.draw(gl);

	gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
	gl.glLoadIdentity(); 

    }

    public void onDrawFrame(GL10 gl) {
	// clear Screen and Depth Buffer
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	// Reset the Modelview Matrix
	gl.glLoadIdentity();

	square.loadGLTexture(gl, this.context);
	// Drawing
	gl.glTranslatef(0.0f, 0.0f, -5.0f);		// move 5 units INTO the screen
							// is the same as moving the camera 5 units away
	//square.draw(gl);
    }
 
    Bitmap bitmap = null;
  
    /**
     * Volana pri kazdem novem framu
     * @param bytes data z kamery v divnym formatu :)
     */
    public void onPreviewFrame(byte[] yuvsSource, Camera camera) {
	if(this.prevX == 0) return;
	//Log.d("callback", this.prevX + " " + this.prevY);
	
	cameraFrame = new byte[yuvsSource.length];
	
	int bwCounter=0;
	int yuvsCounter=0;
	for (int x=0; x < this.prevX; x++) {
		System.arraycopy(yuvsSource, yuvsCounter, this.cameraFrame, bwCounter, this.prevY);
		yuvsCounter=yuvsCounter+this.prevY;
		bwCounter=bwCounter+this.prevY;
	}
	/*{if(bitmap == null) {
	    bitmap = BitmapFactory.decodeByteArray(cameraFrame, 0, yuvsSource.length-1);
	}*/
	
    }
    
    /**
     * Generates a texture from the black and white array filled by the onPreviewFrame
     * method.
     */
    void bindCameraTexture(GL10 gl) {
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
    
    FloatBuffer makeFloatBuffer(float[] arr) {
	ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
	bb.order(ByteOrder.nativeOrder());
	FloatBuffer fb = bb.asFloatBuffer();
	fb.put(arr);
	fb.position(0);
	return fb;
    }
    
    private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
    private float texture[] = {
		    // Mapping coordinates for the vertices
		    0.0f, 1.0f,		// top left		(V2)
		    0.0f, 0.0f,		// bottom left	(V1)
		    1.0f, 1.0f,		// top right	(V4)
		    1.0f, 0.0f		// bottom right	(V3)
    };
    

    
    private class Square {
	private FloatBuffer vertexBuffer;	// buffer holding the vertices

	private float vertices[] = {
			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
			-1.0f,  1.0f,  0.0f,		// V2 - top left
			 1.0f, -1.0f,  0.0f,		// V3 - bottom right
			 1.0f,  1.0f,  0.0f			// V4 - top right
	};

	public Square() {
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
	
	/** The texture pointer */
	private int[] textures = new int[1];

	public void loadGLTexture(GL10 gl, Context context) {
	    // loading texture
	    //if(cameraFrame == null) return;
	    
	    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.bitmap);
	    // generate one texture pointer
	    gl.glGenTextures(1, textures, 0);
	    // ...and bind it to our array
	    //gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

	    // create nearest filtered texture
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

	    // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	    
	    /*gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, 
			    prevX, prevY, 0, GL10.GL_LUMINANCE, 
			    GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(cameraFrame));*/

	    // Clean up
	    //bitmap.recycle();
	}



	/** The draw method for the square with the GL context */
	public void draw(GL10 gl) {
	    if(cameraFrame == null) return;
	    // bind the previously generated texture
	    //gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

	    // Point to our buffers
	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	    // Set the face rotation
	    gl.glFrontFace(GL10.GL_CW);
	    // set the colour for the square
	    gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);

	    // Point to our vertex buffer
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

	    // Draw the vertices as triangle strip
	    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

	    //Disable the client state before leaving
	    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

    }
}
