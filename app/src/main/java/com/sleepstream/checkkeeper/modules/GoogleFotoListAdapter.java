package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class GoogleFotoListAdapter extends RecyclerView.Adapter<GoogleFotoListAdapter.ItemViewHolder>  {
    private Context context;
    public List<String> placePhotoMetadataList = new ArrayList<>();
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
        File imageFile = new  File(placePhotoMetadataList.get(position));
        if(imageFile.exists()){
            Log.d(LOG_TAG, "onBindViewHolder set img  "+ placePhotoMetadataList.get(position));
            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(placePhotoMetadataList.get(position)));
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placePhotoMetadataList.get(position);
                String imgUrl = placePhotoMetadataList.get(position);
                MainActivity.copyfile(imgUrl, Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/" + "IMG_" + currentInvoice.store.place_id + ".png");
                PurchasesPageFragment.placeImage.setImageBitmap(BitmapFactory.decodeFile(imgUrl));
                blurPlotter.setVisibility(View.GONE);
                addMyPhotoContainer.setVisibility(View.GONE);
                placePhotoMetadataList.clear();
                notifyDataSetChanged();
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
