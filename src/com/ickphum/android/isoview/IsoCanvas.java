/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ickphum.android.isoview;

import com.ickphum.android.isoscene.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

public class IsoCanvas extends View {

    /**
     * The current destination rectangle (in pixel coordinates) into which the chart data should
     * be drawn. Chart labels are drawn outside this area.
     *
     * @see #mCurrentViewport
     */
    private Rect mContentRect = new Rect();

    // Current attribute values and Paints.
    private int mBackgroundLineColor;
    private Paint mBackgroundLinePaint;
    
    // geometry
    private float mOriginX = -100;
    private float mOriginY = -100;
    private float mScale = 0;
    private float mPreviousX;
    private float mPreviousY;

    // State objects and values related to gesture tracking.
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private Handler flingTimer = new Handler();
    
    private Path[] mPaths = new Path[3];
	private Paint[] mPaints = new Paint[3];

    /**
     * The simple math function Y = fun(X) to draw on the chart.
     * @param x The X value
     * @return The Y value
     */
    protected static float fun(float x) {
        return (float) Math.pow(x, 3) - x / 4;
    }

    public IsoCanvas(Context context) {
        this(context, null, 0);
    }

    public IsoCanvas(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IsoCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.IsoCanvas, defStyle, defStyle);

        try {
            mBackgroundLineColor = a.getColor(
                    R.styleable.IsoCanvas_backgroundLineColor, mBackgroundLineColor);
        } finally {
            a.recycle();
        }
        
		mPaints[0] = new Paint();
		mPaints[0].setColor(Color.CYAN);
		mPaints[0].setStyle(Paint.Style.FILL);
		
        float mHalfWidth = (float)4.9;
        float mHalfHeight = (float)5.6;
        float quarter_height = mHalfHeight / 2;

        for (int i = 0; i<3; i++) {
        	mPaths[i] = new Path();
        }
        
        mPaths[0].moveTo(0, 0);
        mPaths[0].lineTo(- mHalfWidth, - quarter_height);
        mPaths[0].lineTo(- mHalfWidth, quarter_height);
        mPaths[0].lineTo(0, quarter_height * 2);
        mPaths[0].setFillType(Path.FillType.WINDING);
        
        mPaths[1].moveTo(0, 0);
        mPaths[1].lineTo(10, 0);
        mPaths[1].lineTo(10, 10);
        mPaths[1].lineTo(0, 10);
        mPaths[1].lineTo(0, 0);
        mPaths[1].setFillType(Path.FillType.WINDING);

        initPaints();

