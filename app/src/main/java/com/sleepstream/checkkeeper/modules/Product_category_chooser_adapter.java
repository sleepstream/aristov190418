package com.sleepstream.checkkeeper.modules;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;
import com.sleepstream.checkkeeper.Product_category_create_activity;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class Product_category_chooser_adapter extends RecyclerView.Adapter<Product_category_chooser_adapter.ItemViewHolder>  {
    PurchasesList purchasesList;
    Context context;
    private boolean chooser = false;
    public  Integer rowHeight = 0;
    private Integer cellCount = 0;
    public Integer category_selected =null;
    public List<PurchasesListData.Category> categoriesAll = new ArrayList<>();
    private Fragment fragment;


    public List<PurchasesListData.Category> categoriesSelected = new ArrayList<>();
    private final String LOG_TAG="Products_category_adap";


    public Product_category_chooser_adapter(Context context, PurchasesList purchasesList, InvoiceData currentInvoice, Fragment fragment) {
        this.chooser = chooser;
        this.context = context;
        this.purchasesList = purchasesList;
        loadCategories();
        this.fragment = fragment;


        Log.d(LOG_TAG, "Product_category_chooser_adapter with chooser construct\n");
        for(PurchasesListData purchasesListData : purchasesList.purchasesListData)
        {
            Log.d(LOG_TAG, "purchasesListData\n"+purchasesListData.product.nameFromBill);
            if(purchasesListData.product.category != null) {
                if (findeCategory(categoriesSelected, purchasesListData.product.category)==-1) {
                        purchasesListData.product.category.count = 1;
                        categoriesSelected.add(purchasesListData.product.category);
                    } else {
                        categoriesSelected.get(findeCategory(categoriesSelected, purchasesListData.product.category)).count += 1;
                    }
                    for(PurchasesListData.Category categoryAll: categoriesAll)
                    {
                        if(categoryAll.id == purchasesListData.product.category.id) {
                            categoryAll.selected = true;
                            break;
                        }
                    }
            }
        }
    }

    public void loadCategories()
    {
        categoriesAll = loadProductCategories();
        PurchasesListData.Category category = new PurchasesListData.Category();
        category.category = context.getString(R.string.add_new_product_category);
        category.id = -1;
        category.icon_name ="baseline_add_white";
        categoriesAll.add(category);
    }

    private Integer findeCategory(List<PurchasesListData.Category> categories, PurchasesListData.Category category)
    {
        int i =0;
        for(PurchasesListData.Category category1 : categories)
        {
            if(category1.category.equals(category.category))
                return i;

            i++;
        }
        return -1;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.product_cutegory_adapter, viewGroup, false);
        return new Product_category_chooser_adapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, final int i) {
        Log.d(LOG_TAG, "onBindViewHolder\n");
        itemViewHolder.fab_name_title.setText(categoriesAll.get(i).category);
        if (categoriesAll.get(i).icon_name!= null)
            itemViewHolder.fab_cart.setImageResource(getDrawable(context,categoriesAll.get(i).icon_name+"_48dp"));
        else
            itemViewHolder.fab_cart.setImageResource(getDrawable(context,categoriesAll.get(i).icon_name+"_48dp"));

        if(category_selected != null)
        {
            if (categoriesAll.get(i).category_id == category_selected) {
                itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
                itemViewHolder.fab_cart.selectedChange(true);
            }
            else
            {
                itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
                itemViewHolder.fab_cart.selectedChange(false);
            }
        }
        else {
            itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
            itemViewHolder.fab_cart.selectedChange(false);
        }

            /*
            if(categoriesAll.get(i).category_id == category_selected)
            {
                itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorBackground)));
                itemViewHolder.fab_cart.selectedChange();
            }
            else
            {
                itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
            }
            */

        itemViewHolder.fab_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(categoriesAll.get(i).category_id!= null && categoriesAll.get(i).category_id > 0) {

                    if (categoriesAll.get(i).category_id == category_selected) {
                        category_selected = null;
                        itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
                        itemViewHolder.fab_cart.selectedChange(false);
                        notifyDataSetChanged();
                    } else {
                        category_selected = categoriesAll.get(i).category_id;
                        itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorBackground)));
                        itemViewHolder.fab_cart.selectedChange(true);
                        notifyDataSetChanged();
                    }
                }
                else
                {
                    //запуск создания новой категории
                    Intent product_category_create_activity = new Intent(context, Product_category_create_activity.class);
                    fragment.startActivityForResult(product_category_create_activity,REQUEST_FOR_PRODUCT_CATEGORY_ICON_CODE);
                }
            }
        });
        itemViewHolder.fab_layout.setMinimumHeight(rowHeight);

        if(!itemViewHolder.checkedHeight) {
            ViewTreeObserver observer = itemViewHolder.fab_layout.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {

                    Log.d(LOG_TAG, "observer \n");
                    itemViewHolder.fab_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int tmpHeight = itemViewHolder.fab_layout.getMeasuredHeight();
                    itemViewHolder.checkedHeight = true;
                    if (Product_category_chooser_adapter.this.rowHeight < tmpHeight && cellCount < 4) {
                        Product_category_chooser_adapter.this.rowHeight = tmpHeight;
                    } else if (cellCount == 4) {
                        cellCount = 0;
                        Product_category_chooser_adapter.this.rowHeight = tmpHeight;
                        //notifyDataSetChanged();
                        return;
                    }
                    cellCount += 1;

                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return categoriesAll.size();
    }




    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public boolean checkedHeight = false;
        public CounterCheckedFab fab_cart;
        public TextView fab_name_title;
        public  RelativeLayout fab_layout;
        public RelativeLayout  fab_cart_layout;
        public ItemViewHolder(View itemView) {
            super(itemView);
            fab_cart = itemView.findViewById(R.id.fab_cart);
            fab_name_title  = itemView.findViewById(R.id.fab_name_title);
            fab_cart_layout = itemView.findViewById(R.id.fab_layout);
            fab_layout = itemView.findViewById(R.id.fab_layout);


           /* final ViewTreeObserver observer = fab_layout.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    Log.d(LOG_TAG, "observer \n");
                    fab_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int tmpHeight =  fab_layout.getMeasuredHeight();
                    checkedHeight = true;
                    if(Product_category_chooser_adapter.this.rowHeight < tmpHeight && cellCount <4)
                    {
                        Product_category_chooser_adapter.this.rowHeight = tmpHeight;
                    }
                    else if(cellCount == 4)
                    {
                        cellCount = 0;
                        notifyDataSetChanged();
                        return;
                    }


                    cellCount+=1;
                }
            });*/

        }

        @Override
        public void onClick(View view) {

        }
    }

    public List<PurchasesListData.Category> loadProductCategories() {
        List<PurchasesListData.Category> categories = new ArrayList<>();
        Cursor cur = dbHelper.query("product_category_data", null, null, null, null, null, null, null);
        if(cur.moveToFirst()) {
            do {
                PurchasesListData.Category category = new PurchasesListData.Category();
                category.icon_name = cur.getString(cur.getColumnIndex("icon_name"));
                category.category = cur.getString(cur.getColumnIndex("category"));
                category.category_id = cur.getInt(cur.getColumnIndex("id"));
                categories.add(category);
            }
            while (cur.moveToNext());
        }
        cur.close();
        return categories;
    }
}
