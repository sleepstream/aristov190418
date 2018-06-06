package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperAdapter;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.linkedListObjects.LinkedListData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.sleepstream.checkkeeper.MainActivity.getThemeColor;
import static com.sleepstream.checkkeeper.MainActivity.linkedListClass;


public class InvoiceListAdapter extends RecyclerView.Adapter<InvoiceListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter, SectionTitleProvider {
    private final Navigation navigation;

    public interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final Context context;
    private static List<InvoiceData> itemList;
    private TextView invsNumber;
    private static Invoice invoice;
    private static AccountingList accountingList;
    private final int invoicePageRequest =3000;
    private String currentGroupDate= "";

    private TextView groupDate;
    private float totalSum=0;
    private boolean movement = false;

    public Integer row_index = -1;
    final String LOG_TAG = "InvoiceListAdapter";
    public String subTitle="";



   private final OnStartDragListener dragStartListener;

    public InvoiceListAdapter(Context context, OnStartDragListener dragStartListener, TextView invsNumber, Invoice invoice, AccountingList accountingList, View view, Navigation navigation) {
        this.context = context;
        this.dragStartListener=dragStartListener;
        this.invsNumber = invsNumber;
        this.invoice = invoice;
        this.accountingList = accountingList;
        this.itemList = invoice.invoices;
        this.navigation = navigation;

    }
    @Override
    public String getSectionTitle(int position) {
        return itemList.get(position).getDateInvoice(null).substring(3);
    }

    public void swap(List<InvoiceData> itemList)
    {
        if(this.itemList != null && itemList != null)
        {
            this.itemList.clear();
            this.itemList = null;
            this.itemList = itemList;
        }
        else if(this.itemList != null && itemList == null)
        {
            //this.itemList.clear();
        }
        else
        {
            this.itemList = itemList;
        }
        notifyDataSetChanged();
    }


    @Override
    public void onItemDismiss(final int position) {

        final InvoiceData item =itemList.get(position);

        notifyItemRemoved(position);
        itemList.remove(position);
        notifyDataSetChanged();
        item.setIn_basket(1);
        invoice.updateInvoice(item);

        //notifyItemRangeChanged(0, getItemCount());


        final Snackbar snackbar =  Snackbar

                .make(invsNumber,context.getResources().getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                .setAction(context.getResources().getString(R.string.item_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       //itemList.add(position, item);
                        item.setIn_basket(0);
                        invoice.updateInvoice(item);
                       invoice.addInvoice(position, item);
                       invsNumber.setText(String.valueOf(getItemCount()));
                       notifyItemInserted(position);
                    }

                });


        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.color.colorAccent);
        TextView tvSnack = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        TextView tvSnackAction = (TextView) snackbar.getView().findViewById( android.support.design.R.id.snackbar_action );
        tvSnack.setTextColor(Color.WHITE);
        tvSnack.setTypeface(Typefaces.getRobotoMedium(context));
        tvSnackAction.setTypeface(Typefaces.getRobotoMedium(context));
        snackbar.show();


       Runnable runnableUndo = new Runnable() {

            @Override
            public void run() {
                invsNumber.setText(String.valueOf(itemList.size()));
                snackbar.dismiss();
            }
        };
        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        movement = true;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {


                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                InvoiceData tmp = itemList.get(i);
                tmp.setOrder(i+1);
                itemList.set(i, tmp);


                tmp = itemList.get(i+1);
                tmp.setOrder(i);
                itemList.set(i+1, tmp);

                Collections.swap(itemList, i, i + 1);
                invoice.updateInvoice(itemList.get(i));
                invoice.updateInvoice(itemList.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {

                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                InvoiceData tmp = itemList.get(i);
                tmp.setOrder(i-1);
                itemList.set(i, tmp);

                tmp = itemList.get(i-1);
                tmp.setOrder(i);
                itemList.set(i-1, tmp);


                Collections.swap(itemList, i, i - 1);
                invoice.updateInvoice(itemList.get(i-1));
                invoice.updateInvoice(itemList.get(i));
            }
        }
        Log.d(LOG_TAG, "item moove from " + fromPosition + " to " + toPosition);
        notifyItemMoved(fromPosition, toPosition);

    }

    public void addItem(Integer position, InvoiceData item) {

        if(itemList.get(position)!= null) {
            itemList.remove(position);
            itemList.add(position, item);
            notifyItemChanged(position);
        }
        else {
            itemList.add(position, item);
            notifyItemInserted(position);
            invsNumber.setText(String.valueOf(itemList.size()));
        }

    }

