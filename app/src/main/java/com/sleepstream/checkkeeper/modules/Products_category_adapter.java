package com.sleepstream.checkkeeper.modules;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.andremion.counterfab.CounterFab;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;

import java.util.ArrayList;
import java.util.List;

public class Products_category_adapter extends RecyclerView.Adapter<Products_category_adapter.ItemViewHolder>  {
    PurchasesList purchasesLists;
Context context;


    public List<PurchasesListData.Category> categories = new ArrayList<>();

    public Products_category_adapter(Context context, PurchasesList purchasesList, InvoiceData currentInvoice, View view) {
        this.context = context;
        for(PurchasesListData purchasesListData : purchasesList.purchasesListData)
        {
            if(purchasesListData.product.categories != null) {
                for (PurchasesListData.Category category : purchasesListData.product.categories) {

                    if (findeCategory(categories, category)==-1) {
                        category.count = 1;
                        categories.add(category);
                    } else {
                        categories.get(findeCategory(categories, category)).count += 1;
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
        return new Products_category_adapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.fab_cart.setCount(categories.get(i).count);
        if(categories.get(i).icon_name.equals(context.getString(R.string.default_icon_name_product_category)))
            itemViewHolder.fab_cart.setImageResource(R.drawable.ic_product_category_default_48);

    }


    @Override
    public int getItemCount() {
        return categories.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public CounterFab fab_cart;
        public ItemViewHolder(View itemView) {
            super(itemView);
            fab_cart = itemView.findViewById(R.id.fab_cart);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
