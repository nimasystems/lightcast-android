package com.nimasystems.lightcast.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public class Alerts {

    public static AlertDialog AlertBox(String title, String mymessage,
                                       final Context context,
                                       DialogInterface.OnClickListener onClickListener) {
        Builder dialog = new AlertDialog.Builder(context);
        if (!title.equals("")) {
            dialog.setTitle(title);
        }

        dialog.setMessage(mymessage);

        dialog.setCancelable(false);

        dialog.setNeutralButton("OK", onClickListener);

        return dialog.create();
    }

    public static AlertDialog AlertBox(String title, String mymessage,
                                       final Context context) {
        return AlertBox(title, mymessage, context, null);
    }

}
