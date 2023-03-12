package com.a.shon.scoutszone2.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.a.shon.scoutszone2.R;
import com.a.shon.scoutszone2.Firebases.ScoutZoneFirebase;
import com.a.shon.scoutszone2.Utils;
import com.a.shon.scoutszone2.Zone;
import com.a.shon.scoutszone2.enums.ZoneType;
import com.a.shon.scoutszone2.enums.isZonebusy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;

import static com.a.shon.scoutszone2.enums.isZonebusy.Available;

public class UploadzoneActivity extends Activity implements AdapterView.OnItemSelectedListener {

    EditText etZonename, etSpace;
    Button btnUpload, btnAcceptmap;
    ImageView imgZone;
    TextView tvTypeset, tvId;
    Spinner spinner_zonetype;
    Bitmap bmp = null;
    DocumentSnapshot currentDoc;
    boolean imageUpdated;
    String id = "";

    // For firebase
    private FirebaseFirestore db;
    private DocumentReference mDocRef;
    private CollectionReference mColctRef;
    // for firebase storage (images)
    Uri imageUri = null;  // the *Local* URI of the image file in the Android device
    private StorageReference mStorageRef;
    private StorageReference fileReference;
    private ProgressBar progressBarUpload;
    private StorageTask mUploadTask; // used to refrain from multiple concurrent uploads
    private String downloadUrl = ""; // the URI in the image file in the storage (to be saved to the DB)
    public static final int PICK_IMAGE_REQUEST = 234;

    private SharedPreferences.Editor editor;
    public String myBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadzone);

        spinner_zonetype = findViewById(R.id.spinner_zonetype);
        etZonename = findViewById(R.id.etZonename);
        etSpace = findViewById(R.id.etSpace);
        btnUpload = findViewById(R.id.btnUpload);
        imgZone = findViewById(R.id.imgLogo);
        tvId = findViewById(R.id.tvId);
        btnAcceptmap = findViewById(R.id.btnAcceptmap);
        tvTypeset = findViewById(R.id.tvTypeset);
        progressBarUpload = findViewById(R.id.progressBarUpload);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ZoneType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_zonetype.setAdapter(adapter);
        spinner_zonetype.setOnItemSelectedListener(this);

        initFirebase();
        imageUpdated = false;

        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        editor = result.edit();
        myBranch = result.getString("myBranch", "");

        Intent in = getIntent();
        Bundle xtras = in.getExtras();
        id = xtras.getString("id");

       if (id.length() > 0)
           initDetails();
       //TODO- add to the if, make spinner of iszonebusy visible.
    }

    private void initFirebase()
    {
        db = FirebaseFirestore.getInstance();
        mColctRef = db.collection(ScoutZoneFirebase.ZonesCollection);
        mStorageRef = FirebaseStorage.getInstance().getReference(ScoutZoneFirebase.ImageFolder);
    }

    private void initForm(DocumentSnapshot document)
    {
        id = document.getId();
        String name = document.get(ScoutZoneFirebase.ZoneName).toString();
        String type = document.get(ScoutZoneFirebase.type).toString();
        int space = Integer.parseInt(document.get(ScoutZoneFirebase.space).toString());
        Object obj = document.get(ScoutZoneFirebase.ImageUriKey); // needed to handle empty URI
        if (obj != null)
        {
            downloadUrl = obj.toString();
            Utils.download2ImageView(downloadUrl, imgZone, this);
        }
        else
            downloadUrl = "";
        if (downloadUrl.equals(""))
            imgZone.setImageResource(R.drawable.logo);

        String space2 = String.valueOf(space);
        if (id.length() > 0)
        {
            tvId.setText(id);
        }
        else
            tvId.setText("Currently there is no id");
        tvTypeset.setText(type);
        etZonename.setText(name);
        etSpace.setText(space2);
    }

    private void initDetails()
    {
        Toast.makeText(UploadzoneActivity.this, "Requesting data for Zone " + id + " please wait", Toast.LENGTH_SHORT).show();
        mColctRef.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("ScoutZoneFirebase-GetItem", "Zone " + id + " was retrived successfully");
                    Toast.makeText(UploadzoneActivity.this, "Zone " + id + " was retrived successfully", Toast.LENGTH_SHORT).show();
                    currentDoc = task.getResult();
                    initForm(currentDoc);
                    mDocRef = currentDoc.getReference();
                }
                else
                {
                    Log.e("ScoutZoneFirebase-GetItem", "Oy vey", task.getException());
                    Toast.makeText(UploadzoneActivity.this, "Oy vey:\n" + task.getException(), Toast.LENGTH_LONG).show();
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void pickPicture(View v)
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    public void onUpload(View v)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    public void onSave(View v)
    {
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        Zone current;
        String Zonename = etZonename.getText().toString();
        int Space = Integer.parseInt(etSpace.getText().toString());
        ZoneType zoneType = ZoneType.valueOf(tvTypeset.getText().toString());
        String branchCode = myBranch;
        isZonebusy resetActive = Available;

        if (mUploadTask != null && mUploadTask.isInProgress())
        {
            Toast.makeText(this, "File upload is still in progress, please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id.length() == 0) //adds the new zone
        {
            current = new Zone(Zonename, zoneType, resetActive, Space, branchCode, downloadUrl);
            ScoutZoneFirebase.add2Firebase(current, mColctRef, this);
        }
        else //updates a zone
        {
            current = new Zone(id, Zonename, zoneType, resetActive, Space, branchCode, downloadUrl);
            ScoutZoneFirebase.updateInFirebase(current, mDocRef, this);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0)//coming from camera
        {
            if (resultCode == RESULT_OK) {
                bmp = (Bitmap) data.getExtras().get("data");
                imgZone.setImageBitmap(bmp);
                imageUpdated = true;
                imageUri = Utils.writeImage(this, bmp, "image");
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                this.imageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = this.getContentResolver().query(this.imageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(picturePath, options);
                imgZone.setImageBitmap(bmp);
                imageUpdated = true;
            }
        }
        if (imageUri != null)
            uploadFile();
    }

    public void uploadFile()
    {
        String storageFileName =
                System.currentTimeMillis() + ".jpg";
        fileReference = mStorageRef.child(storageFileName);

        mUploadTask = fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBarUpload.setProgress(0);
                            }
                        }, 500); // Delay zeroing of the 100% progress bar for 0.5 sec
                        // Now add a DB entry for the upload
                        Toast.makeText(UploadzoneActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                /*
                                lastUpload = new Upload(etName.getText().toString().trim(), url);
                                String uploadId = mDatabaseRef.push().getKey();

                                // Add the actual entry - key generated by the DB, vaule = the upload object
                                mDatabaseRef.child(uploadId).setValue(upload);
                                 */
                            }
                        });



                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(UploadzoneActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressBarUpload.setProgress((int) progress);
                    }
                });
    }

    @Override //spinner
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position).equals("Select a type"))
        {
            String text = "";
            tvTypeset.setText(text);
        }
        else {
            String type = parent.getItemAtPosition(position).toString();
            if (parent.getItemAtPosition(position).equals("Inside a room"))
                type = "Inside_a_room";
            tvTypeset.setText(type);
            Toast.makeText(this,type, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        String text = "";
        tvTypeset.setText(text);
    }
}
