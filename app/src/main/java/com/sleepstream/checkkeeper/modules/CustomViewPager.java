package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.sleepstream.checkkeeper.MainActivity;

public class CustomViewPager extends ViewPager {

    private boolean enabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        TabLayout.Tab tab;
        switch(item)
        {
            case 0:
                MainActivity.pageNow = "linkedList";
                tab = MainActivity.tabLayout.getTabAt(0);
                tab.select();
                break;
            case 1:
                MainActivity.pageNow = "accountingList";
                //tab = MainActivity.tabLayout.getTabAt(1);
                //tab.select();
                break;
            case 2:
                MainActivity.pageNow = "invoicesLists";
                tab = MainActivity.tabLayout.getTabAt(2);
                tab.select();
                break;
            case 3:
                MainActivity.pageNow = "purchasesList";
                break;
            case 4:
                MainActivity.pageNow = "invoiceBasket";
                tab = MainActivity.tabLayout.getTabAt(3);
                tab.select();
                break;
        }


        super.setCurrentItem(item, smoothScroll);
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}