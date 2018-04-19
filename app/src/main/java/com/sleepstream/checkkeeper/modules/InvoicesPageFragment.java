package com.sleepstream.checkkeeper.modules;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class InvoicesPageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, InvoiceListAdapter.OnStartDragListener, AccountingListAdapter.OnStartDragListener {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "PageFragment";
    public static String page_title;

    int pageNumber;
    int backColor;
    private CalendarPickerView calendar;
    public static RecyclerView recyclerViewInvList;
    public RelativeLayout filter;
    public static RelativeLayout mainView;
    public ImageView ivFilter;
    public FastScroller fastScroller;
    private ItemTouchHelper mItemTouchHelperInvList;
    private Context context;
    private boolean statusDateFilter = false;
    //public String pageNow= "accountingLists";

    public static InvoiceListAdapter invoiceListAdapter;
    public static WrapContentLinearLayoutManager llm;
    private Navigation navigation;
    private SwipeRefreshLayout swipe_container;


    public InvoicesPageFragment(){}

    public static InvoicesPageFragment newInstance(Page page) {
        InvoicesPageFragment invoicesPageFragment = new InvoicesPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page.position);
        arguments.putString("currentName", page.pageName);
        page_title= page.pageName;
        invoicesPageFragment.setArguments(arguments);

        return invoicesPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_invoices_page, null);
        swipe_container = view.findViewById(R.id.swipe_container);
        swipe_container.setOnRefreshListener(this);
        final Context context = view.getContext();

        fab.show();
        mainView = view.findViewById(R.id.mainView);
        MainActivity.currentNumber.setText(String.valueOf(invoice.invoices.size()));

        Log.d(LOG_TAG, " Reload accountingListData size "+ accountingList.accountingListData.size());




        recyclerViewInvList = view.findViewById(R.id.invoicesList);
        fastScroller = view.findViewById(R.id.fastscroll);


        assert recyclerViewInvList != null;
        recyclerViewInvList.setHasFixedSize(true);

        llm = new WrapContentLinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewInvList.setLayoutManager(llm);

        invoiceListAdapter = new InvoiceListAdapter(context, this, currentNumber, invoice, accountingList, view, navigation);
        recyclerViewInvList.setAdapter(invoiceListAdapter);
        fastScroller.setRecyclerView(recyclerViewInvList);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(invoiceListAdapter, context);
        mItemTouchHelperInvList = new ItemTouchHelper(callback);
        mItemTouchHelperInvList.attachToRecyclerView(recyclerViewInvList);




        recyclerViewInvList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE  && llm.findLastVisibleItemPosition()- llm.findFirstVisibleItemPosition() == invoiceListAdapter.getItemCount()-1)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState );
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.d(LOG_TAG, "scroll5 " + llm.findLastVisibleItemPosition() + " " + llm.findFirstVisibleItemPosition() + " " + (invoiceListAdapter.getItemCount()-1)+ " dx - dy " + dx + " - " + dy);
                if((llm.findLastVisibleItemPosition() - llm.findFirstVisibleItemPosition()) < (invoiceListAdapter.getItemCount()-1)) {

                    if (llm.findLastVisibleItemPosition() == invoiceListAdapter.getItemCount() - 1)
                        fab.hide();
                    else
                        fab.show();
                }
                else
                {fab.show();}
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelperInvList.startDrag(viewHolder);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, invoiceListAdapter.row_index+"");
        fab.show();
        /*if(invoice.lastIDCollection > 0) {
            llm.smoothScrollToPosition(recyclerViewInvList, null, invoice.lastIDCollection);
        }*/
        invoice.reLoadInvoice();
        MainActivity.currentNumber.setText(String.valueOf(invoice.invoices.size()));
        invoiceListAdapter.notifyDataSetChanged();
    }

    public void InvoicesPageFragmentSet(Navigation navigation) {
        this.navigation = navigation;
    }

    @Override
    public void onRefresh() {
        invoice.reLoadInvoice();
        invoiceListAdapter.notifyDataSetChanged();
        swipe_container.setRefreshing(false);

    }
}
