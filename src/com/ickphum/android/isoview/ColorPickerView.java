package com.ickphum.android.isoview;

import com.ickphum.android.isoscene.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ColorPickerView extends View {

    // Current attribute values and Paints.
    private Paint mPaint;
	private float mCurrentHue = 0;
	private int mCurrentX = 0, mCurrentY = 0;
	private int mCurrentColor = 0;
	public int getCurrentColor() {
		return mCurrentColor;
	}

	public void setCurrentColor(int mCurrentColor) {
		this.mCurrentColor = mCurrentColor;

		// Get the current hue from the current color and update the main color field
		float[] hsv = new float[3];
		Color.colorToHSV(mCurrentColor, hsv);
		mCurrentHue = hsv[0];
		updateMainColors();
	}

	private final int[] mHueBarColors = new int[258];
	private int[] mMainColors = new int[65536];

    public ColorPickerView(Context context) {
        this(context, null, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //TypedArray a = context.getTheme().obtainStyledAttributes(
        //        attrs, R.styleable.ColorPickerView, defStyle, defStyle);

        setCurrentColor(0);

		// Initialize the colors of the hue slider bar
		int index = 0;
		for (float i=0; i<256; i += 256/42) // Red (#f00) to pink (#f0f)
		{
			mHueBarColors[index] = Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Pink (#f0f) to blue (#00f)
		{
			mHueBarColors[index] = Color.rgb(255-(int) i, 0, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Blue (#00f) to light blue (#0ff)
		{
			mHueBarColors[index] = Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Light blue (#0ff) to green (#0f0)
		{
			mHueBarColors[index] = Color.rgb(0, 255, 255-(int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Green (#0f0) to yellow (#ff0)
		{
			mHueBarColors[index] = Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Yellow (#ff0) to red (#f00)
		{
			mHueBarColors[index] = Color.rgb(255, 255-(int) i, 0);
			index++;
		}

		// Initializes the Paint that will draw the View
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(12);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSize = 100;
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minSize + getPaddingLeft() + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minSize + getPaddingTop() + getPaddingBottom(),
                                heightMeasureSpec)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
	// Get the current selected color from the hue bar
	private int getCurrentMainColor()
	{
		int translatedHue = 255-(int)(mCurrentHue*255/360);
		int index = 0;
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255-(int) i, 0, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(0, 255, 255-(int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255, 255-(int) i, 0);
			index++;
		}
		return Color.RED;
	}

	// Update the main field colors depending on the current selected hue
	private void updateMainColors()
	{
		int mainColor = getCurrentMainColor();
		int index = 0;
		int[] topColors = new int[256];
		for (int y=0; y<256; y++)
		{
			for (int x=0; x<256; x++)
			{
				if (y == 0)
				{
					mMainColors[index] = Color.rgb(255-(255-Color.red(mainColor))*x/255, 255-(255-Color.green(mainColor))*x/255, 255-(255-Color.blue(mainColor))*x/255);
					topColors[x] = mMainColors[index];
				}
				else
					mMainColors[index] = Color.rgb((255-y)*Color.red(topColors[x])/255, (255-y)*Color.green(topColors[x])/255, (255-y)*Color.blue(topColors[x])/255);
				index++;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int translatedHue = 255-(int)(mCurrentHue*255/360);
		// Display all the colors of the hue bar with lines
		for (int x=0; x<256; x++)
		{
			// If this is not the current selected hue, display the actual color
			if (translatedHue != x)
			{
				mPaint.setColor(mHueBarColors[x]);
				mPaint.setStrokeWidth(1);
			}
			else // else display a slightly larger black line
			{
				mPaint.setColor(Color.BLACK);
				mPaint.setStrokeWidth(3);
			}
			canvas.drawLine(x+10, 5, x+10, 45, mPaint);
		}

		// Display the main field colors using LinearGradient
		for (int x=0; x<256; x++)
		{
			int[] colors = new int[2];
			colors[0] = mMainColors[x];
			colors[1] = Color.BLACK;
			Shader shader = new LinearGradient(0, 50, 0, 306, colors, null, Shader.TileMode.REPEAT);
			mPaint.setShader(shader);
			canvas.drawLine(x+10, 50, x+10, 306, mPaint);
		}
		mPaint.setShader(null);

		// Display the circle around the currently selected color in the main field
		if (mCurrentX != 0 && mCurrentY != 0)
		{
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.BLACK);
			canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
		}

		// Draw a block with the currently selected color
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mCurrentColor);
		canvas.drawRect(10, 316, 266, 356, mPaint);

		// Set the text color according to the brightness of the color
		if (Color.red(mCurrentColor)+Color.green(mCurrentColor)+Color.blue(mCurrentColor) < 384)
			mPaint.setColor(Color.WHITE);
		else
			mPaint.setColor(Color.BLACK);
		canvas.drawText(getResources().getString(R.string.sample), 133, 340, mPaint);

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
		float x = event.getX();
		float y = event.getY();

		// If the touch event is located in the hue bar
		if (x > 10 && x < 266 && y > 5 && y < 45)
		{
			// Update the main field colors
			mCurrentHue = (255-x)*360/255;
			updateMainColors();

			// Update the current selected color
			int transX = mCurrentX-10;
			int transY = mCurrentY-60;
			int index = 256*(transY-1)+transX;
			if (index > 0 && index < mMainColors.length)
				mCurrentColor = mMainColors[256*(transY-1)+transX];

			// Force the redraw of the dialog
			invalidate();
		}

		// If the touch event is located in the main field
		if (x > 10 && x < 266 && y > 50 && y < 306)
		{
			mCurrentX = (int) x;
			mCurrentY = (int) y;
			int transX = mCurrentX-10;
			int transY = mCurrentY-60;
			int index = 256*(transY-1)+transX;
			if (index > 0 && index < mMainColors.length)
			{
				// Update the current color
				mCurrentColor = mMainColors[index];
				// Force the redraw of the dialog
				invalidate();
			}
		}

		return true;
	}

    public int getColor() {
    	return mCurrentColor;
    }


}