        // Sets up interactions
        if (! this.isInEditMode()) {
        	mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        	mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        }
        
    }

    /**
     * (Re)initializes {@link Paint} objects based on current attribute values.
     */
    private void initPaints() {
        mBackgroundLinePaint = new Paint();
        mBackgroundLinePaint.setStrokeWidth(1);
        mBackgroundLinePaint.setAntiAlias(true);
        mBackgroundLinePaint.setColor(mBackgroundLineColor);
        mBackgroundLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentRect.set(0,0,w,h);
               
        // clear the tile cache
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minChartSize = getResources().getDimensionPixelSize(R.dimen.min_chart_size);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minChartSize + getPaddingLeft() + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minChartSize + getPaddingTop() + getPaddingBottom(),
                                heightMeasureSpec)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to drawing
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(-mOriginX, -mOriginY);
        
        for(int x = 0; x < 100; x++) {
            for(int y = 0; y < 100; y++) {
            	canvas.save();
                canvas.translate(-x * 7, -y * 7);
		        canvas.drawPath(mPaths[0], mPaints[0]);
		        //canvas.drawRect(0.0f, 0.0f, 5.0f, 5.0f, mPaints[0]);
		        canvas.restore();
            }
        }
        
        
        canvas.drawLine(mContentRect.left, mContentRect.top,
        		mContentRect.right, mContentRect.bottom, mBackgroundLinePaint);
        canvas.drawLine(mContentRect.left, mContentRect.bottom,
        		mContentRect.right, mContentRect.top, mBackgroundLinePaint);
        
        for(int x = -1000; x < 1000; x+=200) {
            for(int y = -1000; y < 1000; y+=200) {
            	canvas.drawText(x + "," + y, x, y, mBackgroundLinePaint);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Finds the chart point (i.e. within the chart's domain and range) represented by the
     * given pixel coordinates, if that pixel is within the chart region described by
     * {@link #mContentRect}. If the point is found, the "dest" argument is set to the point and
     * this function returns true. Otherwise, this function returns false and "dest" is unchanged.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private float lastSpanX;
        private float lastSpanY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            lastSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            Log.d("onScaleBegin", "lastSpanX = " + lastSpanX + ", lastSpanY = " + lastSpanY);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float spanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            float spanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();
            Log.d("onScale", "spanX = " + spanX + ", spanY = " + spanY
            		+ ", focusX = " + focusX + ", focusY = " + focusY);

            ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);

            lastSpanX = spanX;
            lastSpanY = spanY;
            return true;
        }
    };

	protected float mFlingVelocityX;
	protected float mFlingVelocityY;
	protected float mStartFlingVelocityY;
	protected float mStartFlingVelocityX;

    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            Log.d("onDown", e.getX() + "," + e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            Log.d("onDoubleTap", e.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	//Log.d("onScroll", "e1 " + e1 + ", e2 " + e2 + ", distanceX " + distanceX + ", distanceY " + distanceY);
        	Log.d("onScroll", "distanceX " + distanceX + ", distanceY " + distanceY);
        	mOriginX += distanceX;
        	mOriginY += distanceY;
            ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	Log.d("onFling", "velocityX " + velocityX + ", velocityY " + velocityY);
        	mFlingVelocityX = velocityX * 0.8f;
        	mFlingVelocityY = velocityY * 0.8f;
        	mStartFlingVelocityX = velocityX;
        	mStartFlingVelocityY = velocityY;
        	flingTimer.postDelayed(flingTimerCallback, 20);

            return true;
        }
    };


    private Runnable flingTimerCallback = new Runnable() {
    	@Override
    	public void run() {
    		
    		/*
    		float factor = 0.35f;
    		mFlingVelocityX -= (mStartFlingVelocityX - mFlingVelocityX) * factor;
    		mFlingVelocityY -= (mStartFlingVelocityY - mFlingVelocityY) * factor;
    		
    		boolean xInProgress = mStartFlingVelocityX < 0
    				? mFlingVelocityX < 0
    				: mFlingVelocityX > 0;
    		boolean yInProgress = mStartFlingVelocityY < 0
    				? mFlingVelocityY < 0
    				: mFlingVelocityY > 0;
    		*/
    		
    		float factor = 0.7f;
    		mFlingVelocityX *= factor;
    		mFlingVelocityY *= factor;
    		
    		boolean xInProgress = Math.abs(mFlingVelocityX) > 5.0f;
    		boolean yInProgress = Math.abs(mFlingVelocityY) > 5.0f;
    				   	    		
    		Log.d("flingTimerCallback", "mFlingVelocityX " + mFlingVelocityX 
    				+ ", mFlingVelocityY " + mFlingVelocityY);
    		
    		if (xInProgress) {
    			mOriginX -= mFlingVelocityX / 40;
    		}
    		if (yInProgress) {
    			mOriginY -= mFlingVelocityY / 40;
    		}
    		
    		if (xInProgress || yInProgress) {
    			ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            	flingTimer.postDelayed(flingTimerCallback, 20);   		
    		}
    		
    	}
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods related to custom attributes
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and classes related to view state persistence.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    /**
     * Persistent state that is saved by IsoCanvas.
     */
    public static class SavedState extends BaseSavedState {
        private RectF viewport;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(viewport.left);
            out.writeFloat(viewport.top);
            out.writeFloat(viewport.right);
            out.writeFloat(viewport.bottom);
        }

        @Override
        public String toString() {
            return "IsoCanvas.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " viewport=" + viewport.toString() + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });

        SavedState(Parcel in) {
            super(in);
            viewport = new RectF(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        }
    }
}
