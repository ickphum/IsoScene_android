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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ickphum.android.isoscene.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.support.v4.app.FragmentActivity;

public class IsoFrame extends FragmentActivity implements TestDialog.OnColorChangedListener {
    //private InteractiveLineGraphView mGraphView;
	
	private boolean mMoveMode = false;
	private boolean mAreaMode = false;

	public enum ToolEffect {
	    CURRENT, OTHERS, ALL;
	}

	public enum CubeSide {
	    LEFT, TOP, RIGHT;
	}
	private CubeSide mCubeSide = CubeSide.RIGHT;
	private List<CubeSide> othersFromLeft = Arrays.asList(CubeSide.TOP, CubeSide.RIGHT);
	private List<CubeSide> othersFromTop = Arrays.asList(CubeSide.LEFT, CubeSide.RIGHT);
	private List<CubeSide> othersFromRight = Arrays.asList(CubeSide.LEFT, CubeSide.TOP);
	private static List<CubeSide> allSides = Arrays.asList(CubeSide.LEFT, CubeSide.TOP, CubeSide.RIGHT);
	public CubeSide getCubeSide() {
		return mCubeSide;
	}
	public void setCubeSide(CubeSide cubeSide) {
		this.mCubeSide = cubeSide;
	}
	public List<CubeSide> getOtherSides() {
		return mCubeSide == CubeSide.LEFT
				? othersFromLeft
				: mCubeSide == CubeSide.TOP
					? othersFromTop
					: othersFromRight;
	}
	static public List<CubeSide> getAllSides() {
		return allSides;
	}
	public char getSideChar() {
		return mCubeSide == CubeSide.LEFT
			? 'l'
			: mCubeSide == CubeSide.TOP
				? 't'
				: 'r';
	}

	public enum Action {
	    PAINT,
	    SAMPLE, 
	    ERASE, 
	    ERASE_OTHERS, 
	    ERASE_ALL, 
	    SELECT, 
	    SELECT_OTHERS, 
	    SELECT_ALL,
	    LIGHTEN, 
	    DARKEN, 
	    SHADE, 
	    SHADE_CUBE, 
	    PASTE, 
	    IMPORT_IMAGE, 
	    CHOOSE_BRANCH,
	    ;
   	}
	private Action mAction = Action.PAINT;
	public Action getAction() {
		return mAction;
	}
	public void setAction(Action a) {
		mAction = a;
		findViewById(R.id.cube).invalidate();
	}
	public boolean actionMatchesErase() {
		return mAction == Action.ERASE_ALL || mAction == Action.ERASE_OTHERS || mAction == Action.ERASE;
	}
	public boolean actionMatchesSelect() {
		return mAction == Action.SELECT_ALL || mAction == Action.SELECT_OTHERS || mAction == Action.SELECT;
	}
	public boolean actionMatchesOthers() {
		return mAction == Action.ERASE_OTHERS || mAction == Action.SELECT_OTHERS;
	}
	public boolean actionMatchesAll() {
		return mAction == Action.ERASE_ALL || mAction == Action.SELECT_ALL;
	}
	public boolean actionInSet(Set<Action> actions) {
		return actions.contains(mAction);
	}
    private SparseArray<Action> buttonAction = new SparseArray<Action>();

    // store the selection modes for erase and select buttons. These have to persist
    // across change of action.
	private ToolEffect eraseEffect = ToolEffect.CURRENT ;
	public ToolEffect getEraseEffect() {
		return eraseEffect;
	}
 	public void setEraseEffect(ToolEffect eraseEffect) {
		this.eraseEffect = eraseEffect;
		setButtonImage(Arrays.asList(R.id.eraseButton));
	}
	
	
	private ToolEffect selectEffect = ToolEffect.CURRENT ;
	public ToolEffect getSelectEffect() {
		return selectEffect;
	}
 	public void setSelectEffect(ToolEffect selectEffect) {
		this.selectEffect = selectEffect;
		setButtonImage(Arrays.asList(R.id.selectButton));
	}

	// what tool are we choosing the effect for, erase or select.
	private int mToolEffectActionId;
 
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // mGraphView = (InteractiveLineGraphView) findViewById(R.id.chart);
        
        registerForContextMenu(findViewById(R.id.eraseButton));
        registerForContextMenu(findViewById(R.id.selectButton));
        
