package com.sleepstream.checkkeeper.purchasesObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;
import static com.sleepstream.checkkeeper.MainActivity.getDrawable;

public class PurchasesList {


    final String LOG_TAG = "PurchasesList";
    private Context context;
    public List<PurchasesListData> purchasesListData = new ArrayList<>();
    private String tableName="purchases";
    private String[] tableName_fk = new String[]{"accountinglist", "invoice", "stores", "products", "currency"};
    public int lastIDCollection;
    public Integer lastShowedCategory;

    private Map<String, String> filterParam = new HashMap<>();
    public String title;

    public void setfilter(String param, String value) {
        if(filterParam.containsKey(param)) {
            filterParam.remove(param);
        }
        filterParam.put(param, value);
    }
    public boolean checkFilter()
    {
        return filterParam.isEmpty();
    }
    public void clearFilter()
    {
        filterParam.clear();
    }

    public PurchasesList(Context context) {
        this.context = context;
    }




    public void reloadPurchasesList(Integer categoryId)
    {
        if(categoryId != null)
            lastShowedCategory = categoryId;
        
        this.purchasesListData.clear();
        purchasesListData = new ArrayList<>();
        String selection ="";
        List<String> selectionArgs=new ArrayList<>();
        if(categoryId != null && categoryId != -1)
        {
            selectionArgs.add(categoryId.toString());
        }

        for(Map.Entry<String, String> entry : filterParam.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(value != null)
            {
                selection+=key+"=? and ";
                selectionArgs.add(value);
            }
        }
        String[] args = selectionArgs.toArray(new String[selectionArgs.size()]);
        //Log.d(LOG_TAG, "Reload selection args!" + args[0]+"!");
        Cursor cur;

        if(categoryId == null)
        {
            if (selection != "") {
                selection = selection.substring(0, selection.length() - 5);
                Log.d(LOG_TAG, "Reload selection !" + selection + "!");
                cur = dbHelper.query(tableName, null, selection, args, null, null, "_order", null);
            } else {
                cur = dbHelper.query(tableName, null, null, null, null, null, "_order", null);
            }
        }
        else if(categoryId == -1)
        {
            String selectionQuery  ="Select * from "+tableName+" where fk_purchases_products not in " +
                    "(Select id from Products where id in " +
                    "(Select fk_product_category_products from product_category)) "+(selection != ""? " AND "+selection.substring(0, selection.length() - 5) : "");
            cur = dbHelper.rawQuery(selectionQuery,args.length>0 ? args : null);
        }
        else
        {
            String selectionQuery  ="Select * from "+tableName+" where fk_purchases_products in " +
                    "(Select id from Products where id in " +
                    "(Select fk_product_category_products from product_category where fk_product_category_data = ?)) "+(selection != ""? " AND "+selection.substring(0, selection.length() - 5) : "");
            cur = dbHelper.rawQuery(selectionQuery, args);
        }

        if(cur.moveToFirst())
        {

            do {
                PurchasesListData purchasesListData = new PurchasesListData();
                for(int i=0; i<tableName_fk.length; i++)
                {
                    try {
                        Log.d(LOG_TAG, "purchases fk index "+"fk_" + tableName.toLowerCase() + "_" + tableName_fk[i]);
                        int fk = cur.getInt(cur.getColumnIndex("fk_" + tableName.toLowerCase() + "_" + tableName_fk[i]));
                        purchasesListData.fk.put(tableName_fk[i], fk);
                        //get data from fk tables
                        Cursor fk_cur;

                        switch(tableName_fk[i])
                        {
                            case "invoice":
                                fk_cur = dbHelper.query(tableName_fk[i], null, "id=?", new String[]{fk+""}, null, null, null, null);
                                if(fk_cur.moveToFirst())
                                {
                                    PurchasesListData.Invoice invoice = new PurchasesListData.Invoice();

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS", Locale.getDefault());
                                    java.util.Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(fk_cur.getLong(fk_cur.getColumnIndex("date")));

                                    invoice.date = dateFormat.format(calendar.getTime());
                                    invoice.id = fk_cur.getInt(fk_cur.getColumnIndex("id"));
                                    invoice.fullprice = fk_cur.getDouble(fk_cur.getColumnIndex("fullprice"));
                                    purchasesListData.invoice = invoice;
                                }
                                fk_cur.close();
                                break;
                            case "stores":
                                fk_cur = dbHelper.query(tableName_fk[i], null, "id=?", new String[]{fk + ""}, null, null, null, null);
                                if(fk_cur.moveToFirst()) {
                                    PurchasesListData.Store store = new PurchasesListData.Store();
                                    store.adress = fk_cur.getString(fk_cur.getColumnIndex("address"));
                                    store.id = fk_cur.getInt(fk_cur.getColumnIndex("id"));
                                    store.name = fk_cur.getString(fk_cur.getColumnIndex("name"));
                                    purchasesListData.store = store;
                                }
                                fk_cur.close();
                                break;
                            case "products":
                                fk_cur = dbHelper.query(tableName_fk[i], null, "id=?", new String[]{fk+""}, null, null, null, null);
                                if(fk_cur.moveToFirst()) {
                                    PurchasesListData.Product product = new PurchasesListData.Product();
                                    product.id = fk_cur.getInt(fk_cur.getColumnIndex("id"));
                                    product.correctName = fk_cur.getString(fk_cur.getColumnIndex("correctName"));
                                    product.nameFromBill = fk_cur.getString(fk_cur.getColumnIndex("nameFromBill"));
                                    product.barcode = fk_cur.getInt(fk_cur.getColumnIndex("barcode"));
                                    purchasesListData.product = product;

                                    Cursor cur_product_category = dbHelper.query("product_category", null, "fk_product_category_products=?", new String[]{product.id.toString()},
                                            null, null, null, null);
                                    PurchasesListData.Category category = new PurchasesListData.Category();
                                    if(cur_product_category.moveToFirst())
                                    {

                                        do{

                                            category.id = cur_product_category.getInt(cur_product_category.getColumnIndex("id"));
                                            category.fk_product_category_data = cur_product_category.getInt(cur_product_category.getColumnIndex("fk_product_category_data"));
                                            category.fk_product_category_products = cur_product_category.getInt(cur_product_category.getColumnIndex("fk_product_category_products"));

                                            Cursor cur_product_category_name = dbHelper.query("product_category_data", null, "id=?", new String[]{category.fk_product_category_data.toString()},
                                                    null, null, null, null);
                                            if(cur_product_category_name.moveToFirst())
                                            {
                                                category.category = cur_product_category_name.getString(cur_product_category_name.getColumnIndex("category"));
                                                category.icon_name = cur_product_category_name.getString(cur_product_category_name.getColumnIndex("icon_name"));
                                                category.category_id = cur_product_category_name.getInt(cur_product_category_name.getColumnIndex("id"));
                                                category.icon_id = cur_product_category_name.getInt(cur_product_category_name.getColumnIndex("icon_id"));

                                                if(category.icon_id == 0 && category.icon_name!= "")
                                                {
                                                    category.icon_id = getDrawable(context, category.icon_name);
                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put("icon_id", category.icon_id);
                                                    dbHelper.update("product_category_data", contentValues, "id=?", new String[]{category.category_id.toString()});
                                                }
                                                cur_product_category_name.close();
                                            }
                                        }
                                        while(cur_product_category.moveToNext());
                                        cur_product_category.close();
                                    }
                                    else
                                    {
                                        category.count=0;
                                        category.icon_name= "ic_product_category_default";//R.string.default_icon_name_product_category;
                                        category.category = "default";
                                        category.category_id = -1;

                                    }
                                    purchasesListData.product.category = category;
                                }
                                fk_cur.close();
                                break;
                        }
                    }
                    catch (Exception ex)
                    {
                        purchasesListData.fk.put(tableName_fk[i], null);
                        ex.printStackTrace();
                    }
                }
                purchasesListData.id = cur.getInt(cur.getColumnIndex("id"));
                purchasesListData.prise_for_item=cur.getFloat(cur.getColumnIndex("prise_for_item"));
                purchasesListData.quantity = cur.getFloat(cur.getColumnIndex("quantity"));
                purchasesListData.sum= cur.getFloat(cur.getColumnIndex("sum"));
                this.purchasesListData.add(purchasesListData);
            }
            while(cur.moveToNext());
            Log.d(LOG_TAG, "Loaded from DB records " + purchasesListData.size());
        }
        cur.close();
        Log.d(LOG_TAG, "No records in DB");
    }


    public boolean updatePurchasesListData(PurchasesListData purchasesListData)
    {
        


        ContentValues values = new ContentValues();
        Integer id = purchasesListData.id;
        values.put("_order", purchasesListData.getOrder());
        long count = dbHelper.update(tableName,values,"id=?", new String[]{id.toString()});
        //check list in another tables
        
        if(count>0) {
            return true;

        }
        return false;

    }

    public boolean deletePurchasesListData(Integer id)
    {
        

        long count = dbHelper.delete(tableName, "id=?", new String[]{id.toString()});
        
        if(count>0) {
            for (int i = 0; i < purchasesListData.size(); i++) {
                if (purchasesListData.get(i).id == id)
                {
                    purchasesListData.remove(i);
                    Log.d(LOG_TAG, "tr removeD purchasesList "+ id);
                    return true;
                }
            }
            Log.d(LOG_TAG, "tr removeD only from DB purchases "+ id);

        }
        return false;

    }

    public Integer addPurchasesList(Integer position, PurchasesListData purchasesListData)
    {
        return null;
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
                category.icon_id = cur.getInt(cur.getColumnIndex("icon_id"));
                categories.add(category);
            }
            while (cur.moveToNext());
        }
        cur.close();
        return categories;
    }
}
