package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import java.util.Calendar;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.linkedListObjects.LinkedListData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.sleepstream.checkkeeper.MainActivity.linkedListClass;
import static com.sleepstream.checkkeeper.modules.InvoicesBasketPageFragment.selectedCount;


public class InvoiceBasketListAdapter extends RecyclerView.Adapter<InvoiceBasketListAdapter.ItemViewHolder> {


    private final Context context;
    public static List<InvoiceData> itemList = new ArrayList<>();
    private static Invoice invoice;
    private static AccountingList accountingList;
    private final int invoicePageRequest =3000;
    private Navigation navigation;
    private String currentGroupDate= "";

    private boolean movement = false;

    public Integer row_index = -1;
    final String LOG_TAG = "InvoiceListAdapter";


    public InvoiceBasketListAdapter(Context context, Invoice invoice, View view, Navigation navigation) {
        this.context = context;
        this.invoice = invoice;
        itemList.clear();
        if(invoice.invoices != null) {
            this.itemList.addAll(invoice.invoices);
        }
        this.navigation = navigation;

    }
    public void updateData(Invoice invoice)
    {
        itemList.clear();
        if(invoice.invoices != null) {
            this.itemList.addAll(invoice.invoices);
        }
    }


    public void swap(List<InvoiceData> itemList)
    {
        if(this.itemList != null && itemList != null)
        {
            this.itemList.clear();
            this.itemList = null;
            this.itemList.addAll(itemList);
        }
        else if(this.itemList != null && itemList == null)
        {
            //this.itemList.clear();
        }
        else
        {
            this.itemList.addAll(itemList);
        }
        notifyDataSetChanged();
    }




    public void removeItem(int position)
    {
        itemList.remove(position);
        MainActivity.currentNumber.setText(String.valueOf(itemList.size()));
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        try {
            final InvoiceData item = itemList.get(position);
            //itemList.get(position).selected = false;

            if(!item.selected) {
                itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
            }
            else {
                itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            }


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
            Calendar calendar = Calendar.getInstance();
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
                        itemViewHolder.marker.setBackgroundResource(R.color.colorAccent);

                        MainActivity.purchasesList.clearFilter();
                        MainActivity.purchasesList.setfilter("fk_purchases_invoice", item.getId().toString());
                        MainActivity.currentInvoice = item;
                        navigation.openCurrentPage(new MainActivity.Page("", 4));
                    }

                }
            });

            //invsNumber.setText(String.valueOf(getItemCount()));
            itemViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Integer color = null;
                    Drawable background = itemViewHolder.container.getBackground();
                    if (background instanceof ColorDrawable) {
                        itemViewHolder.oldColorContainer = ((ColorDrawable) background).getColor();
                    }

                    //row_index = position;
                    //notifyDataSetChanged();

                    if(itemList.get(position).selected) {
                        itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                        itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
                        selectedCount-=1;
                        if(selectedCount < 0 || selectedCount == 0)
                            InvoicesBasketPageFragment.selectAll = false;
                        itemList.get(position).selected = false;
                        //notifyItemChanged(position);
                    }
                    else {
                        itemViewHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                        itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                        itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
                        selectedCount+=1;
                        if(selectedCount  == itemList.size())
                            InvoicesBasketPageFragment.selectAll = true;
                        itemList.get(position).selected = true;
                        //notifyItemChanged(position);
                    }
                    InvoicesBasketPageFragment.fabShowHide();
                    return true;
                }
            });
            if (row_index == position) {
                itemViewHolder.marker.setBackgroundResource(R.color.colorAccent);
                //itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.white));
                //itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            } else {
                InvoiceListAdapter.colorIvoiceList(item, itemViewHolder);
                //itemViewHolder.ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                //itemViewHolder.storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
            }

        }
        catch (Exception ex)
        {
            Log.d(LOG_TAG, "error on onBindViewHolder " + ex.getMessage());
            ex.printStackTrace();
        }


    }



    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_adapter, viewGroup, false);
        return new ItemViewHolder(itemView);
    }




    public class ItemViewHolder extends ItemViewHolderInvoices{

        public ItemViewHolder(final View v) {
            super(v);
        }

        @Override
        public void onItemClear(Context context) {
            if(oldColorContainer!= null) {
                container.setBackgroundColor(oldColorContainer);
                ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
                storeName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
                oldColorContainer = null;
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
        popupMenu.getMenu().add(R.id.menugroup1, 102, Menu.NONE, R.string.title_UnPinAccointingList);

        //generate it from DB


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
                        else if(item.getItemId() == 102)
                        {
                            LinkedListData linkedListData = new LinkedListData();
                            linkedListData.setFk_name(invoice.getTableName());
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


        Handler handlerUndo=new Handler();
        handlerUndo.postDelayed(runnableUndo,2500);

    }

    public int findPosition(InvoiceData invoiceData) {
        if(invoiceData.getId() != null) {
            for (int i = 0; i < itemList.size(); i++) {
                InvoiceData tmp = itemList.get(i);
                if (tmp.getId() == invoiceData.getId())
                    return i;
            }
        }
        return  -1;
    }


}
