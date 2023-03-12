package com.a.shon.scoutszone2.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.UserArrayAdapter;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.UserManager;
import com.a.shon.scoutszone2.enums.GradeType;
import com.a.shon.scoutszone2.enums.RoleType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.a.shon.scoutszone2.Firebases.UserFirebase.UsersCollection;

public class ApproveActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    Button btnDoneapprove;
    ListView lstReqguides;

    ArrayList<UserManager> users;
    UserArrayAdapter userAdapter;

    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 20;

    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference mColctRef;

    private SharedPreferences.Editor editor;
    public String myBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve);

        btnDoneapprove = findViewById(R.id.btnDoneapprove);

        users = new ArrayList<UserManager> ();

        userAdapter = new UserArrayAdapter(this, users);
        lstReqguides = findViewById(R.id.lstReqguides);
        lstReqguides.setOnItemClickListener(this);
        lstReqguides.setOnItemLongClickListener(this);
        lstReqguides.setAdapter(userAdapter);

        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");

        initFirebase();
        refreshList();
    }


    private void initFirebase()
    {
        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(UsersCollection);
    }

    private void refreshList()
    {
        Toast.makeText(this, "Requesting data from firebase, please wait", Toast.LENGTH_SHORT).show();
        mColctRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("UserZoneFirebase - Read", "Collection was loaded successfuly");
                    Toast.makeText(ApproveActivity.this, "Collection was loaded successfuly", Toast.LENGTH_SHORT).show();
                    add2list(task.getResult());
                    userAdapter.notifyDataSetChanged();
                }
                else
                {
                    Log.w("UserFirebase - Read", "Oy vey", task.getException());
                    Toast.makeText(ApproveActivity.this, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public void onResume()
    {
        super.onResume();
        refreshList();
        userAdapter.notifyDataSetChanged();
    }

    //updates the list
    private void add2list(QuerySnapshot result)
    {
        String id;
        String firstName;
        String lastName;
        RoleType role;
        GradeType grade;
        String email;
        String branchCode;
        boolean isApproved;

        users.clear();
        for (QueryDocumentSnapshot document : result) {
            branchCode = document.get(UserFirebase.branchCode).toString();
            if (branchCode.equals(myBranch))
            {
                id = document.getId();
                firstName = document.get(UserFirebase.firstName).toString();
                lastName = document.get(UserFirebase.lastName).toString();
                role = RoleType.valueOf(document.get(UserFirebase.roleType).toString());
                grade = GradeType.valueOf(document.get(UserFirebase.grade).toString());
                email = document.get(UserFirebase.email).toString();
                isApproved = Boolean.parseBoolean(document.get(UserFirebase.isApproved).toString());
                UserManager user = new UserManager(id, firstName, lastName, role,grade, email, isApproved);
                users.add(user);
            }

        }
    }

    public void openUserDetails(String id2)
    {
        Intent user = new Intent(this, GuidedetailsActivity.class);
        user.putExtra("userId", id2);
        startActivity(user);
    }

    public void onDoneapprove(View v)
    {
        Toast.makeText(this, "guides updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override //List view
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String id2 = users.get(position).getUserID();
        openUserDetails(id2);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        //pops up an alert dialog if clicked yes, then the user is removed.
        showMyAlertDialog(position);
        return true;
    }

    private void deleteDocument(int position)
    {
        String userID = users.get(position).getUserID();
        db.collection(UsersCollection).document(userID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful())
                {Log.e("Deleting-", "Didn't delete the user!"); }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void showMyAlertDialog(int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove the user?");
        builder.setTitle("Removing a user");
        builder.setIcon(R.drawable.ic_delete_black_24dp);
        builder.setCancelable(true);
        OnAlertDialogClickListener listener = new OnAlertDialogClickListener(position);
        builder.setPositiveButton("Yes", listener);
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
                    users.remove(position);
                    userAdapter.notifyDataSetChanged();
                    break;
            }
            dialog.dismiss();
        }
    }
}
