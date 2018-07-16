package com.sleepstream.checkkeeper.invoiceObjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;
import com.sleepstream.checkkeeper.GetFnsData;
import com.sleepstream.checkkeeper.LoadingFromFNS;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.Navigation;
import com.sleepstream.checkkeeper.purchasesObjects.PurchasesListData;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class Invoice {

    final String LOG_TAG = "InvoiceClass";
    private Navigation navigation;
    public List<InvoiceData> invoices = new ArrayList<>();
    public List<InvoiceData> pinnedItems = new ArrayList<>();

    public static final Source source = Source.SERVER;


    public String getTableNameInvoice() {
        return tableNameInvoice;
    }

    public final static String tableNameInvoice ="invoice";
    public final static String tablenameStores ="stores";
    public final static String tableJsonData ="collectedData";
    public final static String tableNameKktRegId = "kktRegId";
    public final static String tableNamePurchases = "purchases";
    public final static String tableNameProducts = "Products";
    public int lastIDCollection;
    public ArrayList<Date> filterDates;


    public Map<String, String[]> filterParam = new LinkedHashMap<>();

    public void setfilter(String param, String[] value) {
        if(filterParam.containsKey(param)) {
            filterParam.remove(param);
        }
        else {
            filterParam.put(param, value);
        }
    }

    public void setfilter(String param, String value) {
        if(filterParam.containsKey(param)) {
            filterParam.remove(param);
        }
        filterParam.put(param, new String[]{value});
    }

    public boolean checkFilter(String param, @Nullable Integer id)
    {
        if(param != null) {
            if (filterParam.containsKey(param)) {
                if(id != null && filterParam.get(param).equals(id)) {
                    return true;
                }
                else if (id == null)
                    return true;
                else
                    return false;
            }
            else
                return false;
        }
        else
        {
            return filterParam.isEmpty();
        }
    }

    public void clearFilter(String key)
    {

        if(filterParam.containsKey("date_day") && key == "") {
            Map<String, String[]> filter = filterParam;
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

    public String[] getFilter(String param)
    {
        return filterParam.get(param);
    }

    private void updateOlddataBase()
    {
        Cursor cur = dbHelper.query(tableNamePurchases, null, "fk_purchases_stores is null", null, null, null, null, null);
        if(cur.moveToFirst())
        {

            Integer tmpInv =  null;
            do {
                Integer invoice = cur.getInt(cur.getColumnIndex("fk_purchases_invoice"));
                if(invoice != tmpInv)
                {
                    Cursor cur_invoice = dbHelper.query(tableNameInvoice, null, "id = ?", new String[]{invoice.toString()}, null, null, null, null);
                    if(cur_invoice.moveToFirst())
                    {
                        Integer store = cur_invoice.getInt(cur_invoice.getColumnIndex("fk_invoice_stores"));
                        if(store != null && store > 0)
                        {
                            ContentValues values = new ContentValues();
                            values.put("fk_purchases_stores", store);
                            int count  = dbHelper.update(tableNamePurchases, values, "fk_purchases_invoice=?", new String[]{invoice.toString()});
                            Log.d(LOG_TAG, "update purchases count " + count+ " invoice = "+invoice+"\n");
                        }
                    }
                    cur_invoice.close();
                }
                tmpInv =  cur.getInt(cur.getColumnIndex("fk_purchases_invoice"));
            }while (cur.moveToNext());
            cur.close();
        }
    }

    public Invoice(Navigation navigation) {
        this.navigation = navigation;

        //updateOlddataBase();

        /*Cursor cur = dbHelper.query(tableNameInvoice, null, null, null, null, null, "date_day DESC, _order ASC", null);
        invoices.clear();
        invoices.addAll(loadData(cur));
        if(invoices.size()>0)
        {
            Log.d(LOG_TAG, "Loaded from DB records " + invoices.size());
        }
        else {
            Log.d(LOG_TAG, "No records in DB");
        }*/
        
    }

    public void reLoadInvoice() {
        if(navigation != null )
            navigation.copyFiltersToInvoice();
        String selection ="";
        List<String> selectionArgs=new ArrayList<>();
        String tmp="";

        for(Map.Entry<String, String[]> entry : filterParam.entrySet()) {
            String key = entry.getKey();
            String[] value= null;
            if(entry.getValue() != null) {
                value = entry.getValue();
            }
            else
                value= new String[]{""};

            if(key == "date_day" && filterDates.size()>0)
            {
                selection+=" (";

                for(int i = 0; i < filterDates.size(); i++)
                {
                    selection+=key+"=? or ";
                    selectionArgs.add(String.valueOf(filterDates.get(i).getTime()));
                    tmp += value[0]+ " ";
                }
                selection = selection.substring(0, selection.length()-4) + ") and ";
            }
            else if(key == "_status")
            {
                selection+=" (";

                for(int i = 0; i < value.length; i++)
                {
                    selection+=key+"=? or ";
                    selectionArgs.add(value[i]);
                    tmp += value[i]+ " ";
                }
                selection = selection.substring(0, selection.length()-4) + ") and ";
            }
            else if(value != null)
            {
                selection+=key+"=? and ";
                selectionArgs.add(value[0]);
                tmp += value+ " ";
            }

        }

        if(!filterParam.containsKey("in_basket")) {
            selection += " in_basket=? and ";
            selectionArgs.add("0");
        }

        String[] args = selectionArgs.toArray(new String[selectionArgs.size()]);
        //Log.d(LOG_TAG, "Reload selection args!" + args[0]+"!");
        Cursor cur;
        if(selection!="")
        {
            selection = selection.substring(0, selection.length()-5);
            Log.d(LOG_TAG, "Reload selection !" + selection+"!  " + tmp);
            cur = dbHelper.query(tableNameInvoice, null, selection, args, null, null, "date_day DESC, _order ASC", null);
        }
        else {
            cur = dbHelper.query(tableNameInvoice, null, null, null, null, null, "date_day DESC, _order ASC", null);
        }

        //Cursor cur = MainActivity.dbHelper.query(tableNameInvoice, null, selection, args, null, null, "_order", null);
        //this.invoices.clear();
        List<InvoiceData> invoiceDataTMP = new ArrayList<>();
        invoiceDataTMP = loadData(cur);
        cur.close();
        if(invoiceDataTMP.size()>0)
        {
            this.invoices.clear();
            this.invoices.addAll(invoiceDataTMP);
            invoiceDataTMP= null;
            Log.d(LOG_TAG, "Reload Loaded from DB records " + this.invoices.size());
        }
        else
        {
            this.invoices.clear();
            //InvoicesPageFragment.invoiceListAdapter.swap(null);
            Log.d(LOG_TAG, " Reload No records in DB size "+ this.invoices.size());
        }
        
    }

    private List<InvoiceData> loadData(Cursor cur)
    {
        List<InvoiceData> invoiceDataTMP = new ArrayList<>();
        if(cur.moveToFirst())
        {
            do {
                InvoiceData invoiceData = new InvoiceData();
                invoiceData.setAll(cur.getString(cur.getColumnIndex("FP")),
                        cur.getString(cur.getColumnIndex("FD")),
                        cur.getString(cur.getColumnIndex("FN")),
                        Long.parseLong(cur.getString(cur.getColumnIndex("dateInvoice"))),
                        cur.getString(cur.getColumnIndex("fullPrice")),
                        cur.getInt(cur.getColumnIndex("id")),
                        cur.getInt(cur.getColumnIndex("_order")),
                        cur.getInt(cur.getColumnIndex("fk_invoice_accountinglist")),
                        cur.getInt(cur.getColumnIndex("fk_invoice_kktRegId")));
                invoiceData.setIn_basket(cur.getInt(cur.getColumnIndex("in_basket")));
                invoiceData.set_status(cur.getInt(cur.getColumnIndex("_status")));
                invoiceData.setfk_invoice_stores(cur.getInt(cur.getColumnIndex("fk_invoice_stores")));

                invoiceData.latitudeAdd =cur.getDouble(cur.getColumnIndex("latitudeAdd"));
                invoiceData.longitudeAdd =cur.getDouble(cur.getColumnIndex("longitudeAdd"));
                invoiceData.repeatCount =cur.getInt(cur.getColumnIndex("repeatCount"));

                invoiceData.server_status =cur.getInt(cur.getColumnIndex("server_status"));


                invoiceData.google_id = cur.getString(cur.getColumnIndex("google_id"));
                invoiceData.fk_invoice_accountinglist_google_id =cur.getString(cur.getColumnIndex("fk_invoice_accountinglist_google_id"));
                invoiceData.fk_invoice_kktRegId_google_id =cur.getString(cur.getColumnIndex("fk_invoice_kktRegId_google_id"));
                invoiceData.fk_invoice_stores_google_id =cur.getString(cur.getColumnIndex("fk_invoice_stores_google_id"));
                invoiceData.user_google_id =cur.getString(cur.getColumnIndex("user_google_id"));



                if(invoiceData.getfk_invoice_kktRegId() !=null)
                {
                    Cursor cur_purchases = dbHelper.query("purchases", null, "fk_purchases_invoice=?", new String[]{invoiceData.getId().toString()}, null, null, null, null);
                    if(cur_purchases.moveToFirst())
                    {
                        invoiceData.quantity = cur_purchases.getCount();
                    }
                    cur_purchases.close();

                    Integer id = invoiceData.getfk_invoice_kktRegId();
                    Cursor cur_kktRegId = dbHelper.query("kktRegId", null, "id=?", new String[]{id.toString()}, null, null, null, null);
                    if(cur_kktRegId.moveToFirst()) {
                        invoiceData.kktRegId = new InvoiceData.KktRegId();

                        invoiceData.kktRegId.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));
                        invoiceData.kktRegId.fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                        invoiceData.kktRegId.fk_kktRegId_stores_google_id = cur_kktRegId.getString(cur_kktRegId.getColumnIndex("fk_kktRegId_stores_google_id"));
                        invoiceData.kktRegId.google_id = cur_kktRegId.getString(cur_kktRegId.getColumnIndex("google_id"));
                        invoiceData.kktRegId.kktRegId = cur_kktRegId.getLong(cur_kktRegId.getColumnIndex("kktRegId"));
                        invoiceData.kktRegId._status = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("_status"));
                        if(invoiceData.kktRegId.fk_kktRegId_stores > 0 && invoiceData.kktRegId.fk_kktRegId_stores != invoiceData.getfk_invoice_stores())
                            invoiceData.setfk_invoice_stores(invoiceData.kktRegId.fk_kktRegId_stores);
                        cur_kktRegId.close();
                    }
                    Cursor cur_stores = dbHelper.query("stores", null, "id=?", new String[]{invoiceData.getfk_invoice_stores().toString()}, null, null, null, null);
                    if(cur_stores.moveToFirst())
                    {
                        InvoiceData.Store store = new InvoiceData.Store();
                        store.address = cur_stores.getString(cur_stores.getColumnIndex("address"));
                        store.id = cur_stores.getInt(cur_stores.getColumnIndex("id"));
                        store.google_id = cur_stores.getString(cur_stores.getColumnIndex("google_id"));
                        store.name = cur_stores.getString(cur_stores.getColumnIndex("name"));
                        store.name_from_fns = cur_stores.getString(cur_stores.getColumnIndex("name_from_fns"));
                        store.address_from_fns = cur_stores.getString(cur_stores.getColumnIndex("address_from_fns"));
                        store.store_type = cur_stores.getString(cur_stores.getColumnIndex("store_type"));

                        store.longitude = cur_stores.getDouble(cur_stores.getColumnIndex("longitude"));
                        store.latitude = cur_stores.getDouble(cur_stores.getColumnIndex("latitude"));
                        store.inn = cur_stores.getLong(cur_stores.getColumnIndex("inn"));
                        store.place_id = cur_stores.getString(cur_stores.getColumnIndex("place_id"));
                        store.iconName = cur_stores.getString(cur_stores.getColumnIndex("iconName"));
                        store._status = cur_stores.getInt(cur_stores.getColumnIndex("_status"));
                        cur_stores.close();
                        invoiceData.store = store;
                    }
                }

                Cursor cur_linked_objects = dbHelper.query("linked_objects", null, "fk_name = ? and fk_id = ?", new String[]{tableNameInvoice, invoiceData.getId().toString()}, null, null, null, null);
                if(cur_linked_objects.moveToFirst())
                    invoiceData.setPinId(cur_linked_objects.getInt(cur_linked_objects.getColumnIndex("id")));
                invoiceDataTMP.add(invoiceData);
                cur_linked_objects.close();

            }
            while(cur.moveToNext());
            //cur.close();

            Log.d(LOG_TAG, "Loaded from DB records " + invoiceDataTMP.size());
        }
        //cur.close();
        return invoiceDataTMP;
    }

    public void addJsonData(String json, int fk_invoice) {
        Integer id = null;
        Cursor jsonCur = dbHelper.query(tableJsonData, new String[]{"id"}, "fk_" + tableJsonData + "_invoice=?", new String[]{String.valueOf(fk_invoice)}, null, null, null, null);
        if (jsonCur.getCount() > 1)
        {
            dbHelper.delete(tableJsonData, "fk_" + tableJsonData + "_invoice=?", new String[]{String.valueOf(fk_invoice)});
        }
        else if(jsonCur.getCount() == 1)
        {
            jsonCur.moveToFirst();
            id = jsonCur.getInt(jsonCur.getColumnIndex("id"));
        }
        jsonCur.close();
        ContentValues contentValues = new ContentValues();
        contentValues.put("fk_"+tableJsonData+"_invoice", fk_invoice);
        contentValues.put("jsonData", json);
        if(id != null)
            dbHelper.update(tableJsonData, contentValues, "id=?", new String[]{id.toString()});
        else
            dbHelper.insert(tableJsonData, null, contentValues);


        
    }

    public String addInvoice(Integer position, InvoiceData invoiceData, MainActivity.AsyncFirstAddInvoice asyncFirstAddInvoice)
    {

        Log.d(LOG_TAG, "Try to add invoice");
        //just add data from QR - first time to save and check already exist
        String FP = invoiceData.FP;
        String FD= invoiceData.FD;
        String FN = invoiceData.FN;
        Long dateInvoice = Long.parseLong(invoiceData.getDateInvoice(1));
        if(dateInvoice == 0)
        {
            dateInvoice = new Date().getTime();
        }
        long id=0;
        Float fullPrice = invoiceData.getFullPrice();
        if(fullPrice == null)
            fullPrice = (float)0;
        Cursor cur;
        if(position != null)
        {
            id = invoiceData.getId();
            cur = dbHelper.query(tableNameInvoice, null, "id=?",
                    new String[]{id+""}, null, null, null, null);

            if(this.invoices.contains(position)) {
                if (cur.moveToFirst() && this.invoices.get(position).equals(invoiceData)) {
                    cur.close();
                    return "exist";
                }
            }
            else if(cur.moveToFirst())
            {
                if(cur.getInt(cur.getColumnIndex("in_basket")) == 1)
                {
                    Map<String, Object> fStore = new HashMap<>();
                    fStore.put("in_basket", 0);
                    ContentValues data = new ContentValues();
                    data.put("in_basket", 0);
                    dbHelper.update(tableNameInvoice, data, "id=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))});
                    if(On_line && user.google_id != null && invoiceData.google_id != null) {
                        mFirestore.collection(tableNameInvoice).document(invoiceData.google_id).update(fStore);
                    }
                }
                this.invoices.add(position, invoiceData);
                cur.close();
                return "restored";
            }
        }
        else
        {
            Log.d(LOG_TAG, "Try to find invoice");
            cur = dbHelper.query(tableNameInvoice, null, "FP=? and FD=? and FN=?",
            new String[]{FP, FD, FN}, null, null, null, null);

            if(cur.moveToFirst())
            {
                if(cur.getInt(cur.getColumnIndex("in_basket")) == 1)
                {
                    Map<String, Object> fStore = new HashMap<>();
                    fStore.put("in_basket", 0);
                    ContentValues data = new ContentValues();
                    data.put("in_basket", 0);
                    dbHelper.update(tableNameInvoice, data, "id=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))});
                    if(On_line && user.google_id != null && invoiceData.google_id != null) {
                        mFirestore.collection(tableNameInvoice).document(invoiceData.google_id).update(fStore);
                    }

                }
                cur.close();
                boolean notPresent = false;
                for(int i=0; i<invoices.size(); i++) {
                    invoiceData = invoices.get(i);
                    if(invoiceData.FD.equals(FD) && invoiceData.FP.equals(FP)&& invoiceData.FN.equals(FN))
                    {
                        notPresent = true;
                        this.lastIDCollection = i;
                        return "exist";
                    }
                }
                if(notPresent)
                {
                    return "reloaded";
                    /*
                    invoiceData = new InvoiceData();
                    id = cur.getInt(cur.getColumnIndex("id"));
                    Integer order = cur.getInt(cur.getColumnIndex("_order"));
                    Integer fk_invoice_accountinglist=cur.getInt(cur.getColumnIndex("fk_invoice_accountinglist"));
                    invoiceData.setAll(FP, FD, FN, dateInvoice, fullPrice+"", (int)id, order, fk_invoice_accountinglist, null);
                    this.invoices.add(invoiceData);
                    this.lastIDCollection= this.invoices.size()-1;

                    //MainActivity.invoiceListAdapter.notifyItemInserted(this.lastIDCollection);
                    */
                }
                return "exist";
            }
        }
        addInvoiceDataLocal(position, invoiceData);
        //if(On_line && user.google_id != null)
        //   addInvoiceDataServer(invoiceData);
        /*
        Map<String, Object> fStore = new HashMap<>();
        ContentValues data = new ContentValues();
        data.put("FP", FP);
        data.put("FD", FD);
        data.put("FN", FN);
        data.put("_status", 0);
        data.put("in_basket", 0);
        data.put("dateInvoice", dateInvoice);
        data.put("date_day", invoiceData.date_day);
        data.put("fullPrice", fullPrice);
        data.put("date_add", new Date().getTime());

        fStore.put("FP", FP);
        fStore.put("FD", FD);
        fStore.put("FN", FN);
        fStore.put("_status", 0);
        fStore.put("in_basket", 0);
        fStore.put("dateInvoice", dateInvoice);
        fStore.put("date_day", invoiceData.date_day);
        fStore.put("fullPrice", fullPrice);
        fStore.put("date_add", data.get("date_add"));


        if(invoiceData.longitudeAdd != null && invoiceData.latitudeAdd != null)
        {
            data.put("longitudeAdd", invoiceData.longitudeAdd);
            data.put("latitudeAdd", invoiceData.latitudeAdd);

            fStore.put("longitudeAdd", invoiceData.longitudeAdd);
            fStore.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if(checkFilter("fk_invoice_accountinglist", null)) {
            data.put("fk_invoice_accountinglist", filterParam.get("fk_invoice_accountinglist")[0]);
        }
        if(position!=null)
        {
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());
            data.put("_order", invoiceData.get_order());

            fStore.put("_order", invoiceData.get_order());
        }
        id = dbHelper.insert(tableNameInvoice, null, data);
        if(id>-1)
        {
            if(On_line && user.google_id != null && invoiceData.google_id == null)
            {
                fStore.put("user_google_id", user.google_id);
                //пераое сохранение данных
                DocumentReference addedDocRef =  mFirestore.collection(tableNameInvoice).document();
                addedDocRef.set(fStore);
                invoiceData.google_id = addedDocRef.getId();
                ContentValues val = new ContentValues();
                val.put("google_id", invoiceData.google_id);
                dbHelper.update(tableNameInvoice, val, "id=?", new String[]{String.valueOf(id)});
            }

            //invoiceData = new InvoiceData();
            invoiceData.setAll(FP, FD, FN, dateInvoice, String.valueOf(fullPrice), (int)id, (int)id, null, null);
            if(position != null) {
                this.invoices.add(position, invoiceData);
                this.lastIDCollection = position;
            }
            else
            {
                invoiceData.setId((int) id);
                this.invoices.add(invoiceData);
                this.updateInvoice(invoiceData);
                this.lastIDCollection =   this.invoices.size()-1;
            }

            Log.d(LOG_TAG, "Inserted record id: " + id);
            return "";

        }
        else
        {
            Log.d(LOG_TAG, "Inserted ERROR ");
        }
        */
        return "";
    }

    public void  addInvoiceDataLocal(Integer position, InvoiceData invoiceData)
    {
        Long dateInvoice = Long.parseLong(invoiceData.getDateInvoice(1));
        if(dateInvoice == 0)
        {
            dateInvoice = new Date().getTime();
        }

        ContentValues data = new ContentValues();
        data.put("FP", invoiceData.FP);
        data.put("FD", invoiceData.FD);
        data.put("FN", invoiceData.FN);
        if(invoiceData.get_status()!= null)
            data.put("_status",invoiceData.get_status());
        else
           data.put("_status", 0);
        data.put("in_basket", 0);
        data.put("dateInvoice", dateInvoice);
        data.put("date_day", invoiceData.date_day);
        data.put("fullPrice", invoiceData.getFullPrice());

        if(invoiceData.getDate_add() != null)
            data.put("date_add", invoiceData.getDate_add());
        else
            data.put("date_add", new Date().getTime());

        if(invoiceData.server_status!= null) {
            data.put("server_status", invoiceData.server_status);
        }
        else {
            data.put("server_status", 0);
        }

        if(user.google_id!= null)
            data.put("user_google_id", user.google_id);

        invoiceData.setDate_add((long)data.get("date_add"));
        //invoiceData.setDateInvoice(dateInvoice);

        if(invoiceData.longitudeAdd != null && invoiceData.latitudeAdd != null)
        {
            data.put("longitudeAdd", invoiceData.longitudeAdd);
            data.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if(checkFilter("fk_invoice_accountinglist", null)) {
            data.put("fk_invoice_accountinglist", filterParam.get("fk_invoice_accountinglist")[0]);
        }
        if(invoiceData.getFk_invoice_accountinglist()!=null) {
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());
        }
        if(invoiceData.get_order()!= null)
        {
            data.put("_order", invoiceData.get_order());
        }
        long id = dbHelper.insert(tableNameInvoice, null, data);
        if(id>-1)
        {
            invoiceData.setId((int) id);
            if(position != null) {
                this.invoices.add(position, invoiceData);
                this.lastIDCollection = position;
            }
            else
            {
                invoiceData.setId((int) id);
                this.invoices.add(invoiceData);
                this.updateInvoice(invoiceData);
                this.lastIDCollection =   this.invoices.size()-1;
            }

            Log.d(LOG_TAG, "Inserted record id: " + id);

        }
        else
        {
            Log.d(LOG_TAG, "Inserted ERROR ");
        }

    }

    public void  addInvoiceDataServer(InvoiceData invoiceData)
    {
        Long dateInvoice = Long.parseLong(invoiceData.getDateInvoice(1));
        if(dateInvoice == 0)
        {
            dateInvoice = new Date().getTime();
        }

        Map<String, Object> fStore = new HashMap<>();

        fStore.put("FP", invoiceData.FP);
        fStore.put("FD", invoiceData.FD);
        fStore.put("FN", invoiceData.FN);
        if(invoiceData.server_status!= null) {
            fStore.put("server_status", invoiceData.server_status);
        }
        else {
            fStore.put("server_status", 0);
        }
        fStore.put("in_basket", 0);
        fStore.put("dateInvoice", dateInvoice);
        fStore.put("date_day", invoiceData.date_day);
        fStore.put("fullPrice", invoiceData.getFullPrice());
        fStore.put("date_add", invoiceData.getDate_add());


        if(invoiceData.store.google_id != null)
            fStore.put("fk_invoice_stores_google_id", invoiceData.store.google_id);

        if(invoiceData.kktRegId.google_id != null)
            fStore.put("fk_invoice_kktRegId_google_id", invoiceData.kktRegId.google_id);

        if(invoiceData.longitudeAdd != null && invoiceData.latitudeAdd != null)
        {
            fStore.put("longitudeAdd", invoiceData.longitudeAdd);
            fStore.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if(invoiceData.get_order()!= null)
        {
            fStore.put("_order", invoiceData.get_order());
        }

        fStore.put("user_google_id", user.google_id);
        Task<QuerySnapshot> result = mFirestore.collection(tableNameInvoice).whereEqualTo("FP_FD_FN", invoiceData.FP+"_"+invoiceData.FD+"_"+invoiceData.FN).get(source);
        while(!result.isComplete())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(result.isSuccessful()) {
            QuerySnapshot querySnapshot = result.getResult();
            Log.d(LOG_TAG, "\n" + querySnapshot.getMetadata().toString());
            if (!result.getResult().getMetadata().isFromCache()) {
                List<DocumentSnapshot> documents = result.getResult().getDocuments();
                if (documents.size() > 0) {
                    invoiceData.google_id = documents.get(0).getId();
                    if(documents.get(0).contains("server_status"))
                        invoiceData.server_status = ((Long) documents.get(0).get("server_status")).intValue();
                } else {
                    //первое сохранение данных
                    fStore.put("FP_FD_FN",invoiceData.FP+"_"+invoiceData.FD+"_"+invoiceData.FN);
                    DocumentReference addedDocRef = mFirestore.collection(tableNameInvoice).document();
                    addedDocRef.set(fStore);
                    invoiceData.google_id = addedDocRef.getId();

                    //invoiceData.google_id = mFirebase.child(tableNameInvoice).push().getKey();

                    //mFirebase.child(tableNameInvoice).child(invoiceData.google_id).setValue(fStore);
                }
                ContentValues val = new ContentValues();
                val.put("google_id", invoiceData.google_id);
                dbHelper.update(tableNameInvoice, val, "id=?", new String[]{String.valueOf(invoiceData.getId())});
            }
        }
    }

    public boolean writeInvoiceDataFromServer(InvoiceData invoiceData)
    {

        if (invoiceData.fk_invoice_stores_google_id != null) {
            Task<DocumentSnapshot> result_store = mFirestore.collection(tablenameStores).document(invoiceData.fk_invoice_stores_google_id).get(source);
            while (!result_store.isComplete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (result_store.isSuccessful()) {
                InvoiceData.Store store = result_store.getResult().toObject(InvoiceData.Store.class);
                if (store != null) {
                    invoiceData.store = store;
                    invoiceData.store.google_id = result_store.getResult().getId();
                }
            }
        }
        if (invoiceData.fk_invoice_kktRegId_google_id != null) {
            Task<DocumentSnapshot> result_kkt = mFirestore.collection(tableNameKktRegId).document(invoiceData.fk_invoice_kktRegId_google_id).get(source);
            while (!result_kkt.isComplete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (result_kkt.isSuccessful()) {
                InvoiceData.KktRegId kkt = result_kkt.getResult().toObject(InvoiceData.KktRegId.class);
                if (kkt != null) {
                    invoiceData.kktRegId = kkt;
                    invoiceData.kktRegId.google_id = result_kkt.getResult().getId();
                }
            }
        }

        if (invoiceData.kktRegId != null && invoiceData.kktRegId.google_id != null) {
            Cursor cur_kktRegId = dbHelper.query(tableNameKktRegId, null, "kktRegId=?", new String[]{invoiceData.kktRegId.kktRegId.toString()}, null, null, null, null);
            if (cur_kktRegId.moveToFirst()) {
                invoiceData.kktRegId.fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                invoiceData.kktRegId.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));

                cur_kktRegId.close();

                Cursor cur_store = dbHelper.query(tablenameStores, null, "id=?", new String[]{invoiceData.kktRegId.fk_kktRegId_stores.toString()}, null, null, null, null);
                if (cur_store.moveToFirst()) {
                    invoiceData.store.id = cur_store.getInt(cur_store.getColumnIndex("id"));
                    int _status = cur_store.getInt(cur_store.getColumnIndex("_status"));
                    cur_store.close();
                    if (invoiceData.store._status != _status && invoiceData.store._status == 1) {
                        invoiceData.store.update = true;
                        saveStoreDataLocal(invoiceData.store);
                    }
                    //сравнить данные у пользователя и на сервере. обновить если статус магазина на сервера "подтвержден администарцией"
                    //магазин может быть пустой так как добавлена только геометка
                } else if (invoiceData.store != null) {
                    //сохраняем магазин в локальной базе
                    invoiceData.store.update = false;
                    saveStoreDataLocal(invoiceData.store);
                }
            } else if (invoiceData.store != null) {
                invoiceData.store.update = false;
                saveStoreDataLocal(invoiceData.store);
                try {
                    saveKktRegId(invoiceData.kktRegId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (invoiceData.store != null && invoiceData.store.google_id != null) {
            String selection = "google_id=?";
            String[] args = new String[]{invoiceData.store.google_id.toString()};
            if (invoiceData.store.place_id != null) {
                selection += "OR place_id=?";
                args = new String[]{invoiceData.store.google_id.toString(), invoiceData.store.place_id};
            }
            Cursor cur_store = dbHelper.query(tablenameStores, null, selection, args, null, null, null, null);
            if (cur_store.moveToFirst()) {
                invoiceData.store.id = cur_store.getInt(cur_store.getColumnIndex("id"));
                int _status = cur_store.getInt(cur_store.getColumnIndex("_status"));
                cur_store.close();
                if (invoiceData.store._status != _status && invoiceData.store._status == 1) {
                    invoiceData.store.update = true;
                    saveStoreDataLocal(invoiceData.store);
                }
            } else {
                invoiceData.store.update = false;
                saveStoreDataLocal(invoiceData.store);
            }
        }
        //загрузить покупки с севера
        Task<QuerySnapshot> result_purchases = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_invoice_google_id", invoiceData.google_id).get(source);
        while (!result_purchases.isComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (result_purchases.isSuccessful()) {
            List<DocumentSnapshot> documents_purchases = result_purchases.getResult().getDocuments();
            for (DocumentSnapshot documentSnapshot : documents_purchases) {
                PurchasesListData purchasesListData = documentSnapshot.toObject(PurchasesListData.class);
                if (purchasesListData != null) {
                    Integer fk_purchases_products = null;
                    //проеряем наличем продукта в локальной базе и загружаем его
                    if (purchasesListData.fk_purchases_products_google_id != null) {

                        Task<DocumentSnapshot> result_product = mFirestore.collection("products").document(purchasesListData.fk_purchases_products_google_id).get(source);
                        while (!result_product.isComplete()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (result_product.isSuccessful()) {
                            PurchasesListData.Product product = result_product.getResult().toObject(PurchasesListData.Product.class);
                            if(product != null) {
                                String fk_purchases_products_google_id;
                                Cursor cur_products = dbHelper.query("Products", null, "nameFromBill=?",
                                        new String[]{product.nameFromBill}, null, null, null, null);
                                if (cur_products.moveToFirst()) {
                                    fk_purchases_products = cur_products.getInt(cur_products.getColumnIndex("id"));
                                    fk_purchases_products_google_id = cur_products.getString(cur_products.getColumnIndex("google_id"));
                                    cur_products.close();
                                } else {
                                    ContentValues values = new ContentValues();
                                    values.put("nameFromBill", product.nameFromBill);
                                    values.put("google_id", result_product.getResult().getId());
                                    fk_purchases_products = ((int) dbHelper.insert("Products", null, values));
                                }
                            }
                        }
                    }
                    if (fk_purchases_products != null) {
                        ContentValues values = new ContentValues();
                        try {
                            //add in table purchases

                            values.put("fk_purchases_stores", invoiceData.store.id);
                            values.put("fk_purchases_stores_google_id", invoiceData.store.google_id);
                            values.put("fk_purchases_products_google_id", purchasesListData.fk_purchases_products_google_id);
                            values.put("fk_purchases_invoice_google_id", invoiceData.google_id);
                            values.put("fk_purchases_products", fk_purchases_products);
                            values.put("fk_purchases_invoice", invoiceData.getId());
                            if (checkFilter("fk_invoice_accountinglist", null)) {
                                values.put("fk_purchases_accountinglist", filterParam.get("fk_purchases_accountinglist")[0]);
                            }
                            values.put("prise_for_item", purchasesListData.prise_for_item);
                            values.put("quantity", purchasesListData.quantity);
                            values.put("sum", purchasesListData.sum);
                            values.put("date_add", purchasesListData.date_add);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            values.put("prise_for_item", purchasesListData.prise_for_item);
                            values.put("quantity", purchasesListData.quantity);
                            values.put("sum", purchasesListData.sum);
                            values.put("date_add", purchasesListData.date_add);
                        } finally {
                            dbHelper.insert(tableNamePurchases, null, values);
                        }
                    }
                }
            }
        }

        if (invoiceData.store != null && invoiceData.store.google_id != null && invoiceData.kktRegId != null && invoiceData.kktRegId.google_id != null
                && !result_purchases.getResult().isEmpty()) {
            invoiceData.set_status(1);
            updateInvoice(invoiceData);
            return true;
        } else
            return false;
    }

    public boolean writeInvoiceDataToServer(InvoiceData invoiceData)
    {

        boolean loadKktToServer = false;
        boolean loadKktStoreServer = false;
                //проверяем есть ли на сервере касса если она есть локально
        if (invoiceData.getfk_invoice_kktRegId() != null) {
            //если ссылка на кассу есть а данные еще не загружены - получаем из локальной базы данные
            if(invoiceData.kktRegId == null || invoiceData.kktRegId.kktRegId == null)
            {
                Cursor cur_kktRegId = dbHelper.query(tableNameKktRegId, null, "id=?", new String[]{invoiceData.getfk_invoice_kktRegId().toString()}, null, null, null, null);
                if (cur_kktRegId.moveToFirst()) {
                    invoiceData.kktRegId.kktRegId = (long) cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("kktRegId"));
                    invoiceData.kktRegId.google_id = cur_kktRegId.getString(cur_kktRegId.getColumnIndex("google_id"));
                    invoiceData.kktRegId._status = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("_status"));
                    invoiceData.kktRegId.fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                    invoiceData.kktRegId.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));
                }
                cur_kktRegId.close();
            }
            //запрос на сервер в поисках кассы только если ранее она не был сохранена
            InvoiceData.KktRegId kktRegId = null;
            if(invoiceData.kktRegId != null && invoiceData.kktRegId.kktRegId != null)
            {
                Task<QuerySnapshot> result_kkt = mFirestore.collection(tableNameKktRegId).whereEqualTo("kktRegId", invoiceData.kktRegId.kktRegId).get(source);
                while (!result_kkt.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result_kkt.isSuccessful()) {

                    List<DocumentSnapshot> kkt = result_kkt.getResult().getDocuments();
                    if(kkt.size()==1) {
                        kktRegId = kkt.get(0).toObject(InvoiceData.KktRegId.class);
                        if(kktRegId != null) {
                            kktRegId.google_id = kkt.get(0).getId();

                            if (invoiceData.kktRegId.google_id == null ||  !invoiceData.kktRegId.google_id.equals(kktRegId.google_id)) {
                                //ссылка локальная устарела,  нужно обновить локальную ссылку
                                //обновляем локальные данные
                                invoiceData.kktRegId.google_id = kktRegId.google_id;
                                try {
                                    saveKktRegIdLocal(invoiceData.kktRegId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            //ссылка локальная существует на сервере и соответствует локальной версии
                            //нигде обновлять не надо
                        }
                    }
                    //если кассовый аппарат не найден на сервере
                    else if(kkt.size() == 0)
                    {
                        invoiceData.kktRegId.google_id = null;
                        loadKktToServer = true;
                    }
                }
            }



            if (kktRegId!= null) {
                //данные по кассе найдены - необходимо сверить локальную версию и версию на сервере
                // если статус локально != 1 а на сервере == 1 то надо обновить локальную версию.
                //иначе - версию на сервере
                if (kktRegId._status != null && kktRegId._status == 1 && invoiceData.kktRegId._status != 1) {
                    //касса на сервере подтверждена, значит есть подтвержденный магазин на сервере
                    //необходимо сверить ссылки на магазины, но на сервере ссылка - google_id  магазин сейчас хранится только локально.
                    //если касса не подтверждена локально то и магазин тоже - его локально можно смело затирать данными с сервера
                    //нужно загрузить данные по магазину и обновить информацию локально по кассе и магазину
                    if(kktRegId.fk_kktRegId_stores_google_id!=null && !invoiceData.kktRegId.fk_kktRegId_stores_google_id.equals(kktRegId.fk_kktRegId_stores_google_id))
                    {
                        Task<DocumentSnapshot> result_store = mFirestore.collection(tablenameStores).document(kktRegId.fk_kktRegId_stores_google_id).get(source);
                        while (!result_store.isComplete()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(result_store.isSuccessful())
                        {
                            InvoiceData.Store store = result_store.getResult().toObject(InvoiceData.Store.class);

                            if(store != null)
                            {
                                store.google_id = result_store.getResult().getId();
                                //если магазин найден - обновляем информацию локально по кассе и по магазину
                                store.id = invoiceData.kktRegId.fk_kktRegId_stores;
                                kktRegId.id = invoiceData.kktRegId.id;
                                kktRegId.fk_kktRegId_stores = store.id;
                                invoiceData.kktRegId = kktRegId;
                                invoiceData.store = store;
                                saveStoreDataLocal(invoiceData.store);
                                try {
                                    saveKktRegIdLocal(invoiceData.kktRegId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else if(kktRegId._status != null && kktRegId._status != 1 && invoiceData.kktRegId._status == 1)
                {
                    loadKktToServer = true;
                }
            }
            //если данные на сервере не найдены вообще
            else
            {

                //загружаем данные на сервер
                if(invoiceData.kktRegId!= null)
                {invoiceData.kktRegId.google_id = null;
                    loadKktToServer = true;
                    /*
                    try {
                        saveKktRegIdServer(invoiceData.kktRegId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            }
        }
        //если  есть ссылка на магазин
        if(invoiceData.getfk_invoice_stores()!= null)
        {
            if(invoiceData.store == null)
            {
                Map<String, String> get = new HashMap<>();
                get.put("id", invoiceData.getfk_invoice_stores().toString());
                List<InvoiceData.Store> stores = loadDataFromStore(get);
                if(stores.size() == 1)
                {
                    //получили магазин из локальной базы
                    invoiceData.store = stores.get(0);
                }

            }

            if(invoiceData.store.place_id!=null)
            {
                Task<QuerySnapshot> result_store = mFirestore.collection(tablenameStores).whereEqualTo("place_id", invoiceData.store.place_id).get(source);
                while (!result_store.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(result_store.isSuccessful())
                {
                    List<DocumentSnapshot> stores = result_store.getResult().getDocuments();
                    if(stores.size()==1)
                    {
                        InvoiceData.Store store = stores.get(0).toObject(InvoiceData.Store.class);
                        //если версия на сервере имеет данные по магазину из ФНС то надо обновлять локальную версию
                        //т.к. если в коде дошли до сюда значит в данном чеке информация из ФНС отсутствует.
                        //до этого пытались найти инфу по
                        if(store.inn != null)
                        {
                            store.id = invoiceData.store.id;
                            store.update = true;
                            invoiceData.store = store;
                            saveStoreDataLocal(invoiceData.store);
                        }
                    }
                    else
                    {
                        invoiceData.kktRegId.google_id = null;
                        loadKktStoreServer = true;
                        //если на сервере такого магазина нет то загружем данные из локальной базы данных
                        //saveStoreDataServer(invoiceData.store);
                    }

                }
                //есть возможность проверить магазин только по метке
                //если магазина с такой меткой нет на сервере то найти там именно этот локальный магазин невозможно поэтому если метки нет и в локальном магазине нет ссылки google_id
                //то просто загрузим магазин на сервер и обновим локальные данные
                //если ссылка есть то проверим наличие магазина на сервере и если его нет то загрузим данные на сервер а если есть то обновим локальные данные
            }
            else if(invoiceData.store.google_id != null)
            {
                Task<DocumentSnapshot> result_store = mFirestore.collection(tablenameStores).document(invoiceData.fk_invoice_stores_google_id).get(source);
                while (!result_store.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result_store.isSuccessful() && result_store.getResult().exists()) {
                    InvoiceData.Store store = result_store.getResult().toObject(InvoiceData.Store.class);
                    if (store != null) {
                        store.google_id = result_store.getResult().getId();
                        //если магазин есть на сервере и на сервере статус 1 а локально нет - обновляем локально
                        if(invoiceData.store._status != store._status && store._status == 1)
                        {
                            store.id = invoiceData.store.id;
                            saveStoreDataLocal(store);
                        }
                        else if(invoiceData.store._status == 1)
                        {
                            loadKktStoreServer = true;
                            //saveStoreDataServer(invoiceData.store);
                        }
                    }
                    else
                    {
                    }
                }
                else if(!result_store.getResult().exists())
                {
                    //если магазин по ссылке не найден, ранее он не был найден по place_id
                    //сохраняем данные на сервере.
                    //saveStoreDataServer(invoiceData.store);
                    loadKktStoreServer = true;
                }
            }
        }

        if(loadKktStoreServer) {
            invoiceData.store.google_id = null;
            saveStoreDataServer(invoiceData.store);
        }

        if(loadKktToServer) {
            try {
                //invoiceData.kktRegId.google_id = null;
                invoiceData.kktRegId.fk_kktRegId_stores_google_id = invoiceData.store.google_id;
                saveKktRegIdServer(invoiceData.kktRegId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        //загружаем данные по чеку на сервер
        addInvoiceDataServer(invoiceData);

        //старт проверки списка покупок и загрузки на сервер

        Cursor cur_purchases = dbHelper.query(tableNamePurchases, null, "fk_purchases_invoice=?", new String[]{invoiceData.getId().toString()}, null, null, null, null);
        if(cur_purchases.moveToFirst())
        {
            do{

                PurchasesListData purchaseLocal = new PurchasesListData();
                purchaseLocal.product = new PurchasesListData.Product();

                //собираем объект покукпи из локальных данный - не плохо было бы вынести в функцию

                purchaseLocal.id = cur_purchases.getInt(cur_purchases.getColumnIndex("id"));
                purchaseLocal.prise_for_item = cur_purchases.getFloat(cur_purchases.getColumnIndex("prise_for_item"));
                purchaseLocal.quantity = cur_purchases.getFloat(cur_purchases.getColumnIndex("quantity"));
                purchaseLocal.sum = cur_purchases.getFloat(cur_purchases.getColumnIndex("sum"));
                purchaseLocal.date_add = cur_purchases.getLong(cur_purchases.getColumnIndex("date_add"));

                purchaseLocal.fk_purchases_accountinglist = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_accountinglist"));
                purchaseLocal.fk_purchases_invoice = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_invoice"));
                purchaseLocal.fk_purchases_stores = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_stores"));
                purchaseLocal.fk_purchases_products = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_products"));

                purchaseLocal.google_id = cur_purchases.getString(cur_purchases.getColumnIndex("google_id"));
                purchaseLocal.fk_purchases_invoice_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_invoice_google_id"));
                purchaseLocal.fk_purchases_products_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_products_google_id"));
                purchaseLocal.fk_purchases_stores_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_stores_google_id"));


                Integer fk_purchases_products = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_products"));


                //проверяем, что если выше мы загрузили на сервер магазин и кассу и чек, что локально хранится верная инфа, иначе обновляем
                //проверять после проверки наличия продукта на сервере
                if(invoiceData.google_id!= null && !invoiceData.google_id.equals(purchaseLocal.fk_purchases_invoice_google_id)
                        ||  invoiceData.store.google_id!= null && !invoiceData.store.google_id.equals(purchaseLocal.fk_purchases_stores_google_id))
                {
                    //обновляем запись в таблице локально
                    //обновляем ссылку на чек и магазин в товаре из чека
                    purchaseLocal.fk_purchases_stores_google_id = invoiceData.store.google_id;
                    purchaseLocal.fk_purchases_invoice_google_id = invoiceData.google_id;
                }

                //собираем продукт из данных в локальной базе
                Cursor cur_product = dbHelper.query(tableNameProducts, null, "id=?", new String[]{fk_purchases_products.toString()}, null, null, null, null);
                if (cur_product.moveToFirst()) {
                    purchaseLocal.product.google_id = cur_product.getString(cur_product.getColumnIndex("google_id"));
                    purchaseLocal.product.nameFromBill = cur_product.getString(cur_product.getColumnIndex("nameFromBill"));
                    purchaseLocal.product.id = cur_product.getInt(cur_product.getColumnIndex("id"));
                    cur_product.close();
                }

                if(purchaseLocal.google_id!= null && purchaseLocal.google_id.length()>0)
                {
                    //если у покупки есть ссылка на гугл - проверяем  ее наличие на сервере - если на сервере нет загружаем на него
                    //если есть ссылка на гугл у локальной покупки то должна быть и ссылка на чек и на продукт
                    //проверять все покупки по чеку на сервере или индивидуально каждую покупку?
                    //в конце обязательно сверить количество
                    Task<DocumentSnapshot> result_purchases = mFirestore.collection(tableNamePurchases).document(purchaseLocal.google_id).get(source);
                    while (!result_purchases.isComplete()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(result_purchases.isSuccessful())
                    {
                        if(result_purchases.getResult().exists())
                        {
                            //найдена покупка из чека на сервере
                            PurchasesListData purchase = result_purchases.getResult().toObject(PurchasesListData.class);
                            if (purchase != null)
                            {
                                if (purchase.fk_purchases_products_google_id != null )
                                {
                                    //если есть ссылка на продукт пытаемся загрузить его с сервера
                                    if(!purchase.fk_purchases_products_google_id.equals(purchaseLocal.fk_purchases_products_google_id))
                                    {
                                        purchaseLocal.fk_purchases_products_google_id = purchase.fk_purchases_products_google_id;
                                    }

                                    Task<DocumentSnapshot> result_Products = mFirestore.collection(tableNameProducts).document(purchase.fk_purchases_products_google_id).get(source);
                                    while (!result_Products.isComplete()) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (result_Products.isSuccessful())
                                    {
                                        if(result_Products.getResult().exists())
                                        {
                                            PurchasesListData.Product product = result_Products.getResult().toObject(PurchasesListData.Product.class);
                                            if (product != null) {
                                                //если в локальной базе по ссылке есть запись то идем дальше
                                                //иначе надо просто сохранить локально новый продукт и обновить ссылку? вопрос? как появилась ссылка на продукт без продукта?
                                                //на случай ошибки стоит проверять
                                                if (purchaseLocal.product.google_id.equals(purchase.fk_purchases_products_google_id)) {
                                                    //если ссылки совпадают проверяем совпадение названий и бар кода
                                                    if (!purchaseLocal.product.nameFromBill.equals(product.nameFromBill)) {
                                                        //если не совпадают ищем на сервере товар и обновляем данные локально или добавляем на сервер новый продукт
                                                        checkProductExistOnServerAndSave(purchaseLocal.product);

                                                    }
                                                }
                                                //если ссылки не совпадают, но одинаоквые названия товаров - обновляем локальные данные
                                                else if (purchaseLocal.product.nameFromBill.equals(product.nameFromBill)) {
                                                    purchaseLocal.product.google_id = purchase.fk_purchases_products_google_id;
                                                    insertProductDataLocal(purchaseLocal.product);
                                                }
                                                //если ссылки не совпали и названия разные, то ищем на сервере продукт из локальной базы и пытаемся его добавить
                                                else {
                                                    checkProductExistOnServerAndSave(purchaseLocal.product);
                                                }

                                            }
                                        }
                                        else
                                        {
                                            //если по ссылке в покупке на сервере не найден продукт то необходимо проверить есть ли продукт на сервере по названию из чека и если надо загрузить
                                            //загружаем из локальной базы данные по продукту
                                            purchaseLocal.product = getProductLocal(fk_purchases_products);
                                            if (purchaseLocal.product.id != null) {
                                                //если данные есть
                                                //проверяем их на сервере
                                                checkProductExistOnServerAndSave(purchaseLocal.product);
                                            }
                                            else
                                            {
                                                //если данных по продукту локально нет и нет на сервере - обнулем ссылки для обновления локальных данных и данных на сервере
                                                purchaseLocal.product.id=0;
                                                purchaseLocal.fk_purchases_products_google_id="";
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        else
                        {
                            //загружаем данные по покупке на сервер
                            //полностью собрать обЪект и загрузить
                            //загрузить продукт на сервер
                            purchaseLocal.fk_purchases_products = fk_purchases_products;
                            purchaseLocal.product.google_id = null;
                            loadPurchaseDataToServer(purchaseLocal);
                        }

                    }
                }
                else
                {
                    //загружаем данные по покупке на сервер
                    //полностью собрать обЪект и загрузить
                    loadPurchaseDataToServer(purchaseLocal);
                }
            }
            while(cur_purchases.moveToNext());
            cur_purchases.close();
        }
        return false;
    }

    private void loadPurchaseDataToServer(PurchasesListData purchaseLocal) {

        ContentValues contentValues = new ContentValues();
        Map<String, Object> toServer = new HashMap<>();

        if(purchaseLocal.prise_for_item != null)
        {
            toServer.put("prise_for_item", purchaseLocal.prise_for_item);
        }
        if(purchaseLocal.date_add != null)
        {
            toServer.put("date_add", purchaseLocal.date_add);
        }
        if(purchaseLocal.sum!= null)
        {
            toServer.put("sum", purchaseLocal.sum);
        }
        if(purchaseLocal.quantity!= null)
        {
            toServer.put("quantity", purchaseLocal.quantity);
        }




        if(purchaseLocal.fk_purchases_products != null && purchaseLocal.fk_purchases_products >0)
        {
            if(purchaseLocal.product == null)
                purchaseLocal.product = getProductLocal(purchaseLocal.fk_purchases_products);
            if(purchaseLocal.product.id != null)
            {
                insertProductDataServer(purchaseLocal.product);
                if(purchaseLocal.product.google_id != null) {
                    purchaseLocal.fk_purchases_products_google_id = purchaseLocal.product.google_id;

                    toServer.put("fk_purchases_products_google_id", purchaseLocal.fk_purchases_products_google_id);

                    purchaseLocal.google_id = mFirestore.collection(tableNamePurchases).document().getId();
                    mFirestore.collection(tableNamePurchases).document(purchaseLocal.google_id).set(toServer);
                    if(purchaseLocal.google_id !=null)
                    {
                        contentValues.put("google_id", purchaseLocal.google_id);
                    }

                    if(purchaseLocal.id != null)
                    {
                        dbHelper.update(tableNamePurchases, contentValues, "id=?", new String[]{purchaseLocal.id.toString()});
                    }
                }
            }
        }
    }

    private PurchasesListData.Product getProductLocal(int id)
    {
        PurchasesListData.Product product = new PurchasesListData.Product();
        Cursor cur = dbHelper.query(tableNameProducts, null, "id=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if(cur.moveToFirst())
        {
            product.google_id = cur.getString(cur.getColumnIndex("google_id"));
            product.nameFromBill = cur.getString(cur.getColumnIndex("nameFromBill"));
            product.id = cur.getInt(cur.getColumnIndex("id"));
            cur.close();
        }

        return product;
    }

    private void checkProductExistOnServerAndSave(PurchasesListData.Product product) {

        //проверяем что продукта еще нет на сервере
        Task<QuerySnapshot> result_Products_new = mFirestore.collection(tableNameProducts).whereEqualTo("nameFromBill", product.nameFromBill).get(source);
        while (!result_Products_new.isComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (result_Products_new.isSuccessful()) {
            if (!result_Products_new.getResult().isEmpty()) {
                //обновляем данные локально по существубщей покупке
                PurchasesListData.Product product_tmp = result_Products_new.getResult().getDocuments().get(0).toObject(PurchasesListData.Product.class);
                if (product_tmp != null) {
                    product_tmp.id = product.id;
                    product_tmp.google_id = result_Products_new.getResult().getDocuments().get(0).getId();
                    insertProductDataLocal(product_tmp);
                }
            } else {
                //обновляем данные на сервере
                PurchasesListData.Product product_tmp = new PurchasesListData.Product();
                product_tmp.id = product.id;
                product_tmp.nameFromBill = product.nameFromBill;
                insertProductDataServer(product_tmp);
            }
        }
    }

    private long insertProductDataLocal(PurchasesListData.Product product_tmp) {
        ContentValues contentValues = new ContentValues();
        if(product_tmp.correctName != null)
            contentValues.put("correctName", product_tmp.correctName);

        if(product_tmp.nameFromBill != null)
            contentValues.put("nameFromBill", product_tmp.nameFromBill);

        if(product_tmp.barcode != null)
            contentValues.put("barcode", product_tmp.barcode);

        if(product_tmp.id != null)
        {
            return dbHelper.update(tableNameProducts, contentValues, "id=?", new String[]{product_tmp.id.toString()});
        }
        else
        {
            return dbHelper.insert(tableNameProducts, null, contentValues);
        }


    }

    private void insertProductDataServer(PurchasesListData.Product product_tmp) {
        Map<String, Object> contentValues = new HashMap<>();
        if(product_tmp.correctName != null)
            contentValues.put("correctName", product_tmp.correctName);

        if(product_tmp.nameFromBill != null)
            contentValues.put("nameFromBill", product_tmp.nameFromBill);

        if(product_tmp.barcode != null)
            contentValues.put("barcode", product_tmp.barcode);

        if(product_tmp.google_id != null)
        {
            mFirestore.collection(tableNameProducts).document(product_tmp.google_id).update(contentValues);
        }
        else
        {
            product_tmp.google_id = mFirestore.collection(tableNameProducts).document().getId();
            mFirestore.collection(tableNameProducts).document(product_tmp.google_id).set(contentValues);

            if(product_tmp.id!= null && product_tmp.google_id != null)
            {
                ContentValues db = new ContentValues();
                db.put("google_id", product_tmp.google_id);
                dbHelper.update(tableNameProducts, db, "id=?", new String[]{product_tmp.id.toString()});
            }
        }




    }

    public boolean getInvoiceFromServer(InvoiceData invoiceData, LoadingFromFNS.AsyncLoadDataInvoice asyncFirstAddInvoice) throws Exception {
        if(invoiceData != null) {
            InvoiceData tmp = (InvoiceData) invoiceData.clone();
            if(On_line && user.google_id != null)
            {
                Task<QuerySnapshot> result = null;
                Task<DocumentSnapshot> doc = null;
                result = mFirestore.collection(tableNameInvoice).whereEqualTo("FP_FD_FN", invoiceData.FP+"_"+invoiceData.FD+"_"+invoiceData.FN).get(source);

                while(!result.isComplete())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                QuerySnapshot querySnapshot = result.getResult();

                if(!querySnapshot.getMetadata().isFromCache())
                {
                    //Log.d(LOG_TAG, result.getException().toString());
                    List<DocumentSnapshot> documents = result.getResult().getDocuments();
                    if (documents.size() == 1)
                    {

                        invoiceData = documents.get(0).toObject(InvoiceData.class);
                        if(invoiceData!= null) {
                            invoiceData.setId(tmp.getId());
                            return writeInvoiceDataFromServer(invoiceData);
                        }
                        else
                        {
                            Exception ex = result.getException();
                            if(ex != null)
                                ex.printStackTrace();
                            throw new Exception("Error creating invoice object");
                        }


                    }
                    else if(documents.size() == 0 && !querySnapshot.getMetadata().isFromCache()) {
                        //добавляем чек
                        Long dateInvoice = Long.parseLong(invoiceData.getDateInvoice(1));
                        if(dateInvoice == 0)
                        {
                            dateInvoice = new Date().getTime();
                        }
                        Map<String, Object> fStore = new HashMap<>();
                        fStore.put("FP", invoiceData.FP);
                        fStore.put("FD", invoiceData.FD);
                        fStore.put("FN", invoiceData.FN);
                        fStore.put("FP_FD_FN",invoiceData.FP+"_"+invoiceData.FD+"_"+invoiceData.FN);
                        if(invoiceData.server_status!= null)
                            fStore.put("server_status",invoiceData.server_status);
                        else
                            fStore.put("server_status", 0);
                        fStore.put("in_basket", 0);
                        fStore.put("dateInvoice", dateInvoice);
                        fStore.put("date_day", invoiceData.date_day);
                        fStore.put("fullPrice", invoiceData.getFullPrice());
                        fStore.put("date_add", invoiceData.getDate_add());
                        if(invoiceData.longitudeAdd != null && invoiceData.latitudeAdd != null)
                        {
                            fStore.put("longitudeAdd", invoiceData.longitudeAdd);
                            fStore.put("latitudeAdd", invoiceData.latitudeAdd);
                        }
                        if(invoiceData.get_order()!= null)
                        {
                            fStore.put("_order", invoiceData.get_order());
                        }

                        if(invoiceData.user_google_id != null)
                            fStore.put("user_google_id", invoiceData.user_google_id);
                        else
                            if(user.google_id != null)
                                fStore.put("user_google_id", invoiceData.user_google_id);
                        DocumentReference addedDocRef = mFirestore.collection(tableNameInvoice).document();
                        addedDocRef.set(fStore);
                        invoiceData.google_id = addedDocRef.getId();
                        ContentValues val = new ContentValues();
                        val.put("google_id", invoiceData.google_id);
                        dbHelper.update(tableNameInvoice, val, "id=?", new String[]{String.valueOf(invoiceData.getId())});
                        return false;
                    }
                }
                else
                {
                    Exception ex = result.getException();
                    if(ex != null)
                        ex.printStackTrace();
                    throw new Exception("Error loading from server, check connection");
                }
            }
        }
        return false;
    }

    public void setStoreDataFull(InvoiceData finalInvoiceData) throws Exception {
        if(finalInvoiceData.fromFNS)
        {
            InvoiceData.KktRegId kktRegId = finalInvoiceData.kktRegId;
            Cursor cur_kktRegId= null;

            InvoiceData.KktRegId on_line_kkt = new InvoiceData.KktRegId();
            InvoiceData.Store on_line_store = new InvoiceData.Store();

            if(On_line && user.google_id != null)
            {
                if(kktRegId.google_id == null) {
                    Task<QuerySnapshot> result_kkt = mFirestore.collection(tableNameKktRegId).whereEqualTo("kktRegId", kktRegId.kktRegId).get(source);
                    while (!result_kkt.isComplete()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    List<DocumentSnapshot> documents_kkt = result_kkt.getResult().getDocuments();
                    if (documents_kkt.size() == 1) {
                        for (DocumentSnapshot documentReference : documents_kkt) {
                            on_line_kkt = documentReference.toObject(InvoiceData.KktRegId.class);
                            on_line_kkt.google_id = documentReference.getId();
                            //on_line_kkt.google_id = documentReference.getId();
                            //on_line_kkt.fk_kktRegId_stores_google_id = documentReference.getString("fk_kktRegId_stores_google_id");;
                            //on_line_kkt._status = (Integer) documentReference.get("_status");
                            //on_line_kkt.kktRegId = (Long) documentReference.get("kktRegId");
                            finalInvoiceData.kktRegId = on_line_kkt;

                        }
                        if (on_line_kkt != null && on_line_kkt.fk_kktRegId_stores_google_id != null && finalInvoiceData.store.google_id == null) {
                            Task<DocumentSnapshot> result_store = mFirestore.collection(tablenameStores).document(on_line_kkt.fk_kktRegId_stores_google_id).get(source);
                            while (!result_store.isComplete()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            DocumentSnapshot documents_store = result_store.getResult();
                            if (documents_store.exists()) {
                                on_line_store = documents_store.toObject(InvoiceData.Store.class);
                                on_line_store.google_id = documents_store.getId();

                                finalInvoiceData.store = on_line_store;
                            }
                        }
                    }
                }
            }

            //проверка что касса уже существует в таблице
            cur_kktRegId=dbHelper.query(tableNameKktRegId, null, "kktRegId=?", new String[]{kktRegId.kktRegId.toString()}, null, null, null, null);
            if(cur_kktRegId.moveToFirst())
            {
                //загружаем данные из строки с найденной кассой
                Integer fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                Integer kktRegId_id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));
                cur_kktRegId.close();
                //загружаме информацю по магазину к которому привязана касса
                Map<String, String> args = new ArrayMap<String, String>();
                args.put("id", fk_kktRegId_stores.toString());
                List<InvoiceData.Store> stores = loadDataFromStore(args);
                //проверка что данные загружены, загружаем по id  поэтому больше 1 записи быть не может, но может быть 0 если ранее магазин удалили или т.п.
                if(stores.size()>0)
                {
                    InvoiceData.Store store = stores.get(0);
                    //проверка что магазин который сейчас привязан к чеку (если данные были добавлены вручную раньше загрузки с ФНС) отличается от того на который ссылается касса
                    if(finalInvoiceData.store.id!= null && finalInvoiceData.store.id != store.id)
                    {
                        //если в магазине на который ссылается чек прописана гугл метка места обновляем магазин к которому привязана найденная касса
                        //если геометки различаются изменять ссылку на магазин нельзя
                        //если есть геометки - статус магазина уже 1 -- подтвержден пользователем
                        //
                        if(finalInvoiceData.store.place_id!= null && finalInvoiceData.store.place_id.equals(store.place_id)) {

                            finalInvoiceData.store.id = store.id;
                            finalInvoiceData.store.update = true;
                            saveStoreDataLocal(finalInvoiceData.store);


                            //Обновляем ссылки на кассу и новый найденный магазин
                            finalInvoiceData.kktRegId.id = kktRegId_id;
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("fk_invoice_stores", store.id);
                            contentValues.put("fk_invoice_kktRegId", kktRegId_id);
                            dbHelper.update(tableNameInvoice, contentValues, "id=?", new String[]{finalInvoiceData.getId().toString()});

                            //обновляем ссылку на найденный магазин в таблице с покупками
                            contentValues = new ContentValues();
                            contentValues.put("fk_purchases_stores", store.id);
                            dbHelper.update(tableNamePurchases, contentValues, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});

                            //пытаемя удалить магазин, который раньше был приявязан к чеку, если больше он ни к чему не привязан
                            deleteStoreLink(finalInvoiceData.store.id, finalInvoiceData.store.google_id);
                            finalInvoiceData.store = store;
                        }
                        else
                        {
                            finalInvoiceData.store.update = true;
                            saveStoreDataLocal(finalInvoiceData.store);
                        }

                    }
                    //Если к чеку еще не привязан магазин, а мы нашли кассу с магазином мы обновляем данные в чеке, чтобы он ссылался на найденую кассу и магазин
                    else if(finalInvoiceData.store.id == null)
                    {
                        finalInvoiceData.store = store;
                        if(on_line_store.google_id == null)
                        {
                            finalInvoiceData.store.update = false;
                            saveStoreDataServer(finalInvoiceData.store);
                        }

                        finalInvoiceData.kktRegId.id = kktRegId_id;
                        finalInvoiceData.set_status(1);
                        finalInvoiceData.setfk_invoice_stores(finalInvoiceData.store.id);
                        finalInvoiceData.setfk_invoice_kktRegId(kktRegId_id);
                        updateInvoice(finalInvoiceData);
                    }

                }

            }
            else
            {
                if(finalInvoiceData.store.id!= null && finalInvoiceData.store.id != 0)
                {
                    finalInvoiceData.kktRegId.fk_kktRegId_stores = finalInvoiceData.store.id;
                    if(finalInvoiceData.store.google_id != null)
                        finalInvoiceData.kktRegId.fk_kktRegId_stores_google_id = finalInvoiceData.store.google_id;
                    setStoreData(finalInvoiceData);
                    try {
                        finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                    } catch (Exception e) {
                        log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                        e.printStackTrace();
                        //finalInvoiceData.kktRegId.id = 0;
                    }
                    //finalInvoiceData.setfk_invoice_kktRegId(finalInvoiceData.kktRegId.id);
                }
                else
                {
                    if(on_line_store == null || on_line_store._status ==  null)
                    {
                        finalInvoiceData.store._status = 0;
                    }

                    finalInvoiceData.store.update = false;
                    setStoreData(finalInvoiceData);
                    if(finalInvoiceData.store.id >0) {
                        finalInvoiceData.kktRegId.fk_kktRegId_stores = finalInvoiceData.store.id;
                        if(finalInvoiceData.store.google_id != null)
                            finalInvoiceData.kktRegId.fk_kktRegId_stores_google_id = finalInvoiceData.store.google_id;
                        try {
                            finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                        } catch (Exception e) {
                            log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                            e.printStackTrace();
                            //finalInvoiceData.kktRegId.id = 0;
                        }
                        //finalInvoiceData.setfk_invoice_kktRegId(finalInvoiceData.kktRegId.id);
                    }

                }
                //updateInvoice(finalInvoiceData);
            }

        }
       /* else if (finalInvoiceData.fromServer)
        {
            if(finalInvoiceData.kktRegId!= null && finalInvoiceData.kktRegId.google_id != null)
            {
                Cursor cur_kktRegId = dbHelper.query(tableNameKktRegId, null, "google_id=?", new String[]{finalInvoiceData.kktRegId.google_id}, null, null, null, null);
                if(cur_kktRegId.moveToFirst())
                {
                    finalInvoiceData.kktRegId.fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                    finalInvoiceData.kktRegId.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));

                    Cursor cur_store = dbHelper.query(tablenameStores, null, "id=?", new String[]{finalInvoiceData.kktRegId.fk_kktRegId_stores.toString()}, null, null, null, null);
                    if(cur_store.moveToFirst())
                    {
                        finalInvoiceData.store.id = cur_store.getInt(cur_store.getColumnIndex("id"));
                        int _status = cur_store.getInt(cur_store.getColumnIndex("_status"));
                        if(finalInvoiceData.store._status != _status && finalInvoiceData.store._status == 1)
                        {
                            setStoreData(finalInvoiceData);
                        }
                        //сравнить данные у пользователя и на сервере. обновить если статус магазина на сервера "подтвержден администарцией"
                        //магазин может быть пустой так как добавлена только геометка
                    }
                    else
                    {
                        //сохраняем магазин в локальной базе
                        setStoreData(finalInvoiceData);
                    }
                }
                else
                {
                    setStoreData(finalInvoiceData);
                    try {
                        saveKktRegId(finalInvoiceData.kktRegId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //выходим, остальные действия делаем в  фукнции загрузки чека с сервера
            return;
        }*/
        else
        {
            if(finalInvoiceData.store.place_id!= null && finalInvoiceData.store.place_id.length()>0)
            {
                InvoiceData.Store on_line_store = null;

                if(On_line && user.google_id != null)
                {

                    Task<QuerySnapshot> result_store = null;//mFirestore.collection(tablenameStores).whereEqualTo("place_id", finalInvoiceData.store.place_id).get(source);

                        /*FirebaseFirestoreSettings fireBaseSettings = new FirebaseFirestoreSettings.Builder()
                                .setPersistenceEnabled(false)
                                .build();
                        mFirestore.setFirestoreSettings(fireBaseSettings);*/
                    result_store = mFirestore.collection(tablenameStores).whereEqualTo("place_id", finalInvoiceData.store.place_id).get(source);
                    while (!result_store.isComplete()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    
                    
                    if(result_store.isSuccessful())
                    {
                        Log.d(LOG_TAG, "ok check server done");
                        List<DocumentSnapshot> documents_store = result_store.getResult().getDocuments();
                        if (documents_store.size() == 1) {
                            for (DocumentSnapshot documentReference : documents_store) {
                                on_line_store = documentReference.toObject(InvoiceData.Store.class);
                                if(on_line_store != null) {
                                    on_line_store.google_id = documentReference.getId();
                                    if (!finalInvoiceData.store.google_id.equals(on_line_store.google_id))
                                    {
                                        mFirestore.collection(tablenameStores).document(finalInvoiceData.store.google_id).delete();
                                        finalInvoiceData.store.google_id = on_line_store.google_id;
                                        finalInvoiceData.fk_invoice_stores_google_id=on_line_store.google_id;
                                        if(finalInvoiceData.kktRegId != null)
                                            finalInvoiceData.kktRegId.fk_kktRegId_stores_google_id=on_line_store.google_id;
                                    }
                                }

                            }
                        }
                    }
                }



                Cursor cur_store = dbHelper.query(tablenameStores, null, "place_id=?", new String[]{finalInvoiceData.store.place_id}, null, null, null, null);
                if(cur_store. moveToFirst())
                {
                    Integer id = cur_store.getInt(cur_store.getColumnIndex("id"));
                    Map<String, String> args = new ArrayMap<String, String>();
                    args.put("id", id.toString());
                    List<InvoiceData.Store> stores = loadDataFromStore(args);

                    if(finalInvoiceData.store.id!= null && finalInvoiceData.store.id >0)
                    {
                        if(finalInvoiceData.store.id == id)
                        {
                            finalInvoiceData.store.update = checkStoretoUpdate(finalInvoiceData);
                            finalInvoiceData.store._status = 1;
                            setStoreData(finalInvoiceData);
                        }
                        else
                        {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("fk_invoice_stores",id);
                            dbHelper.update(tableNameInvoice, contentValues, "id=?", new String[]{finalInvoiceData.getId().toString()});
                            contentValues =  new ContentValues();
                            contentValues.put("fk_purchases_stores", id);
                            dbHelper.update(tableNamePurchases, contentValues, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});
                            contentValues =  new ContentValues();
                            contentValues.put("fk_kktRegId_stores", id);
                            dbHelper.update(tableNameKktRegId, contentValues, "id=?", new String[]{finalInvoiceData.kktRegId.id.toString()});
                            finalInvoiceData.kktRegId.fk_kktRegId_stores = id;


                            //try to delete old store
                            deleteStoreLink(finalInvoiceData.store.id, finalInvoiceData.store.google_id);
                            finalInvoiceData.store = stores.get(0);
                        }
                    }
                    else
                    {
                        //new item in table stores
                        finalInvoiceData.store._status = 1;
                        setStoreData(finalInvoiceData);
                    }
                }
                else
                {
                    finalInvoiceData.store.update = false;
                    if(finalInvoiceData.store.id != null && finalInvoiceData.store.id >0)
                    {
                        finalInvoiceData.store.update = checkStoretoUpdate(finalInvoiceData);
                    }
                    finalInvoiceData.store._status = 1;
                    setStoreData(finalInvoiceData);
                    //finalInvoiceData.setfk_invoice_stores(finalInvoiceData.store.id);
                    //updateInvoice(finalInvoiceData);
                }

            }
        }

        PurchasesListData purchasesListData = new PurchasesListData();
        purchasesListData.store = new PurchasesListData.Store();
        purchasesListData.store.id = finalInvoiceData.store.id;
        purchasesListData.store.google_id = finalInvoiceData.store.google_id;

        updatePurchases(purchasesListData, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});

        if(finalInvoiceData.kktRegId!= null && finalInvoiceData.kktRegId.kktRegId>0)
        {
            finalInvoiceData.set_status(1);
            //updateInvoice(finalInvoiceData);
            finalInvoiceData.kktRegId.fk_kktRegId_stores_google_id = finalInvoiceData.store.google_id;
            finalInvoiceData.kktRegId._status = finalInvoiceData.store._status;
            try {
                finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                if(finalInvoiceData.kktRegId.google_id != null)
                    finalInvoiceData.fk_invoice_kktRegId_google_id = finalInvoiceData.kktRegId.google_id;
            } catch (Exception e) {
                log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                finalInvoiceData.kktRegId.id = 0;
            }
        }
    }

    private void updatePurchases(PurchasesListData purchasesListData, String where, String[] args)
    {
        ContentValues contentValues = new ContentValues();

        if(purchasesListData.invoice != null && purchasesListData.invoice.id != null)
            contentValues.put("fk_purchases_invoice", purchasesListData.invoice.id);
        if(purchasesListData.store != null && purchasesListData.store.id != null)
            contentValues.put("fk_purchases_stores", purchasesListData.store.id);
        if(purchasesListData.product != null && purchasesListData.product.id != null)
            contentValues.put("fk_purchases_products", purchasesListData.product.id);


        if(purchasesListData.product != null && purchasesListData.product.google_id != null)
            contentValues.put("fk_purchases_products_google_id", purchasesListData.product.google_id);
        if(purchasesListData.store != null && purchasesListData.store.google_id != null)
            contentValues.put("fk_purchases_stores_google_id", purchasesListData.store.google_id);


        dbHelper.update(tableNamePurchases, contentValues, where, args);

    }

    private boolean checkStoretoUpdate(InvoiceData finalInvoiceData)
    {
        //проверка что магазин можно обновить - изменить его положение на карте, если это магазин привязан только к одной кассе и одному чеку
        finalInvoiceData.store.update = false;
        if(finalInvoiceData.store.id!= null)
        {
            //поиск чеков с текущим магазином
            Cursor cur = dbHelper.query(tableNameInvoice, null, "fk_invoice_stores=?", new String[]{finalInvoiceData.store.id.toString()}, null, null, null, null);
            if(cur.getCount()<=1)
            {
                //если чеки не найдены - проверяем кассы с этим магазином отличные от текущей
                // если не найдены обновляем данные по магазину
                //если кассы нет? выдаст ли ошибку?
                cur.close();
                cur = dbHelper.query(tableNameKktRegId, null, "fk_kktRegId_stores=? AND id <>?", new String[]{finalInvoiceData.store.id.toString(), finalInvoiceData.kktRegId.id.toString()}, null, null, null, null);
                if(!cur.moveToFirst())
                {
                    finalInvoiceData.store.update = true;
                }
            }
            else
            {
                //если чеки найдены считаем количество
                int invCount = cur.getCount();
                cur.close();
                //берем уникальные ссылки на кассы из чеков где указан текущий магазин
                //если касса еще не привязана к чеку???  проверять
                cur = dbHelper.rawQuery("SELECT DISTINCT fk_invoice_kktRegId FROM "+tableNameInvoice+" WHERE fk_invoice_stores=?", new String[]{finalInvoiceData.store.id.toString()});
                int kktCount = cur.getCount();
                if(kktCount == 1) {
                    //если кассы найдены в количестве 1 шт - только текущий чек ссылается на эту кассу
                    cur.close();
                    //проверяем что статус текущего магазина не 1
                    cur = dbHelper.query(tablenameStores, null, "id =? and _status <>?", new String[]{finalInvoiceData.store.id.toString(), "1"}, null, null, null, null);
                    if (cur.moveToFirst())
                        finalInvoiceData.store.update = true;
                    else
                    {
                        cur.close();
                        //если статус магазина == 1 проверяем количество касс привязанных к этому магазину - если одна - обновляем.
                        cur = dbHelper.query(tableNameKktRegId, null, "fk_kktRegId_stores =?", new String[]{finalInvoiceData.store.id.toString()}, null, null, null, null);
                        if (cur.getCount() == 1)
                            finalInvoiceData.store.update = true;
                    }
                }
            }
            cur.close();
        }
        return finalInvoiceData.store.update;
    }

    public String latinToKirillic(String string)
    {
        String latin ="ETOPAHKXCBMetopakxcm";
        String kirillic ="ЕТОРАНКХСВМеторакхсм";
        for(int i=0; i<latin.length(); i++)
        {
            string = string.replaceAll(String.valueOf(latin.charAt(i)), String.valueOf(kirillic.charAt(i)));
        }
        return string;
    }

    private void saveStoreDataLocal(InvoiceData.Store store)
    {

        final ContentValues data = new ContentValues();
        if(store.name != null) {
            data.put("name", store.name.trim());
        }
        if(store.address != null) {
            data.put("address", store.address.trim());
        }
        if(store.latitude != null) {
            data.put("latitude", store.latitude);
        }
        if(store.longitude != null) {
            data.put("longitude", store.longitude);
        }
        if(store.inn != null) {
            data.put("inn", store.inn);
        }
        if(store.name_from_fns != null) {
            store.name_from_fns= latinToKirillic(store.name_from_fns);
            data.put("name_from_fns", store.name_from_fns.trim());
        }
        if(store.address_from_fns != null) {
            data.put("address_from_fns", store.address_from_fns.trim());
        }
        if(store.place_id != null)
        {
            data.put("place_id", store.place_id);
        }
        if(store.store_type != null) {
            data.put("store_type", store.store_type);
        }
        if(store.iconName != null) {
            data.put("iconName", store.iconName);
        }
        if(store.photo_reference != null) {
            data.put("photo_reference", store.photo_reference);
        }
        if(store._status != null) {
            data.put("_status", store._status);
        }

        if(store.date_add == null) {
            data.put("date_add", new Date().getTime());
        }
        else
        {
            data.put("date_add",store.date_add);
        }

        if(store.google_id!= null)
            data.put("google_id", store.google_id);

        if(store.update && store.id!= null)
        {
            dbHelper.update(tablenameStores, data, "id=?", new String[]{store.id.toString()});
        }
        else
        {
            store.id = (int)dbHelper.insert(tablenameStores, null, data);
        }
    }

    private void saveStoreDataServer(InvoiceData.Store store)
    {
        Map<String, Object> fStore = new HashMap<>();

        if(store.name != null) {
            fStore.put("name", store.name.trim());
        }
        if(store.address != null) {
            fStore.put("address", store.address.trim());
        }
        if(store.latitude != null) {
            fStore.put("latitude", store.latitude);
        }
        if(store.longitude != null) {
            fStore.put("longitude", store.longitude);
        }
        if(store.inn != null) {
            fStore.put("inn", store.inn);
        }
        if(store.name_from_fns != null) {
            store.name_from_fns= latinToKirillic(store.name_from_fns);
            fStore.put("name_from_fns", store.name_from_fns.trim());
        }
        if(store.address_from_fns != null) {
            fStore.put("address_from_fns", store.address_from_fns.trim());
        }
        if(store.place_id != null)
        {
            fStore.put("place_id", store.place_id);
        }
        if(store.store_type != null) {
            fStore.put("store_type", store.store_type);
        }
        if(store.iconName != null) {
            fStore.put("iconName", store.iconName);
        }
        if(store.photo_reference != null) {
            fStore.put("photo_reference", store.photo_reference);
        }
        if(store._status != null) {
            fStore.put("_status", store._status);
        }

        if(store.date_add == null) {
            fStore.put("date_add", new Date().getTime());
        }

        if(store.update && store.google_id!= null)
        {
            mFirestore.collection(tablenameStores).document(store.google_id).update(fStore);

            Map<String, Object> updateChildren = new HashMap<>();
            updateChildren.put("/" + tablenameStores + "/" + store.google_id, fStore);
            mFirebase.updateChildren(updateChildren);
        }
        else
        {
            DocumentReference addedDocRef = mFirestore.collection(tablenameStores).document();
            store.google_id = addedDocRef.getId();
            addedDocRef.set(fStore);


            //store.google_id = mFirebase.child(tablenameStores).push().getKey();
            mFirebase.child(tablenameStores).child(store.google_id).setValue(fStore);


            ContentValues dataUpdate = new ContentValues();
            dataUpdate.put("google_id", store.google_id);
            dbHelper.update(tablenameStores, dataUpdate, "id=?", new String[]{store.id.toString()});
        }
    }

    public void setStoreData(InvoiceData invoiceData) throws Exception {

        InvoiceData.Store store = invoiceData.store;
        Boolean check_placeid = false;
        if(On_line && user.google_id != null)
        {
            if(store.google_id!= null)
            {
                Task<DocumentSnapshot> task = mFirestore.collection(tablenameStores).document(store.google_id).get(source);
                while (!task.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        saveStoreDataLocal(store);
                        saveStoreDataServer(store);
                    } else {
                        check_placeid = true;
                    }
                } else {
                    throw new Exception(LOG_TAG + "\nsetStoreData()\n" + "Ошибка обращения к серверу");
                }
            }

            if((store.google_id != null && check_placeid) || (store.google_id == null && store.place_id!= null))
            {
                Task<QuerySnapshot> task_pl_id = mFirestore.collection(tablenameStores).whereEqualTo("place_id", store.place_id).get(source);
                while(!task_pl_id.isComplete())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(task_pl_id.isSuccessful())
                {
                    if(task_pl_id.getResult().isEmpty())
                    {
                        store.update = false;
                        saveStoreDataServer(store);
                    }
                    else
                    {
                        InvoiceData.Store storeDoc = task_pl_id.getResult().getDocuments().get(0).toObject(InvoiceData.Store.class);
                        storeDoc.id = store.id;
                        saveStoreDataLocal(storeDoc);
                    }
                }
                else
                {
                    throw  new Exception(LOG_TAG +"\nsetStoreData()\n"+"Ошибка обращения к серверу");
                }
            }
            else if(store.google_id == null && store.place_id == null)
            {
                saveStoreDataLocal(store);
                saveStoreDataServer(store);
            }
        }
        else
        {
            saveStoreDataLocal(store);
        }
    }

    private void deleteStoreLink(Integer Id, @Nullable String google_id)
    {
        Cursor cur = dbHelper.query(tableNameInvoice, null, "fk_invoice_stores=?", new String[]{Id.toString()}, null, null, null, null);
        boolean delete = false;
        if(!cur.moveToFirst())
            delete = true;
        else
            delete = false;
        cur = dbHelper.query(tableNamePurchases, null, "fk_purchases_stores=?", new String[]{Id.toString()}, null, null, null, null);
        if(!cur.moveToFirst())
            delete = true;
        else
            delete = false;

        cur = dbHelper.query(tableNameKktRegId, null, "fk_kktRegId_stores=?", new String[]{Id.toString()}, null, null, null, null);
        if(!cur.moveToFirst())
            delete = true;
        else
            delete = false;

        cur.close();
        if(delete)
        {
            dbHelper.delete(tablenameStores, "id=?", new String[]{Id.toString()});
        }

        if(On_line && user.google_id != null && google_id != null)
        {
            Boolean removeStore = false;
            Task<QuerySnapshot> result = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_stores_google_id", google_id).get(source);
            while(!result.isComplete())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(result.isSuccessful())
            {
                List<DocumentSnapshot> documents = result.getResult().getDocuments();
                if(documents.size()==0)
                {
                    removeStore = true;
                }
                else
                {
                    removeStore = false;
                }
            }
            result = mFirestore.collection(tableNameInvoice).whereEqualTo("fk_invoice_stores_google_id", google_id).get(source);
            while(!result.isComplete())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(result.isSuccessful())
            {
                List<DocumentSnapshot> documents = result.getResult().getDocuments();
                if(documents.size()==0)
                {
                    removeStore = true;
                }
                else
                {
                    removeStore = false;
                }
            }
            result = mFirestore.collection(tableNameKktRegId).whereEqualTo("fk_kktRegId_stores_google_id", google_id).get(source);
            while(!result.isComplete())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(result.isSuccessful())
            {
                List<DocumentSnapshot> documents = result.getResult().getDocuments();
                if(documents.size()==0)
                {
                    removeStore = true;
                }
                else
                {
                    removeStore = false;
                }
            }

            if(removeStore)
            {
                Task<Void> resultDelet = mFirestore.collection(tablenameStores).document(google_id).delete();
                while(!resultDelet.isComplete())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private List<InvoiceData.Store> loadDataFromStore(Map<String, String> keyFields)
    {
        List<InvoiceData.Store> listStore = new ArrayList<>();


        String selection="";
        List<String> listArgs = new ArrayList<>();
        for(Map.Entry<String, String> entry : keyFields.entrySet())
        {
            selection+= entry.getKey()+"=? AND ";
            listArgs.add(entry.getValue());
        }

        selection=selection.substring(0, selection.length() - 5);
        String[] args = listArgs.toArray(new String[listArgs.size()]);
        Cursor cur_Store = dbHelper.query(tablenameStores, null, selection, args, null, null, null, null);
        if(cur_Store.moveToFirst())
        {
            do{
                InvoiceData.Store store = new InvoiceData.Store();
                store.address_from_fns = cur_Store.getString(cur_Store.getColumnIndex("address_from_fns"));
                store.name_from_fns = cur_Store.getString(cur_Store.getColumnIndex("name_from_fns"));
                store.latitude = cur_Store.getDouble(cur_Store.getColumnIndex("latitude"));
                store.longitude = cur_Store.getDouble(cur_Store.getColumnIndex("longitude"));
                store.name = cur_Store.getString(cur_Store.getColumnIndex("name"));
                store.address = cur_Store.getString(cur_Store.getColumnIndex("address"));
                store.inn = cur_Store.getLong(cur_Store.getColumnIndex("inn"));
                store.place_id = cur_Store.getString(cur_Store.getColumnIndex("place_id"));
                store.iconName = cur_Store.getString(cur_Store.getColumnIndex("iconName"));
                store.photo_reference = cur_Store.getString(cur_Store.getColumnIndex("photo_reference"));
                store._status = cur_Store.getInt(cur_Store.getColumnIndex("_status"));
                store.id = cur_Store.getInt(cur_Store.getColumnIndex("id"));
                store.google_id = cur_Store.getString(cur_Store.getColumnIndex("google_id"));

                listStore.add(store);
            }
            while(cur_Store. moveToNext());
            cur_Store.close();
        }


        return listStore;
    }

    public int fillReceiptData(GetFnsData.Receipt receipt, InvoiceData finalInvoiceData) throws Exception {
        
        Cursor cur_products;

        int countPurchases=0;
        Integer id_kktRegId=null;
        InvoiceData.KktRegId kktRegId = new InvoiceData.KktRegId();


        //add field inn for magazine and check it if there is no address
        //check and add update fk_Stores

        //!!  if no address or name  you can check by kktRegId  -
        //
        // find another invoices where fk_stores not null and the same kktRegId ask user to check address!

        InvoiceData invoiceData = new InvoiceData();

        //invoiceData.setDateInvoice(receipt.);
        if(finalInvoiceData.store == null)
            finalInvoiceData.store = new InvoiceData.Store();
        if(receipt.user != null)
            finalInvoiceData.store.name_from_fns= receipt.user;
        if(receipt.retailPlaceAddress != null)
            finalInvoiceData.store.address_from_fns = receipt.retailPlaceAddress.trim().replaceAll("\\s{2,}", "");

        if(finalInvoiceData.store.inn == null)
            finalInvoiceData.store.update = true;
        else if(finalInvoiceData.store.inn == 0)
            finalInvoiceData.store.update = true;

        if(finalInvoiceData.store._status == null)
            finalInvoiceData.store._status = 0;
        if(receipt.userInn != null)
            finalInvoiceData.store.inn = Long.valueOf(receipt.userInn.trim());

        if(finalInvoiceData.kktRegId == null)
            finalInvoiceData.kktRegId = new InvoiceData.KktRegId();

        if(receipt.kktRegId != null)
            finalInvoiceData.kktRegId.kktRegId = Long.valueOf(receipt.kktRegId.trim());

        setStoreDataFull(finalInvoiceData);

        ContentValues updateData = new ContentValues();
        Map<String, Object> fStore = new HashMap<>();
        if(receipt.dateTime!= null)
        {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateFormat_day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Long invoiceDate = null;
                Long invoiceDate_day = null;

                Date newDate = oldDateFormat.parse(receipt.dateTime);
                invoiceDate = dateFormat.parse(dateFormat.format(newDate)).getTime();
                invoiceDate_day = dateFormat_day.parse(dateFormat_day.format(newDate)).getTime();
                updateData.put("dateInvoice", invoiceDate);
                updateData.put("date_day", invoiceDate_day);

                fStore.put("dateInvoice", invoiceDate);
                fStore.put("date_day", invoiceDate_day);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        log.info(LOG_TAG+"\n"+receipt.totalSum +" totalSum");
        updateData.put("fullPrice", Float.parseFloat(receipt.totalSum)/100);
        updateData.put("ecashTotalSum", Float.parseFloat(receipt.ecashTotalSum)/100);
        updateData.put("cashTotalSum", Float.parseFloat(receipt.cashTotalSum)/100);


        fStore.put("fullPrice", updateData.get("fullPrice"));
        fStore.put("ecashTotalSum",updateData.get("ecashTotalSum") );
        fStore.put("cashTotalSum",updateData.get("cashTotalSum"));
        if(finalInvoiceData.kktRegId.id != null) {
            updateData.put("fk_invoice_kktRegId", finalInvoiceData.kktRegId.id);
        }
        if(finalInvoiceData.kktRegId.google_id != null) {
            updateData.put("fk_invoice_kktRegId_google_id", finalInvoiceData.kktRegId.google_id);


            fStore.put("fk_invoice_kktRegId_google_id", finalInvoiceData.kktRegId.google_id);
        }
        if(finalInvoiceData.store.id != null) {
            updateData.put("fk_invoice_stores", finalInvoiceData.store.id);
        }
        if(finalInvoiceData.store.google_id != null) {
            updateData.put("fk_invoice_stores_google_id", finalInvoiceData.store.google_id);


            fStore.put("fk_invoice_stores_google_id", finalInvoiceData.store.google_id);
        }
        updateData.put("_status", finalInvoiceData.get_status());


        if(finalInvoiceData.get_status() != null && finalInvoiceData.get_status()==1 && finalInvoiceData.server_status == null)
            fStore.put("server_status",1);
        else if(finalInvoiceData.server_status!= null)
            fStore.put("server_status",finalInvoiceData.server_status);
        else
            fStore.put("server_status", 0);


        fStore.put("jsonData", finalInvoiceData.jsonData);
        if(On_line && user.google_id != null && finalInvoiceData.google_id != null && (finalInvoiceData.server_status == null || finalInvoiceData.server_status != 1))
        {
            mFirestore.collection(tableNameInvoice).document(finalInvoiceData.google_id).update(fStore);
            Map<String, Object> updateChildren = new HashMap<>();
            updateChildren.put("/"+tableNameInvoice+"/"+finalInvoiceData.google_id, fStore);
            mFirebase.updateChildren(updateChildren);
        }
        dbHelper.update(tableNameInvoice, updateData, "id=?", new String[]{finalInvoiceData.getId().toString()});
        Log.d(LOG_TAG, "updated invoice id " +finalInvoiceData.getId());


        //Start adding purchases and prod
        ArrayList<GetFnsData.Items> items = receipt.items;
        if(finalInvoiceData.store.id != null && finalInvoiceData.kktRegId.id != null) {
            for (GetFnsData.Items item : items) {
                int fk_purchases_products = -1;
                String google_item_id = null;
                String item_name_from_bill = null;
                String fk_purchases_products_google_id = null;
                boolean add_to_google = true;
                //check in products db
                if (item.name != null) {

                    if(On_line && user.google_id != null)
                    {
                        Task<QuerySnapshot> result_product = mFirestore.collection("Products").whereEqualTo("nameFromBill", item.name).get(source);
                        while (!result_product.isComplete()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        List<DocumentSnapshot> documents = result_product.getResult().getDocuments();
                        if(documents.size() == 1)
                        {
                            for(DocumentSnapshot document : documents)
                            {
                                google_item_id = document.getId();
                            }
                        }
                    }
                    cur_products = dbHelper.query("Products", null, "nameFromBill=?",
                            new String[]{item.name}, null, null, null, null);
                    if (cur_products.moveToFirst()) {
                        fk_purchases_products = cur_products.getInt(cur_products.getColumnIndex("id"));
                        fk_purchases_products_google_id = cur_products.getString(cur_products.getColumnIndex("google_id"));
                        add_to_google = false;
                    }
                    cur_products.close();
                }
                if (item.name != null && fk_purchases_products == -1) {

                    ContentValues values = new ContentValues();
                    values.put("nameFromBill", item.name);
                    if(google_item_id != null)
                        values.put("google_id", google_item_id);
                    fk_purchases_products = ((int) dbHelper.insert("Products", null, values));
                }
            /*
            else if(item.name == null)
            {
                ContentValues values = new ContentValues();
                values.put("nameFromBill", "empty");
                fk_purchases_products = ((int) dbHelper.insert("Products", null, values));
            }
*/

                //error adding product stop
                if (fk_purchases_products == -1 && countPurchases == 0)
                    return -1;

                //add in table purchases
                ContentValues values = new ContentValues();
                final Map<String, Object> fPurchases = new HashMap<>();

                if (finalInvoiceData.store.id != null)
                    values.put("fk_purchases_stores", finalInvoiceData.store.id);

                if (finalInvoiceData.store.google_id != null) {
                    fPurchases.put("fk_purchases_stores_google_id", finalInvoiceData.store.google_id);
                }


                values.put("fk_purchases_products", fk_purchases_products);
                values.put("fk_purchases_invoice", finalInvoiceData.getId());

                if (fk_purchases_products_google_id != null) {
                    fPurchases.put("fk_purchases_products_google_id", fk_purchases_products_google_id);
                }
                if (finalInvoiceData.google_id != null) {
                    fPurchases.put("fk_purchases_invoice_google_id", finalInvoiceData.google_id);
                }

                if (filterParam.containsKey("fk_invoice_accountinglist")) {
                    values.put("fk_purchases_accountinglist", filterParam.get("fk_purchases_accountinglist")[0]);

                    fPurchases.put("fk_purchases_accountinglist", filterParam.get("fk_purchases_accountinglist")[0]);
                }

                values.put("prise_for_item", item.price / 100);
                values.put("quantity", item.quantity);
                values.put("sum", item.sum / 100);
                values.put("date_add", new Date().getTime());

                fPurchases.put("prise_for_item", item.price / 100);
                fPurchases.put("quantity", item.quantity);
                fPurchases.put("sum", item.sum / 100);
                fPurchases.put("date_add", values.get("date_add"));

                final Long tmp = dbHelper.insert("purchases", null, values);
                if (tmp == -1)
                    return -1;
                else
                    countPurchases += 1;

                Log.d(LOG_TAG, "added purchase id " + tmp);
                if (On_line && user.google_id != null) {

                    if (add_to_google) {

                        if(google_item_id == null) {
                            Map<String, Object> fProducts = new HashMap<>();
                            fProducts.put("nameFromBill", item.name);
                            DocumentReference addedDocRef_product = mFirestore.collection("products").document();
                            google_item_id = addedDocRef_product.getId();
                            addedDocRef_product.set(fProducts);

                            Log.d(LOG_TAG, "added in google product "+google_item_id + " name from bill " + item.name);
                            //String google_id = mFirebase.child("products").push().getKey();
                            mFirebase.child("products").child(google_item_id).setValue(fProducts);


                            ContentValues val = new ContentValues();
                            val.put("google_id", google_item_id);
                            dbHelper.update("Products", val, "id =?", new String[]{String.valueOf(fk_purchases_products)});
                        }
                        fPurchases.put("fk_purchases_products_google_id", google_item_id);
                        DocumentReference addedDocRef_purchase = mFirestore.collection("purchases").document();
                        addedDocRef_purchase.set(fPurchases);
                        String google_id = addedDocRef_purchase.getId();

                        //String google_id = mFirebase.child("purchases").push().getKey();
                        mFirebase.child("purchases").child(google_id).setValue(fPurchases);

                        ContentValues valuesUpdate = new ContentValues();
                        valuesUpdate.put("google_id", google_id);
                        dbHelper.update("purchases", valuesUpdate, "id=?", new String[]{tmp.toString()});
                    } else {
                        DocumentReference addedDocRef_purchase = mFirestore.collection("purchases").document();
                        addedDocRef_purchase.set(fPurchases);
                        String google_id = addedDocRef_purchase.getId();

                        //String google_id = mFirebase.child("purchases").push().getKey();
                        mFirebase.child("purchases").child(google_id).setValue(fPurchases);

                        ContentValues valuesUpdate = new ContentValues();
                        valuesUpdate.put("google_id", google_id);
                        dbHelper.update("purchases", valuesUpdate, "id=?", new String[]{tmp.toString()});
                    }
                }


            }
        }



        
        this.reLoadInvoice();
        return countPurchases;
    }

    private Integer saveKktRegId(InvoiceData.KktRegId kktRegId) throws Exception {
        //check kkt exist in base

        Log.d(LOG_TAG, "saveKktRegId "+kktRegId.kktRegId);
        String tableName ="kktRegId";
        int id;

        ContentValues data = new ContentValues();
        Map<String, Object> fStore = new HashMap<>();


        if(kktRegId.fk_kktRegId_stores != null) {
            data.put("fk_kktRegId_stores", kktRegId.fk_kktRegId_stores.toString());
        }
        if(kktRegId.fk_kktRegId_stores_google_id != null)
        {
            data.put("fk_kktRegId_stores_google_id", kktRegId.fk_kktRegId_stores_google_id);

            fStore.put("fk_kktRegId_stores_google_id", kktRegId.fk_kktRegId_stores_google_id);
        }
        if(kktRegId.kktRegId != null) {
            data.put("kktRegId", kktRegId.kktRegId.toString());

            fStore.put("kktRegId", kktRegId.kktRegId);
        }
        else
            throw new Exception("Error kktRegId is null");
        data.put("_status", kktRegId._status != null ? kktRegId._status : 0);
        data.put("date_add", new Date().getTime());

        fStore.put("_status", kktRegId._status != null ? kktRegId._status : 0);
        fStore.put("date_add", new Date().getTime());

        if(On_line && user.google_id != null)
        {
            if(kktRegId.google_id!= null)
            {
                mFirestore.collection(tableNameKktRegId).document(kktRegId.google_id).update(fStore);

                Map<String, Object> updateChildren = new HashMap<>();
                updateChildren.put("/"+tableNameKktRegId+"/"+kktRegId.google_id, fStore);
                mFirebase.updateChildren(updateChildren);
            }
            else
            {
                DocumentReference addedDocRef = mFirestore.collection(tableNameKktRegId).document();
                addedDocRef.set(fStore);
                kktRegId.google_id = addedDocRef.getId();

                //kktRegId.google_id = mFirebase.child(tableNameKktRegId).push().getKey();
                mFirebase.child(tableNameKktRegId).child(kktRegId.google_id).setValue(fStore);
            }
        }

        if(kktRegId.google_id != null)
        {
            data.put("google_id", kktRegId.google_id);
        }

        if(kktRegId.id != null) {
            id =(int) dbHelper.update(tableName, data, "id=?", new String[]{kktRegId.id.toString()});
        }
        else {
            id = (int) dbHelper.insert(tableName, null, data);
        }




        if(id == -1)
            //what to do with error
            return null;
        else if(kktRegId.id != null)
            return kktRegId.id;
        else
            return id;

    }

    private void saveKktRegIdServer(InvoiceData.KktRegId kktRegId) throws Exception {
        //check kkt exist in base

        Log.d(LOG_TAG, "saveKktRegId "+kktRegId.kktRegId);
        String tableName ="kktRegId";
        int id;

        Map<String, Object> fStore = new HashMap<>();


        if(kktRegId.fk_kktRegId_stores_google_id != null)
        {
            fStore.put("fk_kktRegId_stores_google_id", kktRegId.fk_kktRegId_stores_google_id);
        }
        if(kktRegId.kktRegId != null) {
            fStore.put("kktRegId", kktRegId.kktRegId);
        }
        else
            throw new Exception("Error kktRegId is null");

        fStore.put("_status", kktRegId._status != null ? kktRegId._status : 0);
        fStore.put("date_add", new Date().getTime());

        if(On_line && user.google_id != null)
        {
            if(kktRegId.google_id!= null)
            {
                mFirestore.collection(tableNameKktRegId).document(kktRegId.google_id).update(fStore);

                Map<String, Object> updateChildren = new HashMap<>();
                updateChildren.put("/"+tableNameKktRegId+"/"+kktRegId.google_id, fStore);
                mFirebase.updateChildren(updateChildren);
            }
            else
            {
                DocumentReference addedDocRef = mFirestore.collection(tableNameKktRegId).document();
                addedDocRef.set(fStore);
                kktRegId.google_id = addedDocRef.getId();

                //kktRegId.google_id = mFirebase.child(tableNameKktRegId).push().getKey();
                mFirebase.child(tableNameKktRegId).child(kktRegId.google_id).setValue(fStore);


                if(kktRegId.id!= null) {
                    ContentValues dataUpdate = new ContentValues();
                    dataUpdate.put("google_id", kktRegId.google_id);
                    dbHelper.update(tablenameStores, dataUpdate, "id=?", new String[]{kktRegId.id.toString()});
                }
            }
        }

    }

    private Integer saveKktRegIdLocal(InvoiceData.KktRegId kktRegId) throws Exception {
        //check kkt exist in base

        Log.d(LOG_TAG, "saveKktRegId "+kktRegId.kktRegId);
        String tableName ="kktRegId";
        int id;

        ContentValues data = new ContentValues();

        if(kktRegId.fk_kktRegId_stores != null) {
            data.put("fk_kktRegId_stores", kktRegId.fk_kktRegId_stores.toString());
        }
        if(kktRegId.fk_kktRegId_stores_google_id != null)
        {
            data.put("fk_kktRegId_stores_google_id", kktRegId.fk_kktRegId_stores_google_id);
        }
        if(kktRegId.kktRegId != null) {
            data.put("kktRegId", kktRegId.kktRegId.toString());
        }
        else
            throw new Exception("Error kktRegId is null");
        data.put("_status", kktRegId._status != null ? kktRegId._status : 0);
        data.put("date_add", new Date().getTime());

        if(kktRegId.google_id != null)
        {
            data.put("google_id", kktRegId.google_id);
        }

        if(kktRegId.id != null) {
            id =(int) dbHelper.update(tableName, data, "id=?", new String[]{kktRegId.id.toString()});
        }
        else {
            id = (int) dbHelper.insert(tableName, null, data);
        }

        if(id == -1)
            //what to do with error
            return null;
        else if(kktRegId.id != null)
            return kktRegId.id;
        else
            return id;

    }

    public void updateInvoice (InvoiceData invoiceData)
    {
        Long dateInvoice = Long.valueOf(invoiceData.getDateInvoice(1));
        Map<String, Object> fStore = new HashMap<>();
        ContentValues data = new ContentValues();
        data.put("FP", invoiceData.FP);
        data.put("FD", invoiceData.FD);
        data.put("FN", invoiceData.FN);
        if(dateInvoice > 0)
            data.put("dateInvoice", dateInvoice);
        data.put("fullPrice", invoiceData.getFullPrice());
        data.put("in_basket", invoiceData.isIn_basket()==null ? 0: invoiceData.isIn_basket());

        fStore.put("FP", invoiceData.FP);
        fStore.put("FD", invoiceData.FD);
        fStore.put("FN", invoiceData.FN);
        fStore.put("FP_FD_FN",invoiceData.FP+"_"+invoiceData.FD+"_"+invoiceData.FN);
        if(dateInvoice > 0)
            fStore.put("dateInvoice",dateInvoice);
        fStore.put("fullPrice", invoiceData.getFullPrice());
        fStore.put("in_basket", invoiceData.isIn_basket()==null ? 0: invoiceData.isIn_basket());

        if(invoiceData.repeatCount != null) {
            data.put("repeatCount", invoiceData.repeatCount);

            fStore.put("repeatCount", invoiceData.repeatCount);
        }

        if(invoiceData.google_id != null) {
            data.put("google_id", invoiceData.google_id);

            fStore.put("google_id", invoiceData.google_id);
        }
        if(invoiceData.get_status() != null) {
            data.put("_status", invoiceData.get_status());
        }



        if(invoiceData.get_order()!=null) {
            data.put("_order", invoiceData.get_order());

            fStore.put("_order", invoiceData.get_order());
        }
        if(invoiceData.getFk_invoice_accountinglist() !=null) {
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());
        }

        if(invoiceData.get_status() != null && invoiceData.get_status()==1 ) {
            fStore.put("server_status", 1);
            data.put("server_status", 1);
        }
        else if(invoiceData.server_status!= null) {
            fStore.put("server_status", invoiceData.server_status);
            data.put("server_status", invoiceData.server_status);
        }
        else {
            fStore.put("server_status", 0);
            data.put("server_status", 0);
        }

        if(invoiceData.store!= null && invoiceData.store.id != null) {
            data.put("fk_invoice_stores", invoiceData.store.id);
        }
        if (invoiceData.kktRegId!= null && invoiceData.kktRegId.id!= null) {
            data.put("fk_invoice_kktRegId", invoiceData.kktRegId.id);
        }

        if (invoiceData.latitudeAdd != null) {
            data.put("latitudeAdd", invoiceData.latitudeAdd);

            fStore.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if (invoiceData.longitudeAdd != null) {
            data.put("longitudeAdd", invoiceData.longitudeAdd);

            fStore.put("longitudeAdd", invoiceData.longitudeAdd);
        }

        if (invoiceData.fk_invoice_kktRegId_google_id != null) {
            data.put("fk_invoice_kktRegId_google_id", invoiceData.fk_invoice_kktRegId_google_id);

            fStore.put("fk_invoice_kktRegId_google_id", invoiceData.fk_invoice_kktRegId_google_id);
        }

        if (invoiceData.fk_invoice_stores_google_id != null) {
            data.put("fk_invoice_stores_google_id", invoiceData.fk_invoice_stores_google_id);

            fStore.put("fk_invoice_stores_google_id", invoiceData.fk_invoice_stores_google_id);
        }

        if (invoiceData.fk_invoice_accountinglist_google_id != null) {
            data.put("fk_invoice_accountinglist_google_id", invoiceData.fk_invoice_accountinglist_google_id);

            fStore.put("fk_invoice_accountinglist_google_id", invoiceData.fk_invoice_accountinglist_google_id);
        }

        Integer id = invoiceData.getId();

        //обновляем на сервере только если законченное состояние чека: только добавлен, загружен с фнс, подтвержден пользователем
        if(On_line && user.google_id != null && invoiceData.google_id != null && invoiceData.get_status() >= 0 )
        {
            mFirestore.collection(tableNameInvoice).document(invoiceData.google_id).update(fStore);

            Map<String, Object> updateChildren = new HashMap<>();
            updateChildren.put("/"+tableNameInvoice+"/"+invoiceData.google_id, fStore);
            mFirebase.updateChildren(updateChildren);
        }


        int count = dbHelper.update(tableNameInvoice, data, "id=?", new String[]{id + ""});
        Log.d(LOG_TAG, "updated rows "+count+" with id "+id);
        
    }

    public void insertInvoiceData ()
    {
        for(int i=0; i<invoices.size(); i++)
        {
            InvoiceData invoiceData = invoices.get(i);
            if(invoiceData.getId() == null)
            {
                ContentValues data = new ContentValues();
                data.put("FP", invoiceData.FP);
                data.put("FD", invoiceData.FD);
                data.put("FN", invoiceData.FN);
                data.put("dateInvoice", invoiceData.getDateInvoice(1));
                data.put("fullPrice", invoiceData.getFullPrice());
                data.put("_order", invoiceData.get_order());


                
                if(dbHelper.query(tableNameInvoice, null, "FP=? and FD=? and FN=?",
                        new String[]{invoiceData.FP, invoiceData.FD, invoiceData.FN}, null, null, null, null).getCount() == 0) {
                    long count = dbHelper.insert(tableNameInvoice, null, data);
                    if (count > -1) {
                        invoiceData.setId((int) count);
                        //updateInformation in collection
                        invoices.set(i, invoiceData);
                    }

                    Log.d(LOG_TAG, "Inserted record id: " + count);
                }
                else
                {
                    Log.d(LOG_TAG, "Insert record ERROR: record with FP/FD/FN exist: " + invoiceData.FP +"/"+ invoiceData.FD +"/"+ invoiceData.FN +"\n");
                }
                
            }
        }



    }

    public void deleteInvoiceData(final InvoiceData invoiceData)
    {
        
        long count = dbHelper.delete(tableNameInvoice, "id=?", new String[]{invoiceData.getId().toString()});
        if(count>0) {
            if(On_line && invoiceData.google_id != null)
            {
                mFirestore.collection(tableNameInvoice).document(invoiceData.google_id).delete();
                mFirestore.collection("purchases").whereEqualTo("fk_purchases_invoice_google_id", invoiceData.google_id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot document : documents)
                        {
                            mFirestore.collection("purchases").document(document.getId()).delete();
                        }
                    }
                });

                mFirestore.collection("linked_objects").whereEqualTo("fk_id_google_id", invoiceData.google_id).whereEqualTo("fk_name", tableNameInvoice).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot document : documents)
                        {
                            mFirestore.collection("linked_objects").document(document.getId()).delete();
                        }
                    }
                });

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Map<String, Object> childUpdates = new HashMap<>();

                        final Boolean[] deleteInProgress1 = {true};
                        final Boolean[] deleteInProgress2 = {true};
                        Query refLnk = mFirebase.child("linked_objects").orderByChild("fk_id_google_id").equalTo( invoiceData.google_id);
                        refLnk.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                                {
                                    childUpdates.put("/linked_objects/"+postSnapshot.getKey(), null);
                                }
                                deleteInProgress1[0] = false;
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                deleteInProgress1[0] = false;
                            }
                        });

                        Query ref = mFirebase.child("purchases").orderByChild("fk_purchases_invoice_google_id").equalTo( invoiceData.google_id);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                                {
                                    childUpdates.put("/purchases/"+postSnapshot.getKey(), null);
                                }
                                deleteInProgress2[0] = false;
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                deleteInProgress2[0] = false;
                            }
                        });

                        while(deleteInProgress1[0] || deleteInProgress2[0])
                        {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        childUpdates.put("/invoice/"+invoiceData.google_id, null);
                        if(childUpdates.size()>0)
                        {
                            mFirebase.updateChildren(childUpdates);
                        }
                    }
                });
                thread.start();

            }
            count = dbHelper.delete("purchases", "fk_purchases_invoice=?", new String[]{invoiceData.getId().toString()});
            count = dbHelper.delete("linked_objects", "fk_name=? AND fk_id=?", new String[]{tableNameInvoice, invoiceData.getId().toString()});
            /*for (int i = 0; i < invoices.size(); i++) {
                InvoiceData invoiceData = invoices.get(i);
                if (invoiceData.getId() == id)
                {
                    invoices.remove(i);
                }
            }*/
            count = dbHelper.delete("collectedData", "fk_collectedData_invoice=?", new String[]{invoiceData.getId().toString()});

        }
        
    }

    public int getCount(Map<String, String[]> filter) {
        Cursor cur = null;
        if(filter == null) {
            cur = dbHelper.query(tableNameInvoice, null, null, null, null, null, null, null);
        }
        else
        {
            String selection="";
            List<String> selectionArgs=new ArrayList<>();
            for(Map.Entry<String, String[]> entry : filter.entrySet())
            {
                if(entry.getValue().length>1)
                {
                    selection += "(";
                    for(String val : entry.getValue())
                    {
                        selection += entry.getKey()+"=? OR ";
                        selectionArgs.add(val);
                    }
                    selection = selection.substring(0, selection.length()-4)+") AND ";
                }
                else
                {
                    selection += entry.getKey()+"=? AND ";
                    selectionArgs.add(entry.getValue()[0]);
                }
            }
            selection = selection.substring(0, selection.length()-5);
            String[] args = selectionArgs.toArray(new String[selectionArgs.size()]);
            cur = dbHelper.query(tableNameInvoice, null, selection, args, null, null, null, null);
        }
        int count = cur.getCount();
        cur.close();
        if(count >0)
            return count;
        else
            return 0;
    }

    public Double[] findBestLocation(InvoiceData.Store store) {
        Double[] latLng = new Double[2];
        String selection="";
        String[] args;
        int count=0;
        if(store.name_from_fns!= null&& store.address_from_fns != null && store.address_from_fns != "" && store.inn != null)
        {
            selection = "name_from_fns=? AND address_from_fns=? AND inn=? AND _status=? AND (latitude notnull AND longitude notnull)";
            args = new String[]{store.name_from_fns, store.address_from_fns, store.inn.toString(), "1"};
        }
        else if (store.name_from_fns!= null && store.inn != null)
        {
            selection = "name_from_fns=? AND inn=? AND _status=? AND (latitude notnull AND longitude notnull)";
            args = new String[]{store.name_from_fns, store.inn.toString(), "1"};
        }
        else if(store.inn != null)
        {
            selection = "inn=? AND _status=? AND (latitude notnull AND longitude notnull)";
            args = new String[]{ store.inn.toString(), "1"};
        }
        else
        {
            return  null;
        }
        Cursor cur = dbHelper.query(tablenameStores, null, selection, args,null, null, null, null);

        if(cur.moveToFirst())
        {
            int id = cur.getInt(cur.getColumnIndex("id"));
            do {
                Cursor curInvoice = dbHelper.query(tableNameInvoice, null, "fk_invoice_stores=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))}, null, null, null, null);
                int tmp = curInvoice.getCount();
                if(count< tmp) {
                    count = tmp;
                    latLng[0] = cur.getDouble(cur.getColumnIndex("latitude"));
                    latLng[1] = cur.getDouble(cur.getColumnIndex("longitude"));
                }
            }
            while(cur.moveToNext());
        }
        return  latLng;
    }
}
