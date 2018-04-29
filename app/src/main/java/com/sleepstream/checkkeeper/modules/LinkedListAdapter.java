package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperAdapter;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperViewHolder;
import com.sleepstream.checkkeeper.linkedListObjects.LinkedListData;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.*;


public class LinkedListAdapter extends RecyclerView.Adapter<LinkedListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {


    final String LOG_TAG = "LinkedListAdapter";
    private final Context context;
    public Integer row_index = -1;
    private View parentView;
    public List<LinkedListData> linked_items = new ArrayList<>();
    private Navigation navigation;

    public LinkedListAdapter(Context context, View view, List<LinkedListData> llData, Navigation navigation) {
        this.context = context;
        this.parentView = view;
        this.linked_items = llData;
        this.navigation = navigation;
    }
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(final int position) {
        final LinkedListData item = linkedListClass.linkedListData.get(position);

        linkedListClass.linkedListData.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
        Log.d(LOG_TAG, "Dismiss position "+position);



        final Snackbar snackbar =  Snackbar
                .make(MainActivity.currentNumber,context.getResources().getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                .setAction(context.getResources().getString(R.string.item_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //itemList.add(position, item);
                        linked_items.add(position, item);
                        MainActivity.currentNumber.setText(String.valueOf(getItemCount()));
                        notifyItemInserted(position);
                    }
                });
        snackbar.addCallback(new Snackbar.Callback()
        {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if(event == DISMISS_EVENT_MANUAL || event == DISMISS_EVENT_CONSECUTIVE) {
                    linkedListClass.deleteLinkedObject(item.getId());
                    Toast.makeText(context, "Dismissed", Toast.LENGTH_LONG).show();
                }
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
                MainActivity.currentNumber.setText(String.valueOf(getItemCount()));
                snackbar.dismiss();
            }
        };
        Handler handlerUndo=new Handler();handlerUndo.postDelayed(runnableUndo,2500);

    }


    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        if(!linked_items.isEmpty()) {
            final LinkedListData item = linked_items.get(position);
            switch(item.getFk_name()) {
                case "invoice": {
                    itemViewHolder.invoiceSum.setText(String.valueOf(item.invoiceData.getFullPrice()));

                    if (item.invoiceData.quantity != null)
                        itemViewHolder.quantity.setText(String.valueOf(item.invoiceData.quantity));
                    else
                        itemViewHolder.quantity.setText("");
                    if (item.invoiceData.store != null && item.invoiceData.store.name != null)
                        itemViewHolder.itemName.setText(item.invoiceData.store.name);
                    else {
                        itemViewHolder.itemName.setText(MainActivity.setInvoiceNameByStatus(item.invoiceData.get_status()));
                    }
                    itemViewHolder.invoiceDate.setText(item.invoiceData.getDateInvoice(null));
                    itemViewHolder.groupDate.setVisibility(View.GONE);

                    itemViewHolder.container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (item.invoiceData.get_status() != null && item.invoiceData.get_status() != -1 && item.invoiceData.get_status() != -2) {
                                row_index = position;

                                Integer color = null;
                                Drawable background = itemViewHolder.container.getBackground();
                                if (background instanceof ColorDrawable) {
                                    itemViewHolder.oldColorContainer = ((ColorDrawable) background).getColor();
                                }

                                itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                                itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
                                itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);


                                notifyDataSetChanged();

                                MainActivity.purchasesList.clearFilter();
                                MainActivity.purchasesList.setfilter("fk_purchases_invoice", item.invoiceData.getId().toString());
                                MainActivity.currentInvoice = item.invoiceData;

                                MainActivity.Page page =new MainActivity.Page("", 4);
                                page.positionInList = position;
                                navigation.openCurrentPage(page);
                                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS", Locale.getDefault());
                                //android.icu.util.Calendar calendar = Calendar.getInstance();
                                //calendar.setTimeInMillis(Long.parseLong(item.invoiceData.getDateInvoice(1)));
                                //String date = dateFormat.format(calendar.getTime());
                            }

                        }
                    });

                    itemViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Integer color = null;
                            Drawable background = itemViewHolder.container.getBackground();
                            if (background instanceof ColorDrawable) {
                                itemViewHolder.oldColorContainer = ((ColorDrawable) background).getColor();
                            }
                            itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                            itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
                            itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);

                            row_index = position;
                            notifyDataSetChanged();
                            showPopupMenuAccountingList(view, position);
                            return false;
                        }
                    });
                    break;
                }
                case "accountingList":
                    itemViewHolder.itemName.setText(item.accountingListData.getName());
                    itemViewHolder.groupDate.setVisibility(View.GONE);

                    itemViewHolder.container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            row_index = position;
                            notifyDataSetChanged();

                            navigation.clearFilter("");
                            navigation.setFilter("fk_invoice_accountinglist", new String[]{item.accountingListData.getId().toString()});


                            MainActivity.Page page =new Page(item.accountingListData.getName(), 6);
                            page.positionInList = position;
                            navigation.openCurrentPage(page);
                        }
                    });
                    if (row_index == position) {
                        itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                        itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
                    } else {
                        itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        itemViewHolder.itemName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
                    }
                    break;
            }
        }


    }

    public void addItem(Integer position , String link)
    {

    }

    @Override
    public int getItemCount() {
        return linked_items.size();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.linked_list_adapter, viewGroup, false);
        return new LinkedListAdapter.ItemViewHolder(itemView);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder,View.OnClickListener {

        protected RelativeLayout container;
        protected ImageView ivReorder;
        protected RelativeLayout relativeReorder;
        protected LinearLayout groupDate;
        protected TextView invoiceSum;
        protected TextView itemName;
        protected TextView invoiceDate;
        protected TextView quantity;
        protected TextView groupDateText;



        public Integer oldColorContainer;




        public ItemViewHolder(final View v) {
            super(v);
            container = v.findViewById(R.id.container);
            ivReorder = v.findViewById(R.id.ivReorder);
            invoiceDate = v.findViewById(R.id.invoiceDate);
            itemName = v.findViewById(R.id.itemName);
            quantity = v.findViewById(R.id.quantity);
            invoiceSum = v.findViewById(R.id.invoiceSum);
            groupDateText = v.findViewById(R.id.groupDateText);
            groupDate=v.findViewById(R.id.groupDate);
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
                ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                itemName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
                oldColorContainer = null;
            }
            try {
                notifyDataSetChanged();
            }
            catch (Exception ex)
            {
                Log.d(LOG_TAG, ex.getMessage());
            }


            //ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
            //storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
        }

    }

    public interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private void showPopupMenuAccountingList(final View view, final int position) {
        final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popupmenu_item);


        //generate it from DB
        for(int i=0; i<accountingList.accountingListData.size(); i++)
        {
            Log.d(LOG_TAG, "Add popUpMenu id ="+accountingList.accountingListData.get(i).getId());
            popupMenu.getMenu().add(R.id.menugroup1, accountingList.accountingListData.get(i).getId(), Menu.NONE, accountingList.accountingListData.get(i).getName());
        }

        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        if(item.getItemId() == R.id.addButton)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle(R.string.title_addAccointingList);
                            final EditText input = new EditText(context);
                            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AccountingListData tmp = new AccountingListData();
                                    tmp.setName(input.getText().toString());

                                    String res = AccountingListPageFragment.accountingListAdapter.addItem(null,tmp);
                                    if(res.equals("")) {

                                        invoice.invoices.get(position).setFk_invoice_accountinglist(accountingList.accountingListData.get(accountingList.lastIDCollection).getId());
                                        invoice.invoices.set(position, invoice.invoices.get(position));
                                        invoice.updateInvoice(invoice.invoices.get(position));

                                        row_index = -1;
                                    }
                                    else if(res. equals("exist"))
                                    {
                                        Toast.makeText(context, "exist", Toast.LENGTH_LONG).show();
                                    }


                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();

                        }
                        else
                        {
                            linked_items.get(position).invoiceData.setFk_invoice_accountinglist(item.getItemId());
                            //invoice.invoices.get(position).setFk_invoice_accountinglist(item.getItemId());
                            invoice.updateInvoice(linked_items.get(position).invoiceData);


                            row_index = -1;
                            notifyDataSetChanged();
                            Toast.makeText(view.getContext(),item.getItemId()+"",Toast.LENGTH_SHORT).show();
                        }
                        return false;

                    }
                });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {
                row_index = -1;
                notifyDataSetChanged();
                //Toast.makeText(context, "onDismiss", Toast.LENGTH_SHORT).show();
            }
        });
        popupMenu.show();

        Runnable runnableUndo = new Runnable() {

            @Override
            public void run() {
                row_index = -1;
                popupMenu.dismiss();
                notifyDataSetChanged();
            }
        };


        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);

    }

}
