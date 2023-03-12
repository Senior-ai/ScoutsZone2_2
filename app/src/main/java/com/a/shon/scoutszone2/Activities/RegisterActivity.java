package com.a.shon.scoutszone2.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.a.shon.scoutszone2.Branch;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.UserManager;
import com.a.shon.scoutszone2.enums.GradeType;
import com.a.shon.scoutszone2.enums.RoleType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static com.a.shon.scoutszone2.Firebases.UserFirebase.isApproved;

public class RegisterActivity extends Activity implements AdapterView.OnItemSelectedListener {
    Button btnDone;
    EditText etFirstname;
    EditText etLastname;
    EditText etEmail2;
    EditText etPlace;
    EditText etAPass;
    Spinner spinner_role, spinner_grade;
    TextView tvRole, tvGrade;
    ImageView imgLogo;
    private ProgressBar rprogressBar;

    ArrayList<Branch> branches;
    String id = "";

    SharedPreferences sharedPreferences;

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;

    // For firebase
    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference mColctRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imgLogo = findViewById(R.id.imgLogo);
        btnDone = findViewById(R.id.btnDone);

        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etFirstname);
        etEmail2 = findViewById(R.id.etEmail2);
        etAPass = findViewById(R.id.etApass);
        etPlace = findViewById(R.id.etPlace);

        spinner_role = findViewById(R.id.spinner_role);
        spinner_grade = findViewById(R.id.spinner_grade);
        tvRole = findViewById(R.id.tvRole);
        tvGrade = findViewById(R.id.tvGrade);
        rprogressBar = findViewById(R.id.rprogressBar);

        spinner_role.setAdapter(new ArrayAdapter<RoleType>(this, android.R.layout.simple_spinner_item, RoleType.values()));
        spinner_role.setOnItemSelectedListener(this);

        spinner_grade.setAdapter(new ArrayAdapter<GradeType>(this, android.R.layout.simple_spinner_item, GradeType.values()));
        spinner_grade.setOnItemSelectedListener(this);

        branches = new ArrayList<Branch>();
        //Data
        Branch.readFileFromResources(branches,this);
        //Initializing Authentication
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();

        initFirebase();

        //if user is already logged in
        if (fbUser != null)
        {
            Toast.makeText(this, "User already logged in: " + fbUser.getEmail(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initFirebase()
    {
        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(UserFirebase.UsersCollection);
    }

    public void onFinish(View v) //When the user finished the sign up
    {
        if (isEmpty())
            return;
        if (checkBranch())
            return;
        inProgress(true);
        fbAuth.createUserWithEmailAndPassword(etEmail2.getText().toString().trim(), etAPass.getText().toString())
                .addOnSuccessListener(new OnSuccessLstnr("User Registered successfully", true))
                .addOnFailureListener(new OnFailLstnr("Registration failed!"));
    }

    private void inProgress(boolean inProgress) {
        if (inProgress) {
            rprogressBar.setVisibility(View.VISIBLE);
            btnDone.setEnabled(false);
        }
        else
        {
            rprogressBar.setVisibility(View.INVISIBLE);
            btnDone.setEnabled(true);
        }
    }

    private boolean isEmpty() //When edittext are empty and the user tries to click the button
    {
        if (TextUtils.isEmpty(etFirstname.getText().toString()))
        {
            etFirstname.setError("REQUIRED!");
            return true;
        }
        if (TextUtils.isEmpty(etLastname.getText().toString()))
        {
            etFirstname.setError("REQUIRED!");
            return true;
        }
        if (TextUtils.isEmpty(etEmail2.getText().toString()))
        {
            etEmail2.setError("REQUIRED!");
            return true;
        }
        if (TextUtils.isEmpty(etAPass.getText().toString()))
        {
            etAPass.setError("REQUIRED!");
            return true;
        }

        return false;
    }

    private boolean checkBranch()
    {
        for (int i = 0; i <= branches.size(); i++)
        {
            if (!etPlace.getText().toString().equals(branches.get(i).getBranchCode()))
            {
                etPlace.setError("Try Again!");
                return true;
            }
            return false;
        }
        return false;
    }

    private class OnFailLstnr implements OnFailureListener
    {
        String msg;
        public OnFailLstnr(String _msg)
        {
            this.msg = _msg;
        }

        @Override
        public void onFailure(@NonNull Exception e)
        {
            inProgress(false);
            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    }

    public class OnSuccessLstnr implements OnSuccessListener<AuthResult>
    {
        String msg;
        boolean open;

        OnSuccessLstnr(String _msg, boolean _open)
        {
            this.msg = _msg;
            this.open = _open;
        }

        @Override
        public void onSuccess(AuthResult authResult)
        {
            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
            inProgress(true);
            if (open)
            {
                UserManager current;
                String Firstname = etFirstname.getText().toString();
                String Lastname = etLastname.getText().toString();
                String email = etEmail2.getText().toString();
                String BranchCode = etPlace.getText().toString();
                RoleType roleType = RoleType.valueOf(spinner_role.getSelectedItem().toString());
                GradeType gradeType = GradeType.valueOf(spinner_grade.getSelectedItem().toString());
                boolean isApproved = false;
                if (id.length() == 0) //adds the new user
                {
                    current = new UserManager(Firstname, Lastname, email, roleType, gradeType, isApproved, BranchCode);
                    sharedPreferences = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("myBranch", BranchCode);
                    editor.putString("myRole", roleType.toString());
                    editor.putBoolean("isApproved", isApproved);
                    editor.apply();
                    Log.d("Register", current.toString());
                    UserFirebase.add2Firebase(current, mColctRef, RegisterActivity.this);
                }
                finish();
            }
        }
    }

    @Override //Both Spinners
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner_grade = (Spinner)parent;
        Spinner spinner_role = (Spinner)parent;
        if(spinner_grade.getId() == R.id.spinner_grade)
        {
            if (parent.getItemAtPosition(position).equals("Select your grade"))
            {
                String text2 = "";
                tvGrade.setText(text2);
            }
            else
            {
                String grade = parent.getItemAtPosition(position).toString();
                tvGrade.setText("Your grade is: " + spinner_grade.getSelectedItem().toString());
            }
        }
        if (spinner_role.getId() == R.id.spinner_role)
        {
            if (parent.getItemAtPosition(position).equals("Select a type"))
            {
                String text = "";
                tvRole.setText(text);
            }
            else {
                String type = parent.getItemAtPosition(position).toString();
                tvRole.setText("The chosen type is: " + spinner_role.getSelectedItem().toString());
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        String text = "";
        tvRole.setText(text);
        tvGrade.setText(text);
    }

}
