package com.a.shon.scoutszone2.Firebases;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.a.shon.scoutszone2.Zone;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class ScoutZoneFirebase {
    public static final String ZonesCollection = "Zones";

    public static final String ZoneName = "name";
    public static final String branchCode = "branchCode";
    public static final String isActive = "isActive";
    public static final String space = "space";
    public static final String rating = "rating";
    public static final String raters = "raters";
    public static final String type = "ZoneType";
    public static final String ImageUriKey = "imageUri";
    public static final String ImageFolder = "zones";

    private static Map<String, Object> prepareData2Save(Zone aw)
    {
        String name;
        String branchCode;
        String isActive;
        int space;
        double rating;
        int raters;
        String ZoneType;
        String imageFileURI;
        String ImageFolder;

        name = aw.getZonename();
        branchCode = aw.getBranchCode();
        isActive = aw.getAvailability().toString();
        space = aw.getSpace();
        rating = aw.getRating();
        raters = aw.getRaters();
        ZoneType = aw.getType().toString();
        imageFileURI = aw.getImageFileURI();

        Map<String, Object> data2save = new HashMap<String, Object>();
        data2save.put(ScoutZoneFirebase.ZoneName, name);
        data2save.put(ScoutZoneFirebase.branchCode, branchCode);
        data2save.put(ScoutZoneFirebase.isActive, isActive);
        data2save.put(ScoutZoneFirebase.space, space);
        data2save.put(ScoutZoneFirebase.rating, rating);
        data2save.put(ScoutZoneFirebase.raters, raters);
        data2save.put(ScoutZoneFirebase.type, ZoneType);
        data2save.put(ScoutZoneFirebase.ImageUriKey, imageFileURI);
        return data2save;
    }

    public static String add2Firebase(Zone aw, CollectionReference mColctRef, final Context context)
    {
        Map<String, Object> data2save = prepareData2Save(aw);

        DocumentReference document = mColctRef.document();
        String id  = document.getId();

        document.set(data2save).
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

    public static void updateInFirebase(Zone aw, DocumentReference mDocRef, final Context context)
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
