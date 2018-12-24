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

public class GLIsoCanvas<ScaleEvent> extends GLSurfaceView {

    // State objects and values related to gesture tracking.
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mGestureDetector;
    private Handler mFlingTimer = new Handler();
    private IsoFrame frame;
    
    protected GlRenderer mRenderer;

	public GLIsoCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setRenderer(mRenderer = new GlRenderer(context));
		this.setRenderMode(RENDERMODE_WHEN_DIRTY);
		
		
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
    	mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

	}
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //     Methods and objects related to gesture handling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		Log.d("onUp", event.toString());
    	}
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        retVal = mGestureDetector.onTouchEvent(event) || retVal;
        return retVal || super.onTouchEvent(event);
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     */
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            float spanX = detector.getCurrentSpanX();
            float spanY = detector.getCurrentSpanY();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            float factor = detector.getScaleFactor();
            Log.d("onScaleBegin", "factor = " + factor + ", spanX = " + spanX + ", spanY = " + spanY
            		+ ", focusX = " + focusX + ", focusY = " + focusY);

            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);

            mRenderer.changeScale(factor, focusX, focusY);
        	requestRender();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            float factor = detector.getScaleFactor();
            //Log.d("onScale", "factor = " + factor + ", spanX = " + spanX + ", spanY = " + spanY
            //		+ ", focusX = " + focusX + ", focusY = " + focusY);

            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);

            mRenderer.changeScale(factor, focusX, focusY);
        	requestRender();
            return true;
        }
        
        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
            ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
            //Log.d("onScaleEnd", "lastSpanX = " + lastSpanX + ", lastSpanY = " + lastSpanY);
            return ;
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
            Log.d("onDown", e.getX() + "," + e.getY());
            mRenderer.addTile(e.getX(), e.getY(), frame.getCubeSide().getShape());
            requestRender();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //ViewCompat.postInvalidateOnAnimation(IsoCanvas.this);
            Log.d("onDoubleTap", e.toString());
            //mRenderer.addTriangle();
            mRenderer.changeScale(2.0f, e.getX(), e.getY());
        	requestRender();
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
        	Log.d("onLongPress", e.toString());
            mRenderer.changeScale(0.5f, e.getX(), e.getY());
        	requestRender();
        	return;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
        	Log.d("onSingleTapUpPress", e.toString());
        	return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        	Log.d("onShowPress", e.toString());
        	return;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	//Log.d("onScroll", "e1 " + e1 + ", e2 " + e2 + ", distanceX " + distanceX + ", distanceY " + distanceY);
        	//Log.d("onScroll", "distanceX " + distanceX + ", distanceY " + distanceY);
        	if (frame.panning()) {
        		mRenderer.moveOrigin(-distanceX, distanceY);	
        	}
        	requestRender();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	Log.d("onFling", "velocityX " + velocityX + ", velocityY " + velocityY);
        	if (frame.panning()) {
	        	mFlingVelocityX = velocityX * 0.8f;
	        	mFlingVelocityY = velocityY * 0.8f;
	        	mStartFlingVelocityX = velocityX;
	        	mStartFlingVelocityY = velocityY;
	        	mFlingTimer.postDelayed(flingTimerCallback, 20);
        	}
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
    		
    		float factor = 0.9f;
    		mFlingVelocityX *= factor;
    		mFlingVelocityY *= factor;
    		long rateInMilliseconds = 20;
    		float velocityToDistanceFactor = 30f;
    		
    		boolean xInProgress = Math.abs(mFlingVelocityX) > 1.0f;
    		boolean yInProgress = Math.abs(mFlingVelocityY) > 1.0f;
    				   	    		
    		//Log.d("flingTimerCallback", "mFlingVelocityX " + mFlingVelocityX + ", mFlingVelocityY " + mFlingVelocityY);
    		
    		if (xInProgress || yInProgress) {
        		mRenderer.moveOrigin(
        				xInProgress ? mFlingVelocityX / velocityToDistanceFactor : 0, 
        				yInProgress ? -mFlingVelocityY / velocityToDistanceFactor : 0);
        		requestRender();
            	mFlingTimer.postDelayed(flingTimerCallback, rateInMilliseconds);   		
    		}
    		
    	}
    };

	public void setFrame(IsoFrame isoFrame) {
		frame = isoFrame;
		mRenderer.setFrame(isoFrame);
	}

}
