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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class IsoFrame extends FragmentActivity implements ColorPickerDialog.OnColorChangedListener {
    //private InteractiveLineGraphView mGraphView;
	
	private boolean moveMode = false;
	private boolean areaMode = false;
	
	public enum CubeSide {
	    LEFT('l'),
	    TOP('t'),
	    RIGHT('r');

	    private final char side;

	    private CubeSide(char side) {
	        this.side = side;
	    }

	    public char getSide() {
	        return side;
	    }
	}
	private CubeSide cubeSide = CubeSide.RIGHT;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // mGraphView = (InteractiveLineGraphView) findViewById(R.id.chart);
        
        registerForContextMenu(findViewById(R.id.eraseButton));
        registerForContextMenu(findViewById(R.id.selectButton));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.side_choice_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        	/*
            case R.id.action_pan_left:
                mGraphView.panLeft();
                return true;
            */

        }

        return super.onOptionsItemSelected(item);
    }

	public void showSelectionTools (View view) {
		((Activity) view.getContext()).openContextMenu(view);
	}

	public void toggleMoveFlag (View view) {
		ImageButton moveButton = (ImageButton) view.findViewById(R.id.moveButton);
		moveMode = ! moveMode;
		moveButton.setImageResource(moveMode ? R.drawable.move_on : R.drawable.move_off);
	}

	public void toggleAreaFlag (View view) {
		ImageButton areaButton = (ImageButton) view.findViewById(R.id.areaButton);
		areaMode = ! areaMode;
		areaButton.setImageResource(areaMode 
			? cubeSide == CubeSide.LEFT 
				? R.drawable.area_l_on 
				: cubeSide == CubeSide.TOP
					? R.drawable.area_t_on
					: R.drawable.area_r_on
			: cubeSide == CubeSide.LEFT 
				? R.drawable.area_l_off 
				: cubeSide == CubeSide.TOP
					? R.drawable.area_t_off
					: R.drawable.area_r_off);
	}
	
	public void chooseColor() {
		//new ColorPickerDialog(this, this, "testkey", 0xff0000, 0xff00).show();
		DialogFragment newFragment = new TestDialog();
	    newFragment.show(getSupportFragmentManager(), "ikmdialog");

	}
	
	public void colorChanged(String key, int color) {
		Log.d("colorChanged", "new color chosen " + key + ", " + color);
	}

}
