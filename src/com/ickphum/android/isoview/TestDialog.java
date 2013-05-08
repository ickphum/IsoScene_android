package com.ickphum.android.isoview;

import com.ickphum.android.isoscene.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class TestDialog extends DialogFragment {
	/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_signin, null))
        // Add action buttons
               .setPositiveButton(R.string.paint, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       // sign in the user ...
                   }
               })
               .setNegativeButton(R.string.darken, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       TestDialog.this.getDialog().cancel();
                   }
               });       
        
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        
        final WindowManager.LayoutParams WMLP = dialog.getWindow().getAttributes();

        WMLP.gravity = Gravity.TOP;
        WMLP.y = 100;
        WMLP.x = 100;
        dialog.getWindow().setAttributes(WMLP);
        
        return dialog;
    }
    */
	
    public TestDialog() {
    	//You need to provide a default constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, 
                     ViewGroup container,
                     Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.dialog_signin, container);
	
	    //WindowManager.LayoutParams wmlp = getDialog().getWindow().getAttributes();
	    //wmlp.gravity = Gravity.LEFT;
	    
	    return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setStyle(DialogFragment.STYLE_NO_FRAME, R.style.colorPickerStyle);
	    // this setStyle is VERY important.
	    // STYLE_NO_FRAME means that I will provide my own layout and style for the whole dialog
	    // so for example the size of the default dialog will not get in my way
	    // the style extends the default one. see bellow.        
    }
    

}
