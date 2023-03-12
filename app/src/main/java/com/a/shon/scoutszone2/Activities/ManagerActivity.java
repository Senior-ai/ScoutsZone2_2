package com.a.shon.scoutszone2.Activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.a.shon.scoutszone2.R;

import static com.a.shon.scoutszone2.App.CHANNEL_1_ID;

public class ManagerActivity extends Activity {
    private NotificationManagerCompat notificationManager;

    Vibrator vbr;

    //Button btnCreate;
    //Button btnMap;
    Button btnUpdatezone;
    Button btnAddzone;
    Button btnApprove;
    Button btnVibrate;
    Button btnFinish;
    //Button btnChangeactivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        //btnCreate = findViewById(R.id.btnCreate);
        //btnMap = findViewById(R.id.btnAddzone);
        btnAddzone = findViewById(R.id.btnAddzone);
        btnUpdatezone = findViewById(R.id.btnUpdatezone);
        btnApprove = findViewById(R.id.btnApprove);
        btnVibrate = findViewById(R.id.btnVibrate);
        btnFinish = findViewById(R.id.btnFinish);
       // btnChangeactivity = findViewById(R.id.btnChangeactivity);

        vbr = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        notificationManager = NotificationManagerCompat.from(this);

    }

    public void onFinish(View v) {
        finish();
    }

    public void onNotify(View v)
    {
        vbr.vibrate(1000);
        String title = "Hey come back!";
        String message = "The time is over! come back (:";
        Toast.makeText(this, "Notified!", Toast.LENGTH_SHORT).show();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID).setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        notificationManager.notify(1, notification);
    }

    public void onAddzone(View v)
    {
        openDetails("");
    }

    public void onUpdatezone(View v)
    {
        Intent update = new Intent(this, UpdatezoneActivity.class);
        startActivity(update);
    }

    public void onApprove(View v)
    {
        Intent approve = new Intent(this, ApproveActivity.class);
        startActivity(approve);
    }

    public void openDetails(String id)
    {
        Intent in = new Intent(this, UploadzoneActivity.class);
        in.putExtra("id", id);
        startActivity(in);
    }


}
