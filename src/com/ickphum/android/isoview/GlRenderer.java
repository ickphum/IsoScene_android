package com.ickphum.android.isoview;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

public class GlRenderer implements Renderer {
    private Triangle    triangle;   // the square to be drawn

    /** Constructor */
    public GlRenderer() {
    }
    
    private float mOriginX = 0;
    private float mOriginY = 0;
    private final float[] mProjMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig egl) {
        // Set the background frame color
    	this.triangle = new Triangle();
    	gl.glClearColor(0.0f, 0.0f, 0.3f, 1.0f);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) {                       //Prevent A Divide By Zero By
			height = 1;                         //Making Height Equal One
		}

		Log.d("onSurfaceChanged", "width " + width + ", height " + height);
		gl.glViewport(0, 0, width, height);     //Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
		gl.glLoadIdentity();                    //Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		//GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
		gl.glOrthof(0f, 85f, 0f, 62f, 0.0f, 20.0f);
		//gl.glOrthof(-7.65f, 7.65f, -5f, 5f, 0.0f, 20.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Modelview Matrix
		gl.glLoadIdentity();                    //Reset The Modelview Matrix

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		
		//Log.d("onDrawFrame", "test");

		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();

		// Drawing
		gl.glTranslatef(mOriginX, mOriginY, -5.0f);     // move 5 units INTO the screen
		// is the same as moving the camera 5 units away
		triangle.draw(gl);                      // Draw the square

		//gl.glTranslatef(5.0f, 0.0f, -15.0f);
		//triangle.draw(gl);
		
		/*
		gl.glTranslatef(-20.0f, 20.0f, -50.0f);
		for (int i = 0; i<100; i++) {
			for (int j = 0; j<100; j++) {
				gl.glTranslatef(0.5f, 0.0f, 0.0f);
				triangle.draw(gl);                  
			}
			gl.glTranslatef(-50.0f, -1.0f, 0.0f);
		}
		*/
	}

	public void moveOrigin(float dx, float dy) {
		//Log.d("moveOrigin", "dx " + dx + ", dy " + dy);
		mOriginX += dx;
		mOriginY += dy;
	}
	
}
