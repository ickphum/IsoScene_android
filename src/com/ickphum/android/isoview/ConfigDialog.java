package com.ickphum.android.isoview;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ickphum.android.isoscene.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;

@SuppressLint("ValidFragment")
public class ConfigDialog extends DialogFragment {
	
	private IsoConfig config;

	private static SparseArray<String> booleanSettingName;
	private static SparseArray<String> intSettingName;
	private static SparseArray<String> stringSettingName;
	private static SparseArray<String> doubleSettingName;

	public ConfigDialog() {
		
    }
	
	
	@Override
	public void onAttach(Activity activity) {
		Log.d("ConfigDialog", "onAttach");
		
		booleanSettingName = new SparseArray<String>();
	    booleanSettingName.put(R.id.displayKey, getString(R.string.display_key_setting));
	    booleanSettingName.put(R.id.displayColor, getString(R.string.display_color_setting));
	    booleanSettingName.put(R.id.displayPaletteIndex, getString(R.string.display_palette_index_setting));
	    booleanSettingName.put(R.id.repeatedPasting, getString(R.string.repeated_pasting_setting));
	    booleanSettingName.put(R.id.repaintSameTile, getString(R.string.repaint_same_tile_setting));
	    booleanSettingName.put(R.id.automaticBranching, getString(R.string.automatic_branching_setting));
	    booleanSettingName.put(R.id.autosaveOnExit, getString(R.string.autosave_on_exit_setting));
	    booleanSettingName.put(R.id.useCompressedFiles, getString(R.string.use_compressed_files_setting));

		intSettingName = new SparseArray<String>();
	    intSettingName.put(R.id.shadeChange, getString(R.string.shade_change_setting));
	    intSettingName.put(R.id.scriptStepDelay, getString(R.string.script_delay_milliseconds_setting));
	    intSettingName.put(R.id.darkenLightenChange, getString(R.string.darken_lighten_change_setting));
	    intSettingName.put(R.id.relativeShades, getString(R.string.relative_shades_setting));
	    intSettingName.put(R.id.undoWaitDelay, getString(R.string.undo_wait_milliseconds_setting));
	    intSettingName.put(R.id.undoRepeatDelay, getString(R.string.undo_repeat_milliseconds_setting));
	    intSettingName.put(R.id.undoManyCount, getString(R.string.undo_many_count_setting));
	    intSettingName.put(R.id.autosavePeriod, getString(R.string.autosave_period_seconds_setting));
	    intSettingName.put(R.id.autosaveIdle, getString(R.string.autosave_idle_seconds_setting));

		stringSettingName = new SparseArray<String>();
	    stringSettingName.put(R.id.defaultFilename, getString(R.string.default_scene_file_setting));
	    stringSettingName.put(R.id.defaultLeftRGB, getString(R.string.default_scene_left_rgb_setting));
	    stringSettingName.put(R.id.defaultTopRGB, getString(R.string.default_scene_top_rgb_setting));
	    stringSettingName.put(R.id.defaultRightRGB, getString(R.string.default_scene_right_rgb_setting));
	    stringSettingName.put(R.id.defaultBackgroundRGB, getString(R.string.default_scene_background_rgb_setting));
	    stringSettingName.put(R.id.defaultBackgroundLineRGB, getString(R.string.default_scene_bg_line_rgb_setting));

		doubleSettingName = new SparseArray<String>();
	    doubleSettingName.put(R.id.defaultScale, getString(R.string.default_scene_scale_setting));

	    config = ((IsoFrame)activity).getConfig();
		if (config != null) {
			Log.d("ConfigDialog", "attached, initial file is " + config.getString("default_scene_file"));
		}
		super.onAttach(activity);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		Log.d("onCreate", "onCreate " + savedInstanceState.toString());
    	}
    	else {
    		Log.d("onCreate", "no saved state");
    	}
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
	    
	    Button okButton = (Button) view.findViewById(R.id.okButton);
	    okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
                // When button is clicked, call up to owning activity.
            	Log.d("ConfigDialog", "save values");
        		IsoFrame parent = (IsoFrame) getActivity();
        		config = parent.getConfig();

