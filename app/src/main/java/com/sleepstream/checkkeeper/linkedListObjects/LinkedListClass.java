package com.sleepstream.checkkeeper.linkedListObjects;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingListData;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;
import static com.sleepstream.checkkeeper.MainActivity.invoice;
import static com.sleepstream.checkkeeper.invoiceObjects.Invoice.*;

public class LinkedListClass {
    final String LOG_TAG = "LinkedListClass";
    public List<LinkedListData> linkedListData = new ArrayList<>();

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
        Cursor cur = dbHelper.query(tableNameLinkedObjects, null, null, null, null, null, null, null);
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
                        List<InvoiceData> listInvoice = invoice.loadData(fk_cur);
                        if(listInvoice.size()>0)
                            linkedData.invoiceData = listInvoice.get(0);
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

    public  Integer addLinkedObject(LinkedListData linkedListData)
    {
        Integer id = null;
        Cursor cur = dbHelper.query(tableNameLinkedObjects, null, "fk_name=? AND fk_id=?", new String[]{linkedListData.getFk_name(), linkedListData.getFk_id().toString()}, null, null, null, null);
        if(!cur.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("fk_id", linkedListData.getFk_id());
            contentValues.put("fk_name", linkedListData.getFk_name());
            if (linkedListData.get_order() != null)
                contentValues.put("_order", linkedListData.get_order());
            id = (int)dbHelper.insert(tableNameLinkedObjects, null, contentValues);
            if (id > -1) {
                contentValues.put("_order", id);
                dbHelper.update(tableNameLinkedObjects, contentValues, "id=?", new String[]{id.toString()});
            }
        }
        else
        {
            id = cur.getInt(cur.getColumnIndex("id"));
        }
        cur.close();


        return id;
    }

    public void deleteLinkedObject(Integer id)
    {
        Long count = dbHelper.delete(tableNameLinkedObjects, "id=?", new String[]{id.toString()});
    }

    public int getCount() {
        Cursor cur = dbHelper.query(tableNameLinkedObjects, null, null, null, null, null, null, null);
        int count = cur.getCount();
        cur.close();
        if(count >0)
            return count;
        else
            return 0;
    }
}
