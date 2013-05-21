package com.ickphum.android.isoview;

import com.ickphum.android.isoscene.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class ColorPickerDialog extends DialogFragment {
	
    public interface OnColorChangedListener {
        void colorChanged(String key, int color);
    }
    
    private OnColorChangedListener mListener;
    private int mCurrentColor;
    private ColorPickerView mPicker;

    public ColorPickerDialog() {
    }
    
    public void setListener(OnColorChangedListener l) {
    	mListener = l;
    	Log.d("setListener", "listener " + mListener);
    	
    	// get the current color from the IsoFrame, which is our listener
    	mCurrentColor = ((IsoFrame)mListener).getCurrentColor();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.d("onCreate", "onCreate");
    	
    	setStyle(DialogFragment.STYLE_NO_FRAME, R.style.colorPickerStyle);
	    // this setStyle is VERY important.
	    // STYLE_NO_FRAME means that I will provide my own layout and style for the whole dialog
	    // so for example the size of the default dialog will not get in my way
	    // the style extends the default one. see bellow.        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, 
                     ViewGroup container,
                     Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.color_picker, container);
	    
	    Log.d("onCreateView", "view created " + view + ", container " + container);
	    
	    mPicker = (ColorPickerView) view.findViewById(R.id.colorPicker);
	    mPicker.setCurrentColor(mCurrentColor);
	    Log.d("onCreateView", "cpv ? " + mPicker);
	
	    Button okButton = (Button) view.findViewById(R.id.okButton);
	    okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
                // When button is clicked, call up to owning activity.
                mListener.colorChanged("abc", mPicker.getColor());
                getDialog().dismiss();
            }
        });
	    Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
	    cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
	    
	    return view;
    }
    
}
