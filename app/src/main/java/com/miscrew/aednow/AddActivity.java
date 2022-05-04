package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.UUID;

public class AddActivity extends AppCompatActivity {
    ArrayList<String> filePaths = new ArrayList<>();
    ArrayList<String> uploadPaths = new ArrayList<>();
    GoogleSignInAccount account;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseUser user;
    FirebaseAuth auth;
    StorageReference storageReference;
    PreviewView preView;
    ImageCapture imgCapture;
    EditText edtLatitude, edtLongitude, edtNotes;
    ImageView img1, img2, img3;
    Button btnSubmit;
    ImageView imgCap;
    Bitmap capturedImg;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    LatLng coords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        account = GoogleSignIn.getLastSignedInAccount(this);
        // get all firebase references
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = storage.getReference();
        Bundle extras = getIntent().getExtras();
        if (extras == null || account == null || user == null) { // pre-emptive account/data check
            onBackPressed();
        }

        Utils.configureToolbar(this, "Add AED location");
        edtLatitude = findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
        edtNotes = findViewById(R.id.edtNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        img1 = findViewById(R.id.imageCap1);
        img2 = findViewById(R.id.imageCap2);
        img3 = findViewById(R.id.imageCap3);
        preView = findViewById(R.id.cameraView);
        initCamera();
        setSSClickListener(img1);
        setSSClickListener(img2);
        setSSClickListener(img3);
        Gson gson = new Gson();
        coords = gson.fromJson(getIntent().getStringExtra("coords"), LatLng.class);
        edtLatitude.setText(Double.toString(coords.latitude));
        edtLongitude.setText(Double.toString(coords.longitude));
        btnSubmit.setOnClickListener(view -> {
            if(account != null) {
                if(filePaths.size() != 0) {
                    doUpload();
                } else Snackbar.make(findViewById(R.id.AddCoordinator), "Please capture at least one image first", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void doUpload() {
        btnSubmit.setEnabled(false);
        View current = getCurrentFocus();
        if (current != null) current.clearFocus();
        uploadImage();
    }

    private void doSubmit () {

        // create mapper from textbox data
        MapData m = new MapData("x", edtNotes.getText().toString(), coords.latitude, coords.longitude);
        m.setImages(uploadPaths);
        m.setVotes(0);
        // create dao object
        AEDDAO db = new AEDDAO();
        db.mapdata = m;
        db.email = account.getEmail();
        db.username = account.getDisplayName();
        DatabaseReference dbRef = database.getReference();
        // database name
        String locName = "AEDLocRequests";
        // entry name
        String entryName = account.getDisplayName();
        dbRef.child(locName)
                .child(entryName)
                .setValue(db)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Data write successful", Snackbar.LENGTH_SHORT)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        onBackPressed();
                                    }
                                }).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Data write failed", Snackbar.LENGTH_SHORT)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        onBackPressed();
                                    }
                                }).show();
                    }
                });
    }


    // method to set click listener for dynamically-generated ImageViews
    private void setSSClickListener (ImageView img) {
        img.setOnLongClickListener(view -> {
            savePhoto(img);
            takePhoto();
            imgCap = img;
            // if you want to turn off the click listener, uncomment this
            //unsetClickListener(img);
            return true;
        });
    }

    // method to unset click listener for dynamically-generated ImageViews
    private void unsetClickListener (ImageView img) {
        img.setOnLongClickListener(null);
    }

    // menu creation, unused atm
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // action menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    // initialize Camera, calls startCameraX
    private void initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                if (Utils.hasPermission(this, Manifest.permission.CAMERA)
                        && Utils.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        && Utils.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    startCameraX(cameraProvider);
                } else {
                    requestPermission(Utils.CAMERA_PERMISSION, Utils.CAMERA_REQUEST_CODE);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // method to initialize cameraX
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(Utils.CAMERA_LENS)
                .build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(preView.getSurfaceProvider());

        imgCapture = new ImageCapture.Builder()
                .setCaptureMode(Utils.CAMERA_QUALITY)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imgCapture);
    }


    public void takePhoto() {
        imgCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                capturedImg = Utils.getBitmap(imageProxy);
                imgCap.setImageBitmap(capturedImg);
                imageProxy.close();
                System.out.println("Image successfully captured.");
            }

            @Override
            public void onError(ImageCaptureException e) {
                capturedImg = null;
                System.out.println("Image capture failed.");
            }
        });
    }

    private void savePhoto(ImageView img) {
        long timestamp = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        imgCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Photo saved successfully at  " + outputFileResults.getSavedUri(), Snackbar.LENGTH_SHORT).show();
                        System.out.println("Photo saved successfully at" + outputFileResults.getSavedUri());
                        String fp = outputFileResults.getSavedUri().toString();
                        filePaths.add(fp);
                        img.setOnClickListener(view -> {
                            new Utils(AddActivity.this).showPhoto(Uri.parse(fp));
                        });
                        // content://media/external/images/media/87
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Snackbar.make(findViewById(R.id.AddCoordinator), "Error saving photo" + exception.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );

    }


    private void requestPermission(String[] permission, int permissionCode) {
        ActivityCompat.requestPermissions(
                this,
                permission,
                permissionCode
        );
    }



    // UploadImage method
    private void uploadImage()
    {
        if (!filePaths.isEmpty()) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


            // adding listeners on upload
            // or failure of image
            for(String f: filePaths) {
                // Defining the child of storageReference
                StorageReference ref
                        = storageReference
                        .child(
                                "AEDLocRequests/images/"
                                        + UUID.randomUUID().toString());
                // Progress Listener for loading
// percentage on the dialog box
                ref.putFile(Uri.parse(f))
                        .addOnSuccessListener(ContextCompat.getMainExecutor(this),
                                taskSnapshot -> {
                                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                                            String generatedFilePath = task.getResult().toString();
                                            System.out.println("## Stored path is "+generatedFilePath);
                                            uploadPaths.add(generatedFilePath);
                                            filePaths.remove(f);
                                            if(filePaths.size() == 0) doSubmit();
                                    });
                                    progressDialog.dismiss();
                                    Toast.makeText(AddActivity.this,"Image Uploaded!!",Toast.LENGTH_SHORT).show();
                                })
                        .addOnFailureListener(ContextCompat.getMainExecutor(this),
                                e -> {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(AddActivity.this,"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
                        })
                        .addOnProgressListener(ContextCompat.getMainExecutor(this),
                                taskSnapshot -> {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                });
            }
        }
    }


}