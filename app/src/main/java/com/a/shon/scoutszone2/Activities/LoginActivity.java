package com.a.shon.scoutszone2.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.Firebases.UserFirebase;
import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.UserManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoginActivity extends Activity {

    private Button btnRegister, btnSignIn, btnForgot;
    private EditText etPhone, etPassword;

    private ProgressBar mprogressBar;

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;

    private FirebaseFirestore db;
    private DocumentReference uDocRef;
    private CollectionReference uColctRef;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnForgot = findViewById(R.id.btnForgot);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        mprogressBar = findViewById(R.id.mprogressBar);

        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();

        initFirebase();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        uColctRef = db.collection(UserFirebase.UsersCollection);
        if (fbUser != null)
        {
            Toast.makeText(this, "User already logged in: " + fbUser.getEmail(), Toast.LENGTH_LONG).show();
            refreshExistingUser();
            Intent zone = new Intent(this, ZoneActivity.class);
            startActivity(zone);
        }
    }

    private void refreshListAndSubscribe() {
        uColctRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot result,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("FirebaseDemo-Query", "Listen failed", e);
                            e.printStackTrace();
                            return;
                        }
                        Log.i("FirebaseDemo-Query", "Listen succeded");
                        checklist(result);
                    }
                });

    }

    private void checklist(QuerySnapshot result) {
        String email;
        boolean isApproved;
        sharedPreferences = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (QueryDocumentSnapshot document : result) {
            email = document.get(UserFirebase.email).toString();
            if (etPhone.getText().toString().trim().equals(email))
            {
                String branchCode = String.valueOf(document.get(UserFirebase.branchCode));
                String role = document.get(UserFirebase.roleType).toString();
                isApproved = Boolean.parseBoolean(document.get(UserFirebase.isApproved).toString());
                editor.putString("myBranch", branchCode);
                editor.putString("myRole", role);
                editor.putString("myEmail", email);
                editor.putBoolean("isApproved", isApproved);
                editor.apply();
            }
        }
    }

    private void refreshExistingUser() {
        uColctRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot result,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("FirebaseDemo-Query", "Listen failed", e);
                            e.printStackTrace();
                            return;
                        }
                        Log.i("FirebaseDemo-Query", "Listen succeded");
                        checkuser(result);
                    }
                });
    }

    private void checkuser(QuerySnapshot result) {
        String email;
        boolean isApproved;
        sharedPreferences = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (QueryDocumentSnapshot document : result) {
            email = document.get(UserFirebase.email).toString();
            if (fbUser.getEmail().equals(email)) { //When the user isnt == null (Reopening the app and such)
                String branchCode = String.valueOf(document.get(UserFirebase.branchCode));
                String role = document.get(UserFirebase.roleType).toString();
                isApproved = Boolean.parseBoolean(document.get(UserFirebase.isApproved).toString());
                editor.putString("myBranch", branchCode);
                editor.putString("myRole", role);
                editor.putString("myEmail", email);
                editor.putBoolean("isApproved", isApproved);
                editor.apply();
            }
        }
    }
    public void onSign(View v)
    {
       if (isEmpty())
            return;
            inProgress(true);
                fbAuth.signInWithEmailAndPassword(etPhone.getText().toString().trim(), etPassword.getText().toString())
               .addOnSuccessListener(new OnSuccessLstnr("User Signed In", true))
                .addOnFailureListener(new OnFailLstnr("Sign-in failed!"));
    }

    public void setPassword(View v)
    {
        Intent pass = new Intent(this, PasswordActivity.class);
        startActivity(pass);
    }

    public void onRegister(View v)
    {
        Intent auth = new Intent (this, RegisterActivity.class);
        startActivity(auth);
    }

    private void inProgress(boolean inProgress)
    {
        if (inProgress)
        {
            mprogressBar.setVisibility(View.VISIBLE);
            btnSignIn.setEnabled(false);
            btnRegister.setEnabled(false);
            btnForgot.setEnabled(false);
        }
        else
        {
            mprogressBar.setVisibility(View.INVISIBLE);
            btnSignIn.setEnabled(true);
            btnRegister.setEnabled(true);
            btnForgot.setEnabled(true);
        }
    }

    private boolean isEmpty()
    {
        if (TextUtils.isEmpty(etPhone.getText().toString()))
        {
            etPhone.setError("REQUIRED!");
            return true;
        }
        if (TextUtils.isEmpty(etPassword.getText().toString()))
        {
            etPassword.setError("REQUIRED!");
            return true;
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
            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
            inProgress(false);
            if (open)
            {
                refreshListAndSubscribe();
                Intent in = new Intent(LoginActivity.this, ZoneActivity.class);
                startActivity(in);
                finish();
            }
        }
    }



}
