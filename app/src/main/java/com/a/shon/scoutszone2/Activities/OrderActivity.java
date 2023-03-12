package com.a.shon.scoutszone2.Activities;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.a.shon.scoutszone2.Assignment;
import com.a.shon.scoutszone2.Firebases.AssignmentFirebase;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.Utils;
import com.a.shon.scoutszone2.Zone;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.Date;

public class OrderActivity extends Activity implements Button.OnClickListener {

    Vibrator vbr;

    TextView tvInfo; //textView for the chosen time
    TextView tvInfo2; //tv for the chosen date
    TextView tvZonename, tvId, tvType, tvSpace, tvIsActive, tvRating; //All of the textviews tell the details about the specific zone and the time and date.
    ImageView imgPhoto2, imgStar; //ImgPhoto2 - is the photo of the zone itself, imgStar is for showing a star near the number of the rating.
    String id = "";
    String time;
    String date;
    String AssignmentId = "";
    Bitmap bmp = null;
    DocumentSnapshot currentDoc;
    boolean imageUpdated;

    private SharedPreferences.Editor editor;
    public String myBranch; //The user's branch

    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference mColctRef, AColctRef;

    // for firebase storage (images)
    Uri imageUri = null;  // the *Local* URI of the image file in the Android device
    private StorageReference mStorageRef;
    private StorageReference fileReference;
    private ProgressBar progressBarUpload;
    private StorageTask mUploadTask; // used to refrain from multiple concurrent uploads
    private String downloadUrl = ""; // the URI in the image file in the storage (to be saved t

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        tvInfo = findViewById(R.id.tvInfo);
        tvInfo2 = findViewById(R.id.tvInfo2);
        tvSpace = findViewById(R.id.tvSpace);
        tvId = findViewById(R.id.tvId);
        tvType = findViewById(R.id.tvType);
        tvIsActive = findViewById(R.id.tvIsActive);
        tvZonename = findViewById(R.id.tvZonename);
        tvRating = findViewById(R.id.tvRating);
        imgPhoto2 = findViewById(R.id.imgPhoto2);
        imgStar = findViewById(R.id.imgStar);
        progressBarUpload = findViewById(R.id.progressBarUpload);

        vbr = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences result= getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");

        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();

        initFirebase();
        imageUpdated = false;

        Intent order = getIntent();
        Bundle xtras = order.getExtras();
        id = xtras.getString("id");
        time = xtras.getString("time");
        date = xtras.getString("date");
        Toast.makeText(this, "id = " + id,Toast.LENGTH_LONG ).show();


        initDetails();

        tvInfo.setText(time);
        tvInfo2.setText(date);
    }

    private void initFirebase()
    {
        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(ScoutZoneFirebase.ZonesCollection);
        AColctRef = db.collection(AssignmentFirebase.AssignmentCollection);
       mStorageRef = FirebaseStorage.getInstance().getReference(ScoutZoneFirebase.ImageFolder);
    }

    private void initDetails()
    {
        Toast.makeText(OrderActivity.this, "Requesting data for Zone " + id + " please wait", Toast.LENGTH_LONG).show();
        mColctRef.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("ScoutZoneFirebase-GetItem", "Zone " + id + " was retrived successfully");
                    Toast.makeText(OrderActivity.this, "Zone " + id + " was retrived successfully", Toast.LENGTH_LONG).show();
                    currentDoc = task.getResult();
                    initForm(currentDoc);
                    mDocRef = currentDoc.getReference();
                }
                else
                {
                    Log.e("ScoutZoneFirebase-GetItem", "Oy vey", task.getException());
                    Toast.makeText(OrderActivity.this, "Oy vey:\n" + task.getException(), Toast.LENGTH_LONG).show();
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void initForm(DocumentSnapshot document)
    {
        id = document.getId();
        String name = document.get(ScoutZoneFirebase.ZoneName).toString();
        String type = document.get(ScoutZoneFirebase.type).toString();
        String branchCode = document.get(ScoutZoneFirebase.branchCode).toString();
        int space = Integer.parseInt(document.get(ScoutZoneFirebase.space).toString());
        double rating = Double.parseDouble(document.get(ScoutZoneFirebase.rating).toString());
        Object obj = document.get(ScoutZoneFirebase.ImageUriKey);

        if (obj != null)
        {
            downloadUrl = obj.toString();
            Utils.download2ImageView(downloadUrl, imgPhoto2, this);
        }
        else
        {
            downloadUrl = "";
            imgPhoto2.setImageResource(R.drawable.logo);
        }

        if (rating == 0)
        {
            tvRating.setText("No ratings yet");
        }
        else
            tvRating.setText(rating + " Stars");
        tvId.setText(id);
        tvZonename.setText(name);
        tvType.setText(type);
        tvSpace.setText("Space: " + space);
        tvIsActive.setText("Currently Available");

    }

    public void onOrder(View v) //When clicking on the button in the activity
    {
        vbr.vibrate(1000);
        tvIsActive.setText("Zone is taken for this time");
        Assignment current;
        String ZoneId = tvId.getText().toString();
        String UserEmail = fbUser.getEmail();
        String date = tvInfo2.getText().toString();
        String TimeSlot = tvInfo.getText().toString();

        if (mUploadTask != null && mUploadTask.isInProgress())
        {
            Toast.makeText(this, "File upload is still in progress, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        //There is no need to check if the date / timeslot that the user chose is available or if the user is approved or not,
        //All of it happens in ZoneActivity or in the onCreate
        current = new Assignment(TimeSlot, date, UserEmail, ZoneId, myBranch);
        AssignmentFirebase.add2Firebase(current, AColctRef, this);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //case R.id.btnOrder:
        }
    }

}
