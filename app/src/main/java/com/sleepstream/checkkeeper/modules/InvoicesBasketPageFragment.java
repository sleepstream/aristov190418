package com.sleepstream.checkkeeper.modules;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.squareup.timessquare.CalendarPickerView;

import java.util.Random;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class InvoicesBasketPageFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "PageFragment";
    public static int page_title;

    int pageNumber;
    int backColor;
    public static FloatingActionButton fabDelete;
    public static FloatingActionButton fabSelectAll;
    public static FloatingActionButton fabRestore;
    private CalendarPickerView calendar;
    public static RecyclerView recyclerViewInvList;
    public FastScroller fastScroller;
    private ItemTouchHelper mItemTouchHelperInvList;
    private Context context;
    private boolean statusDateFilter = false;
    protected static boolean selectAll = false;
    public static Integer selectedCount = 0;
    //public String pageNow= "accountingLists";

    public static InvoiceBasketListAdapter invoiceBasketListAdapter;
    private WrapContentLinearLayoutManager llm;
    private Navigation navigation;

    public InvoicesBasketPageFragment(){}

    public static InvoicesBasketPageFragment newInstance(int page) {
        InvoicesBasketPageFragment invoicesBasketPageFragment = new InvoicesBasketPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        page_title = R.string.invoicesBasketListTitle;
        arguments.putInt("currentName", R.string.invoicesBasketListTitle);
        invoicesBasketPageFragment.setArguments(arguments);
        return invoicesBasketPageFragment;
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

        View view = inflater.inflate(R.layout.content_invoices_basket_page, null);
        final Context context = view.getContext();
        hideFABMenu();
        fabDelete = view.findViewById(R.id.fabDelete);
        fabSelectAll = view.findViewById(R.id.fabSelectAll);
        fabRestore = view.findViewById(R.id.fabRestore);


        recyclerViewInvList = view.findViewById(R.id.invoicesList);
        fastScroller = view.findViewById(R.id.fastscroll);


        assert recyclerViewInvList != null;
        recyclerViewInvList.setHasFixedSize(true);

        llm = new WrapContentLinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewInvList.setLayoutManager(llm);

        invoiceBasketListAdapter = new InvoiceBasketListAdapter(context,  invoice, view, navigation);
        recyclerViewInvList.setAdapter(invoiceBasketListAdapter);
        fastScroller.setRecyclerView(recyclerViewInvList);

        /*ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(invoiceBasketListAdapter, context);
        mItemTouchHelperInvList = new ItemTouchHelper(callback);
        mItemTouchHelperInvList.attachToRecyclerView(recyclerViewInvList);
*/

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedCount > 0)
                {

                    for(InvoiceData tmp : invoiceBasketListAdapter.itemList)
                    {
                        if(tmp.selected)
                        {

                            invoice.deleteInvoiceData(tmp);
                            selectedCount-=1;
                        }
                    }
                    if(selectedCount < 0 || selectedCount == 0) {
                        selectedCount = 0;
                        selectAll = false;
                        fabShowHide();
                    }
                    invoice.reLoadInvoice();
                    invoiceBasketListAdapter.itemList.clear();
                    invoiceBasketListAdapter.notifyDataSetChanged();
                }
            }
        });

        fabSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(InvoiceData tmp : invoiceBasketListAdapter.itemList)
                {
                    if(!selectAll) {
                        tmp.selected = true;
                        selectedCount += 1;
                    }
                    else
                    {
                        tmp.selected = false;
                        selectedCount -= 1;
                    }
                }
                if(selectedCount < 0 || selectedCount == 0) {
                    selectedCount = 0;
                    selectAll = false;
                    fabShowHide();
                }
                else {
                    selectAll = true;
                    fabShowHide();
                }
                invoiceBasketListAdapter.notifyDataSetChanged();
            }
        });
        fabRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedCount > 0)
                {
                    for(InvoiceData tmp : invoiceBasketListAdapter.itemList)
                    {
                        if(tmp.selected)
                        {
                            tmp.setIn_basket(0);
                            invoice.updateInvoice(tmp);
                            //invoice.deleteInvoiceData(tmp.getId());
                        }
                    }
                    selectedCount = 0;
                    selectAll = false;

                    invoice.reLoadInvoice();
                    invoiceBasketListAdapter.updateData(invoice);
                    invoiceBasketListAdapter.notifyDataSetChanged();
                    MainActivity.currentNumber.setText(String.valueOf(invoice.invoices.size()));
                    fabShowHide();
                }
            }
        });
        if(navigation.page!= null && navigation.page.positionInList != null) {
            llm.scrollToPosition(navigation.page.positionInList);
            invoiceBasketListAdapter.row_index = navigation.page.positionInList;
            navigation.page.positionInList = null;
        }

        return view;
    }



    @Override
    public void onResume() {

        fab.hide();
        /*if(invoice.lastIDCollection > 0) {
            llm.smoothScrollToPosition(recyclerViewInvList, null, invoice.lastIDCollection);
        }*/
        if(!selectAll) {
            selectedCount =0;
            invoice.reLoadInvoice();
            invoiceBasketListAdapter.updateData(invoice);
            invoiceBasketListAdapter.notifyDataSetChanged();
        }
        MainActivity.currentNumber.setText(String.valueOf(invoice.invoices.size()));
        super.onResume();
    }
    public static void fabShowHide()
    {
        if(selectedCount==0) {
            fabDelete.setVisibility(View.GONE);
            fabRestore.setVisibility(View.GONE);
            /*RelativeLayout.LayoutParams layoutParams;
            layoutParams = (RelativeLayout.LayoutParams) fabSelectAll.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
            layoutParams.addRule(RelativeLayout.ABOVE, 0);
            fabSelectAll.setLayoutParams(layoutParams);*/
        }
        else {
            fabDelete.setVisibility(View.VISIBLE);
            fabRestore.setVisibility(View.VISIBLE);
            /*RelativeLayout.LayoutParams layoutParams;
            layoutParams = (RelativeLayout.LayoutParams) fabSelectAll.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.fabDelete);
            fabSelectAll.setLayoutParams(layoutParams);*/
        }
    }

    public void InvoicesBasketPageFragmentSet(Navigation navigation) {
        this.navigation = navigation;
    }
}
