package com.ickphum.android.isoview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import android.util.Log;

public class Triangle {

	private FloatBuffer vertexBuffer;   // buffer holding the vertices
	private ByteBuffer colorBuffer;

	/*
	private float vertices[] = {
			-0.86f, 0.5f,  0.0f,
			0.0f,  0.0f,  0.0f,
			-0.86f, 1.5f,  0.0f,

			0.0f,  0.0f,  0.0f,
			-0.86f, 1.5f,  0.0f,
			0.0f,  1.0f,  0.0f,
			
			0.0f,  1.0f,  0.0f,
			0.86f, 1.5f,  0.0f,
			0.0f,  0.0f,  0.0f,

			0.86f, 1.5f,  0.0f,
			0.0f,  0.0f,  0.0f,
			0.86f, 0.5f,  0.0f,
	};

	private byte colors[] = {
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,

			0x00, (byte)0xff, (byte)0xff, (byte) 0xff,
			0x00, (byte)0xff, (byte)0xff, (byte) 0xff,
			0x00, (byte)0xff, (byte)0xff, (byte) 0xff,
			
			0x00, 0x00, (byte)0xff, (byte) 0xff,
			0x00, 0x00, (byte)0xff, (byte) 0xff,
			0x00, 0x00, (byte)0xff, (byte) 0xff,
	};
	*/

	private byte colors[] = {
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			(byte)0xff, 0x00, 0x00, (byte) 0xff,
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
			(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
	};

	int n = 2000;
	
	public Triangle() {
		/*
		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		vertexByteBuffer.order(ByteOrder.nativeOrder());
		
		// allocates the memory from the byte buffer
		vertexBuffer = vertexByteBuffer.asFloatBuffer();

		// fill the vertexBuffer with the vertices
		vertexBuffer.put(vertices);

		// set the cursor position to the beginning of the buffer
		vertexBuffer.position(0);
		
		Log.d("Triangle", "color length " + colors.length);

		colorBuffer = ByteBuffer.allocateDirect(colors.length);
		colorBuffer.put(colors);
		colorBuffer.position(0);
		*/
		
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(n * 6 * 3 * 4);
		vertexByteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = vertexByteBuffer.asFloatBuffer();
		
		colorBuffer = ByteBuffer.allocateDirect(n * 6 * 4);
		
		for (int i = 0; i < n; i++) {
			float x_offset = (float)Math.random() * 85f;
			float y_offset = (float)Math.random() * 62f;
			float new_vertices[] = {
					-0.86f + x_offset, 0.5f + y_offset,  0.0f,
					0.0f + x_offset,  0.0f + y_offset,  0.0f,
					-0.86f + x_offset, 1.5f + y_offset,  0.0f,
					0.0f + x_offset,  0.0f + y_offset,  0.0f,
					-0.86f + x_offset, 1.5f + y_offset,  0.0f,
					0.0f + x_offset,  1.0f + y_offset,  0.0f,
			};

			vertexBuffer.put(new_vertices);
			colorBuffer.put(colors);
			
		}

		vertexBuffer.position(0);
		colorBuffer.position(0);
		
	}

	public void draw(GL10 gl) {
		Log.d("draw", "test");
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		// set the colour for the triangle
		//gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, colorBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, n * 6);

		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}

	public void draw(GLES10 gl) {
		// TODO Auto-generated method stub
		
	}
}