package com.sleepstream.checkkeeper;

import android.app.Activity;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;
import com.sleepstream.checkkeeper.modules.*;

import java.util.*;

import static com.sleepstream.checkkeeper.MainActivity.LOG_TAG;
import static com.sleepstream.checkkeeper.MainActivity.invoice;

public class Navigation {

    private final Context context;
    public PurchasesPageFragment purchasesPageFragment;
    public MainActivity.Page currentPage = null;
    public android.app.FragmentTransaction fTrans;
    public ArrayList<Date> filterDates;
    public  Map<String, String[]> filterParam = new LinkedHashMap<>();
    private TextView toolbar_title;
    public NavigableMap<MainActivity.Page, Map<String, String[]>> pageBackList = new TreeMap<>();
    private boolean backpressed;
    public MainActivity.Page page;
    public static Map<String, String[]> statusInvoices = new LinkedHashMap<>();


    public Navigation(Context context, TextView toolbar_title) {
        statusInvoices.put("loading", new String[]{"0", "3", "-2", "-1", "-4", "-3", "-5"});
        statusInvoices.put("loaded", new String[]{"1"});
        //statusInvoices.put("in_basket", new String[]{"1"});
        statusInvoices.put("confirmed", new String[]{"2"});
        this.context = context;
        this.toolbar_title=toolbar_title;
    }

    public void setTitle(String title)
    {
        String old = toolbar_title.getText().toString();
        toolbar_title.setText(old+title);
    }

    public void openCurrentPage(MainActivity.Page page) {
        this.page = page;
        fTrans =((Activity) context).getFragmentManager().beginTransaction();

        if(MainActivity.invoice.filterDates!= null)
        Log.d(LOG_TAG, MainActivity.invoice.filterDates.size()+"");
        if(backpressed)
        {
            backpressed = false;
        }
        else {
            if (currentPage != null) {
                if(page.positionInList != null)
                    currentPage.positionInList = page.positionInList;
                if (pageBackList.size() > 0)
                {
                    boolean exist = false;
                    Map.Entry<MainActivity.Page, Map<String, String[]>> entry =pageBackList.lastEntry();
                    MainActivity.Page pageTmp = entry.getKey();
                    Map<String, String[]> filterTmp = entry.getValue();
                    if(!pageTmp.position.equals(currentPage.position) || !filterTmp.equals(filterParam)) {
                        if(page.positionInList != null)
                            currentPage.positionInList =page.positionInList;
                        currentPage.setId();
                        Map<String, String[]> Tmp = new LinkedHashMap<>();
                        for(Map.Entry<String, String[]> item: filterParam.entrySet())
                        {
                            Tmp.put(item.getKey(), item.getValue());
                        }
                        pageBackList.put(currentPage, Tmp);
                    }
                }
                else //if (pageBackList.size() == 0 && currentPage.position >0)
                {
                    currentPage.setId();
                    Map<String, String[]> filterTmp = new LinkedHashMap<>();
                    for(Map.Entry<String, String[]> item: filterParam.entrySet())
                    {
                        filterTmp.put(item.getKey(), item.getValue());
                    }
                    pageBackList.put(currentPage, filterTmp);
                }
            }
        }
        switch (page.position) {
            case 0: {
                currentPage = page;
                clearFilter("");
                //setFilter("place_id", new String[]{"not null"});
                setFilter("_status", statusInvoices.get("confirmed"));
                InvoicesPageFragment fragment = InvoicesPageFragment.newInstance(page);
                fragment.InvoicesPageFragmentSet(this);

                fTrans.replace(R.id.pager, fragment);
                if(InvoicesPageFragment.page_title != "")
                    toolbar_title.setText(InvoicesPageFragment.page_title);
                else
                    toolbar_title.setText(context.getString(R.string.invoicesListTitle));

                break;
            }
            case 7: {
                currentPage = page;
                clearFilter("");
                //setFilter("place_id", new String[]{"is null"});
                setFilter("_status", statusInvoices.get("loaded"));
                InvoicesPageFragment fragment = InvoicesPageFragment.newInstance(page);
                fragment.InvoicesPageFragmentSet(this);

                fTrans.replace(R.id.pager, fragment);
                if(InvoicesPageFragment.page_title != "")
                    toolbar_title.setText(InvoicesPageFragment.page_title);
                else
                    toolbar_title.setText(context.getString(R.string.invoicesShowOnMapTitle));

                break;
            }
            case 1: {
                currentPage = page;
                clearFilter("");
                setFilter("in_basket", new String[]{"1"});
                InvoicesBasketPageFragment fragment =InvoicesBasketPageFragment.newInstance(0);
                fragment.InvoicesBasketPageFragmentSet(this);
                fTrans.replace(R.id.pager, fragment);
                toolbar_title.setText(context.getString(InvoicesBasketPageFragment.page_title));
                break;
            }
            case 2: {
                currentPage = page;
                clearFilter("");
                AccountingListPageFragment fragment = AccountingListPageFragment.newInstance(0);
                fragment.AccountingListPageFragmentSet(this);
                fTrans.replace(R.id.pager, fragment);
                toolbar_title.setText(context.getString(AccountingListPageFragment.page_title));
                break;
            }
            case 3: {
                currentPage = page;
                clearFilter("");
                LinkedListPageFragment fragment = LinkedListPageFragment.newInstance(0);
                fragment.LinkedListPageFragmentSet(this);
                if(fragment.linearLayoutManager != null)
                    fragment.linearLayoutManager.smoothScrollToPosition(fragment.recyclerViewLinkList, null, page.positionInList);
                fTrans.replace(R.id.pager, fragment);
                toolbar_title.setText(context.getString(LinkedListPageFragment.page_title));
                break;
            }
            case 4: {
                currentPage = page;
                purchasesPageFragment = PurchasesPageFragment.newInstance(0);
                purchasesPageFragment.PurchasesPageFragmentSet(this, context);
                fTrans.replace(R.id.pager, purchasesPageFragment);
                toolbar_title.setText(context.getString(PurchasesPageFragment.page_title));
                break;
            }
            case 5: {
                currentPage = page;
                clearFilter("");
                setFilter("_status", statusInvoices.get("loading"));
                InvoicesPageFragment fragment = InvoicesPageFragment.newInstance(page);
                fragment.InvoicesPageFragmentSet(this);
                fTrans.replace(R.id.pager, fragment);
                if(InvoicesPageFragment.page_title != "")
                    toolbar_title.setText(InvoicesPageFragment.page_title);
                else
                    toolbar_title.setText(context.getString(R.string.invoicesLoadingListTitle));
                break;
            }
            case 6: {//load invoices with filters
                currentPage = page;
                InvoicesPageFragment fragment = InvoicesPageFragment.newInstance(page);
                fragment.InvoicesPageFragmentSet(this);
                fTrans.replace(R.id.pager, fragment);
                if(InvoicesPageFragment.page_title != "")
                    toolbar_title.setText(InvoicesPageFragment.page_title);
                else
                    toolbar_title.setText(context.getString(R.string.invoicesNOTitleTitle));
                break;
            }
            /*case 8:
            {
                currentPage = page;
                PlaceChooserActivity fragment = PlaceChooserActivity.newInstance(page);
                fragment.PlaceChooserFragmentSet(this);
                fTrans.replace(R.id.pager, fragment);
                if(InvoicesPageFragment.page_title != "")
                    toolbar_title.setText(InvoicesPageFragment.page_title);
                else
                    toolbar_title.setText(context.getString(R.string.placeChooserTitle));
                break;
            }*/
            default: {
                currentPage = page;
                fTrans.replace(R.id.pager, InvoicesPageFragment.newInstance(page));
                toolbar_title.setText(InvoicesPageFragment.page_title);
                break;
            }
        }

        fTrans.commit();
    }

