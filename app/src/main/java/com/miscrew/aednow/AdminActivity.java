package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseUser user;
    FirebaseAuth auth;
    StorageReference storageReference;
    EditText edtLatitude;
    EditText edtLongitude;
    EditText edtNotes;
    ViewGroup layoutImages;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Bundle extras = getIntent().getExtras();
        // get all firebase references
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = storage.getReference();
        if (account == null) { // no account so why are we here?
            onBackPressed();
        }
        Utils.configureToolbar(this, "Database AED Requests");
        Gson gson = new Gson();
        MapData ob = gson.fromJson(getIntent().getStringExtra("mapdata"), MapData.class);
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference();
        dbRef.child("AEDLocRequests").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    System.out.println("Error parsing data from firebase: " + task.getException());
                }
                else {
                    AEDDAO dbEntry;
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        dbEntry = gson.fromJson(gson.toJsonTree(child.getValue()), AEDDAO.class);
                        // dbEntry contains email, username, mapdata structure
                        // use it to generate controls here



                    }
                }
            }
        });
    }


    /*
    @SuppressLint("ResourceType")
    private void imageExpand(ImageView src) {
        System.out.println("IMAGEVIEW");
        LayoutInflater inflater = (LayoutInflater) InfoExpandActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vs  = inflater.inflate(R.layout.layout_fullscreen_image, (ViewGroup)findViewById(R.id.layoutImages), false);
        // create the popup window
        int width = FrameLayout.LayoutParams.WRAP_CONTENT;
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(vs, width, height, true);

        ImageView ss = vs.findViewById(R.id.imgFs);
        ss.setImageDrawable(src.getDrawable());
        popupWindow.showAtLocation(src, Gravity.TOP, 0, 0);
        // dismiss the popup window when touched
        vs.setOnClickListener((view) -> {
            popupWindow.dismiss();
            //return true;
        });

    }*/



    private void loadImage(ImageView img, String url) {
        if(url.length() == 0) return;
        Picasso.get()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(img);
    }

    // toolbar creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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


}