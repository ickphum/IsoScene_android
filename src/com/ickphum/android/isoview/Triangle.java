package com.ickphum.android.isoview;

import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.ickphum.android.isoscene.R;

public class Triangle {
	
	private RenderGroup rg;

	int n = 10;
	
	public Triangle(GlRenderer renderer) {
		
		int textureID = -1;
		textureID = renderer.getTextureId(R.drawable.select_texture);
		List<Integer> singleColor = null;
		//singleColor = Arrays.asList(0xff, 0, 0, 0x7f);
		this.rg = new RenderGroup("Triangle", GL10.GL_TRIANGLES, textureID, singleColor);

		byte colors[] = {
				(byte)0xff, 0x00, 0x00, (byte) 0x7f,
				(byte)0xff, 0x00, 0x00, (byte) 0x7f,
				(byte)0xff, 0x00, 0x00, (byte) 0x7f,
				(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
				(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
				(byte)0xff, (byte)0xff, 0x00, (byte) 0xff,
		};

		
		float size = 50f;
		for (int i = 0; i < n; i++) {
			float x_offset = (float)Math.random() * 800f;
			float y_offset = (float)Math.random() * 400f;
			float new_vertices[] = {
					size * -0.86f + x_offset, size * 0.5f + y_offset,
					size * 0.0f + x_offset,   size * 0.0f + y_offset,
					size * -0.86f + x_offset, size * 1.5f + y_offset,
					
					size * 0.0f + x_offset,   size * 0.0f + y_offset,
					size * -0.86f + x_offset, size * 1.5f + y_offset,
					size * 0.0f + x_offset,   size * 1.0f + y_offset,
			};
			float new_texture_coords[] = {
					1, 1,
					0, 1,
					1, 0,
					
					0, 1,
					1, 0,
					0, 0
			};

			rg.addVertices(new_vertices);
			if (textureID >= 0)
				rg.addTextureCoords(new_texture_coords);
			else
				if (singleColor == null)
					rg.addColors(colors);
			
			
		}
		
		rg.finishBuffers();

	}
	
	public void draw(GL10 gl) {
		//Log.d("draw", "test");

		rg.draw(gl);
		
	}
}