    public void removeItem(int position)
    {
        itemList.remove(position);
        invsNumber.setText(String.valueOf(itemList.size()));
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onBindViewHolder(final InvoiceListAdapter.ItemViewHolder itemViewHolder, final int position) {

        try {
            itemViewHolder.imageIcon.setImageBitmap(null);
            if (itemList.get(position).store!= null && itemList.get(position).store.iconName != null && itemList.get(position).store.iconName != "") {
                String filepath = Environment.getExternalStorageDirectory()+"/PriceKeeper/icons/";
                File imageFile = new File(filepath,itemList.get(position).store.iconName);
                if ( imageFile.exists()) {
                    Log.d(LOG_TAG, "onBindViewHolder set icon  " + itemList.get(position).store.iconName);
                    itemViewHolder.imageIcon.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
                }
            }
        }
        catch(NullPointerException ex)
        {
            Log.d(LOG_TAG, "onBindViewHolder set icon - Store is empty\n");
            ex.printStackTrace();

        }
        try {
            final InvoiceData item = itemList.get(position);

            itemViewHolder.invoiceSum.setText(String.valueOf(item.getFullPrice()));

            if (item.quantity != null)
                itemViewHolder.quantity.setText(String.valueOf(item.quantity));
            else
                itemViewHolder.quantity.setText("");

            if (item.store != null && item.store.name != null) {
                itemViewHolder.storeName.setText(item.store.name);
            } else if (item.store != null && item.store.name == null && item.store.name_from_fns != null) {
                itemViewHolder.storeName.setText(item.store.name_from_fns);
            } else {
                itemViewHolder.storeName.setText(MainActivity.setInvoiceNameByStatus(item.get_status()));
            }

            itemViewHolder.invoiceDate.setText(item.getDateInvoice(null));

            Long tmp = Long.valueOf(item.getDateInvoice(1));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(tmp);

            String tmpDate = dateFormat.format(calendar.getTime());

            String prvDate = "";
            String forvDate = "";
            if (position - 1 >= 0) {
                calendar.setTimeInMillis(Long.valueOf(itemList.get(position - 1).getDateInvoice(1)));
                prvDate = dateFormat.format(calendar.getTime());
            }
            if (position + 1 <= itemList.size() - 1) {
                calendar.setTimeInMillis(Long.valueOf(itemList.get(position + 1).getDateInvoice(1)));
                forvDate = dateFormat.format(calendar.getTime());
            }


            if (position - 1 >= 0 && !prvDate.equals(tmpDate)) {
            /*if(position+1<=itemList.size()-1 && !forvDate.equals(tmpDate
            ))
            {
                itemViewHolder.groupDateText.setText(tmpDate);
                itemViewHolder.groupDate.setVisibility(View.VISIBLE);
            }
            else
            {
                itemViewHolder.groupDate.setVisibility(View.GONE);
            }*/
                itemViewHolder.groupDateText.setText(tmpDate);
                itemViewHolder.groupDate.setVisibility(View.VISIBLE);
            } else if (position == 0) {
                itemViewHolder.groupDateText.setText(tmpDate);
                itemViewHolder.groupDate.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.groupDate.setVisibility(View.GONE);
            }


            if(item.getPinId()== null) {
                itemViewHolder.fixImage.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
            else
            {
                itemViewHolder.fixImage.setImageResource(R.drawable.ic_star_black_24dp);
            }

            Log.d(LOG_TAG, "position " + position + "  size " + itemList.size());
            //itemViewHolder.storeName.setText(String.valueOf(item.getFullPrice()));
            //open current page with products list in invoice
            itemViewHolder.container.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    if (MainActivity.invoiceClickAble(item.get_status())) {
                        row_index = position;

                        Integer color = null;
                        Drawable background = itemViewHolder.marker.getBackground();
                        if (background instanceof ColorDrawable) {
                            itemViewHolder.oldColorMarker = ((ColorDrawable) background).getColor();
                        }
                        itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNewAccentLink));
                        //itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                        //itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);


                        notifyDataSetChanged();

                        MainActivity.purchasesList.clearFilter();
                        MainActivity.purchasesList.setfilter("fk_purchases_invoice", item.getId().toString());
                        MainActivity.purchasesList.reloadPurchasesList(null);
                        MainActivity.currentInvoice = item;
                        MainActivity.Page page =new MainActivity.Page("", 4);
                        page.positionInList = position;
                        navigation.openCurrentPage(page);
                    }

                }
            });

            //invsNumber.setText(String.valueOf(getItemCount()));
            itemViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Integer color = null;
                    Drawable background = itemViewHolder.marker.getBackground();
                    if (background instanceof ColorDrawable) {
                        itemViewHolder.oldColorMarker = ((ColorDrawable) background).getColor();
                    }
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNewAccentLink));
                    //itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                    //itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);

                    row_index = position;
                    notifyDataSetChanged();
                    showPopupMenuAccountingList(view, position);
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
                        itemViewHolder.container.setBackgroundResource(R.color.drag);
                        Log.d(LOG_TAG, "reorder finger down action " + motionEvent.getAction());
                        dragStartListener.onStartDrag(itemViewHolder);
                    }
                    if (!movement)
                        Log.d(LOG_TAG, "reorder finger out action " + motionEvent.getAction());
                    return false;
                }
            });
            itemViewHolder.fixLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(item.getPinId()== null) {
                        LinkedListData linkedListData = new LinkedListData();
                        linkedListData.setFk_name(invoice.getTableNameInvoice());
                        linkedListData.setFk_id(invoice.invoices.get(position).getId());
                        linkedListClass.addLinkedObject(linkedListData);
                        itemViewHolder.fixImage.setImageResource(R.drawable.ic_star_black_24dp);
                    }
                    else
                    {
                        linkedListClass.deleteLinkedObject(item.getId(), invoice.getTableNameInvoice());
                        itemViewHolder.fixImage.setImageResource(R.drawable.ic_star_border_black_24dp);
                    }
                }
            });

  /*      itemViewHolder.container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(itemViewHolder.oldColorContainer == null) {
                    if (motionEvent.getAction() ==
                            MotionEvent.ACTION_DOWN) {
                        Integer color = null;
                        Drawable background = itemViewHolder.container.getBackground();
                        if (background instanceof ColorDrawable) {
                            itemViewHolder.oldColorContainer = ((ColorDrawable) background).getColor();
                        }
                        itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                        itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                        itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);


                    }
                }
                return false;
            }
        });
*/
            if (row_index == position) {
                itemViewHolder.marker.setBackgroundColor(getThemeColor(context,R.attr.colorNewAccentLink));
                //itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                //itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            } else {
                colorIvoiceList(context, item, itemViewHolder);
                itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
            }
            //notifyDataSetChanged();

        }
        catch (Exception ex)
        {
            Log.d(LOG_TAG, "error on onBindViewHolder " + ex.getMessage()+ "\n"+ex.getCause());
        }
    }


    public static void colorIvoiceList(Context context, InvoiceData item, ItemViewHolderInvoices itemViewHolder)
    {
        if(item.kktRegId != null) {
        Integer status = item.kktRegId._status;
        if (status != null) {
            switch (status) {
                case -2:
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNoData));
                    break;
                case -1:
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNoData));
                    break;
                case 0:
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorLoaded));
                    break;
                case 1:
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorLoadedConfirmed));
                    break;
                default:
                    itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.color.white));
                    break;
            }
        } else
            itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNoData));
        }
        else
        {
            itemViewHolder.marker.setBackgroundColor(getThemeColor(context, R.attr.colorNoData));
        }
    }


    @Override
    public InvoiceListAdapter.ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_adapter, viewGroup, false);
        return new InvoiceListAdapter.ItemViewHolder(itemView);
    }




    public class ItemViewHolder extends ItemViewHolderInvoices {

        public ItemViewHolder(View v) {
            super(v);
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


    private void showPopupMenuAccountingList(final View view, final int position) {
        final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popupmenu_item);


        if(invoice.invoices.get(position).getPinId() == null)
            popupMenu.getMenu().add(R.id.menugroup1, 102, Menu.NONE, R.string.title_pinAccointingList);
        else
            popupMenu.getMenu().add(R.id.menugroup1, 102, Menu.NONE, R.string.title_UnPinAccointingList);

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
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setGravity(Gravity.CENTER);
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AccountingListData tmp = new AccountingListData();
                                    tmp.setName(input.getText().toString());

                                    String res = MainActivity.accountingList.addAccountingList(null,tmp);
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
                        else if(item.getItemId() == 102)
                        {
                            LinkedListData linkedListData = new LinkedListData();
                            linkedListData.setFk_name(invoice.getTableNameInvoice());
                            linkedListData.setFk_id(invoice.invoices.get(position).getId());
                            linkedListClass.addLinkedObject(linkedListData);
                        }
                        else
                        {
                            invoice.invoices.get(position).setFk_invoice_accountinglist(item.getItemId());
                            invoice.invoices.set(position, invoice.invoices.get(position));
                            invoice.updateInvoice(invoice.invoices.get(position));

                            if(invoice.checkFilter("fk_invoice_accountinglist", null)) {
                                if (!invoice.checkFilter("fk_invoice_accountinglist", item.getItemId())) {
                                    removeItem(position);
                                }
                            }
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


        //Handler handlerUndo=new Handler();
        //handlerUndo.postDelayed(runnableUndo,2500);

    }

    public int findPosition(InvoiceData invoiceData) {
        if(invoiceData.getId() != null) {
            for (int i = 0; i < itemList.size(); i++) {
                InvoiceData tmp = itemList.get(i);
                if (Objects.equals(tmp.getId(), invoiceData.getId()))
                    return i;
            }
        }
        return  -1;
    }




}
