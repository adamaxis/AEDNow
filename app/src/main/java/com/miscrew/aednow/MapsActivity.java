package com.miscrew.aednow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.*;
import com.miscrew.aednow.databinding.ActivityMapsBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    public static GoogleSignInAccount account;
    private GoogleMap mMap;
    GoogleSignInClient gsc;
    FirebaseAuth mAuth;
    //SignInButton btnSignIn;
    Button btnSignOut;
    public Mapper md;
    public Marker marker;
    private ActivityMapsBinding binding;
    private int signInIcon = R.mipmap.ic_login;
    private int signOutIcon = R.mipmap.ic_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utils.configureToolbar(this, "AEDNow", false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // begin google sign-in process
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getApplicationContext().getString(R.string.default_web_client_id))
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);
        changeLoginText();
    }


    // change button text to reflect sign-in status
    @SuppressLint("RestrictedApi")
    private void changeLoginText() {
        //account = GoogleSignIn.getLastSignedInAccount(this);
        ActionMenuItemView item = findViewById(R.id.menu_login);
        if (!isLoggedIn()) {
            if (item != null) item.setIcon(getDrawable(signInIcon));
        } else {
            Snackbar.make(findViewById(R.id.MapsCoordinator), "Successfully signed in as user " + account.getDisplayName() + ".", Snackbar.LENGTH_SHORT).show();
            if (item != null) item.setIcon(getDrawable(signOutIcon));
        }
    }

    // check sign-in status
    public Boolean isLoggedIn() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        return (account != null);
    }

    // sign out code
    private void SignOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mAuth.signOut();
                Snackbar.make(findViewById(R.id.MapsCoordinator), "Successfully signed out.", Snackbar.LENGTH_SHORT).show();
                changeLoginText();
            }
        });
    }
    // end sign out code

    // sign in code
    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    account = task.getResult(ApiException.class);
                    // firebase login
                    firebaseAuthWithGoogle(account.getIdToken());
                    changeLoginText();
                } catch (ApiException e) {
                    Snackbar.make(findViewById(R.id.MapsCoordinator), "Error signing in", Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(findViewById(R.id.MapsCoordinator), "Error authenticating with Firebase:" + task.getException(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // end sign in code


    // toolbar creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_login:
                if (account == null) {
                    SignIn();
                } else {
                    SignOut();
                }
                break;
            case R.id.menu_aedrequests:
                Intent mIntent = new Intent(this, AdminActivity.class);
                startActivity(mIntent);
                break;
            case R.id.menu_preferences:
                Intent mIntent2 = new Intent(this, PreferencesActivity.class);
                startActivity(mIntent2);
                break;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // enable zoom and location controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapLongClickListener(this);
        // read in JSON map markers
        readJSONMap();

        LatLng marker = new LatLng(41.62017922107947, -93.60208950567639);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        // zoom in
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

        //googleMap.setOnMarkerClickListener(this);
        CustomMarkerWindow infoWin = new CustomMarkerWindow(getApplicationContext(), md);
        mMap.setInfoWindowAdapter(infoWin);
        mMap.setOnInfoWindowLongClickListener(infoWin);
    }

    private void readJSONMap() {
        // create Gson instance for JSON read
        Gson gson = new Gson();
        try {
            // create a reader to read our JSON file
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("map.json")));
            // read JSON map location data from file into our mapper class
            md = gson.fromJson(reader, Mapper.class);

            for (MapData x : md.mapData) {
                // create a new location marker for each entry
                LatLng marker = new LatLng(x.getLat(), x.getLng());
                // set lat+lng+title+description+icon
                MarkerOptions moMarker = new MarkerOptions();
                // load marker info
                moMarker.position(marker).title(x.getTitle()).snippet(x.getSnippet());

                // load custom icon if specified
                if (x.getIcon() != 0) // null check
                {
                    moMarker.icon(BitmapFromVector(getApplicationContext(), x.getIcon()));
                } else {
                    if (x.getVotes() >= 10) { // confirmed location
                        moMarker.icon(BitmapFromVector(getApplicationContext(), R.mipmap.ic_green_marker));
                    }
                }
                //System.out.println(R.drawable.ic_baseline_not_listed_location_24);
                // add the marker to mMap and set it to x
                x.setMarker(mMap.addMarker(moMarker).getId());
            }
            //}
            // close file
            reader.close();
        } catch (Exception ex) {
            Toast.makeText(this, "Error reading map markers from JSON file", Toast.LENGTH_SHORT).show();
            // print exception to logcat
            ex.printStackTrace();
        }
    }


    // create bitmap from vector image
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // long click: add marker if logged in; show latlng if not logged in
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (isLoggedIn()) {
            Intent mIntent = new Intent(this, AddActivity.class);
            Gson gson = new Gson();
            String json = gson.toJson(latLng);
            mIntent.putExtra("coords", json);
            startActivity(mIntent);
        } else
            Toast.makeText(this, "Lat Lng: " + latLng.latitude + "x" + latLng.longitude, Toast.LENGTH_SHORT).show();

    }
}