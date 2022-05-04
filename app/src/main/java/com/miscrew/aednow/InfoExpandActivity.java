package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

public class InfoExpandActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseUser user;
    FirebaseAuth auth;
    StorageReference storageReference;
    EditText edtLatitude, edtLongitude, edtTitle, edtSnippet;
    ImageView imgUpvote, imgDownvote;
    ViewGroup layoutImages;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_expand);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // get all firebase references
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = storage.getReference();
        Bundle extras = getIntent().getExtras();
        if (extras == null || account == null) { // no account or lat or lng so why are we here?
            onBackPressed();
        }
        Utils.configureToolbar(this, "AED info");
        edtTitle = findViewById(R.id.edtTitle);
        edtSnippet = findViewById(R.id.edtSnippet);
        edtLatitude =  findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
        imgUpvote = findViewById(R.id.imgUpvote);
        imgDownvote = findViewById(R.id.imgDownvote);

        Gson gson = new Gson();
        MapData ob = gson.fromJson(getIntent().getStringExtra("mapdata"), MapData.class);
        edtTitle.setText(ob.getTitle());
        edtSnippet.setText(ob.getSnippet());
        edtLatitude.setText(Double.toString(ob.getLat()));
        edtLongitude.setText(Double.toString(ob.getLng()));
        ViewGroup layoutImages = findViewById(R.id.layoutImages);

        for(String imgUrl: ob.getImages()) {
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 400));
            imgView.setEnabled(true);
            Utils.loadImage(imgView, imgUrl);
            imgView.setOnClickListener(view -> {
                new Utils(InfoExpandActivity.this).showPhoto(Uri.parse(imgUrl));
            });
            layoutImages.addView(imgView);
        }
    }

    // for custom menu, currently unused
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // options menu
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