package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.sleepstream.checkkeeper.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GoogleFotoDilog extends BaseAdapter {
    private Context context;
    public List<String> placePhotoMetadataList = new ArrayList<>();
    private static final String LOG_TAG = "GoogleFotoListAdapter";
    private String backupDBPath;
    LayoutInflater lInflater;

    public GoogleFotoDilog(Context context,  List<String> objects) {
        placePhotoMetadataList = objects;
        this.context = context;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return placePhotoMetadataList.size();
    }

    @Override
    public Object getItem(int i) {
        return placePhotoMetadataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = lInflater.inflate(R.layout.foto_list_adapter, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.imageView);
        File imageFile = new  File(placePhotoMetadataList.get(position));
        if(imageFile.exists()){
            Log.d(LOG_TAG, "onBindViewHolder set img  "+ placePhotoMetadataList.get(position));
            imageView.setImageBitmap(BitmapFactory.decodeFile(placePhotoMetadataList.get(position)));
        }
        return convertView;
    }



}
