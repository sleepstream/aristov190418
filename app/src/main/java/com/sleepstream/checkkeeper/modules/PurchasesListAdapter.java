package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperAdapter;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperViewHolder;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.fromHtml;
import static com.sleepstream.checkkeeper.MainActivity.getDrawable;


public class PurchasesListAdapter extends RecyclerView.Adapter<PurchasesListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    public interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private TextView invsNumber;
    private final OnStartDragListener dragStartListener;
    private static PurchasesList purchasesList;
    final String LOG_TAG = "PurchasesListAdapter";
    private final Context context;
    public Integer row_index = -1;
    private boolean moovement = false;
    private View parrentView;

    public List<Integer> selectedItems = new ArrayList<>();

    public PurchasesListAdapter(Context context, OnStartDragListener dragStartListener, TextView invsNumber, PurchasesList purchasesList, View view) {
        this.invsNumber = invsNumber;
        this.purchasesList = purchasesList;
        this.context = context;
        this.dragStartListener=dragStartListener;
        this.parrentView = view;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        moovement = true;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {


                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                PurchasesListData tmp = purchasesList.purchasesListData.get(i);
                tmp.setOrder(i+1);
                purchasesList.purchasesListData.set(i, tmp);


                tmp = purchasesList.purchasesListData.get(i+1);
                tmp.setOrder(i);
                purchasesList.purchasesListData.set(i+1, tmp);

                Collections.swap( purchasesList.purchasesListData, i, i + 1);
                purchasesList.updatePurchasesListData(purchasesList.purchasesListData.get(i));
                purchasesList.updatePurchasesListData(purchasesList.purchasesListData.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {

                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                PurchasesListData tmp = purchasesList.purchasesListData.get(i);
                tmp.setOrder(i-1);
                purchasesList.purchasesListData.set(i, tmp);


                tmp = purchasesList.purchasesListData.get(i-1);
                tmp.setOrder(i);
                purchasesList.purchasesListData.set(i-1, tmp);

                Collections.swap( purchasesList.purchasesListData, i, i - 1);
                purchasesList.updatePurchasesListData(purchasesList.purchasesListData.get(i-1));
                purchasesList.updatePurchasesListData(purchasesList.purchasesListData.get(i));
            }
        }
        notifyItemMoved(fromPosition, toPosition);

    }

    @Override
    public void onItemDismiss(final int position) {

        /*final PurchasesListData item = purchasesList.purchasesListData.get(position);
        if(purchasesList.deletePurchasesListData(item.id))
        {
            Log.d(LOG_TAG, "Dismiss position "+position);
            notifyItemRemoved(position);
            invsNumber.setText(String.valueOf(getItemCount()));
            //notifyItemRangeChanged(0, getItemCount());

            final Snackbar snackbar =  Snackbar

                    .make(invsNumber,context.getResources().getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                    .setAction(context.getResources().getString(R.string.item_undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            purchasesList.addAccountingList(position, item);
                            invsNumber.setText(String.valueOf(getItemCount()));
                            notifyItemInserted(position);



                        }
                    });
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            TextView tvSnack = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            TextView tvSnackAction = (TextView) snackbar.getView().findViewById( android.support.design.R.id.snackbar_action );
            tvSnack.setTextColor(Color.WHITE);
            tvSnack.setTypeface(Typefaces.getRobotoMedium(context));
            tvSnackAction.setTypeface(Typefaces.getRobotoMedium(context));
            snackbar.show();


            Runnable runnableUndo = new Runnable() {

                @Override
                public void run() {
                    invsNumber.setText(String.valueOf(getItemCount()));
                    snackbar.dismiss();
                }
            };
            Handler handlerUndo=new Handler();handlerUndo.postDelayed(runnableUndo,2500);

        }

        */
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        final PurchasesListData item = purchasesList.purchasesListData.get(position);
        //invsNumber.setText(String.valueOf(getItemCount()));
        itemViewHolder.itemName.setText(item.product.nameFromBill+"");
        itemViewHolder.priceForItem.setText(item.prise_for_item.toString());
        itemViewHolder.quantity.setText(item.quantity.toString());
        itemViewHolder.sumPerPosition.setText(item.sum.toString());
        if(item.product.category != null && item.product.category.icon_name!= null)
            itemViewHolder.product_cutegory_icon.setImageResource(getDrawable(context,item.product.category.icon_name+"_24"));
        else
            itemViewHolder.product_cutegory_icon.setImageResource(android.R.color.transparent);
        /*itemViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index = position;
                notifyDataSetChanged();
                TextView currentName = (TextView)parrentView.findViewById(R.id.currentName);
                TextView currentNumber = (TextView)parrentView.findViewById(R.id.currentNumber);
                SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout)parrentView.findViewById(R.id.sliding_layout);

                currentName.setText(item.product.nameFromBill);
                MainActivity.invoice.setfilter("fk_invoice_accountinglist", item.getId().toString());
                MainActivity.invoice.reLoadLinkedList();

                //currentNumber.setText("12");
                //MainActivity.pageNow = "invoicesLists";
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                row_index =-1;
                notifyDataSetChanged();

            }
        });
*/
        itemViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(selectedItems.contains(position))
                    selectedItems.remove(selectedItems.indexOf(position));
                else
                    selectedItems.add(position);

                if(selectedItems.size()>0)
                    PurchasesPageFragment.button_select_category.setVisibility(View.VISIBLE);
                else
                    PurchasesPageFragment.button_select_category.setVisibility(View.GONE);
                notifyDataSetChanged();
                //showPopupMenuAccountingList(view, position);
                return false;
            }
        });
        itemViewHolder.relativeReorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() ==
                        MotionEvent.ACTION_DOWN) {

                    Integer color = null;
                    Drawable background = itemViewHolder.container.getBackground();
                    if (background instanceof ColorDrawable) {
                        itemViewHolder.oldColorContainer = ((ColorDrawable) background).getColor();
                    }
                    itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.drag));
                    Log.d(LOG_TAG, "reorder finger down action " + motionEvent.getAction());
                    dragStartListener.onStartDrag(itemViewHolder);
                }
                if(!moovement)
                    Log.d(LOG_TAG, "reorder finger out action " + motionEvent.getAction());
                return false;
            }
        });

        if(selectedItems.contains(position))
        {
            itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
            itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        }
        else
        {
            itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
            itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
        }





    }

    @Override
    public int getItemCount() {
        return purchasesList.purchasesListData.size();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grocery_adapter, viewGroup, false);
        return new PurchasesListAdapter.ItemViewHolder(itemView);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder,View.OnClickListener {

        protected RelativeLayout container;
        protected TextView itemName;
        protected ImageView ivReorder;
        protected RelativeLayout relativeReorder;

        protected  TextView quantity;
        protected  TextView priceForItem;
        protected  TextView sumPerPosition;
        public Integer oldColorContainer;
        public ImageView product_cutegory_icon;


        protected TextView sign;


        public ItemViewHolder(final View v) {
            super(v);
            container = (RelativeLayout) v.findViewById(R.id.container);
            itemName = (TextView) v.findViewById(R.id.invoiceName);
            ivReorder = (ImageView) v.findViewById(R.id.ivReorder);
            relativeReorder = (RelativeLayout) v.findViewById(R.id.relativeReorder);

            sign =(TextView)v.findViewById(R.id.sign);
            quantity = (TextView) v.findViewById(R.id.quantity);
            priceForItem = (TextView) v.findViewById(R.id.priceForItem);
            sumPerPosition = (TextView) v.findViewById(R.id.sumPerPosition);
            sign.setText(fromHtml("&#xd7"));
            product_cutegory_icon = v.findViewById(R.id.product_category_icon);

        }

        @Override
        public void onClick(View view) {

        }



        @Override
        public void onItemSelected(Context context) {
            //container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            //itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
            //ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void onItemClear(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
            itemName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
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
                                input.setText(purchasesList.purchasesListData.get(position).getName());
                                builder.setView(input);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        purchasesList.purchasesListData.get(position).setName(input.getText().toString());
                                        purchasesList.updateAccointingListData(purchasesList.purchasesListData.get(position));
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
                                purchasesList.deleteAccointingListData(purchasesList.purchasesListData.get(position).getId());
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
