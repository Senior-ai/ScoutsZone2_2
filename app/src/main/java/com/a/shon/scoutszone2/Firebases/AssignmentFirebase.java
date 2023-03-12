package com.a.shon.scoutszone2.Firebases;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.a.shon.scoutszone2.Assignment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class AssignmentFirebase {
    public static final String AssignmentCollection = "Assignments";

    public static final String branchCode = "branchCode";
    public static final String ZoneId = "ZoneId";
    public static final String UserId = "UserId";
    public static final String date = "date";
    public static final String TimeSlot = "TimeSlot";

    private static Map<String, Object> prepareData2Save(Assignment aw)
    {
        String branchCode;
        String ZoneId;
        String UserId;
        String date;
        String TimeSlot;

        branchCode = aw.getBranchCode();
        ZoneId = aw.getZoneId();
        UserId = aw.getUserId();
        date = aw.getDate();
        TimeSlot = aw.getTimeslot();

        Map<String, Object> data2save = new HashMap<String, Object>();
        data2save.put(AssignmentFirebase.branchCode, branchCode);
        data2save.put(AssignmentFirebase.ZoneId, ZoneId);
        data2save.put(AssignmentFirebase.UserId, UserId);
        data2save.put(AssignmentFirebase.date, date);
        data2save.put(AssignmentFirebase.TimeSlot, TimeSlot);
        return data2save;
    }

    public static void add2Firebase(Assignment aw, CollectionReference mColctRef, final Context context)
    {
        Map<String, Object> data2save = prepareData2Save(aw);

        mColctRef.document().set(data2save).
                addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d("Assignment-Save new", "Docment was saved successfuly");
                            Toast.makeText(context, "Docment was saved successfuly", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Log.w("Assignment-Save new", "Oy vey", task.getException());
                            Toast.makeText(context, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static void updateInFirebase(Assignment aw, DocumentReference mDocRef, final Context context)
    {
        Map<String, Object> data2save = prepareData2Save(aw);
        mDocRef.set(data2save).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("AssignmentFirebase-Save existing", "Docment was saved successfuly");
                    Toast.makeText(context, "Docment was saved successfuly", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Log.w("AssignmentFirebase-Save existing", "Oy vey", task.getException());
                    Toast.makeText(context, "Oy vey:\n" +  task.getException(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