        buttonAction.put(R.id.paintButton, Action.PAINT);
        buttonAction.put(R.id.eraseButton, Action.ERASE);
        buttonAction.put(R.id.sampleButton, Action.SAMPLE);
        buttonAction.put(R.id.selectButton, Action.SELECT);
        buttonAction.put(R.id.shadeCubeButton, Action.SHADE_CUBE);
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
        if (v.getId() == R.id.eraseButton || v.getId() == R.id.selectButton) {
        	
			// we've long-clicked on either erase or select; save the id
			// to indicate which one, which will be used by the context menu selection.
			mToolEffectActionId = v.getId();
	        inflater.inflate(R.menu.side_choice_menu, menu);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d("onOptionsItemSelected", item.toString());
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
		// currently called by clearSelection button; the below statement
		// does nothing afaik.
		((Activity) view.getContext()).openContextMenu(view);
	}

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	Log.d("onContextItemSelected", item.toString());
    	
    	int menuItemId = item.getItemId();
    	ToolEffect newEffect = menuItemId == R.id.current_side
			? ToolEffect.CURRENT
			: menuItemId == R.id.other_sides
				? ToolEffect.OTHERS
				: ToolEffect.ALL;
    	Action newAction;
    	if (mToolEffectActionId == R.id.eraseButton) {
    		newAction = menuItemId == R.id.current_side
    			? Action.ERASE
    			: menuItemId == R.id.other_sides
    				? Action.ERASE_OTHERS
    				: Action.ERASE_ALL;
    		setEraseEffect(newEffect);
    	}
    	else {
    		newAction = menuItemId == R.id.current_side
    			? Action.SELECT
    			: menuItemId == R.id.other_sides
    				? Action.SELECT_OTHERS
    				: Action.SELECT_ALL;
    		setSelectEffect(newEffect);
    	}

    	setAction(newAction);

    	return super.onContextItemSelected(item);
    }

    @SuppressLint("DefaultLocale")
	private void setButtonImage(List<Integer> buttonIds) {
		
		// Set the button images for the specified buttons, 
		// which will depend on current side and (for erase and select)
		// the tool effect setting for that button.
		for (int buttonId: buttonIds) {
			ImageButton button = (ImageButton) findViewById(buttonId);
			ToolEffect effect = buttonId == R.id.eraseButton
				? eraseEffect
				: buttonId == R.id.selectButton
					? selectEffect
					: null;
			
			String bitmapName = "action_" + ((String) button.getTag()).toLowerCase();
			if (effect != null) {
				bitmapName += "_" + effect.toString().toLowerCase();
			}
			if (effect == null || effect != ToolEffect.ALL) {
				bitmapName += "_" + getSideChar();
			}
			
			int bitmapId = getResources().getIdentifier(bitmapName, "drawable", "com.ickphum.android.isoscene");
			Log.d("setButtonImage", bitmapName + ", bitmapId = " + bitmapId);
			button.setImageResource(bitmapId);
		}

	}

	public void changeAction (View view) {
		Log.d("changeAction", view.toString());
		Action action = buttonAction.get(view.getId());
		if (action != null) {
			
			// the action comes from the button id, which just gives
			// us erase or select; check the current effect for these buttons.
			if (action == Action.ERASE) {
				action = eraseEffect == ToolEffect.OTHERS
					? Action.ERASE_OTHERS
					: eraseEffect == ToolEffect.ALL
						? Action.ERASE_ALL
						: Action.ERASE;
			}
			else {
				if (action == Action.SELECT) {
					action = selectEffect == ToolEffect.OTHERS
						? Action.SELECT_OTHERS
						: selectEffect == ToolEffect.ALL
							? Action.SELECT_ALL
							: Action.SELECT;
				}
			}
			setAction(action);
		}
	}

	public void toggleMoveFlag (View view) {
		ImageButton moveButton = (ImageButton) view.findViewById(R.id.moveButton);
		mMoveMode = ! mMoveMode;
		moveButton.setImageResource(mMoveMode ? R.drawable.move_on : R.drawable.move_off);
	}

	public void toggleAreaFlag (View view) {
		ImageButton areaButton = (ImageButton) view.findViewById(R.id.areaButton);
		mAreaMode = ! mAreaMode;
		areaButton.setImageResource(mAreaMode 
			? mCubeSide == CubeSide.LEFT 
				? R.drawable.area_l_on 
				: mCubeSide == CubeSide.TOP
					? R.drawable.area_t_on
					: R.drawable.area_r_on
			: mCubeSide == CubeSide.LEFT 
				? R.drawable.area_l_off 
				: mCubeSide == CubeSide.TOP
					? R.drawable.area_t_off
					: R.drawable.area_r_off);
	}
	
	public void chooseColor() {
		TestDialog newFragment = new TestDialog();
		newFragment.setListener(this);
	    newFragment.show(getSupportFragmentManager(), "ikmdialog");
	}
	
	public void colorChanged(String key, int color) {
		Log.d("colorChanged", "new color chosen " + key + ", " + color);
	}
    
}
