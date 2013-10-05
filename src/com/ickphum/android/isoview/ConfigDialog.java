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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.app.Activity;

public class ConfigDialog extends DialogFragment {
	

    public ConfigDialog() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.d("onCreate", "onCreate");
    	
    	setStyle(DialogFragment.STYLE_NO_FRAME, R.style.colorPickerStyle);
	    // this setStyle is VERY important.
	    // STYLE_NO_FRAME means that I will provide my own layout and style for the whole dialog
	    // so for example the size of the default dialog will not get in my way
	    // the style extends the default one. see below.        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, 
                     ViewGroup container,
                     Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.config, container);
	    
	    Log.d("onCreateView", "view created " + view + ", container " + container);
		TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
		tabHost.setup();
		final TabWidget tabWidget = tabHost.getTabWidget();
		final FrameLayout tabContent = tabHost.getTabContentView();
		// Get the original tab textviews and remove them from the viewgroup.
		TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
		for (int index = 0; index < tabWidget.getTabCount(); index++) {
			originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
		}
		tabWidget.removeAllViews();
		// Ensure that all tab content childs are not visible at startup.
		for (int index = 0; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
		// Create the tabspec based on the textview childs in the xml file.
		// Or create simple tabspec instances in any other way...
		for (int index = 0; index < originalTextViews.length; index++) {
			final TextView tabWidgetTextView = originalTextViews[index];
			final View tabContentView = tabContent.getChildAt(index);
			TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetTextView
					.getTag());
			tabSpec.setContent(new TabHost.TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return tabContentView;
				}
			});

			if (tabWidgetTextView.getBackground() == null) {
				tabSpec.setIndicator(tabWidgetTextView.getText());
			} else {
				tabSpec.setIndicator(tabWidgetTextView.getText(),
						tabWidgetTextView.getBackground());
			}

			tabHost.addTab(tabSpec);
		}
		// tabHost.setCurrentTab(0);
	    /*
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
        */
	    
	    return view;
    }
    
}
