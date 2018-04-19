package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperViewHolder;

public class ItemViewHolderInvoices  extends RecyclerView.ViewHolder implements
        ItemTouchHelperViewHolder,View.OnClickListener{

    protected RelativeLayout root_layout;
    protected RelativeLayout container;
    public ImageView ivReorder;
    protected RelativeLayout relativeReorder;
    protected LinearLayout groupDate;

    protected TextView invoiceSum;
    public TextView storeName;
    protected  ImageView imageIcon;
    protected TextView invoiceDate;
    protected TextView quantity;
    protected TextView groupDateText;
    public View marker;
    public RelativeLayout fixLayout;
    public ImageView fixImage;



    public Integer oldColorContainer;
    public Integer oldColorMarker;



    public ItemViewHolderInvoices(final View v) {
        super(v);
        root_layout = v.findViewById(R.id.root_layout);
        container = v.findViewById(R.id.container);
        //storeName = v.findViewById(R.id.storeName);
        ivReorder = v.findViewById(R.id.ivReorder);

        invoiceDate = v.findViewById(R.id.invoiceDate);
        storeName = v.findViewById(R.id.storeName);
        quantity = v.findViewById(R.id.quantity);
        invoiceSum = v.findViewById(R.id.invoiceSum);
        groupDateText = v.findViewById(R.id.groupDateText);
        groupDate=v.findViewById(R.id.groupDate);
        imageIcon = v.findViewById(R.id.imageIcon);
        marker= v.findViewById(R.id.marker);
        fixLayout = v.findViewById(R.id.fixLayout);
        fixImage= v.findViewById(R.id.fixImage);


        relativeReorder = v.findViewById(R.id.relativeReorder);
    }

    @Override
    public void onClick(View view) {
            /*Drawable background = container.getBackground();
            if (background instanceof ColorDrawable) {
                oldColorContainer = ((ColorDrawable) background).getColor();
            }
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
*/
    }


    @Override
    public void onItemSelected(Context context) {
            /*Drawable background = container.getBackground();
            if (background instanceof ColorDrawable) {
                oldColorContainer = ((ColorDrawable) background).getColor();
            }
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            */
    }

    @Override
    public void onItemClear(Context context) {
        if(oldColorContainer!= null) {
            container.setBackgroundColor(oldColorContainer);
            //ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
            //storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
            //oldColorContainer = null;
        }
        if(oldColorMarker != null)
        {
            marker.setBackgroundColor(oldColorMarker);
        }


        //ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
        //storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
    }
}
