package com.miscrew.aednow;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CustomMarkerWindow extends AppCompatActivity implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowLongClickListener {
    private Mapper markerSet;
    private Context context;
    private View myContentsView;

    // constructor
    public CustomMarkerWindow(Context context, Mapper markerSet) {
        // getApplicationContext() returns null here, so context is required
        this.context = context;
        this.markerSet = markerSet;
    }

    // unused, only implemented b/c necessary for GoogleMap.InfoWindowAdapter
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // create custom marker bubble + load it with our layout
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myContentsView = inflater.inflate(R.layout.map_info_content, null);

        // get handles to layout controls
        TextView title = myContentsView.findViewById(R.id.title);
        TextView snippet = myContentsView.findViewById(R.id.snippet);
        ViewGroup layoutImages = (ViewGroup) myContentsView.findViewById(R.id.layoutImages);

        // mapdata selection loop. Inefficient, but works. Should ideally be a hashmap
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().equals(marker.getId())) { // selected map marker
                title.setText(x.getTitle());
                snippet.setText(x.getSnippet());
                // Image loading loop
                for(String imgUrl: x.getImages()) {
                    // create new imageview for each image
                    ImageView imgView = new ImageView(myContentsView.getContext());
                    imgView.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER));
                    imgView.setTextAlignment(FrameLayout.TEXT_ALIGNMENT_CENTER);
                    imgView.setEnabled(true);
                    // load image into imageview and add it to view
                    loadImage(imgView, imgUrl);
                    layoutImages.addView(imgView);
                    // display image (callback to infowindowrefresher when image fully loaded)
                    // note: callback only necessary because of Google Map API limitation
                    Picasso.get()
                            .load(imgUrl)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(imgView, new InfoWindowRefresher(marker, imgUrl));
                }
            }
        }
            return myContentsView;
    }

    // load image into img from url
    private void loadImage(ImageView img, String url) {
        if(url.length() == 0) return;
        Picasso.get()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(img);
    }


    // longclick for marker
    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        // inefficient marker selection
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().equals(marker.getId())) {
                try {
                    Intent mIntent = new Intent(context, InfoExpandActivity.class);
                    Gson gson = new Gson();
                    // required for later versions of android
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra("mapdata", gson.toJson(x)); // package up map marker data into intent
                   context.startActivity(mIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}