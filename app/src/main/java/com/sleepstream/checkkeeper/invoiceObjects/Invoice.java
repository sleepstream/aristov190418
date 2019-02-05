package com.sleepstream.checkkeeper.invoiceObjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.*;
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
    public final static String tableNameStoresFromFns ="stores_from_fns";
    public final static String tableNameStoresOnMap ="stores_on_map";
    public final static String tableNameKktRegId = "kktRegId";
    public final static String tableNamePurchases = "purchases";
    public final static String tableNameProducts = "products";
    public final static String tableNameCollectedData = "collectedData";
    public final static String tableNameBarCodeProduct = "bar_code_product";
    public final static String tableNameStoresLinks = "stores_links";
    public final static String tableNameKktRegIdStoresLinks = "kktRegId_store_links";
    public final static String tableNameLinkedObjects = "linked_objects";
    public final static String tableNameAccountinglist__invoice_links = "accountinglist__invoice_links";
    public final static String tableNameAccountinglist__purchases_links = "accountinglist__purchases_links";

    public final static String tableNameProduct_category_data = "product_category_data";
    public final static String tableNameProduct_category = "product_category";

    private static  boolean tablenameStoresSnapshot = false;

    public Integer lastIDCollection;
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
        filterParam.remove(param);
        filterParam.put(param, new String[]{value});
    }

    public boolean checkFilter(String param, @Nullable Integer id)
    {
        if(param != null) {
            if (filterParam.containsKey(param)) {
                if(id != null && filterParam.get(param).equals(id)) {
                    return true;
                }
                else return id == null;
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

        if(user!= null && user.google_id!=null && On_line) {
            //addSnaphotListeners();

            //updateOlddataBase();
        }

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

    /*private void addSnaphotListeners() {
        if(!tablenameStoresSnapshot) {
            tablenameStoresSnapshot = true;
            mFirestore.collection(tableNameStoresOnMap).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots!= null) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot doc : documents) {
                            InvoiceData.Store_on_map store = doc.toObject(InvoiceData.Store_on_map.class);
                            if (store != null && store.place_id != null) {
                                store.google_id = doc.getId();
                                Cursor cur = dbHelper.query(tableNameStoresOnMap, null, "google_id=?", new String[]{store.google_id}, null, null, null, null);
                                if (cur.moveToFirst()) {
                                    store.id = cur.getInt(cur.getColumnIndex("id"));

                                    saveStoreDataLocal(null, store);
                                }
                                cur.close();
                            }
                        }
                    }
                }
            });
        }
    }
*/
    public void reLoadInvoice() {
        if(navigation != null )
            navigation.copyFiltersToInvoice();
        String selection ="";
        List<String> selectionArgs=new ArrayList<>();
        String tmp="";
        List<Integer> fk_accountinglists = new ArrayList<>();

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
            else if(key == "fk_invoice_accountinglist")
            {
                Cursor cur_acc = dbHelper.query(tableNameAccountinglist__invoice_links, null, "fk_accountinglist=?",
                        new String[]{value[0].toString()}, null, null, null, null);
                if(cur_acc.moveToFirst())
                {
                    String invoices_id_acc = " (";
                    do{
                        long id = cur_acc.getInt(cur_acc.getColumnIndex("fk_invoice"));
                        invoices_id_acc+= "id=? OR ";
                        selectionArgs.add(String.valueOf(id));
                        tmp += id+ " ";
                    }
                    while(cur_acc.moveToNext());
                    selection+=invoices_id_acc.substring(0, invoices_id_acc.length()-4)+") and ";

                }
                cur_acc.close();
            }
            else if(key.equals("text_search"))
            {}
            else if(key.equals("place_id"))
            {}
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
            //InvoicesPageFragment.placeChooserAdapter.swap(null);
            Log.d(LOG_TAG, " Reload No records in DB size "+ this.invoices.size());
        }
        
    }

    public List<InvoiceData> loadData(Cursor cur)
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
                        cur.getInt(cur.getColumnIndex("_order")));

                invoiceData.setIn_basket(cur.getInt(cur.getColumnIndex("in_basket")));
                invoiceData.set_status(cur.getInt(cur.getColumnIndex("_status")));
                //invoiceData.setfk_invoice_stores_from_fns(cur.getInt(cur.getColumnIndex("fk_invoice_stores_from_fns")));
                //invoiceData.setFk_invoice_stores_on_map(cur.getInt(cur.getColumnIndex("fk_invoice_stores_on_map")));

                if(!cur.isNull(cur.getColumnIndex("fk_invoice_kktRegId_store_links")))
                    invoiceData.fk_invoice_kktRegId_store_links = cur.getLong(cur.getColumnIndex("fk_invoice_kktRegId_store_links"));

                if(!cur.isNull(cur.getColumnIndex("latitudeAdd")))
                    invoiceData.latitudeAdd =cur.getDouble(cur.getColumnIndex("latitudeAdd"));
                if(!cur.isNull(cur.getColumnIndex("longitudeAdd")))
                    invoiceData.longitudeAdd =cur.getDouble(cur.getColumnIndex("longitudeAdd"));

                invoiceData.repeatCount =cur.getInt(cur.getColumnIndex("repeatCount"));

                if(!cur.isNull(cur.getColumnIndex("server_status")))
                    invoiceData.server_status =cur.getInt(cur.getColumnIndex("server_status"));

                invoiceData.setDate_add(cur.getLong(cur.getColumnIndex("date_add")));

                if(!cur.isNull(cur.getColumnIndex("google_id")))
                    invoiceData.google_id = cur.getString(cur.getColumnIndex("google_id"));

                if(!cur.isNull(cur.getColumnIndex("fk_invoice_accountinglist_google_id")))
                    invoiceData.fk_invoice_accountinglist_google_id =cur.getString(cur.getColumnIndex("fk_invoice_accountinglist_google_id"));
                if(!cur.isNull(cur.getColumnIndex("user_google_id")))
                    invoiceData.user_google_id =cur.getString(cur.getColumnIndex("user_google_id"));



                if(invoiceData.getId() !=null) {
                    Cursor cur_accountinglist = dbHelper.query(tableNameAccountinglist__invoice_links, null,"fk_invoice=?",
                            new String[]{invoiceData.getId().toString()}, null, null, null, null);
                    if(cur_accountinglist.moveToFirst())
                    {
                        List<Integer> fk_accountinglist = new ArrayList<>();
                        do{

                            invoiceData.setFk_invoice_accountinglist(cur_accountinglist.getInt(cur_accountinglist.getColumnIndex("fk_accountinglist")));
                        }
                        while(cur_accountinglist.moveToNext());

                    }
                    cur_accountinglist.close();

                    Cursor cur_purchases = dbHelper.query(tableNamePurchases, null, "fk_purchases_invoice=?", new String[]{invoiceData.getId().toString()}, null, null, null, null);
                    if (cur_purchases.moveToFirst()) {
                        invoiceData.quantity = cur_purchases.getCount();
                    }
                    cur_purchases.close();

                    if(invoiceData.fk_invoice_kktRegId_store_links!= null)
                    {
                        Cursor cur_kktRegId_store_links = dbHelper.query(tableNameKktRegIdStoresLinks, null, "id =?", new String[]{invoiceData.fk_invoice_kktRegId_store_links.toString()}, null, null, null, null);
                        if (cur_kktRegId_store_links.moveToFirst()) {
                            invoiceData.fk_invoice_kktRegId_store_links = cur_kktRegId_store_links.getLong(cur_kktRegId_store_links.getColumnIndex("id"));

                            Long fk_kktRegId = cur_kktRegId_store_links.getLong(cur_kktRegId_store_links.getColumnIndex("fk_kktRegId"));
                            if (fk_kktRegId != null && fk_kktRegId > 0) {
                                invoiceData.kktRegId = loadKKtRegIdLocal(fk_kktRegId, "id=?");
                            }
                            if (!cur_kktRegId_store_links.isNull(cur_kktRegId_store_links.getColumnIndex("fk_stores_links"))) {
                                invoiceData.fk_stores_links = cur_kktRegId_store_links.getLong(cur_kktRegId_store_links.getColumnIndex("fk_stores_links"));
                                invoiceData.fk_stores_links_google_id = cur_kktRegId_store_links.getString(cur_kktRegId_store_links.getColumnIndex("fk_stores_links_google_id"));
                                cur_kktRegId_store_links.close();

                                String query = null;

                                String[] args;
                                if (navigation != null && navigation.filterParam != null && navigation.filterParam.containsKey("text_search")) {
                                    String searchName = "%" + navigation.filterParam.get("text_search")[0] + "%";
                                    query = "SELECT * FROM " + tableNameStoresLinks + " WHERE  (fk_stores_from_fns in " +
                                            "(Select id from " + tableNameStoresFromFns + " where name_from_fns like ? ) or " +
                                            "fk_stores_on_map in (Select id from " + tableNameStoresOnMap + " where name like ?)) AND id=?";
                                    args = new String[]{searchName, searchName, invoiceData.fk_stores_links.toString()};
                                } else if (navigation != null && navigation.filterParam != null && navigation.filterParam.containsKey("place_id")) {
                                    args = new String[]{invoiceData.fk_stores_links.toString()};
                                    if (navigation.filterParam.get("place_id")[0].equals("not null")) {
                                        query = "SELECT * FROM " + tableNameStoresLinks + " WHERE  (fk_stores_on_map in " +
                                                "(Select id from " + tableNameStoresOnMap + " where place_id " + navigation.filterParam.get("place_id")[0] + ") ) AND id=?";
                                    } else {
                                        query = "SELECT * FROM " + tableNameStoresLinks + " WHERE  (fk_stores_on_map in " +
                                                "(Select id from " + tableNameStoresOnMap + " where place_id " + navigation.filterParam.get("place_id")[0] + ") or fk_stores_on_map " + navigation.filterParam.get("place_id")[0] + ") AND id=?";
                                    }
                                } else {
                                    query = "SELECT * FROM " + tableNameStoresLinks + " WHERE  id=?";
                                    args = new String[]{invoiceData.fk_stores_links.toString()};
                                }


                                Cursor cur_store_links = dbHelper.rawQuery(query, args);
                                if (cur_store_links.moveToFirst()) {
                                    Integer id_store_from_fns = cur_store_links.getInt(cur_store_links.getColumnIndex("fk_stores_from_fns"));
                                    Integer id_store_on_map = cur_store_links.getInt(cur_store_links.getColumnIndex("fk_stores_on_map"));

                                    if (id_store_from_fns > 0) {
                                        Map<String, String> selection = new ArrayMap<String, String>();
                                        selection.put("id", id_store_from_fns.toString());
                                        List<InvoiceData.Store_from_fns> store_from_fns_list = loadDataFromStore_from_fns(selection);
                                        if (store_from_fns_list.size() > 0) {
                                            invoiceData.store_from_fns = store_from_fns_list.get(0);
                                        }
                                    }
                                    if (id_store_on_map > 0) {
                                        Map<String, List<String>> selection = new ArrayMap<>();
                                        //selection = new ArrayMap<String, String>();
                                        selection.put("id", Arrays.asList(id_store_on_map.toString()));
                                        List<InvoiceData.Store_on_map> store_on_map_list = loadDataFromStore_on_map(selection, null);
                                        if (store_on_map_list.size() > 0) {
                                            invoiceData.store_on_map = store_on_map_list.get(0);
                                        }
                                    }
                                } else if (navigation.filterParam.containsKey("text_search")) {
                                    cur_store_links.close();
                                    continue;
                                } else if (navigation.filterParam.containsKey("place_id")) {
                                    cur_store_links.close();
                                    continue;
                                }
                                if (!cur_store_links.isClosed())
                                    cur_store_links.close();
                            }
                            else if (navigation.filterParam.containsKey("place_id") && navigation.filterParam.get("place_id")[0].equals("not null")) {
                                cur_kktRegId_store_links.close();
                                continue;
                            }
                        }
                        if(!cur_kktRegId_store_links.isClosed())
                            cur_kktRegId_store_links.close();
                    }
                    else if(navigation != null && navigation.filterParam.containsKey("place_id") && navigation.filterParam.get("place_id")[0].equals("not null"))
                    {
                        continue;
                    }
                }

                Cursor cur_linked_objects = dbHelper.query(tableNameLinkedObjects, null, "fk_name = ? and fk_id = ?", new String[]{tableNameInvoice, invoiceData.getId().toString()}, null, null, null, null);
                if(cur_linked_objects.moveToFirst())
                    invoiceData.setPinId(cur_linked_objects.getInt(cur_linked_objects.getColumnIndex("id")));
                invoiceDataTMP.add(invoiceData);
                cur_linked_objects.close();

            }
            while(cur.moveToNext());

            Log.d(LOG_TAG, "Loaded from DB records " + invoiceDataTMP.size());
        }
        return invoiceDataTMP;
    }

    public void addJsonData(InvoiceData invoiceData) {

        Log.d(LOG_TAG, "addJsonData 492");
        Map<String, Object> fStore = new HashMap<>();
        Integer fk_invoice = invoiceData.getId();
        String json = invoiceData.jsonData;
        String google_id = null;

        if(json.length()>0) {
            if (On_line && user.google_id != null) {
                fStore.put("json", json);
                if (invoiceData.google_id != null) {
                    Task<QuerySnapshot> result = mFirestore.collection(tableNameCollectedData).whereEqualTo("fk_collectedData_invoice_google_id", invoiceData.google_id).get(source);
                    while (!result.isComplete()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (result.isSuccessful()) {
                        Log.d(LOG_TAG, "addJsonData  result.isSuccessful() "+ new Exception().getStackTrace()[0].getLineNumber());
                        if (!result.getResult().getMetadata().isFromCache()) {
                            List<DocumentSnapshot> documents = result.getResult().getDocuments();
                            if (documents.size() <= 0) {
                                Log.d(LOG_TAG, "addJsonData  such invoice link not found save new "+ new Exception().getStackTrace()[0].getLineNumber());
                                fStore.put("fk_collectedData_invoice_google_id", invoiceData.google_id);
                                DocumentReference addedDocRef = mFirestore.collection(tableNameCollectedData).document();
                                google_id = addedDocRef.getId();
                                mFirestore.collection(tableNameCollectedData).document(google_id).set(fStore);
                                return;
                            } else {
                                Log.d(LOG_TAG, "addJsonData  such invoice link found  "+ new Exception().getStackTrace()[0].getLineNumber());
                                google_id = documents.get(0).getId();
                            }
                        }
                    } else {
                        Log.d(LOG_TAG, "addJsonData  repeat "+ new Exception().getStackTrace()[0].getLineNumber());
                        addJsonData(invoiceData);
                        return;
                    }
                }

            }
            Integer id = null;
            Cursor jsonCur = dbHelper.query(tableNameCollectedData, new String[]{"id"}, "fk_" + tableNameCollectedData + "_invoice=?", new String[]{String.valueOf(fk_invoice)}, null, null, null, null);
            if (jsonCur.getCount() > 1) {
                dbHelper.delete(tableNameCollectedData, "fk_" + tableNameCollectedData + "_invoice=?", new String[]{String.valueOf(fk_invoice)});
            } else if (jsonCur.getCount() == 1) {
                jsonCur.moveToFirst();
                id = jsonCur.getInt(jsonCur.getColumnIndex("id"));
            }
            jsonCur.close();
            ContentValues contentValues = new ContentValues();
            contentValues.put("fk_" + tableNameCollectedData + "_invoice", fk_invoice);
            contentValues.put("jsonData", json);

            if (google_id != null) {
                contentValues.put("google_id", google_id);
            }
            if (invoiceData.google_id != null)
                contentValues.put("fk_collectedData_invoice_google_id", invoiceData.google_id);

            if (id != null)
                dbHelper.update(tableNameCollectedData, contentValues, "id=?", new String[]{id.toString()});
            else
                dbHelper.insert(tableNameCollectedData, null, contentValues);
        }


        
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

                    //MainActivity.placeChooserAdapter.notifyItemInserted(this.lastIDCollection);
                    */
                }
                return "exist";
            }
            cur.close();
        }
        addInvoiceDataLocal(position, invoiceData);
        Log.d(LOG_TAG, this.lastIDCollection+"11 latst id collection");
        Log.d(LOG_TAG, "00 invoices size  "+invoice.invoices.size());
        if(On_line && user.google_id != null)
           addInvoiceDataServer(invoiceData);

        Log.d(LOG_TAG, " 01 invoices size  "+invoice.invoices.size());
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
            if(On_line && user.stores_from_fns_google_id != null && invoiceData.stores_from_fns_google_id == null)
            {
                fStore.put("user_google_id", user.stores_from_fns_google_id);
                //пераое сохранение данных
                DocumentReference addedDocRef =  mFirestore.collection(tableNameInvoice).document();
                addedDocRef.set(fStore);
                invoiceData.stores_from_fns_google_id = addedDocRef.getId();
                ContentValues val = new ContentValues();
                val.put("stores_from_fns_google_id", invoiceData.stores_from_fns_google_id);
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

        if(invoiceData.getDate_add() == null)
            invoiceData.setDate_add(new Date().getTime());

        data.put("date_add", invoiceData.getDate_add());


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
        
        if(invoiceData.get_order()!= null)
        {
            data.put("_order", invoiceData.get_order());
        }
        long id = dbHelper.insert(tableNameInvoice, null, data);
        Log.d(LOG_TAG, "Check inserted record id: " + id);
        if(id>0)
        {
            Log.d(LOG_TAG, "1Inserted record id: " + id);
            invoiceData.setId((int) id);

            insertNewAccountingList(invoiceData);

            if(position != null) {
                this.invoices.add(position, invoiceData);
                this.lastIDCollection = position;
            }
            else
            {
                invoiceData.setId((int) id);
                boolean i = this.invoices.add(invoiceData);
                this.updateInvoice(invoiceData);
                this.lastIDCollection =   (int)id;
            }

        }
        else
        {
            Log.d(LOG_TAG, "Inserted ERROR ");
        }


    }

    public void insertNewAccountingList(InvoiceData invoiceData)
    {
        if(invoiceData.getFk_invoice_accountinglist() !=null && invoiceData.getFk_invoice_accountinglist().size()>0) {
            long count = dbHelper.delete(tableNameAccountinglist__invoice_links, "fk_invoice=?", new String[]{invoiceData.getId().toString()});
            Log.d(LOG_TAG, "deleted links from "+tableNameAccountinglist__invoice_links+" count = " + count);
            for(Integer accId : invoiceData.getFk_invoice_accountinglist()) {
                ContentValues accountinglist_data = new ContentValues();
                accountinglist_data.put("fk_accountinglist", accId);
                accountinglist_data.put("fk_invoice", invoiceData.getId());
                count = dbHelper.insert(tableNameAccountinglist__invoice_links, null, accountinglist_data);
                Log.d(LOG_TAG, "inserted links to "+tableNameAccountinglist__invoice_links+" count = " + count);
            }
        }
    }

    public void  addInvoiceDataServer(InvoiceData invoiceData)
    {

        Log.d(LOG_TAG, "001 invoices size  "+invoice.invoices.size());
        Long dateInvoice = Long.parseLong(invoiceData.getDateInvoice(1));
        if(dateInvoice == 0)
        {
            dateInvoice = new Date().getTime();
        }

        Map<String, Object> fStore = new HashMap<>();
        /*
        if(invoiceData.getId() != null) {
            Cursor cur_data = dbHelper.query(tableNameCollectedData, null, "fk_collectedData_invoice=?", new String[]{invoiceData.getId().toString()}, null, null, null, null);
            if (cur_data.moveToFirst()) {
                invoiceData.jsonData = cur_data.getString(cur_data.getColumnIndex("jsonData"));
                fStore.put("jsonData", invoiceData.jsonData);
            }
            cur_data.close();
        }
        */

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


        /*if(invoiceData.store_from_fns.google_id != null)
            fStore.put("fk_invoice_stores_from_fns_google_id", invoiceData.store_from_fns.google_id);

        if(invoiceData.kktRegId.google_id != null)
            fStore.put("fk_invoice_kktRegId_google_id", invoiceData.kktRegId.google_id);
        */
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
        Log.d(LOG_TAG, "002 invoices size  "+invoice.invoices.size());

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
            Log.d(LOG_TAG, "003 invoices size  "+invoice.invoices.size());
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
                    invoiceData.google_id = addedDocRef.getId();
                    mFirestore.collection(tableNameInvoice).document(invoiceData.google_id).set(fStore);

                    //invoiceData.stores_from_fns_google_id = mFirebase.child(tableNameInvoice).push().getKey();

                    //mFirebase.child(tableNameInvoice).child(invoiceData.stores_from_fns_google_id).setValue(fStore);
                }
                ContentValues val = new ContentValues();


                if(invoiceData.google_id != null)
                    val.put("google_id", invoiceData.google_id);
                dbHelper.update(tableNameInvoice, val, "id=?", new String[]{String.valueOf(invoiceData.getId())});
            }

            Log.d(LOG_TAG, "004 invoices size  "+invoice.invoices.size());
        }
    }
