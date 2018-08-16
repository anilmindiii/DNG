package com.dng.helper;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.dng.R;
import com.dng.activity.MainActivity;
import com.dng.app.DNG;

import static com.facebook.FacebookSdk.getApplicationContext;

public class TimerAlertBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        dialog("Now you can start the route");
    }

    private void dialog(String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("DNG")
                .setMessage(msg)
                .create();
        alertDialog.setButton("launch app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                dialogIntent.putExtra("type", "LaunchAppAlarm");
                getApplicationContext().startActivity(dialogIntent);
                alertDialog.dismiss();
            }
        });

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }



        if (!DNG.isActivityVisible())alertDialog.show();
    }
}
