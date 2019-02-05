package com.sleepstream.checkkeeper.modules;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.sleepstream.checkkeeper.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sleepstream.checkkeeper.MainActivity.*;
import static com.sleepstream.checkkeeper.modules.PurchasesPageFragment.googleFotoListAdapter;

public class GoogleFotoListAdapter extends RecyclerView.Adapter<GoogleFotoListAdapter.ItemViewHolder>  {
    private Context context;
    public List<String> placePhotoMetadataList = new ArrayList<>();
    public Map<String, String> photoData = new LinkedHashMap<>();
    private static final String LOG_TAG = "GoogleFotoListAdapter";
    private String backupDBPath;


    public GoogleFotoListAdapter(Context applicationContext) {
        this.context= applicationContext;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        backupDBPath  = viewGroup.getContext().getCacheDir().getAbsolutePath()+"/";
        Log.d(LOG_TAG, "onCreateViewHolder ");
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.foto_list_adapter, viewGroup, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GoogleFotoListAdapter.ItemViewHolder holder, final int position) {
        File imageFile = new  File(photoData.get(placePhotoMetadataList.get(position)));
        if(imageFile.exists()){
            Log.d(LOG_TAG, "onBindViewHolder set img  "+ placePhotoMetadataList.get(position));
            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blurPlotter.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                addMyPhotoContainer.setVisibility(View.GONE);

                Uri sourceUri = null;
                try {
                    URL photoUrl = new URL("https://maps.googleapis.com/maps/api/place/photo?maxheight=5000&photo_reference=" + placePhotoMetadataList.get(position) + "&key=" + context.getString(R.string.google_maps_key));
                    sourceUri = Uri.parse(photoUrl.toURI().toString());
                    currentInvoice.store_on_map.photo_reference = placePhotoMetadataList.get(position);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (googleFotoListAdapter != null) {
                    googleFotoListAdapter.placePhotoMetadataList.clear();
                    googleFotoListAdapter.placePhotoMetadataList = null;
                    googleFotoListAdapter.notifyDataSetChanged();
                }

                final String filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/";
                File file = new File(filepath, "IMG_" + currentInvoice.store_on_map.place_id + ".png");
                File file1 = new File(filepath);
                if(!file1.exists())
                {

                    if(!file1.mkdirs())
                    {
                        Toast.makeText(context, context.getString(R.string.error_file_not_found_call_admin), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Uri destinationUri = Uri.parse(file.toURI().toString());

                UCrop.Options options = new UCrop.Options();
                options.setFreeStyleCropEnabled(true);
                options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.SCALE, UCropActivity.SCALE);
                options.setHideBottomControls(true);
                options.setAspectRatioOptions(0, new AspectRatio("16x4", 16, 4));
                UCrop.of(sourceUri, destinationUri)
                        .withOptions(options)
                        .start((Activity) context);

            }
        });
    }


    @Override
    public int getItemCount() {
        return placePhotoMetadataList.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;



        public ItemViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "ItemViewHolder ");
            imageView = itemView.findViewById(R.id.imageView);

        }
    }
}
