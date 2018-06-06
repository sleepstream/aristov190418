package com.sleepstream.checkkeeper.invoiceObjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;
import android.util.Log;
import com.sleepstream.checkkeeper.GetFnsData;
import com.sleepstream.checkkeeper.Navigation;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;
import static com.sleepstream.checkkeeper.MainActivity.log;

public class Invoice {

    final String LOG_TAG = "InvoiceClass";
    private Navigation navigation;
    public List<InvoiceData> invoices = new ArrayList<>();
    public List<InvoiceData> pinnedItems = new ArrayList<>();



    public String getTableNameInvoice() {
        return tableNameInvoice;
    }

    private String tableNameInvoice ="invoice";
    private String tablenameStores ="stores";
    private String tableJsonData ="collectedData";
    private String tableNameKktRegId = "kktRegId";
    private String tableNamePurchases = "purchases";
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


    //update 12.02.18
    //add loadData function
    public Invoice(Navigation navigation) {
        this.navigation = navigation;

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

    //update 12.02.18
    //add loadData function
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

    //update 12.02.18
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
                        Long.parseLong(cur.getString(cur.getColumnIndex("date"))),
                        cur.getString(cur.getColumnIndex("fullprice")),
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

    public String addInvoice (Integer position, InvoiceData invoiceData)
    {

        Log.d(LOG_TAG, "Try to add invoice");
        //just add data from QR - first time to save and check already exist
        String FP = invoiceData.getFP();
        String FD= invoiceData.getFD();
        String FN = invoiceData.getFN();
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
                    ContentValues data = new ContentValues();
                    data.put("in_basket", 0);
                    dbHelper.update(tableNameInvoice, data, "id=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))});
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
                    ContentValues data = new ContentValues();
                    data.put("in_basket", 0);
                    dbHelper.update(tableNameInvoice, data, "id=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))});

                }
                cur.close();
                boolean notPresent = false;
                for(int i=0; i<invoices.size(); i++) {
                    invoiceData = invoices.get(i);
                    if(invoiceData.getFD().equals(FD) && invoiceData.getFP().equals(FP)&& invoiceData.getFN().equals(FN))
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
        ContentValues data = new ContentValues();
        data.put("FP", FP);
        data.put("FD", FD);
        data.put("FN", FN);
        data.put("_status", 0);
        data.put("in_basket", 0);
        data.put("date", dateInvoice);
        data.put("date_day", invoiceData.date_day);
        data.put("fullPrice", fullPrice);
        data.put("date_add", new Date().getTime());

        if(invoiceData.longitudeAdd != null && invoiceData.latitudeAdd != null)
        {
            data.put("longitudeAdd", invoiceData.longitudeAdd);
            data.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if(checkFilter("fk_invoice_accountinglist", null))
            data.put("fk_invoice_accountinglist", filterParam.get("fk_invoice_accountinglist")[0]);
        if(position!=null)
        {
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());
            data.put("_order", invoiceData.getOrder().toString());
        }

        id = dbHelper.insert(tableNameInvoice, null, data);
        if(id>-1)
        {
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
        
        return "";
    }

    public void setStoreDataFull(InvoiceData finalInvoiceData)
    {
        if(finalInvoiceData.fromFNS)
        {
            InvoiceData.KktRegId kktRegId = finalInvoiceData.kktRegId;
            Cursor cur_kktRegId= null;

            cur_kktRegId=dbHelper.query(tableNameKktRegId, null, "kktRegId=?", new String[]{kktRegId.kktRegId.toString()}, null, null, null, null);
            if(cur_kktRegId.moveToFirst())
            {
                Integer fk_kktRegId_stores = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId_stores"));
                Integer kktRegId_id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));
                cur_kktRegId.close();
                Map<String, String> args = new ArrayMap<String, String>();
                args.put("id", fk_kktRegId_stores.toString());
                List<InvoiceData.Store> stores = loadDataFromStore(args);//store should be one for kkt number
                if(stores.size()>0)
                {
                    InvoiceData.Store store = stores.get(0);
                    if(finalInvoiceData.store.id!= null && finalInvoiceData.store.id != store.id)
                    {
                        // first update values in tables where link to store is
                        finalInvoiceData.kktRegId.id = kktRegId_id;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("fk_invoice_stores",store.id);
                        contentValues.put("fk_invoice_kktRegId",kktRegId_id);
                        dbHelper.update(tableNameInvoice, contentValues, "id=?", new String[]{finalInvoiceData.getId().toString()});

                        contentValues =  new ContentValues();
                        contentValues.put("fk_purchases_stores", store.id);
                        dbHelper.update(tableNamePurchases, contentValues, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});

                        //try to delete old store
                        deleteStoreLink(finalInvoiceData.store.id);

                        finalInvoiceData.store = store;

                    }
                    else if(finalInvoiceData.store.id== null)
                    {
                        finalInvoiceData.store.id = store.id;
                        finalInvoiceData.kktRegId.id = kktRegId_id;
                        finalInvoiceData.set_status(1);
                       // finalInvoiceData.setfk_invoice_stores(store.id);
                        //finalInvoiceData.setfk_invoice_kktRegId(kktRegId_id);
                        //updateInvoice(finalInvoiceData);
                    }

                }

            }
            else
            {
                if(finalInvoiceData.store.id!= null && finalInvoiceData.store.id != 0)
                {
                    finalInvoiceData.kktRegId.fk_kktRegId_stores = finalInvoiceData.store.id;
                    setStoreData(finalInvoiceData);
                    try {
                        finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                    } catch (Exception e) {
                        log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                        e.printStackTrace();
                        finalInvoiceData.kktRegId.id = 0;
                    }
                    //finalInvoiceData.setfk_invoice_kktRegId(finalInvoiceData.kktRegId.id);
                }
                else
                {
                    finalInvoiceData.store._status = 0;
                    finalInvoiceData.store.update = false;
                    setStoreData(finalInvoiceData);
                    if(finalInvoiceData.store.id >0) {
                        finalInvoiceData.kktRegId.fk_kktRegId_stores = finalInvoiceData.store.id;
                        try {
                            finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                        } catch (Exception e) {
                            log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                            e.printStackTrace();
                            finalInvoiceData.kktRegId.id = 0;
                        }
                        //finalInvoiceData.setfk_invoice_kktRegId(finalInvoiceData.kktRegId.id);
                    }

                }
                //updateInvoice(finalInvoiceData);
            }

        }
        else
        {
            if(finalInvoiceData.store.place_id!= null && finalInvoiceData.store.place_id.length()>0)
            {
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
                            finalInvoiceData.store._status = 1;
                            finalInvoiceData.store.update = true;
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
                            //contentValues =  new ContentValues();
                            //contentValues.put("fk_kktRegId_stores", id);
                            //dbHelper.update(tableNameKktRegId, contentValues, "fk_kktRegId_stores=?", new String[]{finalInvoiceData.store.id.toString()});


                            //try to delete old store
                            deleteStoreLink(finalInvoiceData.store.id);
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
                        finalInvoiceData.store.update = true;
                    }
                    finalInvoiceData.store._status = 1;
                    setStoreData(finalInvoiceData);
                    //finalInvoiceData.setfk_invoice_stores(finalInvoiceData.store.id);
                    //updateInvoice(finalInvoiceData);

                }

            }
        }
        if(finalInvoiceData.store._status == 1 && finalInvoiceData.kktRegId!= null && finalInvoiceData.kktRegId.kktRegId>0)
        {
            finalInvoiceData.set_status(1);
            //updateInvoice(finalInvoiceData);
            finalInvoiceData.kktRegId._status = 1;
            try {
                finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
            } catch (Exception e) {
                log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
                finalInvoiceData.kktRegId.id = 0;
            }
        }
    }

    public void setStoreData(InvoiceData invoiceData) {

        InvoiceData.Store store = invoiceData.store;

        ContentValues data = new ContentValues();
        if(store.name != null)
            data.put("name", store.name.trim());
        if(store.address != null)
            data.put("address",  store.address.trim());
        if(store.latitude != null)
            data.put("latitude", store.latitude.toString());
        if(store.longitude != null)
            data.put("longitude",  store.longitude.toString());
        if(store.inn != null)
            data.put("inn", store.inn);
        if(store.name_from_fns != null)
            data.put("name_from_fns", store.name_from_fns. trim());
        if(store.address_from_fns != null)
            data.put("address_from_fns", store.address_from_fns.trim());
        if(store.place_id != null)
            data.put("place_id", store.place_id);
        if(store.store_type != null)
            data.put("store_type", store.store_type);
        if(store.iconName != null)
            data.put("iconName", store.iconName);
        if(store.photo_reference != null)
            data.put("photo_reference", store.photo_reference);
        if(store._status != null)
            data.put("_status", store._status);

        if(store.date_add == null)
            data.put("date_add", new Date().getTime());

        if(store.update)
        {
            dbHelper.update(tablenameStores, data, "id=?", new String[]{store.id.toString()});
        }
        else
        {
            store.id = (int)dbHelper.insert(tablenameStores, null, data);
        }
    }

    private void deleteStoreLink(Integer Id)
    {
        Cursor cur = dbHelper.query(tableNameInvoice, null, "fk_invoice_stores=?", new String[]{Id.toString()}, null, null, null, null);
        boolean delete = false;
        if(!cur.moveToFirst())
            delete = true;
        else
            delete = false;
        cur = dbHelper.query(tableNamePurchases, null, "fk_purchases_invoice=?", new String[]{Id.toString()}, null, null, null, null);
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

                listStore.add(store);
            }
            while(cur_Store. moveToNext());
            cur_Store.close();
        }


        return listStore;
    }

    public int fillReceiptData(GetFnsData.Receipt receipt, InvoiceData finalInvoiceData)
    {
        
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
        //2018-01-26T18:34:00
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
                updateData.put("date", invoiceDate);
                updateData.put("date_day", invoiceDate_day);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        log.info(LOG_TAG+"\n"+receipt.totalSum +" totalSum");
        updateData.put("fullprice", Float.parseFloat(receipt.totalSum)/100);
        updateData.put("ecashTotalSum", Float.parseFloat(receipt.ecashTotalSum)/100);
        updateData.put("cashTotalSum", Float.parseFloat(receipt.cashTotalSum)/100);
        if(finalInvoiceData.kktRegId.id != null) {
            updateData.put("fk_invoice_kktRegId", finalInvoiceData.kktRegId.id);
        }
        if(finalInvoiceData.store.id != null) {
            updateData.put("fk_invoice_stores", finalInvoiceData.store.id);
        }
        updateData.put("_status", finalInvoiceData.get_status());

        dbHelper.update(tableNameInvoice, updateData, "id=?", new String[]{finalInvoiceData.getId().toString()});
        Log.d(LOG_TAG, "updated invoice id " +finalInvoiceData.getId());


        //Start adding purchases and prod
        ArrayList<GetFnsData.Items> items = receipt.items;
        for(GetFnsData.Items item: items)
        {
            int fk_purchases_products=-1;

            //check in products db
            if(item.name != null) {
                cur_products = dbHelper.query("Products", null, "nameFromBill=?",
                        new String[]{item.name}, null, null, null, null);
                if(cur_products.moveToFirst())
                {
                    fk_purchases_products = cur_products.getInt(cur_products.getColumnIndex("id"));
                }
                cur_products.close();
            }
            if(item.name != null && fk_purchases_products == -1)
            {
                ContentValues values = new ContentValues();
                values.put("nameFromBill", item.name);
                fk_purchases_products = ((int) dbHelper.insert("Products", null, values));
            }
            else if(item.name == null)
            {
                ContentValues values = new ContentValues();
                values.put("nameFromBill", "empty");
                fk_purchases_products = ((int) dbHelper.insert("Products", null, values));
            }


            //error adding product stop
            if(fk_purchases_products ==-1 && countPurchases ==0)
                return -1;

            //add in table purchases
            ContentValues values = new ContentValues();
            values.put("fk_purchases_products", fk_purchases_products);
            values.put("fk_purchases_invoice", finalInvoiceData.getId());

            if(filterParam.containsKey("fk_invoice_accountinglist"))
                values.put("fk_purchases_accountinglist",filterParam.get("fk_purchases_accountinglist")[0]);

            values.put("prise_for_item", item.price/100);
            values.put("quantity", item.quantity);
            values.put("sum", item.sum/100);
            values.put("date_add", new Date().getTime());
            Long tmp = dbHelper.insert("purchases", null, values);
            if(tmp == -1)
                return -1;
            else
                countPurchases+=1;

            Log.d(LOG_TAG, "added purchase id " +tmp);
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
        if(kktRegId.fk_kktRegId_stores != null)
            data.put("fk_kktRegId_stores",kktRegId.fk_kktRegId_stores.toString());
        if(kktRegId.kktRegId != null)
            data.put("kktRegId", kktRegId.kktRegId.toString());
        else
            throw new Exception("Error kktRegId is null");
        data.put("_status", kktRegId._status != null ? kktRegId._status : 0);
        data.put("date_add", new Date().getTime());
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
        
        ContentValues data = new ContentValues();
        data.put("FP", invoiceData.getFP());
        data.put("FD", invoiceData.getFD());
        data.put("FN", invoiceData.getFN());
        data.put("date", invoiceData.getDateInvoice(1));
        data.put("fullprice", invoiceData.getFullPrice());
        data.put("in_basket", invoiceData.isIn_basket()==null ? 0: invoiceData.isIn_basket());
        if(invoiceData.repeatCount != null)
            data.put("repeatCount", invoiceData.repeatCount);
        if(invoiceData.get_status() != null)
            data.put("_status", invoiceData.get_status());

        if(invoiceData.getOrder()!=null)
            data.put("_order", invoiceData.getOrder().toString());
        if(invoiceData.getFk_invoice_accountinglist() !=null)
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());

        if(invoiceData.store!= null && invoiceData.store.id != null)
            data.put("fk_invoice_stores", invoiceData.store.id.toString());
        if (invoiceData.getfk_invoice_kktRegId() != null)
            data.put("fk_invoice_kktRegId", invoiceData.getfk_invoice_kktRegId());

        if (invoiceData.latitudeAdd != null)
            data.put("latitudeAdd", invoiceData.latitudeAdd);
        if (invoiceData.longitudeAdd != null)
            data.put("longitudeAdd", invoiceData.longitudeAdd);

        Integer id = invoiceData.getId();

        dbHelper.update(tableNameInvoice, data, "id=?", new String[]{id+""});
        
    }



    public void insertInvoiceData ()
    {
        for(int i=0; i<invoices.size(); i++)
        {
            InvoiceData invoiceData = invoices.get(i);
            if(invoiceData.getId() == null)
            {
                ContentValues data = new ContentValues();
                data.put("FP", invoiceData.getFP());
                data.put("FD", invoiceData.getFD());
                data.put("FN", invoiceData.getFN());
                data.put("date", invoiceData.getDateInvoice(1));
                data.put("fullprice", invoiceData.getFullPrice());
                data.put("_order", invoiceData.getOrder());


                
                if(dbHelper.query(tableNameInvoice, null, "FP=? and FD=? and FN=?",
                        new String[]{invoiceData.getFP(), invoiceData.getFD(), invoiceData.getFN()}, null, null, null, null).getCount() == 0) {
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
                    Log.d(LOG_TAG, "Insert record ERROR: record with FP/FD/FN exist: " + invoiceData.getFP() +"/"+ invoiceData.getFD() +"/"+ invoiceData.getFN() +"\n");
                }
                
            }
        }



    }

    public void deleteInvoiceData(Integer id)
    {
        
        long count = dbHelper.delete(tableNameInvoice, "id=?", new String[]{id.toString()});
        if(count>0) {
            count = dbHelper.delete("purchases", "fk_purchases_invoice=?", new String[]{id.toString()});
            count = dbHelper.delete("linked_objects", "fk_name=? AND fk_id=?", new String[]{tableNameInvoice, id.toString()});
            /*for (int i = 0; i < invoices.size(); i++) {
                InvoiceData invoiceData = invoices.get(i);
                if (invoiceData.getId() == id)
                {
                    invoices.remove(i);
                }
            }*/
            count = dbHelper.delete("collectedData", "fk_collectedData_invoice=?", new String[]{id.toString()});

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
        if(store.address_from_fns != null && store.address_from_fns != "")
        {
            selection = "name_from_fns=? AND address_from_fns=? AND inn=? AND _status=? AND (latitude notnull AND longitude notnull)";
            args = new String[]{store.name_from_fns, store.address_from_fns, store.inn.toString(), "1"};
        }
        else
        {
            selection = "name_from_fns=? AND inn=? AND _status=? AND (latitude notnull AND longitude notnull)";
            args = new String[]{store.name_from_fns, store.inn.toString(), "1"};
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
