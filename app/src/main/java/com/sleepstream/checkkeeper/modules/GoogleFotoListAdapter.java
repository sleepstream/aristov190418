package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.crop.CropActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sleepstream.checkkeeper.MainActivity.currentInvoice;

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
                Intent intent = new Intent(context, CropActivity.class);
                intent.putExtra("photo_reference", placePhotoMetadataList.get(position));
                intent.putExtra("place_id", currentInvoice.store.place_id);
                intent.putExtra("store_id", currentInvoice.store.id);
                context.startActivity(intent);
                /*
                MainActivity.copyfile(imgUrl, Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/" + "IMG_" + currentInvoice.store.place_id + ".png");
                PurchasesPageFragment.placeImage.setImageBitmap(BitmapFactory.decodeFile(imgUrl));
                blurPlotter.setVisibility(View.GONE);
                addMyPhotoContainer.setVisibility(View.GONE);
                placePhotoMetadataList.clear();
                notifyDataSetChanged();
                */
                //((Activity)context).setResult(RESULT_OK, intent);
                //((Activity)context).finish();

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
