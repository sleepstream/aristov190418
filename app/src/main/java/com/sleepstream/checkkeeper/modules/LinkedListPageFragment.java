package com.sleepstream.checkkeeper.modules;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.SimpleItemTouchHelperCallback;

import java.util.Random;

import static com.sleepstream.checkkeeper.MainActivity.fab;
import static com.sleepstream.checkkeeper.MainActivity.linkedListClass;

public class LinkedListPageFragment extends Fragment implements LinkedListAdapter.OnStartDragListener{

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String LOG_TAG = "AccountingListPageFragment";
    public static int page_title;

    int pageNumber;
    int backColor;

    public RecyclerView recyclerViewLinkList;

    private ItemTouchHelper mItemTouchHelperLinkList;
    private Context context;

    public static LinkedListAdapter linkedListAdapter;
    public LinearLayoutManager linearLayoutManager;
    private Navigation navigation;


    public static LinkedListPageFragment newInstance(int page) {
        LinkedListPageFragment invoicesPageFragment = new LinkedListPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        page_title = R.string.linkedListPageTitle;
        arguments.putInt("currentName", R.string.linkedListPageTitle);
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



    @SuppressLint("LongLogTag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_linked_list_page, null);
        context = view.getContext();

        fab.show();


        recyclerViewLinkList = view.findViewById(R.id.recyclerViewLinkedList);

        linkedListAdapter = new LinkedListAdapter(context, view, linkedListClass.linkedListData, navigation);
        linkedListAdapter.addItem(0, context.getString(R.string.invoicesListTitle));
        recyclerViewLinkList.setAdapter(linkedListAdapter);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewLinkList.setLayoutManager(linearLayoutManager);


        ItemTouchHelper.Callback callbacLikedList = new SimpleItemTouchHelperCallback(linkedListAdapter, context);
        mItemTouchHelperLinkList = null;
        mItemTouchHelperLinkList = new ItemTouchHelper(callbacLikedList);
        mItemTouchHelperLinkList.attachToRecyclerView(recyclerViewLinkList);


        recyclerViewLinkList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE  && linearLayoutManager.findLastVisibleItemPosition()- linearLayoutManager.findFirstVisibleItemPosition() == linkedListAdapter.getItemCount()-1)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState );
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if((linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition()) < (linkedListAdapter.getItemCount()-1)) {

                    if (linearLayoutManager.findLastVisibleItemPosition() == linkedListAdapter.getItemCount() - 1)
                        fab.hide();
                    else
                        fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        if(navigation.page!= null && navigation.page.positionInList != null) {
            linearLayoutManager.scrollToPosition(navigation.page.positionInList);
            linkedListAdapter.row_index = navigation.page.positionInList;
            navigation.page.positionInList = null;
        }
        return view;


    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelperLinkList.startDrag(viewHolder);
    }

    public void LinkedListPageFragmentSet (Navigation navigation) {
        this.navigation = navigation;
    }
    @Override
    public void onResume() {
        super.onResume();
        linkedListClass.reLoadLinkedList();
        linkedListAdapter.notifyDataSetChanged();
        MainActivity.currentNumber.setText(String.valueOf(linkedListClass.getCount()));



    }
}
