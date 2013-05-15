package com.ickphum.android.isoview;

import com.ickphum.android.isoscene.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class SimpleCustomView extends View {

    // Current attribute values and Paints.
    private float mDataThickness = 3;
    private int mDataColor = 0xff0000;
    private Paint mDataPaint;
    private int mWidth;
    private int mHeight;

    public SimpleCustomView(Context context) {
        this(context, null, 0);
    }

    public SimpleCustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleCustomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SimpleCustomView, defStyle, defStyle);

        try {
            mDataThickness = a.getDimension(
                    R.styleable.SimpleCustomView_dataThickness2, mDataThickness);
            mDataColor = a.getColor(
                    R.styleable.SimpleCustomView_dataColor2, mDataColor);
        } finally {
            a.recycle();
        }

        initPaints();

    }

    /**
     * (Re)initializes {@link Paint} objects based on current attribute values.
     */
    private void initPaints() {

        mDataPaint = new Paint();
        //mDataPaint.setStrokeWidth(mDataThickness);
        mDataPaint.setColor(mDataColor);
        mDataPaint.setStyle(Paint.Style.STROKE);
        mDataPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0,  0, mWidth, mHeight, mDataPaint);
        Log.d("onDraw", "draw to " + mWidth + ","  + mHeight );
    }



}
