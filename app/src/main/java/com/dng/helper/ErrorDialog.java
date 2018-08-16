package com.dng.helper;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.session.Session;

public class ErrorDialog {


    public static void showSessionError(VolleyError error, final Context mContext) {
        if (error != null && error.networkResponse != null) {

            try {
                if (error.networkResponse.statusCode == 400) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("session expired");
                    builder.setCancelable(false);
                    builder.setMessage("Your current session has been expired.");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            Session session = new Session(mContext);
                            session.sessionExpireLogout();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    //MySnackBar.show(WebServiceAPI.getNetworkMessage(error));
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void showMessage(final Context mContext, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("DNG");
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Session session = new Session(mContext);
                session.sessionExpireLogout();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}


