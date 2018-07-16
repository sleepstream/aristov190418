package com.sleepstream.checkkeeper.linkedListObjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;

public class LinkedListClass {
    final String LOG_TAG = "LinkedListClass";
    public List<LinkedListData> linkedListData = new ArrayList<>();
    private String tableName="linked_objects";

    public LinkedListClass() {

        if(loadData())
        {
            Log.d(LOG_TAG, "Loaded from DB records " + linkedListData.size());
        }
        else {
            Log.d(LOG_TAG, "No records in DB");
        }

    }

    private boolean loadData() {
        Cursor cur = dbHelper.query(tableName, null, null, null, null, null, null, null);
        linkedListData.clear();
        if(cur.moveToFirst())
        {
            do{
                LinkedListData linkedData = new LinkedListData();

                linkedData.setId(cur.getInt(cur.getColumnIndex("id")));
                linkedData.setFk_id(cur.getInt(cur.getColumnIndex("fk_id")));
                linkedData.setFk_name(cur.getString(cur.getColumnIndex("fk_name")));
                linkedData.set_order(cur.getInt(cur.getColumnIndex("_order")));


                Cursor fk_cur = dbHelper.query(linkedData.getFk_name(), null, "id=?", new String[]{linkedData.getFk_id().toString()}, null, null, null, null);
                switch(linkedData.getFk_name())
                {
                    case "invoice":
                        linkedData.invoiceData = new InvoiceData();
                        if(fk_cur.moveToFirst())
                        {
                            linkedData.invoiceData.setAll(fk_cur.getString(fk_cur.getColumnIndex("FP")),
                                    fk_cur.getString(fk_cur.getColumnIndex("FD")),
                                    fk_cur.getString(fk_cur.getColumnIndex("FN")),
                                    Long.parseLong(fk_cur.getString(fk_cur.getColumnIndex("dateInvoice"))),
                                    fk_cur.getString(fk_cur.getColumnIndex("fullPrice")),
                                    fk_cur.getInt(fk_cur.getColumnIndex("id")),
                                    fk_cur.getInt(fk_cur.getColumnIndex("_order")),
                                    fk_cur.getInt(fk_cur.getColumnIndex("fk_invoice_accountinglist")),
                                    fk_cur.getInt(fk_cur.getColumnIndex("fk_invoice_kktRegId")));
                            linkedData.invoiceData.set_status(fk_cur.getInt(fk_cur.getColumnIndex("_status")));

                            if(linkedData.invoiceData.getfk_invoice_kktRegId() !=null)
                            {

                                fk_cur.close();
                                fk_cur = dbHelper.query("purchases", null, "fk_purchases_invoice=?", new String[]{linkedData.invoiceData.getId().toString()}, null, null, null, null);
                                if(fk_cur.moveToFirst())
                                {
                                    linkedData.invoiceData.quantity = fk_cur.getCount();
                                }
                                Integer id = linkedData.invoiceData.getfk_invoice_kktRegId();
                                fk_cur.close();
                                fk_cur = dbHelper.query("kktRegId", null, "id=?", new String[]{id.toString()}, null, null, null, null);

                                if(fk_cur.moveToFirst()) {
                                    linkedData.invoiceData.kktRegId = new InvoiceData.KktRegId();

                                    linkedData.invoiceData.kktRegId.id = fk_cur.getInt(fk_cur.getColumnIndex("id"));
                                    linkedData.invoiceData.kktRegId.fk_kktRegId_stores = fk_cur.getInt(fk_cur.getColumnIndex("fk_kktRegId_stores"));
                                    linkedData.invoiceData.kktRegId.kktRegId = fk_cur.getLong(fk_cur.getColumnIndex("kktRegId"));
                                    linkedData.invoiceData.kktRegId._status = fk_cur.getInt(fk_cur.getColumnIndex("_status"));

                                    fk_cur.close();
                                    fk_cur = dbHelper.query("stores", null, "id=?", new String[]{linkedData.invoiceData.kktRegId.fk_kktRegId_stores.toString()}, null, null, null, null);
                                    if(fk_cur.moveToFirst())
                                    {
                                        InvoiceData.Store store = new InvoiceData.Store();
                                        store.address = fk_cur.getString(fk_cur.getColumnIndex("address"));
                                        store.id = fk_cur.getInt(fk_cur.getColumnIndex("id"));
                                        store.name = fk_cur.getString(fk_cur.getColumnIndex("name"));

                                        store.longitude = fk_cur.getDouble(fk_cur.getColumnIndex("longitude"));
                                        store.latitude = fk_cur.getDouble(fk_cur.getColumnIndex("latitude"));
                                        store.inn = fk_cur.getLong(fk_cur.getColumnIndex("inn"));

                                        linkedData.invoiceData.store = store;
                                    }
                                }
                            }


                        }
                        break;
                    case "accountingList":
                        linkedData.accountingListData = new AccountingListData();
                        if(fk_cur.moveToFirst())
                        {
                            linkedData.accountingListData.setName(fk_cur.getString(fk_cur.getColumnIndex("listName")));
                            linkedData.accountingListData.setId(fk_cur.getInt(fk_cur.getColumnIndex("id")));
                            linkedData.accountingListData.setOrder(fk_cur.getInt(fk_cur.getColumnIndex("_order")));
                        }
                        break;
                }

                fk_cur.close();
                linkedListData.add(linkedData);
            }
            while(cur.moveToNext());
            cur.close();
            Log.d(LOG_TAG, "Loaded from DB records " + linkedListData.size());
            cur.close();
            return true;
        }
        cur.close();

        return false;
    }

    public void reLoadLinkedList() {
        loadData();
    }

    public  void addLinkedObject(LinkedListData linkedListData)
    {
        Cursor cur = dbHelper.query(tableName, null, "fk_name=? AND fk_id=?", new String[]{linkedListData.getFk_name(), linkedListData.getFk_id().toString()}, null, null, null, null);
        if(!cur.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("fk_id", linkedListData.getFk_id());
            contentValues.put("fk_name", linkedListData.getFk_name());
            if (linkedListData.get_order() != null)
                contentValues.put("_order", linkedListData.get_order());
            Long id = dbHelper.insert(tableName, null, contentValues);
            if (id > -1) {
                contentValues.put("_order", id);
                dbHelper.update(tableName, contentValues, "id=?", new String[]{id.toString()});
            }
        }
        cur.close();
    }
    public void deleteLinkedObject(Integer id, String tableName)
    {
        Long count = dbHelper.delete(this.tableName, "fk_name=? AND fk_id=?", new String[]{tableName,id.toString()});
    }
    public void deleteLinkedObject(Integer id)
    {
        Long count = dbHelper.delete(tableName, "id=?", new String[]{id.toString()});
    }

    public int getCount() {
        Cursor cur = dbHelper.query(tableName, null, null, null, null, null, null, null);
        int count = cur.getCount();
        cur.close();
        if(count >0)
            return count;
        else
            return 0;
    }
}
