package com.ickphum.android.isoview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GLIsoCanvas extends GLSurfaceView {

    // geometry
    private float mScale = 0;
    private float mPreviousX;
    private float mPreviousY;

	// State objects and values related to gesture tracking.
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private Handler mFlingTimer = new Handler();
    
    protected GlRenderer mRenderer;

	public GLIsoCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setRenderer(mRenderer = new GlRenderer());
		this.setRenderMode(RENDERMODE_WHEN_DIRTY);
		
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
    	mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

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

            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);

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
            requestRender();
            Log.d("onDown", e.getX() + "," + e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            Log.d("onDoubleTap", e.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	//Log.d("onScroll", "e1 " + e1 + ", e2 " + e2 + ", distanceX " + distanceX + ", distanceY " + distanceY);
        	//Log.d("onScroll", "distanceX " + distanceX + ", distanceY " + distanceY);
        	mRenderer.moveOrigin(-distanceX, distanceY);
        	requestRender();
            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	Log.d("onFling", "velocityX " + velocityX + ", velocityY " + velocityY);
        	mFlingVelocityX = velocityX * 0.8f;
        	mFlingVelocityY = velocityY * 0.8f;
        	mStartFlingVelocityX = velocityX;
        	mStartFlingVelocityY = velocityY;
        	mFlingTimer.postDelayed(flingTimerCallback, 20);

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
    				   	    		
    		//Log.d("flingTimerCallback", "mFlingVelocityX " + mFlingVelocityX + ", mFlingVelocityY " + mFlingVelocityY);
    		
    		if (xInProgress || yInProgress) {
        		mRenderer.moveOrigin(xInProgress ? mFlingVelocityX / 40 : 0, yInProgress ? -mFlingVelocityY / 40 : 0);
        		requestRender();
            	mFlingTimer.postDelayed(flingTimerCallback, 20);   		
    		}
    		
    	}
    };


}
