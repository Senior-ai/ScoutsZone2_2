package com.a.shon.scoutszone2.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.a.shon.scoutszone2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;

public class PasswordActivity extends Activity {
    Button btnReturn;
    Button btnSend;
    EditText etRemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        btnReturn = findViewById(R.id.btnReturn);
        btnSend = findViewById(R.id.btnSend);
        etRemail = findViewById(R.id.etRemail);
    }
    public void onSend(View v)
    {
        if (isEmpty())
            return;
        FirebaseAuth.getInstance().sendPasswordResetEmail(etRemail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                { Log.d("PasswordReset", "Email Sent!");}
            }
        });
        Toast.makeText(this, "Sent to: " + etRemail.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    private boolean isEmpty() {
        if (TextUtils.isEmpty(etRemail.getText().toString())) {
            etRemail.setError("REQUIRED!");
            return true;
        }
        return false;
    }

    public void onReturn(View v)
    {
        finish();
    }
}



