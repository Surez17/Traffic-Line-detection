package com.example.project;


import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class Dialog extends AppCompatDialogFragment {

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alert =new AlertDialog.Builder(getActivity());
        alert.setMessage("Vehicle crossed");
        //alert.setNeutralButton("Okay",a);
        return alert.create();

    }
}
