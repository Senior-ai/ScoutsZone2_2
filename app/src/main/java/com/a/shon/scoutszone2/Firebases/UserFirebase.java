package com.a.shon.scoutszone2.Firebases;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.a.shon.scoutszone2.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class UserFirebase {
    public static final String UsersCollection = "Users";

    public static final String email = "email";
    public static final String firstName = "firstName";
    public static final String grade = "grade";
    public static final String UserID = "id";
    public static final String isApproved = "isApproved";
    public static final String lastName = "lastName";
    public static final String roleType = "roleType";
    public static final String branchCode = "branchCode";

    private static Map<String, Object> prepareData2Save(UserManager aw)
    {
        String firstName;
        String lastName;
        String email;
        String branchCode;
        String grade;
        String isApproved;
        String RoleType;

        firstName = aw.getName();
        lastName = aw.getLastName();
        email = aw.getEmail();
        branchCode = aw.getBranchCode();
        grade = aw.getGrade().toString();
        isApproved = String.valueOf(aw.isApproved());
        RoleType = aw.getRole().toString();

        Map<String, Object> data2save = new HashMap<String, Object>();
        data2save.put(UserFirebase.firstName, firstName);
        data2save.put(UserFirebase.lastName, lastName);
        data2save.put(UserFirebase.email, email);
        data2save.put(UserFirebase.branchCode, branchCode);
        data2save.put(UserFirebase.grade, grade);
        data2save.put(UserFirebase.isApproved, isApproved);
        data2save.put(UserFirebase.roleType, RoleType);

        return data2save;
    }

    public static String add2Firebase(UserManager aw, CollectionReference mColctRef, final Context context)
    {
        Map<String, Object> data2save = prepareData2Save(aw);
        DocumentReference document = mColctRef.document();
        String id  = document.getId();

        mColctRef.document().set(data2save).
                addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d("Scoutzone-Save new", "Docment was saved successfuly");
                            Toast.makeText(context, "Docment was saved successfuly", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Log.w("ScoutZone-Save new", "Oy vey", task.getException());
                            Toast.makeText(context, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        return id;
    }

    public static void updateInFirebase(UserManager aw, DocumentReference mDocRef, final Context context)
    {
        Map<String, Object> data2save = prepareData2Save(aw);
        mDocRef.set(data2save).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("ScoutZoneFirebase-Save existing", "Docment was saved successfuly");
                    Toast.makeText(context, "Docment was saved successfuly", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Log.w("ScoutZoneFirebase-Save existing", "Oy vey", task.getException());
                    Toast.makeText(context, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}

