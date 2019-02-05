package com.sleepstream.checkkeeper.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.helper.ItemTouchHelperAdapter;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesList;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sleepstream.checkkeeper.MainActivity.currentNumber;
import static com.sleepstream.checkkeeper.MainActivity.getDrawable;

public class Product_category_icons_viewer_adapter extends RecyclerView.Adapter<Product_category_icons_viewer_adapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    Context context;
    private boolean chooser = false;
    public List<Integer> categories = new ArrayList<>();
    Integer selectedIcon = null;
    public String iconName = null;


    public List<PurchasesListData.Category> categoriesSelected = new ArrayList<>();
    private final String LOG_TAG="Products_category_adap";

    public Product_category_icons_viewer_adapter(Context context, List<Integer> categories) {
        this.context = context;
        this.categories = categories;
        Log.d(LOG_TAG, "Product_category_chooser_adapter  construct\n");
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.product_cutegory_icons_adapter, viewGroup, false);
        return new Product_category_icons_viewer_adapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, final int i) {
        Log.d(LOG_TAG, "onBindViewHolder\n"+categories.get(i));

        if(selectedIcon!= null && selectedIcon == i)
        {
            itemViewHolder.fab_cart.selectedChange(true);
        }
        else
        {
            itemViewHolder.fab_cart.selectedChange(false);
        }
        itemViewHolder.fab_cart.setImageResource(categories.get(i));
        itemViewHolder.fab_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedIcon == null || selectedIcon != i)
                {
                    selectedIcon = i;
                    iconName =  context.getResources().getResourceEntryName(categories.get(i)).replaceAll("_[0-9]{2}dp", "");
                    //itemViewHolder.fab_cart.selectedChange(true);
                }
                else
                {
                    selectedIcon = null;
                    iconName = null;
                    //itemViewHolder.fab_cart.selectedChange(false);
                }
                notifyDataSetChanged();


                /*
                purchasesList.reloadPurchasesList(categoriesSelected.get(i).category_id);
                currentNumber.setText(String.valueOf(purchasesList.purchasesListData.size()));
                PurchasesPageFragment.purchasesListAdapter.notifyDataSetChanged();
                PurchasesPageFragment.mainView.setVisibility(View.VISIBLE);
                */
            }
        });
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {

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
