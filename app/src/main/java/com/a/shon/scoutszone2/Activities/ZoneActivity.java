package com.a.shon.scoutszone2.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.a.shon.scoutszone2.Firebases.AssignmentFirebase;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.MyArrayAdapter;
import com.a.shon.scoutszone2.PermissionUtil;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.SMSReceiver;
import com.a.shon.scoutszone2.Zone;
import com.a.shon.scoutszone2.enums.ZoneType;
import com.a.shon.scoutszone2.enums.isZonebusy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;


public class ZoneActivity extends Activity implements AdapterView.OnItemSelectedListener, RatingBar.OnRatingBarChangeListener, Button.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    ImageView ivSketch;
    ListView lstZone;
    Button btnSetdate;

    TextView tvInfo, tvInfo2; //tvInfo - shows the date, tvInfo2 - shows the time slot
    Spinner spinner_timeslot;
    ArrayList<Zone> zones; // filtered
    ArrayList<Zone> allZones;
    MyArrayAdapter zoneAdapter;

    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 20;
    private final int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 30;
    private final int MY_PERMISSIONS_REQUEST_SMS_SEND = 40;

    SMSReceiver smsRcv;

    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference zoneCollectionRef;
    private CollectionReference aColctRef;
    DocumentSnapshot currentDoc;

    // Firebase Authentication
    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;

    private SharedPreferences.Editor editor;
    public String myBranch;
    public String myRole;
    public boolean isApproved;

    @Override
    protected void onCreate(Bundle icicle) { //add a dialog/menu to logout and manager activities
        super.onCreate(icicle);
        setContentView(R.layout.activity_zone);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        //String currentDate = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
        String currentDate = sdf.format(calendar.getTime());

        tvInfo = findViewById(R.id.tvInfo);
        tvInfo2 = findViewById(R.id.tvInfo2);
        ivSketch = findViewById(R.id.ivSketch);
        btnSetdate = findViewById(R.id.btnSetdate);

        tvInfo.setText(currentDate);
        tvInfo2.setText("Noon");

        //The tvInfo2 set text already happens in the spinner command
        spinner_timeslot = findViewById(R.id.spinner_timeslot);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_timeslot.setAdapter(adapter);
        spinner_timeslot.setOnItemSelectedListener(this);

        // Shared Preferences Initialization
        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");
        myRole = result.getString("myRole", "") ;
        isApproved = result.getBoolean("isApproved", Boolean.parseBoolean(""));

        zones = new ArrayList<Zone>();
        allZones = new ArrayList<Zone>();
        lstZone = findViewById(R.id.lstZone);
        zoneAdapter = new MyArrayAdapter(this, zones);
        lstZone.setAdapter(zoneAdapter);
        lstZone.setOnItemClickListener(this);
        lstZone.setOnItemLongClickListener(this);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        handleRunTimePermissions();
        smsRcv = new SMSReceiver(this);

         //Firebase
        initFirebase();
        refreshList();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        zoneCollectionRef = db.collection(ScoutZoneFirebase.ZonesCollection);
        aColctRef = db.collection(AssignmentFirebase.AssignmentCollection);
    }

    @Override
    public void onResume() {
        super.onResume();
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        refreshList();
        zoneAdapter.notifyDataSetChanged();
        IntentFilter inf = new IntentFilter();
        inf.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsRcv, inf);
        Log.d("SMSReceiver Activity", "Registered");
    }
    @Override
    protected void onPause()
    {
        unregisterReceiver(smsRcv);
        Log.d("SMSReceiver Activiy", "UNRegistered");
        super.onPause();
    }
    @Override
    protected void onDestroy()
    {
        this.smsRcv = null;
        super.onDestroy();
    }

    private void refreshList() {
        Toast.makeText(this, "Requesting data from firebase, please wait", Toast.LENGTH_SHORT).show();
        zoneCollectionRef
                //.whereEqualTo(ScoutZoneFirebase.branchCode, myBranch)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("ScoutZoneFirebase - Read", "Collection was loaded successfuly");
                    Toast.makeText(ZoneActivity.this, "Collection was loaded successfuly", Toast.LENGTH_SHORT).show();
                    add2list(task.getResult());
                    zoneAdapter.notifyDataSetChanged();
                } else {
                    Log.w("ScoutZoneFirebase - Read", "Oy vey", task.getException());
                    Toast.makeText(ZoneActivity.this, "Oy vey:\n" + task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //updates the list
    private void add2list(QuerySnapshot result) {
        String id;
        String name;
        String branchCode;
        ZoneType type;
        int space;
        isZonebusy isActive;

        zones.clear();
        for (QueryDocumentSnapshot document : result) {
            branchCode = document.get(ScoutZoneFirebase.branchCode).toString();
            if (branchCode.equals(myBranch))
            {
                id = document.getId();
                name = document.get(ScoutZoneFirebase.ZoneName).toString();
                type = ZoneType.valueOf(document.get(ScoutZoneFirebase.type).toString());
                space = Integer.parseInt(document.get(ScoutZoneFirebase.space).toString());
                isActive = isZonebusy.valueOf(document.get(ScoutZoneFirebase.isActive).toString());
                Zone item = new Zone(id, name, branchCode, type, space, isActive);
                zones.add(item);
            }
        }
        allZones = (ArrayList) zones.clone();
    }

    private void searchAssignment(final String dateStr, final String time)
    {
        aColctRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d("ZoneFirebase - Read", "Collection was loaded successfuly");
                            updatelist(task.getResult(), dateStr, time);
                        }
                        else
                        {
                            Log.w("ZoneFirebase - Read Filtered", "Oy vey", task.getException());
                        }
                    }
                });
    }
    //Loveliest part of the whole project <3
    private void updatelist(QuerySnapshot result, String dateStr, String time) {
        String id;
        String zoneid;
        String dateAssignment;
        String timeAssignment;
        zones.clear();
        for (Zone z : allZones)
            zones.add(z);
        int count = 0;
        for (QueryDocumentSnapshot document : result) {
            zoneid = document.get(AssignmentFirebase.ZoneId).toString();
            dateAssignment = document.get(AssignmentFirebase.date).toString();
            timeAssignment = document.get(AssignmentFirebase.TimeSlot).toString();
            if (timeAssignment.equals(time) && dateAssignment.equals(dateStr))
                {
                    for (int i =0; i< zones.size(); i++) {
                        if (zones.get(i).getId().equals(zoneid))
                        {
                            zones.remove(i);
                            count++;
                        }
                    }
                }
        }
        zoneAdapter.notifyDataSetChanged();
        Log.d("Removed", count + "");
        //Toast.makeText(this, "Removed" + count, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.the_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item); //menu
        switch (item.getItemId()) {
            case R.id.itemManager:
                if (myRole.equals("בוגר") || myRole.equals("מרכז"))
                {
                    Toast.makeText(this, "Manager...", Toast.LENGTH_SHORT).show();
                    Intent manager = new Intent(this, ManagerActivity.class);
                    startActivity(manager);
                }
                else
                {
                    Toast.makeText(this, "You dont have permission to access this", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.itemLogout:
                if (fbUser != null) {
                    fbAuth.signOut();
                    fbUser = null;
                    editor.clear();
                    editor.commit();
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
        return true;
    }


    private RatingBar mRatingbar;
    private TextView tvRatingscale;
    private TextView tvHappy;
    private EditText etFeedback;
    private Button btnSubmit;

    private void showCustomDialog(final int position) { //A Rating Dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        // set the custom dialog components - text, image and button
        mRatingbar = dialog.findViewById(R.id.mRatingbar);
        tvRatingscale = dialog.findViewById(R.id.tvRatingScale);
        tvHappy = dialog.findViewById(R.id.tvHappy); //Its just the title of the dialog
        etFeedback = dialog.findViewById(R.id.etFeedback);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        dialog.show();

        String Path2font = "Abraham-Regular.ttf"; //Fonts, Just to make it look more professional
        Typeface tf = Typeface.createFromAsset(getAssets(), Path2font);
        tvRatingscale.setTypeface(tf);
        tvHappy.setTypeface(tf);
        etFeedback.setTypeface(tf);
        btnSubmit.setTypeface(tf);

        mRatingbar.setOnRatingBarChangeListener(this); //Rating bar listener
        btnSubmit.setOnClickListener(new View.OnClickListener() { //OnClick Listener
            @Override
            public void onClick(View v) {
                if (etFeedback.getText().toString().isEmpty()) {
                    Toast.makeText(ZoneActivity.this, "Please fill in feedback text box", Toast.LENGTH_LONG).show();
                } else {
                    Zone currentZone = zones.get(position);
                    double newRating = mRatingbar.getRating();
                    currentZone.addRating(newRating);
                    //ScoutZoneFirebase.updateInFirebase(currentZone, mDocRef, ZoneActivity.this);
                    // updates currentZone to Firebase
                    etFeedback.setText("");
                    mRatingbar.setRating(0);
                    dialog.dismiss();
                    Toast.makeText(ZoneActivity.this, "Thank you for sharing your feedback", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onRatingChanged(RatingBar mRatingbar, float v, boolean b) {
        tvRatingscale.setText("איך היה לכם?");

        switch ((int) mRatingbar.getRating()) {
            case 1:
                tvRatingscale.setText("היה נוראי");
                break;
            case 2:
                tvRatingscale.setText("יכול להיות יותר טוב");
                break;
            case 3:
                tvRatingscale.setText("בסדר");
                break;
            case 4:
                tvRatingscale.setText("מצויין");
                break;
            case 5:
                tvRatingscale.setText("מושלם. אני אוהב/ת את זה");
                break;
            default:
                tvRatingscale.setText("");
        }
    }

    //Ordering Order Activity
    private void openOrder(String id) {
            Intent order = new Intent(this, OrderActivity.class);
            String time = tvInfo2.getText().toString();
            String dateStr = tvInfo.getText().toString();
            order.putExtra("id", id);
            order.putExtra("time", time);
            order.putExtra("date", dateStr);
            startActivity(order);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetdate:
                ShowMyDateDialog();
                break;
            default:
                Toast.makeText(this, "Unavailable", Toast.LENGTH_SHORT).show();

        }
    }

    //Button for opening a date dialog
    public void onSetdate(View v) {
        Toast.makeText(this, "Choose your date", Toast.LENGTH_SHORT).show();
        ShowMyDateDialog();
    }

    @Override //Time Spinner
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (adapterView.getItemAtPosition(i).equals("Select time")) {
            tvInfo2.setText("Noon");
        } else {
            String time = adapterView.getItemAtPosition(i).toString();
            tvInfo2.setText(time);
            String date = tvInfo.getText().toString();
            searchAssignment(date, time);
        }
    }

    @Override //when nothing has been selected yet in the time spinner
    public void onNothingSelected(AdapterView<?> adapterView) {
        String text = "";
        tvInfo2.setText(text);
    }

    public void ShowMyDateDialog() //date dialog
    {
        DatePickerDialog.OnDateSetListener listener = new MyDateSetListener();  // see class implementation below
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this, listener, mYear, mMonth, mDay);
        dpd.show();
    }

    @Override //List view
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String id2 = zones.get(position).getId();
        if (isApproved == true)
        {
            if (tvInfo.getText().toString().equals("") || tvInfo2.getText().toString().equals("")) {
                Toast.makeText(this, "Please Choose Time and date!", Toast.LENGTH_SHORT).show();
            }
            else
                openOrder(id2);
        }
        if (isApproved == false)
            Toast.makeText(this, "You are not Approved yet!", Toast.LENGTH_SHORT).show();


    }

    @Override //Long click on a list view
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isApproved = true)
        {
            Toast.makeText(this, "Start Rating!", Toast.LENGTH_SHORT).show();
            showCustomDialog(position); //Rating dialog
        }
        else
            Toast.makeText(this, "You are not Approved yet!", Toast.LENGTH_SHORT).show();
        return true;
    }

    String dateStr;
    private class MyDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override //Date dialog
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Date date = new Date(year, monthOfYear, dayOfMonth);
            //dateStr = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
            dateStr = dayOfMonth + "/" + "0" + (monthOfYear + 1) + "/" + year;
            tvInfo.setText(dateStr);
            String time = tvInfo2.getText().toString();
            searchAssignment(dateStr, time);
        }
    } // private class MyDateSetListener

    private void handleRunTimePermissions() {
        // NOTE: DO NOT FORGET TO ADD THE PERMISSIONS TO THE MANIFEST
        //    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        Toast.makeText(this, "Checking Permissions", Toast.LENGTH_LONG).show();
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "No SMS Receive Permissions - Requesting", Toast.LENGTH_LONG).show();
            this.requestPermissions(
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
        }
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "No SMS Send - Requesting", Toast.LENGTH_LONG).show();
            this.requestPermissions(
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SMS_SEND);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Requesting Permissions", Toast.LENGTH_LONG).show();
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
        else // we have permissions so need to ask for them again
            Toast.makeText(this, "Permissions OK", Toast.LENGTH_LONG).show();

    }

    @Override //In release notes - not working yossi said he will check it out.
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Yipee
                Log.i("SMSReceiver Activity", "MY_PERMISSIONS_REQUEST_SMS_RECEIVE --> YES");
                Toast.makeText(this, "SMS Receive Permissions granted!", Toast.LENGTH_LONG).show();
            } else
            {
                // Bummer
                Log.i("SMSReceiver Activity", "MY_PERMISSIONS_REQUEST_SMS_RECEIVE --> NO");
                Toast.makeText(this, "SMS Receive Permissions denied, SMS will be ignored!", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_SEND) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Yipee
                Log.i("SMSReceiver Activity", "MY_PERMISSIONS_REQUEST_SEND_SMS --> YES");
                Toast.makeText(this, "SMS Send Permissions granted!", Toast.LENGTH_LONG).show();
            } else
            {
                // Bummer
                Log.i("SMSReceiver Activity", "MY_PERMISSIONS_REQUEST_SEND_SMS --> NO");
                Toast.makeText(this, "SMS Send Permissions denied, SMS will be ignored!", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            if (PermissionUtil.verifyAllPermissions(grantResults)){
                Toast.makeText(this, "Granted Permissions", Toast.LENGTH_LONG).show();
                Log.i("TAG", "MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE --> YES");
            }
            else
            {
                Toast.makeText(this, "Read External Storage Permissions Denied", Toast.LENGTH_LONG).show();
                Log.w("TAG", "MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE --> NO");
            }
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE )
            if (PermissionUtil.verifyAllPermissions(grantResults))
            {
                Toast.makeText(this, "Granted Permissions", Toast.LENGTH_LONG).show();
                Log.i("TAG", "MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE --> YES");
            }
            else
            {
                Toast.makeText(this, "Write External Storage Permissions Denied", Toast.LENGTH_LONG).show();
                Log.w("TAG", "MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE --> NO");
            }
    }

}

