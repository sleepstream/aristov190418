package com.sleepstream.checkkeeper.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperAdapter;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperViewHolder;
import com.sleepstream.checkkeeper.linkedListObjects.LinkedListData;

import java.util.Collections;

import static com.sleepstream.checkkeeper.MainActivity.accountingList;
import static com.sleepstream.checkkeeper.MainActivity.linkedListClass;


public class AccountingListAdapter extends RecyclerView.Adapter<AccountingListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    public interface OnStartDragListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private TextView invsNumber;
    private final OnStartDragListener dragStartListener;

    final String LOG_TAG = "AccountingListAdapter";
    private final Context context;
    public Integer row_index = -1;
    private View parrentView;
    private Navigation navigation;

    public AccountingListAdapter(Context context, OnStartDragListener dragStartListener, TextView invsNumber, AccountingList accountingList, View view, Navigation navigation) {
        this.invsNumber = invsNumber;
        this.context = context;
        this.dragStartListener=dragStartListener;
        this.parrentView = view;
        this.navigation = navigation;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {


                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                AccountingListData tmp = accountingList.accountingListData.get(i);
                tmp.setOrder(i+1);
                accountingList.accountingListData.set(i, tmp);


                tmp = accountingList.accountingListData.get(i+1);
                tmp.setOrder(i);
                accountingList.accountingListData.set(i+1, tmp);

                Collections.swap( accountingList.accountingListData, i, i + 1);
                accountingList.updateAccointingListData(accountingList.accountingListData.get(i));
                accountingList.updateAccointingListData(accountingList.accountingListData.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {

                //Toast.makeText(context, fromPosition +" " + toPosition, Toast.LENGTH_LONG).show();

                AccountingListData tmp = accountingList.accountingListData.get(i);
                tmp.setOrder(i-1);
                accountingList.accountingListData.set(i, tmp);


                tmp = accountingList.accountingListData.get(i-1);
                tmp.setOrder(i);
                accountingList.accountingListData.set(i-1, tmp);

                Collections.swap( accountingList.accountingListData, i, i - 1);
                accountingList.updateAccointingListData(accountingList.accountingListData.get(i-1));
                accountingList.updateAccointingListData(accountingList.accountingListData.get(i));
            }
        }
        notifyItemMoved(fromPosition, toPosition);

    }

    @Override
    public void onItemDismiss(final int position) {

        final AccountingListData item = accountingList.accountingListData.get(position);

        accountingList.accountingListData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, getItemCount());
        Log.d(LOG_TAG, "Dismiss position "+position);

        invsNumber.setText(String.valueOf(getItemCount()));

        final Snackbar snackbar =  Snackbar
                .make(invsNumber,context.getResources().getString(R.string.item_deleted), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(context, R.color.white))
                .setAction(context.getResources().getString(R.string.item_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //itemList.add(position, item);
                        accountingList.addAccountingList(position, item);
                        invsNumber.setText(String.valueOf(getItemCount()));
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
                    accountingList.deleteAccointingListData(item.getId());
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
                invsNumber.setText(String.valueOf(getItemCount()));
                snackbar.dismiss();
            }
        };
        Handler handlerUndo=new Handler();handlerUndo.postDelayed(runnableUndo,2500);

    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, final int position) {

        final AccountingListData item = accountingList.accountingListData.get(position);
        itemViewHolder.itemName.setText(item.getName());
        itemViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index = position;
                notifyDataSetChanged();

                navigation.clearFilter("");
                navigation.setFilter("fk_invoice_accountinglist", new String[]{item.getId().toString()});
                MainActivity.Page page =new MainActivity.Page(item.getName(), 6);
                page.positionInList = position;
                navigation.openCurrentPage(page);

                //row_index =-1;
                //notifyDataSetChanged();
                //InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();

                //currentNumber.setText("12");
                //MainActivity.pageNow = "invoicesLists";
                //MainActivity.setPageBack(MainActivity.pager.getCurrentItem(), 2);
                //MainActivity.pager.setCurrentItem(2, false);








            }
        });

        itemViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                row_index = position;
                notifyDataSetChanged();
                showPopupMenuAccountingList(view, position);
                return false;
            }
        });
        itemViewHolder.relativeReorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(itemViewHolder);
                }
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_BUTTON_PRESS) {
                    Toast.makeText(view.getContext(), "Load invoice page "+view.findViewById(R.id.invoiceName).getTransitionName(), Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        if(row_index == position)
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

    public String addItem(Integer position , AccountingListData accountingListData)
    {
        String res = accountingList.addAccountingList(position,accountingListData);
        notifyDataSetChanged();
        return res;
    }

    @Override
    public int getItemCount() {
        return accountingList.accountingListData.size();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.accounting_list_adapter, viewGroup, false);
        return new AccountingListAdapter.ItemViewHolder(itemView);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder,View.OnClickListener {

        protected RelativeLayout container;
        protected TextView itemName;
        protected ImageView ivReorder;
        protected RelativeLayout relativeReorder;


        public ItemViewHolder(final View v) {
            super(v);
            container = v.findViewById(R.id.container);
            itemName = v.findViewById(R.id.invoiceName);
            ivReorder =  v.findViewById(R.id.ivReorder);
            relativeReorder =  v.findViewById(R.id.relativeReorder);
        }

        @Override
        public void onClick(View view) {

        }


        @Override
        public void onItemSelected(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            itemName.setTextColor(ContextCompat.getColor(context, R.color.white));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void onItemClear(Context context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ivReorder.setColorFilter(ContextCompat.getColor(context, R.color.textlight), PorterDuff.Mode.SRC_IN);
            itemName.setTextColor(ContextCompat.getColor(context, R.color.textlight));
        }

    }

    private void showPopupMenuAccountingList(final View view, final int position) {
        final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popupmenu_item);


        //generate it from DB
        if(accountingList.accountingListData.get(position).getPinId()== null)
            popupMenu.getMenu().add(R.id.menugroup1, 102, Menu.NONE, R.string.title_pinAccointingList);
        else
            popupMenu.getMenu().add(R.id.menugroup1, 102, Menu.NONE, R.string.title_UnPinAccointingList);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(R.string.title_addAccointingList);
                                final EditText input = new EditText(context);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                input.setText(accountingList.accountingListData.get(position).getName());
                                builder.setView(input);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        accountingList.accountingListData.get(position).setName(input.getText().toString());
                                        accountingList.updateAccointingListData(accountingList.accountingListData.get(position));
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
                                accountingList.deleteAccointingListData(accountingList.accountingListData.get(position).getId());
                                row_index = -1;
                                invsNumber.setText(String.valueOf(getItemCount()));
                                notifyItemRangeChanged(0, getItemCount());
                                break;
                            case 102:
                                if(accountingList.accountingListData.get(position).getPinId()== null) {
                                    LinkedListData linkedListData = new LinkedListData();
                                    linkedListData.setFk_name(accountingList.getTableName());
                                    linkedListData.setFk_id(accountingList.accountingListData.get(position).getId());
                                    linkedListClass.addLinkedObject(linkedListData);
                                    linkedListClass.reLoadLinkedList();
                                    if(LinkedListPageFragment.linkedListAdapter != null)
                                        LinkedListPageFragment.linkedListAdapter.notifyDataSetChanged();

                                    MenuItem bedMenuItem = popupMenu.getMenu().findItem(item.getItemId());
                                    bedMenuItem.setTitle(R.string.title_UnPinAccointingList);

                                }
                                else
                                {
                                    linkedListClass.deleteLinkedObject(accountingList.accountingListData.get(position).getPinId());
                                    accountingList.accountingListData.get(position).setPinId(null);
                                    linkedListClass.reLoadLinkedList();
                                    MenuItem bedMenuItem = popupMenu.getMenu().findItem(item.getItemId());
                                    bedMenuItem.setTitle(R.string.title_pinAccointingList);

                                    if(LinkedListPageFragment.linkedListAdapter != null)
                                        LinkedListPageFragment.linkedListAdapter.notifyDataSetChanged();

                                }
                                break;
                        }

                        return false;
                    }
                });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {
                //Toast.makeText(getApplicationContext(), "onDismiss", Toast.LENGTH_SHORT).show();
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
