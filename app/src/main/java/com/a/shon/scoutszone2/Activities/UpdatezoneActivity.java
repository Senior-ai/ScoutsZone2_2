package com.a.shon.scoutszone2.Activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.a.shon.scoutszone2.Firebases.AssignmentFirebase;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.MyArrayAdapter;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.Zone;
import com.a.shon.scoutszone2.enums.ZoneType;
import com.a.shon.scoutszone2.enums.isZonebusy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase.ZonesCollection;
import static com.a.shon.scoutszone2.Firebases.UserFirebase.UsersCollection;

public class UpdatezoneActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, Button.OnClickListener {

    ListView lstUpdatezone;
    Button btnApprove;
    ArrayList<Zone> zones;
    MyArrayAdapter zoneAdapter;

    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 20;

    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference zoneCollectionRef;
    private CollectionReference mColctRef;
    private int last;

    private SharedPreferences.Editor editor;
    public String myBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatezone);

        btnApprove = findViewById(R.id.btnApprove);

        zones = new ArrayList<Zone> ();
        zoneAdapter = new MyArrayAdapter(this, zones);

        lstUpdatezone = findViewById(R.id.lstUpdatezone);
        lstUpdatezone.setOnItemClickListener(this);
        lstUpdatezone.setOnItemLongClickListener(this);
        lstUpdatezone.setAdapter(zoneAdapter);

        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(ZonesCollection);
        mDocRef = mColctRef.document("1");
        last = 0;

        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");

        initFirebase();
        refreshList();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        zoneCollectionRef = db.collection(ZonesCollection);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshList();
        zoneAdapter.notifyDataSetChanged();
    }

    private void refreshList()
    {
        mColctRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("ScoutZoneFirebase - Read", "Collection was loaded successfuly");
                    Toast.makeText(UpdatezoneActivity.this, "Collection was loaded successfuly", Toast.LENGTH_LONG).show();
                    add2list(task.getResult());
                    zoneAdapter.notifyDataSetChanged();
                }
                else
                {
                    Log.w("ScoutZoneFirebase - Read", "Oy vey", task.getException());
                    Toast.makeText(UpdatezoneActivity.this, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //updates the list
    private void add2list(QuerySnapshot result)
    {
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
    }

    public void onApprove(View v)
    {
        finish();
    }

    @Override //short click on the List view
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String zoneId = zones.get(position).getId();
        openDetails(zoneId);
    }

    //Long click on the list view
    //TODO - Disable it for the test, you can update it and try to fix it afterwards. (thats for the coder himself not for the checkers of the code)
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showMyAlertDialog(position);
        return true;

    }

    @Override //buttons
    public void onClick(View view) {

    }
    //gets called when the user is clicking the approve button in the alert dialog.
    //deletes the zone from the firebase database
    private void deleteDocument(int position)
    {
        String zoneiD = zones.get(position).getId();
        db.collection(ZonesCollection).document(zoneiD).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful())
                {Log.e("Deleting-", "Didn't delete the zone!"); }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void showMyAlertDialog(int position) //Alert dialog
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the zone?");
        builder.setTitle("Removing the zone");
        builder.setIcon(R.drawable.ic_delete_black_24dp);
        builder.setCancelable(true);
        OnAlertDialogClickListener listener = new OnAlertDialogClickListener(position);
        builder.setPositiveButton("Remove it!", listener);
        builder.setNegativeButton("Cancel", listener);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    class OnAlertDialogClickListener implements DialogInterface.OnClickListener
    {

        int position;
        public OnAlertDialogClickListener(int position)
        {
            this.position = position;
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case Dialog.BUTTON_NEGATIVE:
                    break;
                case Dialog.BUTTON_POSITIVE:
                    // int which = -1
                    deleteDocument(position);
                    zones.remove(position);
                    zoneAdapter.notifyDataSetChanged();
                    break;
            }
            dialog.dismiss();
        }
    }

    public void openDetails(String id) //Gets the required info about the zone and opening the new intent
    {
        Intent in = new Intent(this, UploadzoneActivity.class);
        in.putExtra("id", id);
        startActivity(in);
    }
}
