package com.sleepstream.checkkeeper.modules;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperViewHolder;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.*;


public class PlaceChooserAdapter extends RecyclerView.Adapter<PlaceChooserAdapter.ItemViewHolder>  {

    public interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private TextView invsNumber;
    public List<InvoiceData.Store_on_map> stores_on_map_list;
    final String LOG_TAG = "PlaceChooserAdapter";
    private final Context context;
    public Integer row_index = -1;
    private int YOU_MAX_VALUE = 3;

    public List<Integer> selectedItems = new ArrayList<>();

    public PlaceChooserAdapter(Context context, List<InvoiceData.Store_on_map> stores_on_map_list) {
        this.invsNumber = invsNumber;
        this.stores_on_map_list = stores_on_map_list;
        this.context = context;
    }



    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        final InvoiceData.Store_on_map item = stores_on_map_list.get(position);
        //invsNumber.setText(String.valueOf(getItemCount()));
        itemViewHolder.storeName.setText(item.name);
        itemViewHolder.storeAddress.setText(item.address);
        if(item.distance != null) {
            if(item.distance < 500)
                itemViewHolder.quantity.setText((item.distance) + " м.");
            else
                itemViewHolder.quantity.setText((item.distance/1000) + " км.");
        }

        itemViewHolder.google_link_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "https://www.google.com/maps/search/?api=1&query=1&query_place_id=" + item.place_id;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(intent);
            }
        });
        itemViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setMessage(R.string.select_place_confurm);
                alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "good!", Toast.LENGTH_LONG).show();
                        ((Activity)context).finish();
                        if(currentInvoice.fk_stores_links != null) {
                            currentInvoice.store_on_map = stores_on_map_list.get(position);
                            MainActivity.AsyncSecondAddInvoice asyncSecondAddInvoice = new MainActivity.AsyncSecondAddInvoice();
                            asyncSecondAddInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentInvoice);
                        }


                    }
                });
                alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();

            }
        });


        if(selectedItems.contains(position))
        {
            itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        else
        {
            itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
        }








    }

    @Override
    public int getItemCount() {
/*
        if (stores_on_map_list != null) {
            return Math.min(stores_on_map_list.size(), YOU_MAX_VALUE);
        } else {
            return 0;
        }*/
        return stores_on_map_list.size();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_chooser_adapter, viewGroup, false);
        return new PlaceChooserAdapter.ItemViewHolder(itemView);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder,View.OnClickListener {

        protected RelativeLayout container;
        protected RelativeLayout google_link_layout;
        protected TextView storeName;
        protected RelativeLayout relativeReorder;

        protected  TextView quantity;
        protected  TextView storeAddress;
        protected  TextView sumPerPosition;
        public Integer oldColorContainer;
        public ImageView store_icon;


        protected TextView sign;


        public ItemViewHolder(final View v) {
            super(v);
            container = v.findViewById(R.id.container);
            google_link_layout = v.findViewById(R.id.google_link_layout);
            storeName = v.findViewById(R.id.storeName);
            storeAddress = v.findViewById(R.id.storeAddress);
            store_icon = v.findViewById(R.id.store_icon);

            sign = v.findViewById(R.id.sign);
            quantity = v.findViewById(R.id.quantity);

            sumPerPosition = v.findViewById(R.id.sumPerPosition);
            sign.setText(fromHtml("&#xd7"));


        }

        @Override
        public void onClick(View view) {

        }



        @Override
        public void onItemSelected(Context context) {
            //container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            //storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
            //ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void onItemClear(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
        }

    }
/*
    private void showPopupMenuAccountingList(final View view, final int position) {
        final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popupmenu_item);


        //generate it from DB
        popupMenu.getMenu().add(R.id.menugroup1, 100, Menu.NONE, R.string.rename_btn);
        popupMenu.getMenu().add(R.id.menugroup1, 101, Menu.NONE, R.string.delete_btn);

        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        switch(item.getItemId())
                        {
                            case 100:
                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                builder.setTitle(R.string.title_addAccointingList);
                                final EditText input = new EditText(context);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                input.setText(stores_on_map_list.purchasesListData.get(position).getName());
                                builder.setView(input);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        stores_on_map_list.purchasesListData.get(position).setName(input.getText().toString());
                                        stores_on_map_list.updateAccointingListData(stores_on_map_list.purchasesListData.get(position));
                                        row_index = -1;
                                        notifyDataSetChanged();

                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                                break;
                            case 101:
                                stores_on_map_list.deleteAccointingListData(stores_on_map_list.purchasesListData.get(position).getId());
                                row_index = -1;
                                invsNumber.setText(String.valueOf(getItemCount()));
                                notifyItemRangeChanged(0, getItemCount());
                                break;
                        }

                        return false;
                    }
                });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {
                //Toast.makeText(getApplicationContext(), "onDismiss", Toast.LENGTH_SHORT).show();
       //     }
        });
        popupMenu.show();
    }*/

}
