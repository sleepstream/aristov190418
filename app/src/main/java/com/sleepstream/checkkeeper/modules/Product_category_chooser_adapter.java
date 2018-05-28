package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.getDrawable;
import static com.sleepstream.checkkeeper.MainActivity.getThemeColor;

public class Product_category_chooser_adapter extends RecyclerView.Adapter<Product_category_chooser_adapter.ItemViewHolder>  {
    PurchasesList purchasesList;
    Context context;
    private boolean chooser = false;
    public Integer category_selected =null;
    public List<PurchasesListData.Category> categoriesAll = new ArrayList<>();


    public List<PurchasesListData.Category> categoriesSelected = new ArrayList<>();
    private final String LOG_TAG="Products_category_adap";


    public Product_category_chooser_adapter(Context context, PurchasesList purchasesList, InvoiceData currentInvoice, View view) {
        this.chooser = chooser;
        this.context = context;
        this.purchasesList = purchasesList;
        categoriesAll = purchasesList.loadProductCategories();


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
        if (categoriesAll.get(i).icon_id!= null)
            itemViewHolder.fab_cart.setImageResource(categoriesAll.get(i).icon_id);
        else
            itemViewHolder.fab_cart.setImageResource(R.drawable.ic_product_category_default_48);

        category_selected = null;
        itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
        itemViewHolder.fab_cart.selectedChange(false);

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

        itemViewHolder.fab_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(categoriesAll.get(i).category_id == category_selected )
                {
                    category_selected = null;
                    itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorNewAccentLink)));
                    itemViewHolder.fab_cart.selectedChange(false);
                }
                else
                {
                    category_selected = categoriesAll.get(i).category_id;
                    itemViewHolder.fab_cart.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(context, R.attr.colorBackground)));
                    itemViewHolder.fab_cart.selectedChange(true);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return categoriesAll.size();
    }




    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public CounterCheckedFab fab_cart;
        public TextView fab_name_title;
        public ItemViewHolder(View itemView) {
            super(itemView);
            fab_cart = itemView.findViewById(R.id.fab_cart);
            fab_name_title  = itemView.findViewById(R.id.fab_name_title);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
