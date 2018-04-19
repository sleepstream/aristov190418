package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.sleepstream.checkkeeper.R;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooglePlace {


    public String icon;
    public List<Photo> photos = new ArrayList<>();
    private Context context;
    public String mWidth = "500";
    public String mHeight = "500";
    public String place_id;

    public GooglePlace(String place_id, Context context) {
        this.place_id = place_id;
        this.context = context;
        String urlGet="https://maps.googleapis.com/maps/api/place/details/json?placeid="+place_id+"&key="+context.getString(R.string.google_maps_key);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(urlGet)
                .build();

        JSONObject jResults = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            //System.out.println(urlGet+"\n" + response.body().string().toString());

            JSONObject jObject = new JSONObject(response.body().string().toString());
            jResults = jObject.getJSONObject("result");
            JSONArray jPhotos = jResults.getJSONArray("photos");
            for(int i =0; i<jPhotos.length(); i++)
            {
                Photo photo = new Photo();
                JSONObject jS = (JSONObject) jPhotos.get(i);
                photo.height = jS.getString("height");
                photo.width = jS.getString("width");
                photo.html_attributions = jS.getString("html_attributions");
                photo.photo_reference = jS.getString("photo_reference");

                photos.add(photo);
            }
            icon = jResults.getString("icon");
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            if(jResults != null)
            {
                try {
                    icon = jResults.getString("icon");
                } catch (JSONException e1) {
                    icon="https://maps.gstatic.com/mapfiles/place_api/icons/geocode-71.png";
                    //set default icon!
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }

    }

    public  Bitmap loadImage(String photo_reference)
    {
        String urlGet="https://maps.googleapis.com/maps/api/place/photo?maxwidth="+mWidth+"&photoreference="+photo_reference+"&key="+context.getString(R.string.google_maps_key);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(urlGet)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            //System.out.println(urlGet+"\n" + response.body().string().toString());

            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeStream(response.body().byteStream());
            response.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class Photo
    {
        public String height;
        public String width;
        public String photo_reference;
        public String html_attributions;
    }
}