        		for (int i = 0; i < booleanSettingName.size(); i++) {
        			int resourceId = booleanSettingName.keyAt(i);
        			String settingName = booleanSettingName.get(resourceId);
            		CheckBox cb = (CheckBox) getView().findViewById(resourceId);
            		config.set(settingName, cb.isChecked());
        		}

        		for (int i = 0; i < intSettingName.size(); i++) {
        			int resourceId = intSettingName.keyAt(i);
        			String settingName = intSettingName.get(resourceId);
            		if (resourceId == R.id.relativeShades) {
    	        		Spinner sp = (Spinner) getView().findViewById(resourceId);
    	        		config.set(settingName, sp.getSelectedItemPosition());
            		}
            		else {
    	        		EditText et = (EditText) getView().findViewById(resourceId);

						try {
						    config.set(settingName, Integer.parseInt(et.getText().toString()));
						} catch(NumberFormatException nfe) {
						   System.out.println("Could not parse int " + nfe);
						} 
            		}
        		}
    			
    			for (int i = 0; i < stringSettingName.size(); i++) {
        			int resourceId = stringSettingName.keyAt(i);
        			String settingName = stringSettingName.get(resourceId);
            		EditText et = (EditText) getView().findViewById(resourceId);
        			config.set(settingName, et.getText().toString());
        		}
    			
    			for (int i = 0; i < doubleSettingName.size(); i++) {
        			int resourceId = doubleSettingName.keyAt(i);
        			String settingName = doubleSettingName.get(resourceId);
            		EditText et = (EditText) getView().findViewById(resourceId);
            		
					try {
					    config.set(settingName, Double.parseDouble(et.getText().toString()));
					} catch(NumberFormatException nfe) {
					   System.out.println("Could not parse double " + nfe);
					} 

        		}

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
	@Override
	public void onStart() {
		Log.d("ConfigDialog", "onStart");
		super.onStart();
	}
	
	@Override
	public void onResume() {
		
		if (config != null) {
			
			// we grab a copy of the config object when we first attach;
			// if that copy still exists, this is the initial resumption
			// so we should seed all values to the controls. If we pause and
			// resume later (with config open), we won't have the config object
			// and we don't need it; the control states are preserved, along
			// with any changes we've made. We will need the config object
			// if we save changes, but we can always get it from the main activity.
			Log.d("ConfigDialog", "config exists, seed initial values");

			for (int i = 0; i < booleanSettingName.size(); i++) {
    			int resourceId = booleanSettingName.keyAt(i);
    			String settingName = booleanSettingName.get(resourceId);
        		CheckBox cb = (CheckBox) getView().findViewById(resourceId);
    			cb.setChecked(config.getBoolean(settingName));
    		}
			
			for (int i = 0; i < stringSettingName.size(); i++) {
    			int resourceId = stringSettingName.keyAt(i);
    			String settingName = stringSettingName.get(resourceId);
        		EditText et = (EditText) getView().findViewById(resourceId);
    			et.setText(config.getString(settingName));
    		}
			
			for (int i = 0; i < intSettingName.size(); i++) {
    			int resourceId = intSettingName.keyAt(i);
    			String settingName = intSettingName.get(resourceId);
    			Log.d("ConfigDialog", "setting: " + settingName + ", resourceId " + resourceId);
    			
        		if (resourceId == R.id.relativeShades) {
	        		Spinner sp = (Spinner) getView().findViewById(resourceId);
	    			sp.setSelection(config.getInt(settingName));       			
        		}
        		else {
	        		EditText et = (EditText) getView().findViewById(resourceId);
	        		Integer j = config.getInt(settingName);
	    			et.setText(j.toString());
        		}
    		}
			
			for (int i = 0; i < doubleSettingName.size(); i++) {
    			int resourceId = doubleSettingName.keyAt(i);
    			String settingName = doubleSettingName.get(resourceId);
        		EditText et = (EditText) getView().findViewById(resourceId);
        		Double d = config.getDouble(settingName);
    			et.setText(d.toString());
    		}
			
		}
		super.onResume();
	}
    
}
