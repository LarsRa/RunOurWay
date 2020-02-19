package de.hsf.mobcomgroup1.runourway.View;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class AlertBox{

    public static void showAlertBox(DialogInterface.OnClickListener onClickListener, Activity activity, String message, String option1, String option2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setPositiveButton(option1, onClickListener).setNegativeButton(option2, onClickListener).show();
    }



}
