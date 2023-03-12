package com.a.shon.scoutszone2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.a.shon.scoutszone2.Activities.ZoneActivity;

public class SMSReceiver extends BroadcastReceiver implements AlertDialog.OnClickListener{

    private Bundle bundle;
    private SmsMessage currentSMS;
    private ZoneActivity ma;

    public SMSReceiver(ZoneActivity ma)
    {
        this.ma = ma;
        Log.d("SMSReceiver", "Constructor");
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String message = "";
        String senderNo = "";
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects)
                    {

                        currentSMS = getIncomingMessage(aObject, bundle);

                        senderNo = currentSMS.getDisplayOriginatingAddress();

                        message += currentSMS.getDisplayMessageBody();
                    } // for

                    String msg = "senderNum: " + senderNo + "\nmessage: " + message;
                    String msg1 = "SMS_Recieved\n" + msg;
                    Toast.makeText(context, msg1, Toast.LENGTH_LONG).show();
                    Log.d("SMSReceiver", msg1);
                    showDialog(context, "SMS", msg);

                    if (message.toUpperCase().contains("SCOUTS") || message.toUpperCase().contains("TZOFIM") || message.contains("צופים"))
                    {
                        if (context.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
                        {
                            SmsManager smsMgr = SmsManager.getDefault();
                            smsMgr.sendTextMessage(senderNo, null, "I'll respond in a second, im ordering a zone", null, null);
                        }
                        else
                            Log.e("SMSReceiver", "No permissions to send SMS - ignoring");

                    }
                    // this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }


    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle)
    {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }

    private void showDialog(Context context, String title, String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", this);
        // builder.setNegativeButton("No", new OnAlertDialogClickListener());
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        dialog.dismiss();
    }
}
