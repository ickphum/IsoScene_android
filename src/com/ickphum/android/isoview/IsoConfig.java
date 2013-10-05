package com.ickphum.android.isoview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
			Log.d("IsoConfig", "init default config");
			
			json = new JSONObject();
			
		    try {
				json.put("previousSceneFiles", new JSONArray());
	    
			    json.put("autosave_on_exit", true);
			    json.put("autosave_period_seconds", 60);
			    json.put("autosave_idle_seconds", 2);
			    json.put("use_compressed_files", true);
			    json.put("script_delay_milliseconds", 50);
			    json.put("undo_wait_milliseconds", 200);
			    json.put("undo_repeat_milliseconds", 20);
			    json.put("undo_many_count", 100);
			    json.put("repeated_pasting", true);
			    json.put("repaint_same_tile", false);
			    json.put("automatic_branching", false);
			    json.put("shade_change", 30);
			    json.put("darken_lighten_change", 30);
			    json.put("relative_shades", "L;1;T;0;R;2");
			    json.put("display_palette_index", false);
			    json.put("display_color", false);
			    json.put("display_key", false);
			    json.put("default_scene_file", "Start");
			    json.put("default_scene_scale", 0.5);
			    json.put("default_scene_left_rgb", "#ca9c5b");
			    json.put("default_scene_top_rgb", "#e8ba79");
			    json.put("default_scene_right_rgb", "#ac7e3d");
			    json.put("default_scene_background_rgb", "#AAAAAA");
			    json.put("default_scene_bg_line_rgb", "#B3B3B3");
			    json.put("default_scene_tile_line_rgb", "#DDDDDD");
			} catch (JSONException e) {
				e.printStackTrace();
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