/*
    public boolean writeInvoiceDataFromServer(InvoiceData invoiceData)
    {

        if (invoiceData.fk_invoice_stores_from_fns_google_id != null) {
            Task<DocumentSnapshot> result_store = mFirestore.collection(tableNameStoresFromFns).document(invoiceData.fk_invoice_stores_from_fns_google_id).get(source);
            while (!result_store.isComplete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (result_store.isSuccessful()) {
                InvoiceData.Store_from_fns store = result_store.getResult().toObject(InvoiceData.Store_from_fns.class);
                if (store != null) {
                    invoiceData.store_from_fns = store;
                    invoiceData.store_from_fns.stores_from_fns_google_id = result_store.getResult().getId();
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
                    invoiceData.kktRegId.stores_from_fns_google_id = result_kkt.getResult().getId();
                }
            }
        }

        if (invoiceData.kktRegId != null && invoiceData.kktRegId.stores_from_fns_google_id != null) {
            Cursor cur_kktRegId = dbHelper.query(tableNameKktRegId, null, "kktRegId=?", new String[]{invoiceData.kktRegId.kktRegId.toString()}, null, null, null, null);
            if (cur_kktRegId.moveToFirst()) {
                invoiceData.kktRegId.fk_kktRegId = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId"));
                invoiceData.kktRegId.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));

                cur_kktRegId.close();

                Cursor cur_store = dbHelper.query(tableNameStoresFromFns, null, "id=?", new String[]{invoiceData.kktRegId.fk_kktRegId.toString()}, null, null, null, null);
                if (cur_store.moveToFirst()) {
                    invoiceData.store_from_fns.id = cur_store.getInt(cur_store.getColumnIndex("id"));
                    int _status = cur_store.getInt(cur_store.getColumnIndex("_status"));
                    cur_store.close();
                    if (invoiceData.store_from_fns._status != _status && invoiceData.store_from_fns._status == 1) {
                        invoiceData.store_from_fns.update = true;
                        saveStoreDataLocal(invoiceData.store_from_fns, store);
                    }
                    //сравнить данные у пользователя и на сервере. обновить если статус магазина на сервера "подтвержден администарцией"
                    //магазин может быть пустой так как добавлена только геометка
                } else if (invoiceData.store_from_fns != null) {
                    //сохраняем магазин в локальной базе
                    invoiceData.store_from_fns.update = false;
                    saveStoreDataLocal(invoiceData.store_from_fns, store);
                }
            } else if (invoiceData.store_from_fns != null) {
                invoiceData.store_from_fns.update = false;
                saveStoreDataLocal(invoiceData.store_from_fns, store);
                try {
                    saveKktRegId(invoiceData.kktRegId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (invoiceData.store_from_fns != null && invoiceData.store_from_fns.stores_from_fns_google_id != null) {
            String selection = "stores_from_fns_google_id=?";
            String[] args = new String[]{invoiceData.store_from_fns.stores_from_fns_google_id};
            if (invoiceData.store_from_fns.place_id != null) {
                selection += "OR place_id=?";
                args = new String[]{invoiceData.store_from_fns.stores_from_fns_google_id, invoiceData.store_from_fns.place_id};
            }
            Cursor cur_store = dbHelper.query(tableNameStoresFromFns, null, selection, args, null, null, null, null);
            if (cur_store.moveToFirst()) {
                invoiceData.store_from_fns.id = cur_store.getInt(cur_store.getColumnIndex("id"));
                int _status = cur_store.getInt(cur_store.getColumnIndex("_status"));
                cur_store.close();
                if (invoiceData.store_from_fns._status != _status && invoiceData.store_from_fns._status == 1) {
                    invoiceData.store_from_fns.update = true;
                    saveStoreDataLocal(invoiceData.store_from_fns, store);
                }
            } else {
                invoiceData.store_from_fns.update = false;
                saveStoreDataLocal(invoiceData.store_from_fns, store);
            }
        }
        //загрузить покупки с севера
        Task<QuerySnapshot> result_purchases = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_invoice_google_id", invoiceData.stores_from_fns_google_id).get(source);
        while (!result_purchases.isComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (result_purchases.isSuccessful()) {
            Log.d(LOG_TAG, "find purchases "+result_purchases.getResult().isEmpty());
            List<DocumentSnapshot> documents_purchases = result_purchases.getResult().getDocuments();
            for (DocumentSnapshot documentSnapshot : documents_purchases) {
                PurchasesListData purchasesListData = documentSnapshot.toObject(PurchasesListData.class);
                if (purchasesListData != null) {
                    Integer fk_purchases_products = null;
                    //проеряем наличем продукта в локальной базе и загружаем его
                    if (purchasesListData.fk_purchases_products_google_id != null) {

                        Task<DocumentSnapshot> result_product = mFirestore.collection(tableNameProducts).document(purchasesListData.fk_purchases_products_google_id).get(source);
                        while (!result_product.isComplete()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (result_product.isSuccessful()) {
                            Log.d(LOG_TAG, "product from server exist " + result_product.getResult().exists() +"\n document id "+ purchasesListData.fk_purchases_products_google_id +"\n"
                                    + result_product.getException());
                            PurchasesListData.Product product = result_product.getResult().toObject(PurchasesListData.Product.class);
                            if(product != null) {
                                String fk_purchases_products_google_id;
                                Cursor cur_products = dbHelper.query(tableNameProducts, null, "nameFromBill=?",
                                        new String[]{product.nameFromBill}, null, null, null, null);
                                if (cur_products.moveToFirst()) {
                                    fk_purchases_products = cur_products.getInt(cur_products.getColumnIndex("id"));
                                    fk_purchases_products_google_id = cur_products.getString(cur_products.getColumnIndex("stores_from_fns_google_id"));
                                    cur_products.close();
                                } else {
                                    ContentValues values = new ContentValues();
                                    values.put("nameFromBill", product.nameFromBill);
                                    values.put("stores_from_fns_google_id", result_product.getResult().getId());
                                    fk_purchases_products = ((int) dbHelper.insert(tableNameProducts, null, values));
                                }
                            }
                        }
                    }
                    if (fk_purchases_products != null) {
                        ContentValues values = new ContentValues();
                        try {
                            //add in table purchases

                            values.put("fk_purchases_stores", invoiceData.store_from_fns.id);
                            values.put("fk_purchases_stores_google_id", invoiceData.store_from_fns.stores_from_fns_google_id);
                            values.put("fk_purchases_products_google_id", purchasesListData.fk_purchases_products_google_id);
                            values.put("fk_purchases_invoice_google_id", invoiceData.stores_from_fns_google_id);
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

        Log.d(LOG_TAG, "find purchases "+result_purchases.getResult().isEmpty());
        if (invoiceData.store_from_fns != null && invoiceData.store_from_fns.stores_from_fns_google_id != null && invoiceData.kktRegId != null && invoiceData.kktRegId.stores_from_fns_google_id != null
                && !result_purchases.getResult().isEmpty()) {
            invoiceData.set_status(1);
            updateInvoiceLocal(invoiceData);
            return true;
        } else
            return false;
    }

    public boolean writeInvoiceDataToServer(InvoiceData invoiceData)
    {

        //поиск чека на сервере
        String store_to_delete_googleId = null;
        WriteBatch batch = mFirestore.batch();

        Task<QuerySnapshot> task_invoice = mFirestore.collection(tableNameInvoice).whereEqualTo("FP_FD_FN", invoiceData.FP + "_" + invoiceData.FD + "_" + invoiceData.FN).get(Source.SERVER);
        while (!task_invoice.isComplete())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(task_invoice.isSuccessful())
        {
            //если запрос к серверу был успешным идем дальше. проверяем нашелся ли чек на сервере.
            if(!task_invoice.getResult().isEmpty())
            {
                List<DocumentSnapshot> documents = task_invoice.getResult().getDocuments();
                InvoiceData invoiceData_FromServer = documents.get(0).toObject(InvoiceData.class);
                if(invoiceData_FromServer != null)
                {
                    invoiceData.stores_from_fns_google_id = documents.get(0).getId();
                    if(invoiceData_FromServer.fk_invoice_kktRegId_google_id !=null)
                    {
                        //загружаем с сервера данные по кассе
                        invoiceData.fk_invoice_kktRegId_google_id = invoiceData_FromServer.fk_invoice_kktRegId_google_id;
                        Task<DocumentSnapshot> task_kktRegId = mFirestore.collection(tableNameKktRegId).document(invoiceData_FromServer.fk_invoice_kktRegId_google_id).get(Source.SERVER);
                        while (!task_kktRegId.isComplete())
                        {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(task_kktRegId.isSuccessful())
                        {
                            if(task_kktRegId.getResult().exists())
                            {
                                InvoiceData.KktRegId kktRegId_server = task_kktRegId.getResult().toObject(InvoiceData.KktRegId.class);
                                if(kktRegId_server != null)
                                {
                                    //проверяем что рег номер кассы на сервере и локально совпадают - иначе ОШИБКА В ДАННЫХ!!!
                                    if(kktRegId_server.kktRegId.equals(invoiceData.kktRegId.kktRegId))
                                    {
                                        //прописываем ссылки на найденные документы на сервере
                                        invoiceData.kktRegId.stores_from_fns_google_id = task_kktRegId.getResult().getId();
                                        kktRegId_server.stores_from_fns_google_id = task_kktRegId.getResult().getId();


                                        //сохраняем самую раннюю дату добавления кассы
                                        if(invoiceData.kktRegId.date_add > kktRegId_server.date_add)
                                            invoiceData.kktRegId.date_add = kktRegId_server.date_add;
                                        else
                                            kktRegId_server.date_add = invoiceData.kktRegId.date_add;

                                        //проверяем статус кассы
                                        if(invoiceData.kktRegId._status == 1 && kktRegId_server._status!= 1)
                                            kktRegId_server._status = 1;
                                        else if (invoiceData.kktRegId._status != 1 && kktRegId_server._status == 1)
                                            invoiceData.kktRegId._status = 1;

                                        //ссылка на магазин в чеке на сервере отличается от ссылки в кассе
                                        //это перестаховка - такого быть не может - это ошибка в данных
                                        if(invoiceData_FromServer.fk_invoice_stores_from_fns_google_id != null && !invoiceData_FromServer.fk_invoice_stores_from_fns_google_id.equals(kktRegId_server.fk_kktRegId_google_id))
                                        {
                                            //ссылка на магазин в чеке на сервере отличается от ссылки в кассе
                                            //ссылку в чеке заменить на ссылку в кассе
                                            kktRegId_server.fk_kktRegId_google_id = invoiceData_FromServer.fk_invoice_stores_from_fns_google_id;
                                            invoiceData.kktRegId.fk_kktRegId_google_id = kktRegId_server.fk_kktRegId_google_id;
                                        }

                                        invoiceData.kktRegId.fk_kktRegId_google_id = kktRegId_server.fk_kktRegId_google_id;


                                        //загружаем данные по магазину
                                        Task<DocumentSnapshot> task_store = mFirestore.collection(tableNameStoresFromFns).document(kktRegId_server.fk_kktRegId_google_id).get(Source.SERVER);
                                        while (!task_store.isComplete())
                                        {
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if(task_store.isSuccessful())
                                        {
                                            if(task_store.getResult().exists())
                                            {
                                                InvoiceData.Store_from_fns store_server = task_store.getResult().toObject(InvoiceData.Store_from_fns.class);
                                                if(store_server != null)
                                                {
                                                    //статус локального магазина не важен если на сервере статус 1
                                                    //данные магазина на сервере главнее - обновляем локальные по данным на сервере
                                                    if(store_server._status==1)
                                                    {
                                                        if(store_server.date_add > invoiceData.store_from_fns.date_add)
                                                            store_server.date_add = invoiceData.store_from_fns.date_add;
                                                        else
                                                            invoiceData.store_from_fns.date_add = store_server.date_add;
                                                        store_server.id = invoiceData.store_from_fns.id;

                                                        store_server.stores_from_fns_google_id = task_store.getResult().getId();
                                                        invoiceData.store_from_fns = store_server;
                                                    }
                                                    else
                                                    {
                                                        //если на сервере статус не 1 а локально магазин отмечен карте
                                                        //пробуем найти наиболее подходящий на сервере магазин
                                                        //поиск по place_id
                                                        if(invoiceData.store_from_fns.place_id!= null && invoiceData.store_from_fns.place_id.length()>0) {
                                                            Task<QuerySnapshot> task_store_finde = mFirestore.collection(tableNameStoresFromFns)
                                                                    .whereEqualTo("place_id", invoiceData.store_from_fns.place_id)
                                                                    //.whereEqualTo("inn", invoiceData.store_from_fns.inn)
                                                                    .get(Source.SERVER);

                                                            while (!task_store_finde.isComplete())
                                                            {
                                                                try {
                                                                    Thread.sleep(100);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                            if(task_store_finde.isSuccessful())
                                                            {
                                                                if(!task_store_finde.getResult().isEmpty())
                                                                {
                                                                    List<DocumentSnapshot> documents_stores = task_store_finde.getResult().getDocuments();
                                                                    InvoiceData.Store_from_fns doc_store_finde = documents_stores.get(0).toObject(InvoiceData.Store_from_fns.class);
                                                                    if(doc_store_finde != null)
                                                                    {
                                                                        //найден магазин по place-ID необходимо проверить что inn  в обоих магазинах одинаковый
                                                                        //могло случиться такое, что другой пользователь или текущий ошибочно указал это place_id для другого магазина
                                                                        //либо был указан place_id  торгового центра а inn  у точек разный
                                                                        //если inn не совпадают - надо загружать на сервер локальные данные и удалять привязанную версию магазина

                                                                        if(invoiceData.store_from_fns.inn != null && invoiceData.store_from_fns.inn>0 && doc_store_finde.inn == invoiceData.store_from_fns.inn)
                                                                        {
                                                                            //inn  одинаковые - необходимо обновить локальные данные по найденному магазину и заменить ссылки на магазин

                                                                            if(doc_store_finde.date_add > invoiceData.store_from_fns.date_add)
                                                                                doc_store_finde.date_add = invoiceData.store_from_fns.date_add;
                                                                            else
                                                                                invoiceData.store_from_fns.date_add = doc_store_finde.date_add;
                                                                            doc_store_finde.id = invoiceData.store_from_fns.id;

                                                                            doc_store_finde.stores_from_fns_google_id = documents_stores.get(0).getId();
                                                                            invoiceData.store_from_fns = doc_store_finde;

                                                                            //обновляем ссылки на магазин
                                                                            invoiceData.fk_invoice_stores_from_fns_google_id = doc_store_finde.stores_from_fns_google_id;
                                                                            invoiceData.kktRegId.fk_kktRegId_google_id = doc_store_finde.stores_from_fns_google_id;
                                                                            kktRegId_server.fk_kktRegId_google_id = doc_store_finde.stores_from_fns_google_id;

                                                                            //удаляем старый магазин
                                                                            //чтобы удалить мы должны обновить покупки, сам чек и кассу - удалять магазин будем позже.
                                                                            store_to_delete_googleId = store_server.stores_from_fns_google_id;
                                                                        }
                                                                        else
                                                                        {
                                                                            //загружаем локальный вариант на сервер
                                                                            saveStoreDataServer(invoiceData.store_from_fns);
                                                                        }
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    //загружаем локальный вариант на сервер
                                                                    saveStoreDataServer(invoiceData.store_from_fns);
                                                                }
                                                            }
                                                        }
                                                        else
                                                        {
                                                            //загружаем локальный вариант на сервер
                                                            saveStoreDataServer(invoiceData.store_from_fns);
                                                        }
                                                    }
                                                }
                                            }
                                            //если не найден - ошибка в данных!!!!!!

                                        }

                                        //обновляем кассу на сервере
                                        try {
                                            saveKktRegIdServer(kktRegId_server);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else
                                    {
                                        //если полученная по ссылке касса имеет другой рег номер ищем на сервере по рег номеру, и обновляем ссылки
                                    }
                                }
                            }
                        }
                    }
                    //если на сервере нет ссылки на кассу пробуем найти кассу, если она есть локально, на сервере
                    else
                    {
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
                                    //касса найдена - необходимо привязать кассу к текущему чеку
                                    InvoiceData.KktRegId kktRegId_from_server = kkt.get(0).toObject(InvoiceData.KktRegId.class);
                                    if(kktRegId_from_server != null) {
                                        kktRegId_from_server.stores_from_fns_google_id = kkt.get(0).getId();

                                        //если ссылка локальная на сервер пустая либо отличается от ссылки на найденную кассу
                                        if (invoiceData.kktRegId.stores_from_fns_google_id == null ||  !invoiceData.kktRegId.stores_from_fns_google_id.equals(kktRegId_from_server.stores_from_fns_google_id)) {
                                            //ссылка локальная устарела,  нужно обновить локальную ссылку
                                            //обновляем локальные данные
                                            invoiceData.kktRegId.stores_from_fns_google_id = kktRegId_from_server.stores_from_fns_google_id;
                                            invoiceData.fk_invoice_kktRegId_google_id = kktRegId_from_server.stores_from_fns_google_id;
                                            //обновляем локальную копию кассы
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
                                    //необходимо добавить магазин на сервер, предварительно проверив что магазина с placeid (если не null) не усуществует на сервере
                                    if (invoiceData.store_from_fns.place_id!= null)
                                    {}
                                    invoiceData.kktRegId.stores_from_fns_google_id = null;
                                    try {
                                        saveKktRegIdServer(invoiceData.kktRegId);
                                        if(invoiceData.kktRegId.stores_from_fns_google_id != null)
                                        {
                                            invoiceData.fk_invoice_kktRegId_google_id = invoiceData.kktRegId.stores_from_fns_google_id;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    //если ссылка на магазин существует, а ссылка на кассу нет
                    //если есть ссылка на кассу ранее мы обработали ссылку на магазин
                    if(invoiceData_FromServer.fk_invoice_stores_from_fns_google_id != null && invoiceData_FromServer.fk_invoice_kktRegId_google_id == null) {
                        invoiceData.fk_invoice_stores_from_fns_google_id = invoiceData_FromServer.fk_invoice_stores_from_fns_google_id;


                    }
                }
            }
        }

        boolean loadKktToServer = false;
        boolean loadStoreToServer = false;
                //проверяем есть ли на сервере касса если она есть локально
        if (invoiceData.getfk_invoice_kktRegId() != null) {
            //если ссылка на кассу есть а данные еще не загружены - получаем из локальной базы данные
            if(invoiceData.kktRegId == null || invoiceData.kktRegId.kktRegId == null)
            {
                Cursor cur_kktRegId = dbHelper.query(tableNameKktRegId, null, "id=?", new String[]{invoiceData.getfk_invoice_kktRegId().toString()}, null, null, null, null);
                if (cur_kktRegId.moveToFirst()) {
                    invoiceData.kktRegId.kktRegId = (long) cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("kktRegId"));
                    invoiceData.kktRegId.stores_from_fns_google_id = cur_kktRegId.getString(cur_kktRegId.getColumnIndex("stores_from_fns_google_id"));
                    invoiceData.kktRegId._status = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("_status"));
                    invoiceData.kktRegId.fk_kktRegId = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("fk_kktRegId"));
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
                            kktRegId.stores_from_fns_google_id = kkt.get(0).getId();

                            if (invoiceData.kktRegId.stores_from_fns_google_id == null ||  !invoiceData.kktRegId.stores_from_fns_google_id.equals(kktRegId.stores_from_fns_google_id)) {
                                //ссылка локальная устарела,  нужно обновить локальную ссылку
                                //обновляем локальные данные
                                invoiceData.kktRegId.stores_from_fns_google_id = kktRegId.stores_from_fns_google_id;
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
                        invoiceData.kktRegId.stores_from_fns_google_id = null;
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
                    //необходимо сверить ссылки на магазины, но на сервере ссылка - stores_from_fns_google_id  магазин сейчас хранится только локально.
                    //если касса не подтверждена локально то и магазин тоже - его локально можно смело затирать данными с сервера
                    //нужно загрузить данные по магазину и обновить информацию локально по кассе и магазину
                    if(kktRegId.fk_kktRegId_google_id !=null && !kktRegId.fk_kktRegId_google_id.equals(invoiceData.kktRegId.fk_kktRegId_google_id))
                    {
                        Task<DocumentSnapshot> result_store = mFirestore.collection(tableNameStoresFromFns).document(kktRegId.fk_kktRegId_google_id).get(source);
                        while (!result_store.isComplete()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(result_store.isSuccessful())
                        {
                            InvoiceData.Store_from_fns store = result_store.getResult().toObject(InvoiceData.Store_from_fns.class);

                            if(store != null)
                            {
                                store.stores_from_fns_google_id = result_store.getResult().getId();
                                //если магазин найден - обновляем информацию локально по кассе и по магазину
                                store.id = invoiceData.kktRegId.fk_kktRegId;
                                kktRegId.id = invoiceData.kktRegId.id;
                                kktRegId.fk_kktRegId = store.id;
                                invoiceData.kktRegId = kktRegId;
                                invoiceData.store_from_fns = store;
                                saveStoreDataLocal(invoiceData.store_from_fns, store);
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
                {
                    invoiceData.kktRegId.stores_from_fns_google_id = null;
                    loadKktToServer = true;
                    /*
                    try {
                        saveKktRegIdServer(invoiceData.kktRegId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
  /*              }
            }
        }
        //если  есть ссылка на магазин
        if(invoiceData.getfk_invoice_stores_from_fns()!= null)
        {
            if(invoiceData.store_from_fns == null)
            {
                Map<String, String> get = new HashMap<>();
                get.put("id", invoiceData.getfk_invoice_stores_from_fns().toString());
                List<InvoiceData.Store_from_fns> stores = loadDataFromStore_from_fns(get);
                if(stores.size() == 1)
                {
                    //получили магазин из локальной базы
                    invoiceData.store_from_fns = stores.get(0);
                }

            }

            if(invoiceData.store_from_fns.place_id!=null)
            {
                Task<QuerySnapshot> result_store = mFirestore.collection(tableNameStoresFromFns).whereEqualTo("place_id", invoiceData.store_from_fns.place_id).get(source);
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
                        InvoiceData.Store_from_fns store = stores.get(0).toObject(InvoiceData.Store_from_fns.class);
                        //если версия на сервере имеет данные по магазину из ФНС то надо обновлять локальную версию
                        //т.к. если в коде дошли до сюда значит в данном чеке информация из ФНС отсутствует.
                        //до этого пытались найти инфу по
                        if(store.inn != null)
                        {
                            store.id = invoiceData.store_from_fns.id;
                            store.update = true;
                            store.stores_from_fns_google_id = stores.get(0).getId();
                            invoiceData.store_from_fns = store;
                            saveStoreDataLocal(invoiceData.store_from_fns, store);
                        }
                    }
                    else
                    {
                        invoiceData.kktRegId.stores_from_fns_google_id = null;
                        loadStoreToServer = true;
                        //если на сервере такого магазина нет то загружем данные из локальной базы данных
                        //saveStoreDataServer(invoiceData.store_from_fns);
                    }

                }
                //есть возможность проверить магазин только по метке
                //если магазина с такой меткой нет на сервере то найти там именно этот локальный магазин невозможно поэтому если метки нет и в локальном магазине нет ссылки stores_from_fns_google_id
                //то просто загрузим магазин на сервер и обновим локальные данные
                //если ссылка есть то проверим наличие магазина на сервере и если его нет то загрузим данные на сервер а если есть то обновим локальные данные
            }
            else if(invoiceData.store_from_fns.stores_from_fns_google_id != null)
            {
                Task<DocumentSnapshot> result_store = mFirestore.collection(tableNameStoresFromFns).document(invoiceData.fk_invoice_stores_from_fns_google_id).get(source);
                while (!result_store.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result_store.isSuccessful() && result_store.getResult().exists()) {
                    InvoiceData.Store_from_fns store = result_store.getResult().toObject(InvoiceData.Store_from_fns.class);
                    if (store != null) {
                        store.stores_from_fns_google_id = result_store.getResult().getId();
                        //если магазин есть на сервере и на сервере статус 1 а локально нет - обновляем локально
                        if(invoiceData.store_from_fns._status != store._status && store._status == 1)
                        {
                            store.id = invoiceData.store_from_fns.id;
                            saveStoreDataLocal(store, store);
                        }
                        else if(invoiceData.store_from_fns._status == 1)
                        {
                            loadStoreToServer = true;
                            //saveStoreDataServer(invoiceData.store_from_fns);
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
                    //saveStoreDataServer(invoiceData.store_from_fns);
                    loadStoreToServer = true;
                }
            }
        }

        if(loadStoreToServer) {
            invoiceData.store_from_fns.stores_from_fns_google_id = null;
            saveStoreDataServer(invoiceData.store_from_fns);
        }

        if(loadKktToServer) {
            try {
                //invoiceData.kktRegId.stores_from_fns_google_id = null;
                invoiceData.kktRegId.fk_kktRegId_google_id = invoiceData.store_from_fns.stores_from_fns_google_id;
                saveKktRegIdServer(invoiceData.kktRegId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //обновляем ссылки на кассу и магазин на сервере
        invoiceData.fk_invoice_stores_from_fns_google_id = invoiceData.store_from_fns.stores_from_fns_google_id;
        invoiceData.fk_invoice_kktRegId_google_id = invoiceData.kktRegId.stores_from_fns_google_id;
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

                purchaseLocal.stores_from_fns_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("stores_from_fns_google_id"));
                purchaseLocal.fk_purchases_invoice_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_invoice_google_id"));
                purchaseLocal.fk_purchases_products_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_products_google_id"));
                purchaseLocal.fk_purchases_stores_google_id = cur_purchases.getString(cur_purchases.getColumnIndex("fk_purchases_stores_google_id"));


                Integer fk_purchases_products = cur_purchases.getInt(cur_purchases.getColumnIndex("fk_purchases_products"));


                //проверяем, что если выше мы загрузили на сервер магазин и кассу и чек, что локально хранится верная инфа, иначе обновляем
                //проверять после проверки наличия продукта на сервере
                if(invoiceData.stores_from_fns_google_id!= null && !invoiceData.stores_from_fns_google_id.equals(purchaseLocal.fk_purchases_invoice_google_id)
                        ||  invoiceData.store_from_fns.stores_from_fns_google_id!= null && !invoiceData.store_from_fns.stores_from_fns_google_id.equals(purchaseLocal.fk_purchases_stores_google_id))
                {
                    //обновляем запись в таблице локально
                    //обновляем ссылку на чек и магазин в товаре из чека
                    purchaseLocal.fk_purchases_stores_google_id = invoiceData.store_from_fns.stores_from_fns_google_id;
                    purchaseLocal.fk_purchases_invoice_google_id = invoiceData.stores_from_fns_google_id;
                }

                //собираем продукт из данных в локальной базе
                Cursor cur_product = dbHelper.query(tableNameProducts, null, "id=?", new String[]{fk_purchases_products.toString()}, null, null, null, null);
                if (cur_product.moveToFirst()) {
                    purchaseLocal.product.stores_from_fns_google_id = cur_product.getString(cur_product.getColumnIndex("stores_from_fns_google_id"));
                    purchaseLocal.product.nameFromBill = cur_product.getString(cur_product.getColumnIndex("nameFromBill"));
                    purchaseLocal.product.id = cur_product.getInt(cur_product.getColumnIndex("id"));
                    cur_product.close();
                }

                if(purchaseLocal.stores_from_fns_google_id!= null && purchaseLocal.stores_from_fns_google_id.length()>0)
                {
                    //если у покупки есть ссылка на гугл - проверяем  ее наличие на сервере - если на сервере нет загружаем на него
                    //если есть ссылка на гугл у локальной покупки то должна быть и ссылка на чек и на продукт
                    //проверять все покупки по чеку на сервере или индивидуально каждую покупку?
                    //в конце обязательно сверить количество
                    Task<DocumentSnapshot> result_purchases = mFirestore.collection(tableNamePurchases).document(purchaseLocal.stores_from_fns_google_id).get(source);
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
                                                if (purchaseLocal.product.stores_from_fns_google_id.equals(purchase.fk_purchases_products_google_id)) {
                                                    //если ссылки совпадают проверяем совпадение названий и бар кода
                                                    if (!purchaseLocal.product.nameFromBill.equals(product.nameFromBill)) {
                                                        //если не совпадают ищем на сервере товар и обновляем данные локально или добавляем на сервер новый продукт
                                                        checkProductExistOnServerAndSave(purchaseLocal.product);

                                                    }
                                                }
                                                //если ссылки не совпадают, но одинаоквые названия товаров - обновляем локальные данные
                                                else if (purchaseLocal.product.nameFromBill.equals(product.nameFromBill)) {
                                                    purchaseLocal.product.stores_from_fns_google_id = purchase.fk_purchases_products_google_id;
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

                            purchaseLocal.product.stores_from_fns_google_id = null;
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
*/
  /*
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
        if(purchaseLocal.fk_purchases_invoice_google_id != null)
        {
            toServer.put("fk_purchases_invoice_google_id", purchaseLocal.fk_purchases_invoice_google_id);
        }

        if(purchaseLocal.fk_purchases_products != null && purchaseLocal.fk_purchases_products >0)
        {
            if(purchaseLocal.product == null)
                purchaseLocal.product = getProductLocal(purchaseLocal.fk_purchases_products);
            if(purchaseLocal.product.id != null)
            {
                insertProductDataServer(purchaseLocal.product);
                if(purchaseLocal.product.stores_from_fns_google_id != null) {
                    purchaseLocal.fk_purchases_products_google_id = purchaseLocal.product.stores_from_fns_google_id;

                    toServer.put("fk_purchases_products_google_id", purchaseLocal.fk_purchases_products_google_id);

                    purchaseLocal.stores_from_fns_google_id = mFirestore.collection(tableNamePurchases).document().getId();
                    mFirestore.collection(tableNamePurchases).document(purchaseLocal.stores_from_fns_google_id).set(toServer);
                    if(purchaseLocal.stores_from_fns_google_id !=null)
                    {
                        contentValues.put("stores_from_fns_google_id", purchaseLocal.stores_from_fns_google_id);
                    }

                    if(purchaseLocal.id != null)
                    {
                        dbHelper.update(tableNamePurchases, contentValues, "id=?", new String[]{purchaseLocal.id.toString()});
                    }
                }
            }
        }
    }
*/
    private PurchasesListData.Product getProductLocal(int id)
    {
        PurchasesListData.Product product = new PurchasesListData.Product();
        Cursor cur = dbHelper.query(tableNameProducts, null, "id=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if(cur.moveToFirst())
        {
            product.google_id = cur.getString(cur.getColumnIndex("stores_from_fns_google_id"));
            product.nameFromBill = cur.getString(cur.getColumnIndex("nameFromBill"));
            product.id = cur.getInt(cur.getColumnIndex("id"));

            product.fk_productCutegory_google_id = cur.getString(cur.getColumnIndex("fk_productCutegory_google_id"));
            product.fk_bar_code_product = cur.getInt(cur.getColumnIndex("fk_bar_code_product"));
            product.fk_productCutegory = cur.getInt(cur.getColumnIndex("fk_productCutegory"));
            product.fk_bar_code_product_google_id = cur.getString(cur.getColumnIndex("fk_bar_code_product_google_id"));
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
                db.put("stores_from_fns_google_id", product_tmp.google_id);
                dbHelper.update(tableNameProducts, db, "id=?", new String[]{product_tmp.id.toString()});
            }
        }




    }

    public boolean getInvoiceFromServer(InvoiceData invoiceData, LoadingFromFNS.AsyncLoadDataInvoice asyncFirstAddInvoice) throws Exception {
       /* if(invoiceData != null) {
            InvoiceData tmp = (InvoiceData) invoiceData.clone();
            if(On_line && user.stores_from_fns_google_id != null)
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
                            if(user.stores_from_fns_google_id != null)
                                fStore.put("user_google_id", invoiceData.user_google_id);
                        DocumentReference addedDocRef = mFirestore.collection(tableNameInvoice).document();
                        addedDocRef.set(fStore);
                        invoiceData.stores_from_fns_google_id = addedDocRef.getId();
                        ContentValues val = new ContentValues();
                        val.put("stores_from_fns_google_id", invoiceData.stores_from_fns_google_id);
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
        }*/
        return false;
    }

    public void setStoreDataFull(InvoiceData finalInvoiceData) throws Exception {
        if(finalInvoiceData.fromFNS)
        {
            InvoiceData.KktRegId kktRegId = finalInvoiceData.kktRegId;
            Cursor cur_kktRegId= null;

            InvoiceData.KktRegId on_line_kkt = new InvoiceData.KktRegId();
            InvoiceData.Store_from_fns on_line_store = new InvoiceData.Store_from_fns();

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
                            //on_line_kkt.stores_from_fns_google_id = documentReference.getId();
                            //on_line_kkt.fk_kktRegId_google_id = documentReference.getString("fk_kktRegId_google_id");;
                            //on_line_kkt._status = (Integer) documentReference.get("_status");
                            //on_line_kkt.kktRegId = (Long) documentReference.get("kktRegId");
                            finalInvoiceData.kktRegId = on_line_kkt;

                        }
                        if (on_line_kkt != null && on_line_kkt.fk_kktRegId_google_id != null && finalInvoiceData.store_from_fns.google_id == null) {
                            Task<DocumentSnapshot> result_store = mFirestore.collection(tableNameStoresFromFns).document(on_line_kkt.fk_kktRegId_google_id).get(source);
                            while (!result_store.isComplete()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            DocumentSnapshot documents_store = result_store.getResult();
                            if (documents_store.exists()) {
                                on_line_store = documents_store.toObject(InvoiceData.Store_from_fns.class);
                                on_line_store.google_id = documents_store.getId();

                                finalInvoiceData.store_from_fns = on_line_store;
                            }
                        }
                    }
                }
            }

            //проверка что касса уже существует в таблице
            InvoiceData.KktRegId kktRegId_data =loadKKtRegIdLocal(kktRegId.kktRegId, "kktRegId=?");
            if(kktRegId_data != null)
            {
                finalInvoiceData.kktRegId = kktRegId_data;
                InvoiceData.Store_from_fns storeFromFns = null;
                InvoiceData.Store_on_map storeOnMap = null;
                Integer kktRegId_id = kktRegId_data.id;

                //загружаем данные из строки с найденной кассой
                //загружаем информацю по магазину к которому привязана касса
                Cursor cur_KktRegId_stores_links = dbHelper.query(tableNameKktRegIdStoresLinks, null, "fk_kktRegId=?", new String[]{kktRegId_id.toString()}, null, null, "date_add DESC", null);
                Integer fk_kktRegId_stores_link = null;
                Integer fk_stores_links = null;
                if(cur_KktRegId_stores_links.moveToFirst())
                {
                    fk_kktRegId_stores_link = cur_KktRegId_stores_links.getInt(cur_KktRegId_stores_links.getColumnIndex("id"));;
                    fk_stores_links = cur_KktRegId_stores_links.getInt(cur_KktRegId_stores_links.getColumnIndex("fk_stores_links"));
                    Cursor cur_stores_links = dbHelper.query(tableNameStoresLinks, null, "id=?", new String[]{fk_stores_links.toString()}, null, null, null, null);
                    if(cur_stores_links.moveToFirst()) {
                        Integer id_store_from_fns = cur_stores_links.getInt(cur_stores_links.getColumnIndex("fk_stores_from_fns"));
                        Integer id_store_on_map = cur_stores_links.getInt(cur_stores_links.getColumnIndex("fk_stores_on_map"));

                        if (id_store_from_fns > 0) {
                            //загружаем информацю по магазину к которому привязана касса
                            Map<String, String> args = new ArrayMap<String, String>();
                            args.put("id", id_store_from_fns.toString());
                            List<InvoiceData.Store_from_fns> stores_from_fns = loadDataFromStore_from_fns(args);
                            if (stores_from_fns.size() > 0) {
                                storeFromFns = stores_from_fns.get(0);
                            }
                        }

                        if (id_store_on_map > 0) {
                            //загружаем информацю по магазину к которому привязана касса
                            Map<String, List<String>> args = new ArrayMap<>();
                            args.put("id", Arrays.asList(id_store_on_map.toString()));
                            List<InvoiceData.Store_on_map> stores_on_map = loadDataFromStore_on_map(args, null);
                            if (stores_on_map.size() > 0) {
                                storeOnMap = stores_on_map.get(0);
                            }
                        }
                    }
                    cur_stores_links.close();

                }
                cur_KktRegId_stores_links.close();

                if(storeFromFns != null)
                {
                    //если данные одинковые
                    String newhascode = encryptPassword(finalInvoiceData.store_from_fns.inn+finalInvoiceData.store_from_fns.name_from_fns+finalInvoiceData.store_from_fns.address_from_fns);
                    Log.d(LOG_TAG, "compare two hashcode "+ newhascode + " old hash "+ storeFromFns.hashCode);
                    if(storeFromFns.hashCode.equals(newhascode))
                    {
                        //текущий магазин заменяем на полученый из базы
                        InvoiceData.Store_from_fns old_store_from_fns = finalInvoiceData.store_from_fns;
                        finalInvoiceData.store_from_fns = storeFromFns;
                        deleteStoreFromFnsLink(old_store_from_fns);
                    }
                    else
                    {
                        //иначе сохраняем текущий магазин
                        saveStoreDataServer(finalInvoiceData.store_from_fns, null, finalInvoiceData.fk_stores_links_google_id);
                        saveStoreDataLocal(finalInvoiceData.store_from_fns, null);

                    }
                }
                //чек привзян к магазину на карте
                if(storeOnMap != null)
                {
                    //проверка что магазин который сейчас привязан к чеку (если данные были добавлены вручную раньше загрузки с ФНС) отличается от того на который ссылается касса
                    if(finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.id != storeOnMap.id)
                    {
                        //если в магазине привязанном к  чеку прописана гугл метка
                        //и одинаковая с меткой в магазине привязанном к кассе
                        //или в магазине чека нет гугл метки а в кассе есть
                        // - заменяем привязку магазина к чеку на магазин от кассы
                        //
                        if(storeOnMap.place_id == null && finalInvoiceData.store_on_map.place_id != null)
                        {
                            //если в привязанном магазине нет метки гугул - нужно заменить найденный магазин на новый
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("fk_stores_on_map", finalInvoiceData.store_on_map.id);
                            int count = dbHelper.update(tableNameStoresLinks, contentValues, "fk_stores_on_map=?", new String[]{storeOnMap.id.toString()});
                            Log.d(LOG_TAG, "updated rows in " + tableNameStoresLinks+" count = "+ count);
                            //удаляем ссылку без метки
                            deleteStoreOnMapLink(storeOnMap.id, storeOnMap.google_id);

                        }//storeOnMap.place_id != null = true из-за предыдущего условия
                        else if(finalInvoiceData.store_on_map.place_id != null && !finalInvoiceData.store_on_map.place_id.equals(storeOnMap.place_id))
                        {
                            //если Place_id одинаковые - это 100% одна и та же запись в таблице
                            //и с текущим магазином ничего делать ненадо
                            //Integer oldStore_on_map_id =finalInvoiceData.store_on_map.id;
                            //String oldStore_on_map_google_id =finalInvoiceData.store_on_map.google_id;

                            //finalInvoiceData.store_on_map = storeOnMap;
                            //Обновляем ссылки на кассу и новый найденный магазин
                            //пытаемя удалить магазин, который раньше был приявязан к чеку, если больше он ни к чему не привязан
                            //deleteStoreOnMapLink(oldStore_on_map_id, oldStore_on_map_google_id);


                            //если метки разные - мы выгрузили самую новую метку. нужно сравнть дату из чека и дату добавления загруженного магазина
                            //если даты добавления меток разные нужно проверить еслть ли еще ссылки с кассой на магазины
                            //еслы еще ссылки есть надо проверить там ссылки на магазины ч pl_id
                            //если ссылок больше нет - ищем текущий pl_id
                            //нужно будет сохранить в линки новую ссылку на кассу текущую и на магазин с новой меткой

                            //просто добавлем еще одну ссылку в таблицу ссылок с той же кассой но другим магазином
                            //tryToSaveKktRegStoresLinks(finalInvoiceData);
                        }
                        else if(finalInvoiceData.store_on_map.place_id == null && storeOnMap.place_id != null)
                        {
                            //заменяем текущий магазин на привязанный
                            Integer oldStore_on_map_id =finalInvoiceData.store_on_map.id;
                            String oldStore_on_map_google_id =finalInvoiceData.store_on_map.google_id;
                            finalInvoiceData.store_on_map = storeOnMap;

                            //tryToSaveKktRegStoresLinks(finalInvoiceData);
                            //магазин в данном случае еще не был сохранен так что его нет надобности удалять или заменять

                        }
                        else
                        {
                            //так как гео метки разные  то нужно создавать новую ссылку с той же кассой но новой геометкой

                        }

                    }
                    else if(finalInvoiceData.store_on_map == null)
                    {
                        //если к чеку не привязан магазин на карте, а к кассе привязан - привязываем его к чеку
                        finalInvoiceData.store_on_map = storeOnMap;
                        finalInvoiceData.kktRegId.id = kktRegId_id;
                        tryToSaveKktRegStoresLinks(finalInvoiceData);
                    }
                }

                tryToSaveStoreLinks(finalInvoiceData);
                tryToSaveKktRegStoresLinks(finalInvoiceData);

            }
            else
            {
                //если касса не найде в локальной базе
                //сохраняем магазин локально и потом кассу
                //ищем локально инфу по магазину изи ФНС
                Map<String, String> args = new ArrayMap<String, String>();
                args.put("hashcode", encryptPassword(finalInvoiceData.store_from_fns.inn+finalInvoiceData.store_from_fns.name_from_fns+finalInvoiceData.store_from_fns.address_from_fns));
                List<InvoiceData.Store_from_fns> stores_from_fns = loadDataFromStore_from_fns(args);
                if(stores_from_fns.size()>0)
                {
                    finalInvoiceData.store_from_fns = stores_from_fns.get(0);
                }
                else {
                    saveStoreDataServer(finalInvoiceData.store_from_fns, null, finalInvoiceData.fk_stores_links_google_id);
                    saveStoreDataLocal(finalInvoiceData.store_from_fns, null);

                }

                if(finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.id == null) {
                    saveStoreDataServer(null, finalInvoiceData.store_on_map, finalInvoiceData.fk_stores_links_google_id);
                    saveStoreDataLocal(null, finalInvoiceData.store_on_map);
                }

                Long old_link = null;
                if(finalInvoiceData.fk_stores_links != null)
                    old_link = finalInvoiceData.fk_stores_links;
                tryToSaveStoreLinks(finalInvoiceData);



                try {
                    finalInvoiceData.kktRegId.id = saveKktRegId(finalInvoiceData.kktRegId);
                } catch (Exception e) {
                    log.info(e.getMessage()+ Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    //finalInvoiceData.kktRegId.id = 0;
                }
                tryToSaveKktRegStoresLinks(finalInvoiceData);

                if(old_link!= null)
                    Log.d(LOG_TAG, " old link fk_stores_links "+ old_link+ "new link "+ finalInvoiceData.fk_stores_links);

                if(old_link != null && old_link != finalInvoiceData.fk_stores_links)
                    tryToDeleteStoresLinks(old_link);
            }


            if(finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.place_id!= null)
            {
                //обновляем статус чека на подтвержденный если в текущем чеке есть гугл метка
                finalInvoiceData.set_status(2);
            }

        }
        else
        {
            if(finalInvoiceData.store_on_map.place_id!= null && finalInvoiceData.store_on_map.place_id.length()>0)
            {
                InvoiceData.Store_from_fns on_line_store = null;

  /*              if(On_line && user.stores_from_fns_google_id != null)
                {

                    Task<QuerySnapshot> result_store = null;//mFirestore.collection(tableNameStoresFromFns).whereEqualTo("place_id", finalInvoiceData.store_from_fns.place_id).get(source);


                    result_store = mFirestore.collection(tableNameStoresOnMap).whereEqualTo("place_id", finalInvoiceData.store_on_map.place_id).get(source);
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
                                on_line_store = documentReference.toObject(InvoiceData.Store_from_fns.class);
                                if(on_line_store != null) {
                                    on_line_store.stores_from_fns_google_id = documentReference.getId();
                                    if (!finalInvoiceData.store_from_fns.stores_from_fns_google_id.equals(on_line_store.stores_from_fns_google_id))
                                    {
                                        mFirestore.collection(tableNameStoresFromFns).document(finalInvoiceData.store_from_fns.stores_from_fns_google_id).delete();
                                        finalInvoiceData.store_from_fns.stores_from_fns_google_id = on_line_store.stores_from_fns_google_id;
                                        finalInvoiceData.fk_invoice_stores_from_fns_google_id =on_line_store.stores_from_fns_google_id;
                                        if(finalInvoiceData.kktRegId != null)
                                            finalInvoiceData.kktRegId.fk_kktRegId_google_id =on_line_store.stores_from_fns_google_id;
                                    }
                                }

                            }
                        }
                    }
                }
*/

                Map<String, List<String>> args = new ArrayMap<>();
                args.put("place_id", Arrays.asList(finalInvoiceData.store_on_map.place_id));
                List<InvoiceData.Store_on_map> stores = loadDataFromStore_on_map(args, null);

                //если данный магазин по гугл метке найден в локальной базе
                if(stores.size()>0)
                {
                    Integer id = stores.get(0).id;
                    String google_id = stores.get(0).google_id;

                    if(finalInvoiceData.store_on_map.id == null || finalInvoiceData.store_on_map.id <= 0 || !finalInvoiceData.store_on_map.id.equals(id))
                    {
                        //finalInvoiceData.setFk_invoice_stores_on_map(id);
                        InvoiceData.Store_on_map old_store_on_map = finalInvoiceData.store_on_map;
                        finalInvoiceData.store_on_map = stores.get(0);

                        Long old_link = null;
                        if(finalInvoiceData.fk_stores_links != null)
                            old_link = finalInvoiceData.fk_stores_links;
                        tryToSaveStoreLinks(finalInvoiceData);

                        if(old_link!= null)
                            Log.d(LOG_TAG, " old link fk_stores_links "+ old_link+ "new link "+ finalInvoiceData.fk_stores_links);

                        if(old_link == null || old_link != finalInvoiceData.fk_stores_links) {
                            tryToSaveKktRegStoresLinks(finalInvoiceData);
                        }

                        if(old_link != null && old_link != finalInvoiceData.fk_stores_links)
                            tryToDeleteStoresLinks(old_link);

                        //try to delete old store_on_map
                        if(old_store_on_map != null && (old_store_on_map.id != null || old_store_on_map.google_id != null))
                            deleteStoreOnMapLink(old_store_on_map.id, old_store_on_map.google_id);


                        //обновление статуса магазина от ФНС - обновляем статус локально и на сервере
                        if(finalInvoiceData.store_from_fns._status != 1) {
                            finalInvoiceData.store_from_fns._status = 1;
                            saveStoreDataServer(finalInvoiceData.store_from_fns, null, finalInvoiceData.fk_stores_links_google_id);
                            saveStoreDataLocal(finalInvoiceData.store_from_fns, null);
                        }
                    }
                    else
                    {
                        tryToSaveStoreLinks(finalInvoiceData);
                        tryToSaveKktRegStoresLinks(finalInvoiceData);
                    }

                }
                else
                {
                    finalInvoiceData.store_on_map.update = false;
                    Integer id = finalInvoiceData.store_on_map.id;
                    String google_id = finalInvoiceData.store_on_map.google_id;
                    if(finalInvoiceData.store_on_map.id != null && finalInvoiceData.store_on_map.id >0)
                    {
                        finalInvoiceData.store_on_map.update = checkStoreOnMaptoUpdate(finalInvoiceData);
                    }
                    if(finalInvoiceData.store_from_fns == null)
                        finalInvoiceData.store_from_fns = new InvoiceData.Store_from_fns();
                    if(finalInvoiceData.kktRegId == null)
                        finalInvoiceData.kktRegId = new InvoiceData.KktRegId();

                    finalInvoiceData.store_from_fns._status = 1;
                    finalInvoiceData.kktRegId._status = 1;
                    //finalInvoiceData.store_on_map.fk_stores_from_fns = finalInvoiceData.store_from_fns.id;
                    //finalInvoiceData.store_on_map.fk_stores_from_fns_google_id = finalInvoiceData.store_from_fns.google_id;

                    saveStoreDataServer(finalInvoiceData.store_from_fns, finalInvoiceData.store_on_map, finalInvoiceData.fk_stores_links_google_id);
                    saveStoreDataLocal(finalInvoiceData.store_from_fns, finalInvoiceData.store_on_map);
                    tryToSaveStoreLinks(finalInvoiceData);

                    if(finalInvoiceData.fk_stores_links != null && finalInvoiceData.fk_stores_links > 0)
                    {
                        tryToSaveKktRegStoresLinks(finalInvoiceData);
                    }
                    if(!finalInvoiceData.store_on_map.update)
                    {
                        deleteStoreOnMapLink(id, google_id);
                    }
                    //saveStoreDataLocal(finalInvoiceData.store_from_fns, null);
                    //finalInvoiceData.setfk_invoice_stores_from_fns(finalInvoiceData.store_from_fns.id);
                    //updateInvoice(finalInvoiceData);
                }

            }
        }

        PurchasesListData purchasesListData = new PurchasesListData();
        purchasesListData.store = new PurchasesListData.Store();
        if(finalInvoiceData.fk_invoice_kktRegId_store_links !=  null && finalInvoiceData.fk_invoice_kktRegId_store_links > 0 ) {
            purchasesListData.fk_purchases_kktRegId_stores_links = finalInvoiceData.fk_invoice_kktRegId_store_links;
        }
        updatePurchases(purchasesListData, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});

        /* if(finalInvoiceData.kktRegId!= null && finalInvoiceData.kktRegId.kktRegId>0)
        {
            finalInvoiceData.set_status(1);
            //updateInvoice(finalInvoiceData);

            finalInvoiceData.kktRegId._status = finalInvoiceData.store_from_fns._status;
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
        */
    }

    private boolean tryToDeleteKktRegStoresLinks(Long old_link) {
        Cursor cur = dbHelper.query(tableNameInvoice, null, "fk_invoice_kktRegId_store_links=?", new String[]{old_link.toString()}, null, null, null, null);
        if(!cur.moveToFirst()) {
            cur.close();
            cur = dbHelper.query(tableNamePurchases, null, "fk_purchases_kktRegId_stores_links=?", new String[]{old_link.toString()}, null, null, null, null);
            if(!cur.moveToFirst()) {
                long count = dbHelper.delete(tableNameKktRegIdStoresLinks, "id=?", new String[]{old_link.toString()});
                Log.d(LOG_TAG, "deleted row from " + tableNameKktRegIdStoresLinks + " row id " + old_link + " count " + count);
                return count>0;
            }
        }
        cur.close();
        return false;

    }

    private boolean tryToDeleteStoresLinks(Long old_link) {
        Cursor cur = dbHelper.query(tableNameKktRegIdStoresLinks, null, "fk_stores_links=?", new String[]{old_link.toString()}, null, null, null, null);
        if(!cur.moveToFirst()) {
            cur.close();
            long count = dbHelper.delete(tableNameStoresLinks, "id=?", new String[]{old_link.toString()});
            Log.d(LOG_TAG, "deleted row from " + tableNameStoresLinks + " row id " + old_link + " count " + count);
            return count>0;
        }
        cur.close();
        return false;

    }
    private boolean tryToDeleteStoresLinksServer(String old_link) {
        Task<QuerySnapshot> result = mFirestore.collection(tableNameKktRegIdStoresLinks).whereEqualTo("fk_stores_links_google_id", old_link).get(source);
        while(!result.isComplete())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(result.isSuccessful()) {
            Log.d(LOG_TAG, "tryToDeleteStoresLinksServer  result.isSuccessful() "+ new Exception().getStackTrace()[0].getLineNumber());
            if (!result.getResult().getMetadata().isFromCache()) {
                List<DocumentSnapshot> documents = result.getResult().getDocuments();
                if (documents.size() > 0) {
                    mFirestore.collection(tableNameKktRegIdStoresLinks).document(old_link).delete();
                    return true;
                }
            }
        }
        return false;

    }

    private void tryToSaveKktRegStoresLinks(InvoiceData finalInvoiceData) throws Exception {
        Cursor cur = null;

        if(finalInvoiceData.fk_stores_links != null && finalInvoiceData.kktRegId != null && finalInvoiceData.kktRegId.id != null) {
            cur = dbHelper.query(tableNameKktRegIdStoresLinks, null, "fk_kktRegId=? AND fk_stores_links=?", new String[]{finalInvoiceData.kktRegId.id.toString(), finalInvoiceData.fk_stores_links.toString()}, null, null, null, null);
            if (cur.moveToFirst()) {
                    Long old_link = finalInvoiceData.fk_invoice_kktRegId_store_links;
                    finalInvoiceData.fk_invoice_kktRegId_store_links = cur.getLong(cur.getColumnIndex("id"));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("fk_invoice_kktRegId_store_links", finalInvoiceData.fk_invoice_kktRegId_store_links);
                    int count = dbHelper.update(tableNameInvoice, contentValues, "id=?", new String[]{finalInvoiceData.getId().toString()});
                    Log.d(LOG_TAG, "updated rows in " + tableNameInvoice + " count = " + count);

                    contentValues = new ContentValues();
                    contentValues.put("fk_purchases_kktRegId_stores_links", finalInvoiceData.fk_invoice_kktRegId_store_links);
                    count = dbHelper.update(tableNamePurchases, contentValues, "fk_purchases_invoice=?", new String[]{finalInvoiceData.getId().toString()});
                    Log.d(LOG_TAG, "updated rows in " + tableNamePurchases + " count = " + count);

                    if (old_link != null && !old_link.equals(finalInvoiceData.fk_invoice_kktRegId_store_links)) {
                        count = (int) dbHelper.delete(tableNameKktRegIdStoresLinks, "id=?", new String[]{old_link.toString()});
                        Log.d(LOG_TAG, "deleted row in " + tableNameKktRegIdStoresLinks + " count = " + count);
                    }

            }
        }
        if(cur == null || !cur.moveToFirst())
        {
            long timeAdd = new Date().getTime();
            ContentValues contentValues = new ContentValues();
            Map<String, Object> fStore = new HashMap<>();

            if (finalInvoiceData.fk_stores_links_google_id != null) {
                contentValues.put("fk_stores_links_google_id", finalInvoiceData.fk_stores_links_google_id);
                fStore.put("fk_stores_links_google_id", finalInvoiceData.fk_stores_links_google_id);
            }
            if (finalInvoiceData.kktRegId != null && finalInvoiceData.kktRegId.google_id != null) {
                contentValues.put("fk_kktRegId_google_id", finalInvoiceData.kktRegId.google_id);
                fStore.put("fk_kktRegId_google_id", finalInvoiceData.kktRegId.google_id);
            }

            if (finalInvoiceData.fk_stores_links != null && finalInvoiceData.fk_stores_links > 0)
                contentValues.put("fk_stores_links", finalInvoiceData.fk_stores_links);
            if (finalInvoiceData.kktRegId != null && finalInvoiceData.kktRegId.id != null)
                contentValues.put("fk_kktRegId", finalInvoiceData.kktRegId.id);

            if (finalInvoiceData.fk_invoice_kktRegId_store_links != null && finalInvoiceData.fk_invoice_kktRegId_store_links > 0)
            {
                //обновляем запись в таблице локальной и на сервере
                if(finalInvoiceData.fk_invoice_kktRegId_store_links_google_id != null && On_line && user.google_id != null)
                {
                    mFirestore.collection(tableNameKktRegIdStoresLinks).document(finalInvoiceData.fk_invoice_kktRegId_store_links_google_id).update(fStore);
                }
                dbHelper.update(tableNameKktRegIdStoresLinks, contentValues, "id=?", new String[]{finalInvoiceData.fk_invoice_kktRegId_store_links.toString()});
            } else {
                contentValues.put("date_add", timeAdd);
                fStore.put("date_add", timeAdd);
                if(On_line && user.google_id != null)
                {
                    DocumentReference addedDocRef = mFirestore.collection(tableNameKktRegIdStoresLinks).document();
                    finalInvoiceData.fk_invoice_kktRegId_store_links_google_id = addedDocRef.getId();
                    contentValues.put("google_id", finalInvoiceData.fk_invoice_kktRegId_store_links_google_id);
                    mFirestore.collection(tableNameKktRegIdStoresLinks).document(finalInvoiceData.fk_invoice_kktRegId_store_links_google_id).set(fStore);
                }

                finalInvoiceData.fk_invoice_kktRegId_store_links = dbHelper.insert(tableNameKktRegIdStoresLinks, null, contentValues);
            }
        }
        if(cur != null)
            cur.close();
    }

    private void tryToSaveStoreLinks(InvoiceData finalInvoiceData)
    {
        Log.d(LOG_TAG, "tryToSaveStoreLinks " + new Exception().getStackTrace()[0].getLineNumber());
        Cursor cur = null;
        Map<String, Object> fStore = new HashMap<>();
        if(finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.google_id != null)
            fStore.put("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id);
        if(finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.google_id != null)
            fStore.put("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id);


        if(finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.id != null  && finalInvoiceData.store_on_map!=  null && finalInvoiceData.store_on_map.id != null) {
            cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_from_fns=? AND fk_stores_on_map=?", new String[]{finalInvoiceData.store_from_fns.id.toString(),
                    finalInvoiceData.store_on_map.id.toString()}, null, null, null, null);
        }
        else if(finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.id != null)
        {
            cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_from_fns=? AND fk_stores_on_map is NULL", new String[]{finalInvoiceData.store_from_fns.id.toString()}, null, null, null, null);
        }
        if(cur != null && cur.moveToFirst())
        {
            finalInvoiceData.fk_stores_links = cur.getLong(cur.getColumnIndex("id"));
            cur.close();

            if(On_line && user.google_id != null) {
                //проверка наличия ссылок на сервере
                Task<QuerySnapshot> result = mFirestore.collection(tableNameStoresLinks).whereEqualTo("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id)
                        .whereEqualTo("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id).get(source);
                while (!result.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result.isSuccessful()) {
                    Log.d(LOG_TAG, "tryToSaveStoreLinks  result.isSuccessful() " + new Exception().getStackTrace()[0].getLineNumber());
                    if (!result.getResult().getMetadata().isFromCache()) {
                        List<DocumentSnapshot> documents = result.getResult().getDocuments();
                        if (documents.size() > 0) {
                            finalInvoiceData.fk_stores_links_google_id = documents.get(0).getId();
                        } else {
                            DocumentReference addedDocRef = mFirestore.collection(tableNameStoresLinks).document();
                            finalInvoiceData.fk_stores_links_google_id = addedDocRef.getId();
                            mFirestore.collection(tableNameStoresLinks).document(finalInvoiceData.fk_stores_links_google_id).set(fStore);
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "tryToSaveStoreLinks   !isSuccessful" + new Exception().getStackTrace()[0].getLineNumber());
                }
            }

        }
        else if (finalInvoiceData.fk_stores_links != null && finalInvoiceData.fk_stores_links > 0)
        {
            Log.d(LOG_TAG, "tryToSaveStoreLinks " + new Exception().getStackTrace()[0].getLineNumber());

            //если есть ссылка на сервере
            if(On_line && user.google_id != null && finalInvoiceData.fk_stores_links_google_id != null)
            {
                Task<DocumentSnapshot> result = mFirestore.collection(tableNameStoresLinks).document(finalInvoiceData.fk_stores_links_google_id).get(source);
                while (!result.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result.isSuccessful()) {
                    Log.d(LOG_TAG, "tryToSaveStoreLinks  result.isSuccessful() " + new Exception().getStackTrace()[0].getLineNumber());
                    if (!result.getResult().getMetadata().isFromCache()) {
                        String fk_stores_from_fns_google_id = result.getResult().getString("fk_stores_from_fns_google_id");
                        String fk_stores_on_map_google_id = result.getResult().getString("fk_stores_on_map_google_id");
                        //если в ссылке на сервере указания на другие данные по магазину - например указан другой магазин на карте - создаем новую ссылку
                        //иначе обновляем текущую - когда магазин не был указан ранее.
                        if((!finalInvoiceData.store_from_fns.google_id.equals(fk_stores_from_fns_google_id) && fk_stores_from_fns_google_id != null && fk_stores_from_fns_google_id.length()>0)
                        || (!finalInvoiceData.store_on_map.google_id.equals(fk_stores_on_map_google_id) && fk_stores_on_map_google_id!= null && fk_stores_on_map_google_id.length()>0))
                        {
                            Log.d(LOG_TAG, "tryToSaveStoreLinks  create new link " + new Exception().getStackTrace()[0].getLineNumber() + "\n"+fk_stores_on_map_google_id+"\n"+fk_stores_from_fns_google_id);
                            DocumentReference addedDocRef = mFirestore.collection(tableNameStoresLinks).document();
                            finalInvoiceData.fk_stores_links_google_id = addedDocRef.getId();
                        }

                        mFirestore.collection(tableNameStoresLinks).document(finalInvoiceData.fk_stores_links_google_id).set(fStore);
                    }
                } else {
                    Log.d(LOG_TAG, "tryToSaveStoreLinks   !isSuccessful" + new Exception().getStackTrace()[0].getLineNumber());
                }
            }

            cur.close();
            if(finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.id != null
                    && finalInvoiceData.store_on_map!=  null && finalInvoiceData.store_on_map.id != null) {

                Log.d(LOG_TAG, "tryToSaveStoreLinks " + new Exception().getStackTrace()[0].getLineNumber());
                ContentValues contentValues = new ContentValues();

                contentValues.put("fk_stores_from_fns", finalInvoiceData.store_from_fns.id);
                contentValues.put("fk_stores_on_map", finalInvoiceData.store_on_map.id);
                if (finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.google_id != null && finalInvoiceData.store_from_fns.google_id.length()>0) {
                    contentValues.put("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id);
                }
                if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.google_id != null && finalInvoiceData.store_on_map.google_id.length()>0) {
                    contentValues.put("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id);
                }


                cur = dbHelper.query(tableNameStoresLinks, null, "id=?",
                        new String[]{finalInvoiceData.fk_stores_links.toString()}, null, null, null, null);
                if (cur.moveToFirst()) {
                    int fk_store_on_map = cur.getInt(cur.getColumnIndex("fk_stores_on_map"));
                    int fk_store_from_fns = cur.getInt(cur.getColumnIndex("fk_stores_from_fns"));




                    if(!finalInvoiceData.store_on_map.id.equals(fk_store_on_map) && fk_store_on_map > 0 || !finalInvoiceData.store_from_fns.id.equals(fk_store_from_fns))
                    {
                        finalInvoiceData.fk_stores_links = dbHelper.insert(tableNameStoresLinks, null, contentValues);
                    }
                    else if(fk_store_on_map == 0)
                    {
                        dbHelper.update(tableNameStoresLinks, contentValues, "id=?", new String[]{finalInvoiceData.fk_stores_links.toString()});
                    }
                }
                else
                {
                    finalInvoiceData.fk_stores_links = dbHelper.insert(tableNameStoresLinks, null, contentValues);
                }
            }
            else
            {
                Log.d(LOG_TAG, "tryToSaveStoreLinks " + new Exception().getStackTrace()[0].getLineNumber());
                ContentValues contentValues = new ContentValues();
                if (finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.id != null) {
                    contentValues.put("fk_stores_from_fns", finalInvoiceData.store_from_fns.id);
                }
                if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.id != null) {
                    contentValues.put("fk_stores_on_map", finalInvoiceData.store_on_map.id);
                }

                if (finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.google_id != null && finalInvoiceData.store_from_fns.google_id.length()>0) {
                    contentValues.put("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id);
                }
                if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.google_id != null && finalInvoiceData.store_on_map.google_id.length()>0) {
                    contentValues.put("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id);
                }

                if(On_line && user.google_id != null && finalInvoiceData.fk_stores_links_google_id != null)
                {
                    mFirestore.collection(tableNameStoresLinks).document(finalInvoiceData.fk_stores_links_google_id).update(fStore);
                }

                dbHelper.update(tableNameStoresLinks, contentValues, "id=?", new String[]{finalInvoiceData.fk_stores_links.toString()});
            }
        }
        else
        {
            Log.d(LOG_TAG, "tryToSaveStoreLinks " + new Exception().getStackTrace()[0].getLineNumber());
            ContentValues contentValues = new ContentValues();



            if (finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.id != null) {
                contentValues.put("fk_stores_from_fns", finalInvoiceData.store_from_fns.id);
            }
            if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.id != null) {
                contentValues.put("fk_stores_on_map", finalInvoiceData.store_on_map.id);
            }



            if(On_line && user.google_id != null)
            {
                if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.google_id != null && finalInvoiceData.store_on_map.google_id.length()>0) {
                    fStore.put("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id);
                    contentValues.put("fk_stores_on_map_google_id", finalInvoiceData.store_on_map.google_id);
                }
                if (finalInvoiceData.store_from_fns != null && finalInvoiceData.store_from_fns.google_id != null && finalInvoiceData.store_from_fns.google_id.length()>0) {
                    fStore.put("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id);
                    contentValues.put("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id);

                    Task<QuerySnapshot> result = mFirestore.collection(tableNameStoresLinks).whereEqualTo("fk_stores_from_fns_google_id", finalInvoiceData.store_from_fns.google_id).get(source);
                    while(!result.isComplete())
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(result.isSuccessful()) {
                        Log.d(LOG_TAG, "tryToSaveStoreLinks  result.isSuccessful() "+ new Exception().getStackTrace()[0].getLineNumber());
                        if (!result.getResult().getMetadata().isFromCache()) {
                            List<DocumentSnapshot> documents = result.getResult().getDocuments();
                            Boolean saveLink = false;
                            if (documents.size() > 0) {
                                if (finalInvoiceData.store_on_map != null && finalInvoiceData.store_on_map.google_id != null && finalInvoiceData.store_on_map.google_id.length()>0) {
                                    for (int i = 0; i < documents.size(); i++) {
                                        if (finalInvoiceData.store_on_map.equals(documents.get(i).get("fk_stores_on_map_google_id"))) {
                                            finalInvoiceData.fk_stores_links_google_id = documents.get(i).getId();
                                            saveLink = true;
                                            break;
                                            //String fk_stores_on_map_google_id = documents.get(0).get("fk_stores_on_map_google_id");
                                            //если id найден
                                        }
                                    }
                                }
                            }

                            if(!saveLink)
                            {
                                DocumentReference addedDocRef = mFirestore.collection(tableNameStoresLinks).document();
                                finalInvoiceData.fk_stores_links_google_id = addedDocRef.getId();
                                mFirestore.collection(tableNameStoresLinks).document(finalInvoiceData.fk_stores_links_google_id).set(fStore);
                            }
                        }
                    }
                    else
                    {
                        Log.d(LOG_TAG, "tryToSaveStoreLinks   !isSuccessful"+ new Exception().getStackTrace()[0].getLineNumber());
                    }


                }
                contentValues.put("google_id", finalInvoiceData.fk_stores_links_google_id);

            }

            finalInvoiceData.fk_stores_links = dbHelper.insert(tableNameStoresLinks, null, contentValues);
        }

        if(cur != null)
            cur.close();

    }


    private InvoiceData.KktRegId loadKKtRegIdLocal(Long kktRegId, String selection) {
        Cursor cur_kktRegId=dbHelper.query(tableNameKktRegId, null, selection, new String[]{kktRegId.toString()}, null, null, null, null);
        if(cur_kktRegId.moveToFirst()) {
            InvoiceData.KktRegId kktRegId_data = new InvoiceData.KktRegId();

            kktRegId_data.id = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("id"));
            kktRegId_data.google_id = cur_kktRegId.getString(cur_kktRegId.getColumnIndex("google_id"));
            kktRegId_data.kktRegId = cur_kktRegId.getLong(cur_kktRegId.getColumnIndex("kktRegId"));
            kktRegId_data._status = cur_kktRegId.getInt(cur_kktRegId.getColumnIndex("_status"));

            cur_kktRegId.close();
            return kktRegId_data;
        }
        else {
            cur_kktRegId.close();
            return null;
        }
    }

    private void updatePurchases(PurchasesListData purchasesListData, String where, String[] args)
    {
        ContentValues contentValues = new ContentValues();

        if(purchasesListData.invoice != null && purchasesListData.invoice.id != null)
            contentValues.put("fk_purchases_invoice", purchasesListData.invoice.id);

        if(purchasesListData.product != null && purchasesListData.product.id != null)
            contentValues.put("fk_purchases_products", purchasesListData.product.id);

        if(purchasesListData.product != null && purchasesListData.product.google_id != null)
            contentValues.put("fk_purchases_products_google_id", purchasesListData.product.google_id);
        if(purchasesListData.fk_purchases_kktRegId_stores_links!= null)
            contentValues.put("fk_purchases_kktRegId_stores_links", purchasesListData.fk_purchases_kktRegId_stores_links);

        if(contentValues.size()>0)
            dbHelper.update(tableNamePurchases, contentValues, where, args);

    }

    public boolean checkStoreOnMaptoUpdate(InvoiceData finalInvoiceData)
    {
        //проверка что магазин можно обновить - изменить его положение на карте, если это магазин привязан только к одной кассе и одному чеку
        finalInvoiceData.store_on_map.update = false;
        if(finalInvoiceData.store_on_map.id!= null)
        {
            //поиск чеков с текущим магазином
            Cursor cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_on_map=?",
                    new String[]{finalInvoiceData.store_on_map.id.toString()}, null, null, null, null);
            if(cur.getCount()<=1)
            {
                cur.close();
                if(finalInvoiceData.fk_stores_links != null) {
                    cur = dbHelper.query(tableNameKktRegIdStoresLinks, null, "fk_stores_links=?",
                            new String[]{finalInvoiceData.fk_stores_links.toString()}, null, null, null, null);
                    if (cur.getCount() <= 1) {
                        // если не найдены обновляем данные по магазину
                        finalInvoiceData.store_on_map.update = true;
                    }
                }
                else if(cur.getCount() == 0)
                    finalInvoiceData.store_on_map.update = true;
            }
            /*else if (cur.getCount() == 1)
            {
                finalInvoiceData.store_on_map.update = true;
            }*/
            cur.close();
        }
        return finalInvoiceData.store_on_map.update;
    }

    public String latinToKirillic(String string)
    {
        String checkLatin ="QWRYUISDFGJLZVNqwryuisdfghjlzvbn";

        for(int i=0; i<checkLatin.length(); i++)
        {
            if(string.indexOf(checkLatin.charAt(i))>-1)
            {
                Log.d(LOG_TAG, "latinToKirillic  string in EN! "+string);
                return string;
            }
        }

        String latin ="ETOPAHKXCBMetopakxcm";
        String kirillic ="ЕТОРАНКХСВМеторакхсм";
        for(int i=0; i<latin.length(); i++)
        {
            string = string.replaceAll(String.valueOf(latin.charAt(i)), String.valueOf(kirillic.charAt(i)));
        }
        return string;
    }

    private void saveStoreDataLocal(InvoiceData.Store_from_fns store_from_fns, InvoiceData.Store_on_map store_on_map)
    {

        if(store_from_fns != null)
        {
            final ContentValues data_from_fns = new ContentValues();
            String hashcode = "";
            if(store_from_fns.inn != null) {
                data_from_fns.put("inn", store_from_fns.inn);
                hashcode+= store_from_fns.inn;
            }
            if(store_from_fns.name_from_fns != null) {
                //store_from_fns.name_from_fns= latinToKirillic(store_from_fns.name_from_fns);
                data_from_fns.put("name_from_fns", store_from_fns.name_from_fns.trim());
                hashcode +=store_from_fns.name_from_fns;
            }

            if(store_from_fns.address_from_fns != null) {
                data_from_fns.put("address_from_fns", store_from_fns.address_from_fns.trim());
                hashcode+=store_from_fns.address_from_fns.trim();
            }
            if(store_from_fns._status != null) {
                data_from_fns.put("_status", store_from_fns._status);
            }
            if(store_from_fns.date_add == null)
                store_from_fns.date_add = new Date().getTime();
            data_from_fns.put("date_add", store_from_fns.date_add);

            if(store_from_fns.google_id!= null)
                data_from_fns.put("google_id", store_from_fns.google_id);

            if(store_from_fns.inn != null)
                data_from_fns.put("hashcode", encryptPassword(hashcode));

            if(store_from_fns.id!= null)
            {
                dbHelper.update(tableNameStoresFromFns, data_from_fns, "id=?", new String[]{store_from_fns.id.toString()});
            }
            else if(store_from_fns.inn != null)
            {
                store_from_fns.id = (int)dbHelper.insert(tableNameStoresFromFns, null, data_from_fns);
            }
        }

        if(store_on_map!= null)
        {
            final ContentValues data_on_map = new ContentValues();
            if(store_on_map.name != null) {
                data_on_map.put("name", store_on_map.name.trim());
            }
            if(store_on_map.address != null) {
                data_on_map.put("address", store_on_map.address.trim());
            }
            if(store_on_map.latitude != null) {
                data_on_map.put("latitude", store_on_map.latitude);
            }
            if(store_on_map.longitude != null) {
                data_on_map.put("longitude", store_on_map.longitude);
            }

            if(store_on_map.fk_stores_from_fns != null) {
                data_on_map.put("fk_stores_from_fns", store_on_map.fk_stores_from_fns);
            }

            if(store_on_map.place_id != null)
            {
                data_on_map.put("place_id", store_on_map.place_id);
            }
            if(store_on_map.store_type != null) {
                data_on_map.put("store_type", store_on_map.store_type);
            }
            if(store_on_map.iconName != null) {
                data_on_map.put("iconName", store_on_map.iconName);
            }
            if(store_on_map.photo_reference != null) {
                data_on_map.put("photo_reference", store_on_map.photo_reference);
            }
            if(store_on_map.date_add == null)
                store_on_map.date_add = new Date().getTime();
            data_on_map.put("date_add", store_on_map.date_add);
            if(store_on_map.google_id!= null)
                data_on_map.put("google_id", store_on_map.google_id);


            if(store_on_map.id!= null && store_on_map.update)
            {
                dbHelper.update(tableNameStoresOnMap, data_on_map, "id=?", new String[]{store_on_map.id.toString()});
            }
            else //if(store_on_map.id == null)
            {
                store_on_map.id = (int)dbHelper.insert(tableNameStoresOnMap, null, data_on_map);
            }
        }
    }

    private void saveStoreDataServer(InvoiceData.Store_from_fns store_from_fns, InvoiceData.Store_on_map store_on_map, String fk_stores_links_google_id) {
        if (store_from_fns != null) {
            Map<String, Object> fStore = new HashMap<>();


            String hashcode = "";
            if (store_from_fns.inn != null) {
                fStore.put("inn", store_from_fns.inn);
                hashcode += store_from_fns.inn;
            }
            if (store_from_fns.name_from_fns != null) {
                //store_from_fns.name_from_fns= latinToKirillic(store_from_fns.name_from_fns);
                fStore.put("name_from_fns", store_from_fns.name_from_fns.trim());
                hashcode += store_from_fns.name_from_fns;
            }

            if (store_from_fns.address_from_fns != null) {
                fStore.put("address_from_fns", store_from_fns.address_from_fns.trim());
                hashcode += store_from_fns.address_from_fns.trim();
            }

            if (store_from_fns.inn != null)
                fStore.put("hashcode", encryptPassword(hashcode));

            if (store_from_fns._status != null) {
                fStore.put("_status", store_from_fns._status);
            }
            if (store_from_fns.date_add == null)
                store_from_fns.date_add = new Date().getTime();

            fStore.put("date_add", store_from_fns.date_add);


            if (store_from_fns.google_id != null) {
                mFirestore.collection(tableNameStoresFromFns).document(store_from_fns.google_id).update(fStore);
                Map<String, Object> updateChildren = new HashMap<>();
                updateChildren.put("/" + tableNameStoresFromFns + "/" + store_from_fns.google_id, fStore);
                mFirebase.updateChildren(updateChildren);
            } else {

                Task<QuerySnapshot> result = mFirestore.collection(tableNameStoresFromFns).whereEqualTo("hashcode", fStore.get("hashcode")).get(source);
                while (!result.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (result.isSuccessful()) {
                    Log.d(LOG_TAG, "saveStoreDataServer  result.isSuccessful() " + new Exception().getStackTrace()[0].getLineNumber());
                    if (!result.getResult().getMetadata().isFromCache()) {
                        List<DocumentSnapshot> documents = result.getResult().getDocuments();
                        if (documents.size() <= 0) {
                            Log.d(LOG_TAG, "saveStoreDataServer  not found on server " + new Exception().getStackTrace()[0].getLineNumber());
                            DocumentReference addedDocRef = mFirestore.collection(tableNameStoresFromFns).document();
                            addedDocRef.set(fStore);
                            store_from_fns.google_id = addedDocRef.getId();
                            mFirebase.child(tableNameStoresFromFns).child(store_from_fns.google_id).setValue(fStore);
                        } else {
                            store_from_fns.google_id = documents.get(0).getId();
                            Log.d(LOG_TAG, "saveStoreDataServer  found on server " + store_from_fns.google_id + "\n" + new Exception().getStackTrace()[0].getLineNumber());
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "saveStoreDataServer  repeat " + new Exception().getStackTrace()[0].getLineNumber());
                    //saveStoreDataServer(store_from_fns, store_on_map);
                    return;
                }
                if (store_from_fns.id != null) {
                    ContentValues dataUpdate = new ContentValues();
                    dataUpdate.put("google_id", store_from_fns.google_id);
                    int count = dbHelper.update(tableNameStoresFromFns, dataUpdate, "id=?", new String[]{store_from_fns.id.toString()});
                    Log.d(LOG_TAG, "updated in local db id=" + store_from_fns.id + "\n строк " + count);
                }
            }
        }
        if (store_on_map != null) {
            Map<String, Object> data_on_map = new HashMap<>();

            if (store_on_map.name != null) {
                data_on_map.put("name", store_on_map.name.trim());
            }
            if (store_on_map.address != null) {
                data_on_map.put("address", store_on_map.address.trim());
            }
            if (store_on_map.latitude != null) {
                data_on_map.put("latitude", store_on_map.latitude);
            }
            if (store_on_map.longitude != null) {
                data_on_map.put("longitude", store_on_map.longitude);
            }
            if (store_on_map.place_id != null) {
                data_on_map.put("place_id", store_on_map.place_id);
            }
            if (store_on_map.store_type != null) {
                data_on_map.put("store_type", store_on_map.store_type);
            }
            if (store_on_map.iconName != null) {
                data_on_map.put("iconName", store_on_map.iconName);
            }
            if (store_on_map.photo_reference != null) {
                data_on_map.put("photo_reference", store_on_map.photo_reference);
            }
            if (store_on_map.date_add == null)
                store_on_map.date_add = new Date().getTime();
            data_on_map.put("date_add", store_on_map.date_add);

            Task<QuerySnapshot> result = mFirestore.collection(tableNameStoresOnMap).whereEqualTo("place_id", store_on_map.place_id).get(source);
            while (!result.isComplete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (result.isSuccessful())
            {
                Log.d(LOG_TAG, "saveStoreDataServer  result.isSuccessful() " + new Exception().getStackTrace()[0].getLineNumber());
                if (!result.getResult().getMetadata().isFromCache()) {
                    List<DocumentSnapshot> documents = result.getResult().getDocuments();
                    if (documents.size() <= 0 && store_on_map.google_id != null) {
                        DocumentReference addedDocRef = mFirestore.collection(tableNameStoresOnMap).document(store_on_map.google_id);
                        addedDocRef.set(data_on_map);
                        mFirebase.child(tableNameStoresOnMap).child(store_on_map.google_id).setValue(data_on_map);
                    } else if (documents.size() <= 0) {
                        DocumentReference addedDocRef = mFirestore.collection(tableNameStoresOnMap).document();
                        addedDocRef.set(data_on_map);
                        store_on_map.google_id = addedDocRef.getId();
                        mFirebase.child(tableNameStoresOnMap).child(store_on_map.google_id).setValue(data_on_map);
                    } else if (store_on_map.google_id != null) {
                        tryToDeleteStoreOnMapFromServer(store_on_map, fk_stores_links_google_id);
                        store_on_map.google_id = documents.get(0).getId();
                    } else {
                        store_on_map.google_id = documents.get(0).getId();
                    }


                }
            } else {
                Log.d(LOG_TAG, "saveStoreDataServer  repeat " + new Exception().getStackTrace()[0].getLineNumber());
                //saveStoreDataServer(store_from_fns, store_on_map);
                //return;
            }

            if (store_on_map.id != null) {
                ContentValues dataUpdate = new ContentValues();
                dataUpdate.put("google_id", store_on_map.google_id);
                int count = dbHelper.update(tableNameStoresOnMap, dataUpdate, "id=?", new String[]{store_on_map.id.toString()});
                Log.d(LOG_TAG, "updated in local db id=" + store_on_map.id + "\n строк " + count);
            }
        }
    }

    public void setStoreData(InvoiceData invoiceData) throws Exception {

        InvoiceData.Store_from_fns store_from_fns = invoiceData.store_from_fns;
        InvoiceData.Store_on_map store_on_map = invoiceData.store_on_map;

        Boolean check_placeid = false;
        /*if(On_line && user.stores_from_fns_google_id != null)
        {
            if(store.stores_from_fns_google_id!= null)
            {
                Task<DocumentSnapshot> task = mFirestore.collection(tableNameStoresFromFns).document(store.stores_from_fns_google_id).get(source);
                while (!task.isComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        saveStoreDataLocal(store, store);
                        saveStoreDataServer(store);
                    } else {
                        check_placeid = true;
                    }
                } else {
                    throw new Exception(LOG_TAG + "\nsetStoreData()\n" + "Ошибка обращения к серверу");
                }
            }

            if((store.stores_from_fns_google_id != null && check_placeid) || (store.stores_from_fns_google_id == null && store.place_id!= null))
            {
                Task<QuerySnapshot> task_pl_id = mFirestore.collection(tableNameStoresFromFns).whereEqualTo("place_id", store.place_id).get(source);
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
                        InvoiceData.Store_from_fns storeDoc = task_pl_id.getResult().getDocuments().get(0).toObject(InvoiceData.Store_from_fns.class);
                        storeDoc.id = store.id;
                        saveStoreDataLocal(storeDoc, store);
                    }
                }
                else
                {
                    throw  new Exception(LOG_TAG +"\nsetStoreData()\n"+"Ошибка обращения к серверу");
                }
            }
            else if(store.stores_from_fns_google_id == null && store.place_id == null)
            {
                saveStoreDataLocal(store, store);
                saveStoreDataServer(store);
            }
        }
        else
        {
            saveStoreDataLocal(store, store);
        }*/
        if(On_line && user.google_id != null)
            saveStoreDataServer(store_from_fns, store_on_map, invoiceData.fk_stores_links_google_id);
        saveStoreDataLocal(store_from_fns, store_on_map);
    }

    private void deleteStoreOnMapLink(Integer Id, @Nullable String google_id)
    {
        boolean delete = false;
        Cursor cur = null;
        if(Id != null) {
            cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_on_map=?", new String[]{Id.toString()}, null, null, null, null);
            delete = cur.getCount() <=0;

            if (cur != null)
                cur.close();
            if (delete) {
                dbHelper.delete(tableNameStoresOnMap, "id=?", new String[]{Id.toString()});
            }
        }
        if(On_line && user.google_id != null && google_id != null)
        {
            Boolean removeStore = false;
            Task<QuerySnapshot> result = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_stores_on_map_google_id", google_id).get(source);
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
                removeStore = documents.size() == 0;
            }
            result = mFirestore.collection(tableNameInvoice).whereEqualTo("fk_invoice_stores_on_map_google_id", google_id).get(source);
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
                removeStore = documents.size() == 0;
            }
            result = mFirestore.collection(tableNameKktRegId).whereEqualTo("fk_kktRegId_google_id", google_id).get(source);
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
                removeStore = documents.size() == 0;
            }

            if(removeStore)
            {
                Task<Void> resultDelet = mFirestore.collection(tableNameStoresFromFns).document(google_id).delete();
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

    private void deleteStoreFromFnsLink(InvoiceData.Store_from_fns store_from_fns)
    {
        boolean delete = false;
        Cursor cur = null;
        if(store_from_fns.id != null) {
            if (!delete) {
                cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_from_fns=?", new String[]{store_from_fns.id.toString()}, null, null, null, null);
                delete = cur.moveToFirst();
            }

            if (cur != null)
                cur.close();
            if (!delete) {
                dbHelper.delete(tableNameStoresFromFns, "id=?", new String[]{store_from_fns.id.toString()});
            }
        }

        if(On_line && user.google_id != null && store_from_fns.google_id != null)
        {
            Boolean removeStore = false;
            Task<QuerySnapshot> result = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_stores_from_fns_google_id", store_from_fns.google_id).get(source);
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
                removeStore = documents.size() == 0;
            }
            result = mFirestore.collection(tableNameInvoice).whereEqualTo("fk_invoice_stores_from_fns_google_id", store_from_fns.google_id).get(source);
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
                removeStore = documents.size() == 0;
            }
            result = mFirestore.collection(tableNameKktRegId).whereEqualTo("fk_kktRegId_google_id", store_from_fns.google_id).get(source);
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
                removeStore = documents.size() == 0;
            }

            if(removeStore)
            {
                Task<Void> resultDelet = mFirestore.collection(tableNameStoresFromFns).document(store_from_fns.google_id).delete();
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

    private List<InvoiceData.Store_from_fns> loadDataFromStore_from_fns(Map<String, String> keyFields)
    {
        List<InvoiceData.Store_from_fns> listStore = new ArrayList<>();


        String selection="";
        List<String> listArgs = new ArrayList<>();
        for(Map.Entry<String, String> entry : keyFields.entrySet())
        {
            if(entry.getKey().equals("name_from_fns"))
            {
                selection +=entry.getKey()+" like ? AND ";
                listArgs.add("%"+entry.getValue()+"%");
            }
            else
            {
                selection += entry.getKey() + "=? AND ";
                listArgs.add(entry.getValue());
            }
        }

        selection=selection.substring(0, selection.length() - 5);
        String[] args = listArgs.toArray(new String[listArgs.size()]);
        Cursor cur_Store = dbHelper.query(tableNameStoresFromFns, null, selection, args, null, null, null, null);
        if(cur_Store.moveToFirst())
        {
            do{
                InvoiceData.Store_from_fns store_from_fns = new InvoiceData.Store_from_fns();
                store_from_fns.id = cur_Store.getInt(cur_Store.getColumnIndex("id"));
                store_from_fns.google_id = cur_Store.getString(cur_Store.getColumnIndex("google_id"));
                store_from_fns.hashCode = cur_Store.getString(cur_Store.getColumnIndex("hashcode"));

                store_from_fns.address_from_fns = cur_Store.getString(cur_Store.getColumnIndex("address_from_fns"));
                store_from_fns.name_from_fns = cur_Store.getString(cur_Store.getColumnIndex("name_from_fns"));
                store_from_fns._status = cur_Store.getInt(cur_Store.getColumnIndex("_status"));
                store_from_fns.inn = cur_Store.getLong(cur_Store.getColumnIndex("inn"));

                listStore.add(store_from_fns);
            }
            while(cur_Store. moveToNext());
            cur_Store.close();
        }
        cur_Store.close();


        return listStore;
    }
    private List<InvoiceData.Store_on_map> loadDataFromStore_on_map(Map<String, List<String>> keyFields, @Nullable String key)
    {
        List<InvoiceData.Store_on_map> listStore = new ArrayList<>();


        if(key == null)
        {
            key = "AND";
        }

        String selection="";
        List<String> listArgs = new ArrayList<>();
        for(Map.Entry<String, List<String>> entry : keyFields.entrySet())
        {
            if(entry.getKey().equals("name"))
            {
                selection += "(";
                for(String item : entry.getValue()) {
                    if(item.toLowerCase().equals("not null"))
                    {
                        selection += entry.getKey() + " " + item + " OR ";
                    }
                    else {
                        selection += entry.getKey() + " like ? OR ";
                        listArgs.add("%" + item + "%");
                    }


                }
                selection = selection.substring(0, selection.length() - 4)+") AND ";
            }
            else {
                selection += "(";
                for(String item : entry.getValue()) {
                    if(item.toLowerCase().equals("not null"))
                    {
                        selection += entry.getKey() + " " + item + " OR ";
                    }
                    else {
                        selection += entry.getKey() + "=? OR ";
                        listArgs.add(item);
                    }
                }
                selection = selection.substring(0, selection.length() - 4)+") AND ";
            }
        }

        selection=selection.substring(0, selection.length() - 5);
        String[] args = listArgs.toArray(new String[listArgs.size()]);
        Cursor cur_Store = dbHelper.query(tableNameStoresOnMap, null, selection, args, null, null, null, null);
        if(cur_Store.moveToFirst())
        {
            do{
                InvoiceData.Store_on_map store_on_map = new InvoiceData.Store_on_map();
                store_on_map.id = cur_Store.getInt(cur_Store.getColumnIndex("id"));
                store_on_map.google_id = cur_Store.getString(cur_Store.getColumnIndex("google_id"));

                //store_on_map.fk_stores_from_fns = cur_Store.getInt(cur_Store.getColumnIndex("fk_stores_from_fns"));
                //store_on_map.fk_stores_from_fns_google_id = cur_Store.getString(cur_Store.getColumnIndex("fk_stores_from_fns_google_id"));

                store_on_map.latitude = cur_Store.getDouble(cur_Store.getColumnIndex("latitude"));
                store_on_map.longitude = cur_Store.getDouble(cur_Store.getColumnIndex("longitude"));
                store_on_map.name = cur_Store.getString(cur_Store.getColumnIndex("name"));
                store_on_map.address = cur_Store.getString(cur_Store.getColumnIndex("address"));

                store_on_map.place_id = cur_Store.getString(cur_Store.getColumnIndex("place_id"));
                store_on_map.iconName = cur_Store.getString(cur_Store.getColumnIndex("iconName"));
                store_on_map.photo_reference = cur_Store.getString(cur_Store.getColumnIndex("photo_reference"));

                listStore.add(store_on_map);
            }
            while(cur_Store. moveToNext());
        }
        cur_Store.close();

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
        if(finalInvoiceData.store_from_fns == null)
            finalInvoiceData.store_from_fns = new InvoiceData.Store_from_fns();
        if(receipt.user != null)
            finalInvoiceData.store_from_fns.name_from_fns= latinToKirillic(receipt.user).trim();
        if(receipt.retailPlaceAddress != null)
            finalInvoiceData.store_from_fns.address_from_fns = receipt.retailPlaceAddress.trim().replaceAll("\\s{2,}", "");

        if(finalInvoiceData.store_from_fns.inn == null)
            finalInvoiceData.store_from_fns.update = true;
        else if(finalInvoiceData.store_from_fns.inn == 0)
            finalInvoiceData.store_from_fns.update = true;

        if(finalInvoiceData.store_from_fns._status == null)
            finalInvoiceData.store_from_fns._status = 0;
        if(receipt.userInn != null)
            finalInvoiceData.store_from_fns.inn = Long.valueOf(receipt.userInn.trim());

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

        if(finalInvoiceData.fk_invoice_kktRegId_store_links!= null)
        {
            updateData.put("fk_invoice_kktRegId_store_links", finalInvoiceData.fk_invoice_kktRegId_store_links);
        }

        updateData.put("_status", finalInvoiceData.get_status());
        if(finalInvoiceData.get_status() != null && finalInvoiceData.get_status()==1 && finalInvoiceData.server_status == null)
            fStore.put("server_status",1);
        else if(finalInvoiceData.server_status!= null)
            fStore.put("server_status",finalInvoiceData.server_status);
        else
            fStore.put("server_status", 0);

        if(On_line && user.google_id != null && finalInvoiceData.google_id != null && (finalInvoiceData.server_status == null || finalInvoiceData.server_status != 1))
        {
            mFirestore.collection(tableNameInvoice).document(finalInvoiceData.google_id).update(fStore);
            Map<String, Object> updateChildren = new HashMap<>();
            updateChildren.put("/"+tableNameInvoice+"/"+finalInvoiceData.google_id, fStore);
            mFirebase.updateChildren(updateChildren);
        }
        //dbHelper.update(tableNameInvoice, updateData, "id=?", new String[]{finalInvoiceData.getId().toString()});
        updateInvoice(finalInvoiceData);
        log.info(LOG_TAG+" updated invoice id " +finalInvoiceData.getId());
        Log.d(LOG_TAG, "updated invoice id " +finalInvoiceData.getId());


        //Start adding purchases and prod
        ArrayList<GetFnsData.Items> items = receipt.items;
        if(finalInvoiceData.store_from_fns.id != null && finalInvoiceData.kktRegId.id != null) {
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
                        Task<QuerySnapshot> result_product = mFirestore.collection(tableNameProducts).whereEqualTo("nameFromBill", item.name).get(source);
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

                if (finalInvoiceData.fk_invoice_kktRegId_store_links != null)
                    values.put("fk_purchases_kktRegId_stores_links", finalInvoiceData.fk_invoice_kktRegId_store_links);


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

                    if (google_item_id == null) {

                        Map<String, Object> fProducts = new HashMap<>();
                        ContentValues val = new ContentValues();
                        fProducts.put("nameFromBill", item.name);
                        DocumentReference addedDocRef_product = mFirestore.collection(tableNameProducts).document();
                        google_item_id = addedDocRef_product.getId();
                        addedDocRef_product.set(fProducts);
                        Log.d(LOG_TAG, "added in google product "+google_item_id + " name from bill " + item.name + "\n"+ new Exception().getStackTrace()[0].getLineNumber());
                        mFirebase.child(tableNameProducts).child(google_item_id).setValue(fProducts);

                        val.put("google_id", google_item_id);
                        dbHelper.update(tableNameProducts, val, "id =?", new String[]{String.valueOf(fk_purchases_products)});

                        fPurchases.put("fk_purchases_products_google_id", google_item_id);
                    }

                    Task<QuerySnapshot> result_product = mFirestore.collection(tableNamePurchases).whereEqualTo("fk_purchases_invoice_google_id",finalInvoiceData.google_id)
                            .whereEqualTo("fk_purchases_products_google_id", google_item_id).get(source);
                    while (!result_product.isComplete()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    List<DocumentSnapshot> documents = result_product.getResult().getDocuments();
                    String google_id = null;
                    ContentValues valuesUpdate = new ContentValues();
                    if(documents.size() <=0)
                    {
                        DocumentReference addedDocRef_purchase = mFirestore.collection(tableNamePurchases).document();
                        addedDocRef_purchase.set(fPurchases);
                        google_id = addedDocRef_purchase.getId();
                        Log.d(LOG_TAG, "added in google Purchas "+google_id +  "\n"+ new Exception().getStackTrace()[0].getLineNumber());
                        mFirebase.child(tableNamePurchases).child(google_id).setValue(fPurchases);
                    }
                    else
                    {
                        google_id = documents.get(0).getId();
                    }
                    valuesUpdate.put("google_id", google_id);
                    dbHelper.update(tableNamePurchases, valuesUpdate, "id=?", new String[]{tmp.toString()});
                }


            }
        }



        
        this.reLoadInvoice();
        return countPurchases;
    }

    private Integer saveKktRegId(InvoiceData.KktRegId kktRegId) throws Exception {
        //check kkt exist in base

        Log.d(LOG_TAG, "saveKktRegId "+kktRegId.kktRegId + " line "+new Exception().getStackTrace()[0].getLineNumber());
        String tableName ="kktRegId";
        int id;

        ContentValues data = new ContentValues();
        Map<String, Object> fStore = new HashMap<>();


        if(kktRegId.kktRegId != null) {
            data.put("kktRegId", kktRegId.kktRegId.toString());

            fStore.put("kktRegId", kktRegId.kktRegId);
        }
        else
            throw new Exception("Error kktRegId is null");

        data.put("_status", kktRegId._status != null ? kktRegId._status : 0);

        if(kktRegId.date_add==null)
            kktRegId.date_add = new Date().getTime();

        data.put("date_add", kktRegId.date_add);

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
                Task<QuerySnapshot> result = mFirestore.collection(tableNameKktRegId).whereEqualTo("kktRegId", kktRegId.kktRegId).get(source);
                while(!result.isComplete())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(result.isSuccessful()) {
                    Log.d(LOG_TAG, "saveKktRegId  result.isSuccessful() "+ new Exception().getStackTrace()[0].getLineNumber());
                    if (!result.getResult().getMetadata().isFromCache()) {
                        List<DocumentSnapshot> documents = result.getResult().getDocuments();
                        if (documents.size() <= 0) {
                            DocumentReference addedDocRef = mFirestore.collection(tableNameKktRegId).document();
                            addedDocRef.set(fStore);
                            kktRegId.google_id = addedDocRef.getId();
                            mFirebase.child(tableNameKktRegId).child(kktRegId.google_id).setValue(fStore);
                        }
                        else
                        {
                            kktRegId.google_id = documents.get(0).getId();
                        }
                    }
                }
                else
                {
                    Log.d(LOG_TAG, "saveKktRegId  repeat "+ new Exception().getStackTrace()[0].getLineNumber());
                    return saveKktRegId(kktRegId);
                }
            }
        }

        if(kktRegId.google_id != null)
        {
            data.put("google_id", kktRegId.google_id);
        }

        if(kktRegId.id != null) {
            id = dbHelper.update(tableName, data, "id=?", new String[]{kktRegId.id.toString()});
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


        if(kktRegId.fk_kktRegId_google_id != null)
        {
            fStore.put("fk_kktRegId_google_id", kktRegId.fk_kktRegId_google_id);
        }
        if(kktRegId.kktRegId != null) {
            fStore.put("kktRegId", kktRegId.kktRegId);
        }
        else
            throw new Exception("Error kktRegId is null");

        fStore.put("_status", kktRegId._status != null ? kktRegId._status : 0);

        if(kktRegId.date_add==null)
            kktRegId.date_add = new Date().getTime();


        fStore.put("date_add", kktRegId.date_add);

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
                //addedDocRef.set(fStore);
                kktRegId.google_id = addedDocRef.getId();
                mFirestore.collection(tableNameKktRegId).document(kktRegId.google_id).set(fStore);

                //kktRegId.stores_from_fns_google_id = mFirebase.child(tableNameKktRegId).push().getKey();
                mFirebase.child(tableNameKktRegId).child(kktRegId.google_id).setValue(fStore);


                if(kktRegId.id!= null) {
                    ContentValues dataUpdate = new ContentValues();
                    dataUpdate.put("stores_from_fns_google_id", kktRegId.google_id);
                    if(kktRegId.fk_kktRegId_google_id != null)
                    {
                        dataUpdate.put("fk_kktRegId_google_id", kktRegId.fk_kktRegId_google_id);
                    }
                    int count = dbHelper.update(tableNameKktRegId, dataUpdate, "id=?", new String[]{kktRegId.id.toString()});
                    Log.d(LOG_TAG, "updated in local db id="+kktRegId.id+"\n строк "+count);
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


        if(kktRegId.fk_kktRegId_google_id != null)
        {
            data.put("fk_kktRegId_google_id", kktRegId.fk_kktRegId_google_id);
        }
        if(kktRegId.kktRegId != null) {
            data.put("kktRegId", kktRegId.kktRegId.toString());
        }
        else
            throw new Exception("Error kktRegId is null");
        data.put("_status", kktRegId._status != null ? kktRegId._status : 0);

        if(kktRegId.date_add==null)
            kktRegId.date_add = new Date().getTime();

        data.put("date_add", kktRegId.date_add);

        if(kktRegId.google_id != null)
        {
            data.put("stores_from_fns_google_id", kktRegId.google_id);
        }

        if(kktRegId.id != null) {
            id = dbHelper.update(tableName, data, "id=?", new String[]{kktRegId.id.toString()});
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
        }
        if(invoiceData.get_status() != null) {
            data.put("_status", invoiceData.get_status());
        }

        if(invoiceData.fk_invoice_kktRegId_store_links!= null)// !=0
        {
            data.put("fk_invoice_kktRegId_store_links", invoiceData.fk_invoice_kktRegId_store_links.toString());
        }
        if(invoiceData.fk_invoice_kktRegId_store_links_google_id != null )
        {
            data.put("fk_invoice_kktRegId_store_links_google_id", invoiceData.fk_invoice_kktRegId_store_links_google_id);
            fStore.put("fk_invoice_kktRegId_store_links_google_id", invoiceData.fk_invoice_kktRegId_store_links_google_id);
        }


        if(invoiceData.get_order()!=null) {
            data.put("_order", invoiceData.get_order());

            fStore.put("_order", invoiceData.get_order());
        }

        insertNewAccountingList(invoiceData);

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



        if (invoiceData.latitudeAdd != null) {
            data.put("latitudeAdd", invoiceData.latitudeAdd);

            fStore.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if (invoiceData.longitudeAdd != null) {
            data.put("longitudeAdd", invoiceData.longitudeAdd);

            fStore.put("longitudeAdd", invoiceData.longitudeAdd);
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

    public void updateInvoiceLocal (InvoiceData invoiceData)
    {
        Long dateInvoice = Long.valueOf(invoiceData.getDateInvoice(1));

        ContentValues data = new ContentValues();
        data.put("FP", invoiceData.FP);
        data.put("FD", invoiceData.FD);
        data.put("FN", invoiceData.FN);
        if(dateInvoice > 0)
            data.put("dateInvoice", dateInvoice);
        data.put("fullPrice", invoiceData.getFullPrice());
        data.put("in_basket", invoiceData.isIn_basket()==null ? 0: invoiceData.isIn_basket());



        if(invoiceData.repeatCount != null) {
            data.put("repeatCount", invoiceData.repeatCount);
        }

        if(invoiceData.google_id != null) {
            data.put("stores_from_fns_google_id", invoiceData.google_id);
        }
        if(invoiceData.get_status() != null) {
            data.put("_status", invoiceData.get_status());
        }



        if(invoiceData.get_order()!=null) {
            data.put("_order", invoiceData.get_order());
        }
        if(invoiceData.getFk_invoice_accountinglist() !=null) {
            data.put("fk_invoice_accountinglist", invoiceData.getFk_invoice_accountinglist().toString());
        }

        if(invoiceData.get_status() != null && invoiceData.get_status()==1 ) {
            data.put("server_status", 1);
        }
        else if(invoiceData.server_status!= null) {
            data.put("server_status", invoiceData.server_status);
        }
        else {
            data.put("server_status", 0);
        }

        if(invoiceData.store_from_fns != null && invoiceData.store_from_fns.id != null) {
            data.put("fk_invoice_stores", invoiceData.store_from_fns.id);
        }
        if (invoiceData.kktRegId!= null && invoiceData.kktRegId.id!= null) {
            data.put("fk_invoice_kktRegId", invoiceData.kktRegId.id);
        }

        if (invoiceData.latitudeAdd != null) {
            data.put("latitudeAdd", invoiceData.latitudeAdd);
        }
        if (invoiceData.longitudeAdd != null) {
            data.put("longitudeAdd", invoiceData.longitudeAdd);
        }

        if (invoiceData.fk_invoice_kktRegId_google_id != null) {
            data.put("fk_invoice_kktRegId_google_id", invoiceData.fk_invoice_kktRegId_google_id);
        }

        if (invoiceData.fk_invoice_stores_from_fns_google_id != null) {
            data.put("fk_invoice_stores_from_fns_google_id", invoiceData.fk_invoice_stores_from_fns_google_id);
        }

        if (invoiceData.fk_invoice_accountinglist_google_id != null) {
            data.put("fk_invoice_accountinglist_google_id", invoiceData.fk_invoice_accountinglist_google_id);
        }

        Integer id = invoiceData.getId();


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
            tryToDeleteStoreOnMap(invoiceData.store_on_map);
            

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

    private boolean tryToDeleteStoreFromFns(InvoiceData.Store_from_fns store_from_fns) {
        if(store_from_fns.id != null)
        {
            Cursor cursor = dbHelper.query(tableNameStoresLinks, null, "fk_stores_from_fns=?",
                    new String[]{store_from_fns.id.toString()}, null, null, null, null);
            if (!cursor.moveToFirst()) {
                long count = dbHelper.delete(tableNameStoresOnMap, "id=?", new String[]{store_from_fns.id.toString()});
                return count>0;
            }
        }
        return false;
    }

    private boolean tryToDeleteStoreOnMap(InvoiceData.Store_on_map store_on_map) {
        if(store_on_map != null && store_on_map.place_id == null && store_on_map.id != null) {
            Cursor cursor = dbHelper.query(tableNameStoresLinks, null, "fk_stores_on_map=?",
                    new String[]{store_on_map.id.toString()}, null, null, null, null);
            if (!cursor.moveToFirst()) {
                long count = dbHelper.delete(tableNameStoresOnMap, "id=?", new String[]{store_on_map.id.toString()});
                ContentValues contentValues = new ContentValues();
                contentValues.putNull("fk_stores_on_map");
                dbHelper.update(tableNameStoresLinks, contentValues,"fk_stores_on_map=?", new String[]{store_on_map.id.toString()});
                return count>0;
            }
        }
        return false;
    }
    private void tryToDeleteStoreOnMapFromServer(InvoiceData.Store_on_map store_on_map, String fk_stores_links_google_id)
    {
        Task<QuerySnapshot> result = mFirestore.collection(tableNameStoresLinks).whereEqualTo("fk_stores_on_map_google_id", store_on_map.place_id).get(source);
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
            try {
                String id = result.getResult().getDocuments().get(0).getId();
                if (id != null && id.equals(fk_stores_links_google_id))
                    mFirestore.collection(tableNameStoresOnMap).document(store_on_map.place_id).delete();
            }
            catch (Exception ex){}
        }
    }

    public int getCount(Map<String, String[]> filter) {

        Cursor cur = null;
        if(filter == null) {
            cur = dbHelper.query(tableNameInvoice, null, null, null, null, null, null, null);
        }
        else if(filter.containsKey("place_id"))
        {
            String selection="";
            String[] args;
            int count=0;
            Integer fk_stores_links = 0;


            if(filter.get("place_id")[0]. equals("not null")) {
                selection = "select * from " + tableNameInvoice + " where fk_invoice_kktRegId_store_links in" +
                        "(select id from " + tableNameKktRegIdStoresLinks + " where fk_stores_links in" +
                        "(select id from " + tableNameStoresLinks + " where  fk_stores_on_map in (" +
                        "select id from " + tableNameStoresOnMap + " where place_id " + filter.get("place_id")[0] + ")))";
            }
            else
            {
                selection = "select * from " + tableNameInvoice + " where fk_invoice_kktRegId_store_links in" +
                        "(select id from " + tableNameKktRegIdStoresLinks + " where fk_stores_links in" +
                        "(select id from " + tableNameStoresLinks + " where  fk_stores_on_map is null or fk_stores_on_map in (" +
                        "select id from " + tableNameStoresOnMap + " where place_id " + filter.get("place_id")[0] + ")))";
            }
            cur = dbHelper.rawQuery(selection, null);

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

    public Double[] findBestLocation(InvoiceData.Store_from_fns store) {
        Double[] latLng = new Double[2];
        String selection="";
        String[] args;
        int count=0;
        Integer fk_stores_links = 0;

        selection ="select * from "+tableNameKktRegIdStoresLinks+" where fk_stores_links in"+
                "(select id from "+tableNameStoresLinks+" where fk_stores_on_map in (" +
                "select fk_stores_on_map from "+tableNameStoresLinks+" where fk_stores_from_fns in (select id from "+tableNameStoresFromFns+" where hashcode =?))" +
                "and fk_stores_on_map in" +
                "(select id from "+tableNameStoresOnMap+" where place_id NOT NULL))";
        args = new String[]{encryptPassword(store.inn+ store.name_from_fns+ store.address_from_fns)};
        Cursor cur = dbHelper.rawQuery(selection, args);

        if(cur.moveToFirst())
        {

            Map<Integer, Integer> countFkStores = new HashMap<>();
            do {
                int new_fk_stores_links = cur.getInt(cur.getColumnIndex("fk_stores_links"));

                //запрос с писокм количества чеков привяза
                Cursor curInvoice = dbHelper.query(tableNameInvoice, null, "fk_invoice_kktRegId_store_links=?", new String[]{String.valueOf(cur.getInt(cur.getColumnIndex("id")))}, null, null, null, null);
                int tmp = curInvoice.getCount();
                if(countFkStores.get(new_fk_stores_links)!= null)
                {
                    countFkStores.put(new_fk_stores_links,tmp + countFkStores.get(new_fk_stores_links));
                }
                else
                    countFkStores.put(new_fk_stores_links,tmp);
                curInvoice.close();
            }
            while(cur.moveToNext());
            cur.close();
            if(countFkStores.size()>0)
            {
                for(Map.Entry<Integer, Integer> entry : countFkStores.entrySet())
                {
                    if(count< entry.getValue()) {
                        count = entry.getValue();
                        fk_stores_links = entry.getKey();
                    }
                }
                if(fk_stores_links>0)
                {
                    String query = "select * from "+tableNameStoresOnMap+" where id in (Select fk_stores_on_map from "+tableNameStoresLinks+" where id =? limit 1)";
                    args = new String[]{fk_stores_links.toString()};
                    cur = dbHelper.rawQuery(query, args);
                    if(cur.moveToFirst())
                    {
                        latLng[0] = cur.getDouble(cur.getColumnIndex("latitude"));
                        latLng[1] = cur.getDouble(cur.getColumnIndex("longitude"));
                    }

                }
            }
        }
        cur.close();
        return  latLng;
    }


    public List<InvoiceData.Store_on_map> findBestLocation(InvoiceData invoiceData) {
        List<InvoiceData.Store_on_map> store_on_mapList = new ArrayList<>();

        if(invoiceData.store_from_fns != null && invoiceData.store_from_fns.id!= null)
        {
            Cursor cur = dbHelper.query(tableNameStoresLinks, null, "fk_stores_from_fns=? AND fk_stores_on_map not null ", new String[]{invoiceData.store_from_fns.id.toString()}, null, null, null, null);
            if(cur.moveToFirst())
            {
                List<String> map_tmp = new ArrayList<>();
                do {
                    map_tmp.add(String.valueOf(cur.getInt(cur.getColumnIndex("fk_stores_on_map"))));
                }
                while(cur.moveToNext());
                Map<String, List<String>> map = new HashMap<>();
                map.put("id", map_tmp);
                map.put("place_id", Arrays.asList("not null"));
                store_on_mapList = loadDataFromStore_on_map(map, "OR");
            }
            cur.close();
        }



        return store_on_mapList;
    }


    public void clearPrivatInvoicesOnServer() {
        if(On_line && user.google_id != null)
        {
            Log.d(LOG_TAG, "start deleting from server clearPrivatInvoicesOnServer"  + new Exception().getStackTrace()[0].getLineNumber());
            Task<QuerySnapshot> task = mFirestore.collection(tableNameInvoice).whereEqualTo("user_google_id", user.google_id).get(source);
            while(!task.isComplete())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(task.isSuccessful())
            {
                if (!task.getResult().getMetadata().isFromCache()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    if (documents.size() > 0) {
                        for (int i = 0; i<documents.size(); i++)
                        {
                            String currentGoogleId = documents.get(i).getId();
                            deleteFromServerCollection(tableNamePurchases, "fk_purchases_invoice_google_id", currentGoogleId);
                            deleteFromServerCollection(tableNameCollectedData, "fk_collectedData_invoice_google_id", currentGoogleId);
                            mFirestore.collection(tableNameInvoice).document(currentGoogleId).delete();

                        }
                    }
                }
            }
        }
    }

    private  void deleteFromServerCollection(String collectionName, String whereField, String whereFieldData)
    {
        Log.d(LOG_TAG, " deleteFromServerCollection \nstart deleting from server " + collectionName + " whereField " +whereField + " whereFieldData "+whereFieldData + new Exception().getStackTrace()[0].getLineNumber());
        Task<QuerySnapshot> task = mFirestore.collection(collectionName).whereEqualTo(whereField, whereFieldData).get(source);
        while(!task.isComplete())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(task.isSuccessful())
        {
            Log.d(LOG_TAG, "deleteFromServerCollection \n task.isSuccessful " + collectionName + " whereField " +whereField + " whereFieldData "+whereFieldData + new Exception().getStackTrace()[0].getLineNumber());
            if (!task.getResult().getMetadata().isFromCache()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                if (documents.size() > 0) {
                    for (int i = 0; i<documents.size(); i++)
                    {
                        Log.d(LOG_TAG, "deleteFromServerCollection \n task.isSuccessful " + collectionName + " document " +documents.get(i).getId() + new Exception().getStackTrace()[0].getLineNumber());
                        mFirestore.collection(collectionName).document(documents.get(i).getId()).delete();
                    }
                }
            }
        }
    }
}
