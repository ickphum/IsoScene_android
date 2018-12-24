package com.ickphum.android.isoview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

public class RenderGroup {
	private FloatBuffer vertexBuffer;   // buffer holding the vertices
	private FloatBuffer textureBuffer;
	private ByteBuffer colorBuffer;

	int mTextureID;
	int mVertexCount = 0;
	int mGLDrawType;
	float[] mSingleColor = null;
	String mName;
	
	// render with a texture 
	public RenderGroup(String name, int glDrawType, int textureID) {
		this(name, glDrawType, textureID, null);
	}
	
	// render with single color 
	public RenderGroup(String name, int glDrawType, List<Integer> singleColor) {
		this(name, glDrawType, -1, singleColor);
	}
	
	// render with color array
	public RenderGroup(String name, int glDrawType) {
		this(name, glDrawType, -1, null);
	}
	
	// base constructor
	public RenderGroup(String name, int glDrawType, int textureID, List<Integer> singleColor) {
		
		mName = name;
		mTextureID = textureID;
		mGLDrawType = glDrawType;
		
		if (mTextureID > -1)
			initBuffers(1000, 1000, 0);
		else 
			if (singleColor != null) {
				initBuffers(1000,0,0);
				setSingleColor(singleColor);
			}
			else
				initBuffers(1000, 0, 1000);
	
	}
	
	// set or reset single color
	void setSingleColor(List<Integer> singleColor) {
		if (mSingleColor == null)
			mSingleColor = new float[4];
		for (int i = 0; i<4; i++) {
			mSingleColor[i] = (float) singleColor.get(i) / (float) 0xff;
		}		
	}

	void initBuffers(int vertexBytes, int textureBytes, int colorBytes) {
		vertexBuffer = initFloatBuffer(vertexBytes);
		textureBuffer = (textureBytes > 0)
			? initFloatBuffer(textureBytes)
			: null;
		colorBuffer = (colorBytes > 0)
			? ByteBuffer.allocateDirect(colorBytes)
			: null;
	}

	void clearBuffers() {
		vertexBuffer.clear();
		if (textureBuffer != null)
			textureBuffer.clear();
		if (colorBuffer != null)
			colorBuffer.clear();
		return;
	}

	private FloatBuffer initFloatBuffer(int sizeInBytes) {	
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sizeInBytes);
		byteBuffer.order(ByteOrder.nativeOrder());
		return byteBuffer.asFloatBuffer();
	}

	void finishBuffers() {
		// store count, reset buffer positions and check counts in texture/color buffer
		
		mVertexCount = vertexBuffer.position();
		Log.d("finishBuffers", "mVertexCount = " + mVertexCount);
		vertexBuffer.position(0);
		
		// assume we've either supplied a texture or a color for each vertex.
		// We're counting elements, not bytes.
		// Count should match for textures (2 float texture coords per 2 float vertex)
		// or be 2 times for colors (4 color bytes per 2 float vertex).
		if (textureBuffer != null) {
			if (textureBuffer.position() != mVertexCount) 
				Log.e("finishBuffers", String.format("vertex pos %d != texture position %d", mVertexCount, textureBuffer.position()));
			textureBuffer.position(0);
		}
		if (colorBuffer != null) {
			if (colorBuffer.position() != (int)( mVertexCount * 2)) 
				Log.e("finishBuffers", String.format("vertex pos %d != color position %d", mVertexCount, colorBuffer.position()));
			colorBuffer.position(0);					
		}
	}

	public FloatBuffer addVertices(float[] points) {
		return vertexBuffer = addFloats(vertexBuffer, points);
	}
	public FloatBuffer addVertices(float x1, float y1, float x2, float y2) {
		float[] points = { x1,y1,x2,y2 };
		return vertexBuffer = addFloats(vertexBuffer, points);
	}
	public FloatBuffer addVertices(List<Float> points) {
		return vertexBuffer = addFloats(vertexBuffer, points);
	}

	
	private FloatBuffer addFloats(FloatBuffer floatBuffer, List<Float> points) {
		float[] array = new float[points.size()];
		int i = 0;
		for (Float f : points) {
			array[i++] = f;
		}
		return addFloats(floatBuffer, array);
	}

	public FloatBuffer addTextureCoords(float[] coords) {
		return textureBuffer = addFloats(textureBuffer, coords);
	}
	
	public ByteBuffer addColors(byte[] indexes) {
		return colorBuffer = addBytes(colorBuffer, indexes);
	}
	
	public FloatBuffer addFloats(FloatBuffer buffer, float[] points) {
		
		if (buffer.position() + points.length > buffer.capacity()) {
			
			// grow to required size + 50%
			int newCapacity = (buffer.position() + points.length) * 6;
			Log.d("addFloats", "grow to " + newCapacity);

			FloatBuffer newBuffer = initFloatBuffer(newCapacity);

			// when we copy the existing buffer, it will copy from current position to limit 
			// so set limit to position and position to 0.
			buffer.limit(buffer.position());
			buffer.position(0);
			
			// copy the buffer and swap it in
			newBuffer.put(buffer);
			buffer = newBuffer;
		}

		// add the new points
		buffer.put(points);
		
		return buffer;
	}

	public ByteBuffer addBytes(ByteBuffer buffer, byte[] points) {
		
		if (buffer.position() + points.length > buffer.capacity()) {
			
			// grow to required size + 100%
			int newCapacity = (buffer.position() + points.length) * 2;
			Log.d("addBytes", "grow to " + newCapacity);

			ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);

			// when we copy the existing buffer, it will copy from current position to limit 
			// so set limit to position and position to 0.
			buffer.limit(buffer.position());
			buffer.position(0);
			
			// copy the buffer and swap it in
			newBuffer.put(buffer);
			buffer = newBuffer;
		}

		// add the new points
		buffer.put(points);
		
		return buffer;
	}

	public void draw(GL10 gl) {
		//Log.d("draw", "test");
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		
		if (textureBuffer != null) {
	    	gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		}
		if (colorBuffer != null) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, colorBuffer);
		}

		// set the colour for the triangle
		if (mSingleColor != null)
			gl.glColor4f(mSingleColor[0],mSingleColor[1],mSingleColor[2],mSingleColor[3]);
		
		// Draw the vertices as triangles
		gl.glDrawArrays(this.mGLDrawType, 0, mVertexCount / 2);

		// if we set the color, reset to white to prevent tinting other groups
		if (mSingleColor != null)
			gl.glColor4f(1,1,1,1);

		// Disable the client state(s) before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		if (textureBuffer != null) {
	    	gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		if (colorBuffer != null) {
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
		
	}

	public void setColor(byte r, byte g, byte b, byte a) {
			
		
	}

}