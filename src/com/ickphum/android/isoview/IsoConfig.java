package com.ickphum.android.isoview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;

public class IsoConfig {
	
	private JSONObject json;
	private String configFilename = "isoscene.json";
	private Context _context;  
	
	public IsoConfig(Context context) {
		
		_context = context;
		File configFilePath = context.getFileStreamPath(configFilename);
		FileInputStream configFileStream = null;
		if (configFilePath.exists()) {
			Log.d("IsoConfig", "config file exists " + configFilePath.getAbsolutePath());
			try {
				configFileStream = context.openFileInput(configFilename);
				
				// create a scanner which will return a single token for the whole, 
				// since we're delimiting with "start-of-record".
				java.util.Scanner s = new java.util.Scanner(configFileStream).useDelimiter("\\A");
			    String jsonString = s.hasNext() ? s.next() : "";
			    
			    json = new JSONObject(jsonString);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			finally {
				try {
					configFileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			Log.d("IsoConfig", "create empty config");
			
			json = new JSONObject();
		}
		
		// create am object with all the default settings, then copy those that are missing
		// to the config object, so we can add default settings and they'll automatically be added
		// to existing configs.
		JSONObject defaultSettings = new JSONObject();
	    try {
			defaultSettings.put("previousSceneFiles", new JSONArray());
    
		    defaultSettings.put("autosave_on_exit", true);
		    defaultSettings.put("autosave_period_seconds", 60);
		    defaultSettings.put("autosave_idle_seconds", 2);
		    defaultSettings.put("use_compressed_files", true);
		    defaultSettings.put("script_delay_milliseconds", 50);
		    defaultSettings.put("undo_wait_milliseconds", 200);
		    defaultSettings.put("undo_repeat_milliseconds", 20);
		    defaultSettings.put("undo_many_count", 100);
		    defaultSettings.put("repeated_pasting", true);
		    defaultSettings.put("repaint_same_tile", false);
		    defaultSettings.put("automatic_branching", false);
		    defaultSettings.put("shade_change", 30);
		    defaultSettings.put("darken_lighten_change", 30);
		    defaultSettings.put("relative_shades", 0);
		    defaultSettings.put("display_palette_index", false);
		    defaultSettings.put("display_color", false);
		    defaultSettings.put("display_key", false);
		    defaultSettings.put("default_scene_file", "Start");
		    defaultSettings.put("default_scene_scale", 0.5);
		    defaultSettings.put("default_scene_left_rgb", "#ca9c5b");
		    defaultSettings.put("default_scene_top_rgb", "#e8ba79");
		    defaultSettings.put("default_scene_right_rgb", "#ac7e3d");
		    defaultSettings.put("default_scene_background_rgb", "#AAAAAA");
		    defaultSettings.put("default_scene_bg_line_rgb", "#B3B3B3");
		    defaultSettings.put("default_scene_tile_line_rgb", "#DDDDDD");
		    defaultSettings.put("repaint_same_tile", false);
		    defaultSettings.put("sector_size", 20);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// add any missing items to our config
	    @SuppressWarnings("rawtypes")
		Iterator keys = defaultSettings.keys();
	    while(keys.hasNext()) {
	        String key = (String) keys.next();
	        if (! json.has(key)) {
	        	Log.d("isoConfig", "add new default key " + key);
	        	try {
		        	json.put(key, defaultSettings.get(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        }
	    }
	    
	}
	
	public void save () {
		BufferedOutputStream configFileStream = null;
		try {
			configFileStream = new BufferedOutputStream(
					_context.openFileOutput(configFilename, Context.MODE_PRIVATE));
			
			configFileStream.write(json.toString().getBytes());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				configFileStream.close();
				Log.d("IsoConfig", "saved ok");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public boolean getBoolean (String name) {
		try {
			return json.getBoolean(name);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean set (String name, Boolean value) {
		try {
			json.put(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public String getString (String name) {
		try {
			return json.getString(name);
		} catch (JSONException e) {
			e.printStackTrace();
			return "key not found";
		}
	}
	
	public String set (String name, String value) {
		try {
			json.put(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public double getDouble (String name) {
		try {
			return json.getDouble(name);
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public double set (String name, double value) {
		try {
			json.put(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public int getInt (String name) {
		try {
			return json.getInt(name);
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int set (String name, int value) {
		try {
			json.put(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	

}