    public void clearFilter(String key)
    {

        if(filterParam.containsKey("date_day") && key == "") {
            Map<String, String[]> filter = new ArrayMap<>();
            filter.putAll(filterParam);
            for(Map.Entry<String, String[]> entry : filterParam.entrySet()) {
                String keyTmp = entry.getKey();
                if(keyTmp != "date_day")
                {
                    filter.remove(keyTmp);
                }
            }
            filterParam = filter;
        } else if (key != "" && filterParam.containsKey(key)) {
            filterParam.remove(key);
            if(key == "date_day")
            {
                filterDates.clear();
            }
        }
        else {
            filterParam.clear();
            if(filterDates!= null)
                filterDates.clear();
        }
    }
    public void setFilter(String param, String[] value) {
        if(!filterParam.containsKey(param)) {
            filterParam.put(param, value);
        }

    }

    public String[] getFilter(String param)
    {
        return filterParam.get(param);
    }

    public void copyFiltersToInvoice()
    {
        if(filterDates != null) {
            if(invoice.filterDates != null) {
                invoice.filterDates.clear();
                invoice.filterDates.addAll(filterDates);
            }
            else {
                invoice.filterDates= new ArrayList<>();
                invoice.filterDates.addAll(filterDates);
            }
        }
        if(filterParam != null) {
            invoice.filterParam.clear();
            invoice.filterParam.putAll(filterParam);
        }
    }

    public void backPress() {
        /*if(purchasesPageFragment!= null)
        {
            purchasesPageFragment.onDestroy();
            purchasesPageFragment = null;
        }*/
        backpressed = true;
        if(pageBackList.size()>0)
        {
            MainActivity.Page page = pageBackList.lastEntry().getKey();
            if(filterParam.size()>0 && pageBackList.lastEntry().getValue().size()>0) {

                Map<String, String[]> tmp =  new ArrayMap<>();
                tmp.putAll(pageBackList.lastEntry().getValue());
                filterParam.clear();
                filterParam.putAll(tmp);
            }
            pageBackList.remove(pageBackList.lastKey());
            openCurrentPage(page);

        }
        else
        {

            MainActivity.Page page = new MainActivity.Page("", 0);
            openCurrentPage(page);

        }
    }
}
