package com.sleepstream.checkkeeper.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class WrapContentLinearLayoutManager  extends LinearLayoutManager {
    private String LOG_TAG = "WrapContentLinearLayoutManager";
    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @SuppressLint("LongLogTag")
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            return super.scrollVerticallyBy(dy, recycler, state);
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "meet a IOOBE in RecyclerView");
            return 0;
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "meet a IOOBE in RecyclerView");
        }
    }
}
