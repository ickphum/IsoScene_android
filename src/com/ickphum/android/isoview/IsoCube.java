package com.ickphum.android.isoview;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ickphum.android.isoscene.R;
import com.ickphum.android.isoview.IsoFrame.CubeSide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class IsoCube extends View {
	private GestureDetectorCompat mDetector; 

    public IsoCube(Context context) {
        this(context, null, 0);
    }

    public IsoCube(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

	//private String mAction;
	//private int[] mColors = new int[3];
	
	private Paint[] mPaints = new Paint[3];
    private Path[] mPaths = new Path[3];
    private float mHalfWidth;
    private float mHalfHeight;
    private Bitmap cubeFrameBitmap;
    private Handler longPressTimer = new Handler();
    private Boolean pressInProgress = false;
    private SparseArray<Bitmap> cubeBitmap = new SparseArray<Bitmap>();
    private Map<IsoFrame.CubeSide, List<Integer>> sideOffsets = new HashMap<IsoFrame.CubeSide, List<Integer>>();
    
    private Runnable longPressTimerCallback = new Runnable() {
    	@Override
    	public void run() {
    		IsoCube cube = (IsoCube)findViewById(R.id.cube);
    		cube.timerExpired();
    	}
    };
    
    public void timerExpired() {
    	Log.d("timerExpired", "timer has expired");
    	if (pressInProgress) {
    		pressInProgress = false;
    		Log.d("timerExpired", "slow click, show color dialog");
    		
    		((IsoFrame) getContext()).chooseColor();
    	}
    }
    
	public IsoCube(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		//mPaints[0].setColor(Color.BLUE);
		//mPaints[0].setStyle(Paint.Style.FILL);

		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.IsoCube,
		        0, 0);

		int[] colors = new int[3];
		try {
			//mAction = a.getString(R.styleable.IsoCube_action);
			colors[0] = a.getColor(R.styleable.IsoCube_leftColor, 0);
			colors[1] = a.getColor(R.styleable.IsoCube_topColor, 0);
			colors[2] = a.getColor(R.styleable.IsoCube_rightColor, 0);
		} finally {
			a.recycle();
		}
		
		for (int i=0; i<3; i++) {
			mPaints[i] = new Paint();
			mPaints[i].setColor(colors[i]);
			mPaints[i].setStyle(Paint.Style.FILL);
		}
		/*
		*/
		List<Integer> actions = Arrays.asList(
				R.drawable.paint, 
				R.drawable.sample,
				R.drawable.erase,
				R.drawable.select,
				R.drawable.small_paste,
				R.drawable.shade_cube,
				R.drawable.import_image
				);
		for(Integer action: actions) {
			cubeBitmap.put(action, BitmapFactory.decodeResource(getResources(), action));
		}
		cubeFrameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cube);

        sideOffsets.put(IsoFrame.CubeSide.LEFT, Arrays.asList(-40,3));
        sideOffsets.put(IsoFrame.CubeSide.TOP, Arrays.asList(-15,-44));
        sideOffsets.put(IsoFrame.CubeSide.RIGHT, Arrays.asList(10,-3));
        
		// Create a gesture detector to handle onTouch messages
        if (! this.isInEditMode()) {
        	mDetector = new GestureDetectorCompat(IsoCube.this.getContext(), new MyGestureListener());
        	mDetector.setIsLongpressEnabled(false);
        }


	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSize = 105;
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(minSize + getPaddingLeft() + getPaddingRight(),
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(minSize + getPaddingTop() + getPaddingBottom(),
                                heightMeasureSpec)));
    }

	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        Log.d("onSizeChanged",  "size changed to " + w + ", " + h);
        		        
        /*
        mContentRect.set(
            getPaddingLeft(),
            getPaddingTop(),
            getWidth() - getPaddingRight(),
            getHeight() - getPaddingBottom());
        */
        
        for (int i = 0; i<3; i++) {
        	if (mPaths[i] == null) {
        		mPaths[i] = new Path();
        	}
        	else {
        		
        		// clear path but keep some internal data intact; this is faster 
        		// if the path will always have the same size.
        		mPaths[i].rewind();
        	}
        }
        
        mHalfWidth = (float)49;
        mHalfHeight = (float)56;
        float quarter_height = mHalfHeight / 2;
        Log.d("onSizeChanged", "dims: " + mHalfWidth + "," + mHalfHeight + "," + quarter_height);
        
        mPaths[0].moveTo(0, 0);
        mPaths[0].lineTo(- mHalfWidth, - quarter_height);
        mPaths[0].lineTo(- mHalfWidth, quarter_height);
        mPaths[0].lineTo(0, quarter_height * 2);
        mPaths[0].setFillType(Path.FillType.WINDING);
        
        mPaths[1].moveTo(-mHalfWidth, -quarter_height );
        mPaths[1].lineTo(0, -quarter_height * 2 );
        mPaths[1].lineTo(mHalfWidth, -quarter_height );
        mPaths[1].lineTo(0, 0);
        mPaths[1].setFillType(Path.FillType.WINDING);
        
        mPaths[2].moveTo(0,0);
        mPaths[2].lineTo(mHalfWidth, -quarter_height);
        mPaths[2].lineTo(mHalfWidth, quarter_height);
        mPaths[2].lineTo(0, quarter_height * 2);
        mPaths[2].setFillType(Path.FillType.WINDING);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v("onDraw", "start");
                
        canvas.translate(mHalfWidth, mHalfHeight+1);
        canvas.drawPath(mPaths[0], mPaints[0]);
        canvas.drawPath(mPaths[1], mPaints[1]);
        canvas.drawPath(mPaths[2], mPaints[2]);
        //canvas.translate(0,0);
        canvas.translate(-(mHalfWidth), -(mHalfHeight+1));
        
        // can't do the tricky stuff in the editor
        if (this.isInEditMode()) {
        	return;
        }
        
        IsoFrame frame = (IsoFrame) getContext();
        IsoFrame.Action action = frame.getAction();
        
        int side_bitmap = frame.actionMatchesErase()
        	? R.drawable.erase
        	: frame.actionMatchesSelect()
        		? R.drawable.select
        		: action == IsoFrame.Action.PASTE
        			? R.drawable.small_paste
        			: getResources().getIdentifier(action.toString().toLowerCase(), 
        					"drawable", "com.ickphum.android.isoscene");

        Bitmap actionBitmap = cubeBitmap.get(side_bitmap);

        Log.v("onDraw", "action = " + frame.getAction().toString().toLowerCase() 
        		+ ", side_bitmap_name = " + side_bitmap + ", bitmap = " + actionBitmap);

        //if (action == IsoFrame.Action.PAINT) {
        	canvas.drawBitmap(cubeFrameBitmap, -1, -1, mPaints[0]);
        //}
        
        List<IsoFrame.CubeSide> sides = null;
        if (frame.actionMatchesAll() || action == IsoFrame.Action.PASTE) {
        	sides = IsoFrame.getAllSides();
        }
        else {
        	if (frame.actionMatchesOthers()) {
        		sides = frame.getOtherSides();
        	}
        	else {
                List<Integer> offset = sideOffsets.get(frame.getCubeSide()); 
                canvas.drawBitmap(actionBitmap, 
                		mHalfWidth + offset.get(0), mHalfHeight + offset.get(1), mPaints[0]);       	
        	}
        }
        if (sides != null) {
	        for (IsoFrame.CubeSide side: sides) {
	            List<Integer> offset = sideOffsets.get(side); 
	            canvas.drawBitmap(actionBitmap, 
	            		mHalfWidth + offset.get(0), mHalfHeight + offset.get(1), mPaints[0]);       	
	        }
        }

        //canvas.drawLine(0,0,100,100,mPaints[0]);
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        int action = MotionEventCompat.getActionMasked(event);
        final String DEBUG_TAG = "NewHandler"; 

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(DEBUG_TAG,"Action was DOWN: " + event);
            	changeCubeSide(event);
            	longPressTimer.postDelayed(longPressTimerCallback, 
            			getResources().getInteger(R.integer.longPressMilliSecs));
            	pressInProgress = true;
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(DEBUG_TAG,"Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(DEBUG_TAG,"Action was UP");
            	longPressTimer.removeCallbacks(longPressTimerCallback);
            	if (pressInProgress) {
            		Log.d(DEBUG_TAG, "quick click, change cube side");
            		pressInProgress = false;
            	}
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(DEBUG_TAG,"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;      
            default : 
                return super.onTouchEvent(event);
        }      

        //this.mDetector.onTouchEvent(event);
        //return super.onTouchEvent(event);
    }

    private void changeCubeSide(MotionEvent event) {
		float rise = -(event.getY() - (this.getHeight()/2));
		float run = event.getX() - (this.getWidth()/2);
		
		CubeSide newSide;
		if (run != 0) {
			float gradient = rise / run;
			newSide = rise < 0
				? run > 0
					? CubeSide.RIGHT
					: CubeSide.LEFT
				: Math.abs(gradient) > 0.58
					? CubeSide.TOP
					: run > 0
						? CubeSide.RIGHT
						: CubeSide.LEFT;
		}
		else {
			newSide = rise > 0
				? CubeSide.TOP
				: CubeSide.RIGHT;
		}
		
		Log.d("changeCubeSide", "new side is " + newSide);
		((IsoFrame) getContext()).setCubeSide(newSide);
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures"; 

        @Override
        public boolean onDown(MotionEvent event) { 
            Log.d(DEBUG_TAG,"onDown: " + event.toString()); 
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, 
                float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onLongPress: " + event.toString()); 
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            //Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
            return true;
        }

    }

	public void newColor(CubeSide cubeSide, int color) {
		
		// set the new color for the appropriate side and redraw
		mPaints[cubeSide.getInt()].setColor(color);
		invalidate();
	}

	public int getColor(CubeSide cubeSide) {
		// TODO Auto-generated method stub
		return mPaints[cubeSide.getInt()].getColor();
	}

}
