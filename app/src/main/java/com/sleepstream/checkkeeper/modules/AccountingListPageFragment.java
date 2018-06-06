package com.sleepstream.checkkeeper.modules;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;

import java.util.Random;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class AccountingListPageFragment extends Fragment implements AccountingListAdapter.OnStartDragListener {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "AccountingListPageFragment";
    public static int page_title;

    int pageNumber;
    int backColor;

    public RecyclerView recyclerViewAccList;
    public RecyclerView links;
    public TextView currentName;
    public TextView currentNumber;
    public ImageView ivFilter;
    public RelativeLayout filter;

    private ItemTouchHelper mItemTouchHelperAccList;
    private Context context;
    //public String pageNow= "accountingLists";

    public static AccountingListAdapter accountingListAdapter;
    public static LinkedListAdapter linkedListAdapter;
    public PurchasesListAdapter purchasesListAdapter;
    private LinearLayoutManager llm;
    private boolean statusDateFilter = false;

    private Navigation navigation;


    public AccountingListPageFragment(){}

    public void AccountingListPageFragmentSet(Navigation navigation){
        this.navigation = navigation;
    }

    public static AccountingListPageFragment newInstance(int page) {
        AccountingListPageFragment accountingListPageFragment = new AccountingListPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        page_title=R.string.accountingListTitle;
        arguments.putInt("currentName", R.string.accountingListTitle);
        accountingListPageFragment.setArguments(arguments);


        return accountingListPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));


    }



    @SuppressLint("LongLogTag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.content_accounting_list_page, null);
        context = view.getContext();

        Log.d(LOG_TAG, " Reload accountingListData size "+ accountingList.accountingListData.size());


        recyclerViewAccList = (RecyclerView) view.findViewById(R.id.cardList);
        assert recyclerViewAccList != null;
        recyclerViewAccList.setHasFixedSize(true);
        final LinearLayoutManager llmAccList = new LinearLayoutManager(context);
        llmAccList.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewAccList.setLayoutManager(llmAccList);

        accountingListAdapter = new AccountingListAdapter(context,
                this, currentNumber, accountingList, view, navigation);

        recyclerViewAccList.setAdapter(accountingListAdapter);
        ItemTouchHelper.Callback callbackAccList = new SimpleItemTouchHelperCallback(accountingListAdapter, context);
        mItemTouchHelperAccList = null;
        mItemTouchHelperAccList = new ItemTouchHelper(callbackAccList);
        mItemTouchHelperAccList.attachToRecyclerView(recyclerViewAccList);


        recyclerViewAccList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE  && llmAccList.findLastVisibleItemPosition()- llmAccList.findFirstVisibleItemPosition() == accountingListAdapter.getItemCount()-1)
                    unHideFABMenu();
                super.onScrollStateChanged(recyclerView, newState );
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if((llmAccList.findLastVisibleItemPosition() - llmAccList.findFirstVisibleItemPosition()) < (accountingListAdapter.getItemCount()-1)) {

                    if (llmAccList.findLastVisibleItemPosition() == accountingListAdapter.getItemCount() - 1)
                        hideFABMenu();
                    else
                        unHideFABMenu();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        return view;


    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelperAccList.startDrag(viewHolder);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(currentName != null)
            currentName.setText(context.getString(R.string.accountingListTitle));
        accountingList.reloadAccountingList();
        MainActivity.currentNumber.setText(String.valueOf(accountingList.accountingListData.size()));
        accountingListAdapter.notifyDataSetChanged();

    }
}
