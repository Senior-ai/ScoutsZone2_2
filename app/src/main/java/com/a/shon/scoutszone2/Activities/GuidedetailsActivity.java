package com.a.shon.scoutszone2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.enums.GradeType;
import com.a.shon.scoutszone2.enums.RoleType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import static com.a.shon.scoutszone2.Firebases.UserFirebase.grade;
import static com.a.shon.scoutszone2.Firebases.UserFirebase.roleType;

public class GuidedetailsActivity extends Activity implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    TextView tvFirstname, tvLastname, tvEmail, tvRole, tvGrade;
    Switch switch_isapproved;
    Spinner spinner_role, spinner_grade;
    Button btnSave;

    DocumentSnapshot currentDoc;
    String id = "";

    //Firebase
    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference mColctRef; //u as the first letter of user

    //Shared prefrences
    private SharedPreferences.Editor editor;
    public String myBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidedetails);

        tvFirstname = findViewById(R.id.tvFirstname2);
        tvLastname = findViewById(R.id.tvLastname2);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvGrade = findViewById(R.id.tvGrade);
        switch_isapproved = findViewById(R.id.switch_isapproved);
        spinner_grade = findViewById(R.id.spinner_grade);
        spinner_role = findViewById(R.id.spinner_role);
        btnSave = findViewById(R.id.btnSave);

        spinner_role.setAdapter(new ArrayAdapter<RoleType>(this, android.R.layout.simple_spinner_item, RoleType.values()));
        spinner_role.setOnItemSelectedListener(this);

        spinner_grade.setAdapter(new ArrayAdapter<GradeType>(this, android.R.layout.simple_spinner_item, GradeType.values()));
        spinner_grade.setOnItemSelectedListener(this);

        switch_isapproved.setOnCheckedChangeListener(this);

        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");

        Intent in = getIntent();
        Bundle xtras = in.getExtras();
        id = xtras.getString("userId");

        initFirebase();
        if (id.length() > 0)
            initDetails();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(UserFirebase.UsersCollection);
    }

    private void initDetails() {
        Toast.makeText(GuidedetailsActivity.this, "Requesting data for Zone " + id + " please wait", Toast.LENGTH_SHORT).show();
        mColctRef.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("ScoutZoneFirebase-GetItem", "Zone " + id + " was retrived successfully");
                    Toast.makeText(GuidedetailsActivity.this, "Zone " + id + " was retrived successfully", Toast.LENGTH_SHORT).show();
                    currentDoc = task.getResult();
                    initForm(currentDoc);
                    mDocRef = currentDoc.getReference();
                }
                else
                {
                    Log.e("ScoutZoneFirebase-GetItem", "Oy vey", task.getException());
                    Toast.makeText(GuidedetailsActivity.this, "Oy vey:\n" + task.getException(), Toast.LENGTH_LONG).show();
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void initForm(DocumentSnapshot document) {
        id = document.getId();
        String Firstname = document.get(UserFirebase.firstName).toString();
        String lastName = document.get(UserFirebase.lastName).toString();
        String email = document.get(UserFirebase.email).toString();
        boolean isApproved = Boolean.parseBoolean(document.get(UserFirebase.isApproved).toString());
        String roleType = document.get(UserFirebase.roleType).toString();
        String grade = document.get(UserFirebase.grade).toString();

        tvFirstname.setText(Firstname);
        tvLastname.setText(lastName);
        tvEmail.setText(email);
        if (isApproved == true)
        {
            switch_isapproved.setChecked(true);
            switch_isapproved.setText("guide status: Approved");
        }
        for (int i = 0; i < spinner_grade.getAdapter().getCount(); i++)
        {
            if (grade.equals(spinner_grade.getAdapter().getItem(i).toString()))
            {
                spinner_grade.setSelection(i);
            }
        }
        for (int i=0; i < spinner_role.getAdapter().getCount(); i++)
        {
            if (roleType.equals(spinner_role.getAdapter().getItem(i).toString()))
            {
                spinner_role.setSelection(i);
            }
        }
        tvRole.setText(roleType);
        tvGrade.setText(grade);

    }

    public void onSave(View v)
    {
        //
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner_grade = (Spinner)parent;
        Spinner spinner_role = (Spinner)parent;
        if(spinner_grade.getId() == R.id.spinner_grade)
        {
            if (parent.getItemAtPosition(position).equals("Select your grade"))
            {
                tvGrade.setText(grade);
            }
            else
            {
                String grade = parent.getItemAtPosition(position).toString();
                tvGrade.setText("Your grade is: " + grade);
            }
        }
        if (spinner_role.getId() == R.id.spinner_role)
        {
            if (parent.getItemAtPosition(position).equals("Select a type"))
            {
                tvRole.setText(roleType);
            }
            else {
                String type = parent.getItemAtPosition(position).toString();
                tvRole.setText("The chosen type is: " + type);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (switch_isapproved.isChecked())
        { switch_isapproved.setText("guide status: Approved");
          boolean ApproveCheck = true;
        }
        else
        {switch_isapproved.setText("guide status: Not approved");
         boolean ApproveCheck = false;
        }
    }
}
