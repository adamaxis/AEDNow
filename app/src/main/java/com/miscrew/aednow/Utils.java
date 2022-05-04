package com.miscrew.aednow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.nio.ByteBuffer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;

public class Utils {
    public static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final int CAMERA_REQUEST_CODE = 10;
    public static final int CAMERA_LENS = CameraSelector.LENS_FACING_BACK;
    public static final int CAMERA_QUALITY = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;
    private Activity mActivity;

    public Utils(Activity activity){
        mActivity = activity;
    }

    public void showPhoto(Uri photoUri){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(photoUri, "image/*");
        mActivity.startActivity(intent);
    }

    public static void loadImage(ImageView img, String url) {
        if(url.length() == 0) return;
        Picasso.get()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(img);
    }


    // toolbar
    public static Toolbar configureToolbar(AppCompatActivity ap, String name) {
        return configureToolbar(ap, name, true);
    }
    // configure toolbar for page
    public static Toolbar configureToolbar(AppCompatActivity ap, String name, boolean isHome) {
        // create toolbar
        Toolbar toolbar = ap.findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        ap.setSupportActionBar(toolbar);
        ap.getSupportActionBar().setHomeButtonEnabled(true);
        ap.getSupportActionBar().setDisplayHomeAsUpEnabled(isHome);
        ap.getSupportActionBar().setDisplayShowHomeEnabled(isHome);
        return toolbar;
    }



    // camera stuff
    public static Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == context.getPackageManager().PERMISSION_GRANTED;
        // PackageManager
    }
}
