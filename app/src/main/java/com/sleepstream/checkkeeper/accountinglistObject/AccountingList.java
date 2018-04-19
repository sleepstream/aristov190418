package com.sleepstream.checkkeeper.accountinglistObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;

public class AccountingList {


    final String LOG_TAG = "AccountingList";
    public List<AccountingListData> accountingListData = new ArrayList<>();
    private String tableName="accountingList";
    private String[] tableName_fk = new String[]{"invoice", "purchases"};
    public int lastIDCollection;

    public AccountingList() {


        reloadAccountingList();
        
    }

    public void reloadAccountingList()
    {
        this.accountingListData.clear();
        Cursor cur = dbHelper.query(tableName, null, null, null, null, null, "_order", null);
        if(cur.moveToFirst())
        {

            do {
                AccountingListData accountingListData = new AccountingListData();
                accountingListData.setName(cur.getString(cur.getColumnIndex("listName")));

                accountingListData.setId(cur.getInt(cur.getColumnIndex("id")));
                accountingListData.setOrder(cur.getInt(cur.getColumnIndex("_order")));

                Cursor fk_cur = dbHelper.query("linked_objects", null, "fk_name = ? and fk_id = ?", new String[]{tableName, accountingListData.getId().toString()}, null, null, null, null);
                if(fk_cur.moveToFirst())
                    accountingListData.setPinId(fk_cur.getInt(fk_cur.getColumnIndex("id")));
                fk_cur.close();


                this.accountingListData.add(accountingListData);
            }
            while(cur.moveToNext());

            Log.d(LOG_TAG, "Loaded from DB records " + accountingListData.size());
        }
        cur.close();
        Log.d(LOG_TAG, "No records in DB");
    }

    public String addAccountingList (Integer position, AccountingListData accountingListData)
    {
        String listName = accountingListData.getName();
        long id;
        Integer order=0;
        
        if(position != null)
        {
            order = accountingListData.getOrder();
            id = accountingListData.getId();
            Cursor cur = dbHelper.query(tableName, null, "id=?", new String[]{id+""}, null, null, null, null);
            if(this.accountingListData.contains(position)) {
                if (cur.moveToFirst() && this.accountingListData.get(position).equals(accountingListData)) {
                    cur.close();
                    return "exist";
                }
            }
            else if(cur.moveToFirst())
            {
                cur.close();
                this.accountingListData.add(position, accountingListData);
                return "restored";
            }
            cur.close();
        }
        else
        {
            Cursor cur = dbHelper.query(tableName, null, "listName=?", new String[]{listName}, null, null, null, null);
            if(cur.moveToFirst()) {
                cur.close();
                boolean key = false;
                for(int i=0; i<this.accountingListData.size(); i++)
                {
                    if(this.accountingListData.get(i).getName().equals(listName))
                    {
                        key = true;
                    }
                }
                if(key) {
                    return "exist";
                }
                else
                {
                    this.accountingListData.add(accountingListData);
                    this.lastIDCollection= this.accountingListData.size()-1;
                    return "added in list";
                }
            }
            cur.close();
        }
        ContentValues data = new ContentValues();
        data.put("listName", listName);
        if(position !=null)
            data.put("_order", order);
        id = dbHelper.insert(tableName, null, data);
        
        if(id>-1)
        {
                accountingListData = new AccountingListData();
                accountingListData.setName(listName);
                accountingListData.setId((int) id);
                accountingListData.setOrder((int)id);

                if(position!=null) {
                    this.accountingListData.add(position, accountingListData);
                    this.lastIDCollection= position;
                }
                else {
                    this.accountingListData.add(accountingListData);
                    this.lastIDCollection= this.accountingListData.size()-1;
                }

                this.updateAccointingListData(accountingListData);
                Log.d(LOG_TAG, "Inserted record id: " + id);
                return "";
        }
        else
        {
                Log.d(LOG_TAG, "Inserted ERROR ");
                return "error";
        }

    }


    public void insertAccountingListData ()
    {
        for(int i=0; i<accountingListData.size(); i++)
        {
            //AccountingListData tmpData = accountingListData.get(i);
            if(accountingListData.get(i).getId() == null)
            {
                ContentValues data = new ContentValues();
                data.put("listName", accountingListData.get(i).getName());
                data.put("_order", accountingListData.get(i).getOrder());

                Cursor cur =dbHelper.query(tableName, null, "listName=?", new String[]{accountingListData.get(i).getName()}, null, null, null, null);

                if(cur.getCount() == 0) {
                    long count = dbHelper.insert(tableName, null, data);
                    if (count > -1) {
                        accountingListData.get(i).setId((int) count);
                        //updateInformation in collection
                        accountingListData.set(i, accountingListData.get(i));
                    }

                    Log.d(LOG_TAG, "Inserted record id: " + count);
                }
                else
                {
                    Log.d(LOG_TAG, "Insert record ERROR: record with listName exist: " + accountingListData.get(i).getId() +"/"+ accountingListData.get(i).getName()  +"\n");
                }
                cur.close();
                
            }
        }



    }

    public boolean deleteAccointingListData(Integer id)
    {
        



        Log.d(LOG_TAG, "tr remove accountingList "+ id);
        for(int i = 0; i<tableName_fk.length; i++)
        {
            Cursor cur = dbHelper.query(tableName_fk[i], null, "fk_"+tableName_fk[i]+"_"+tableName+"=?", new String[]{id+""}, null, null, null, "1");
            if(cur.moveToFirst())
            {
                cur.close();
                Log.d(LOG_TAG, "tr remove accountingList есть связанные таблицы"+ id);
                return false;
            }
            cur.close();
        }

        long count = dbHelper.delete(tableName, "id=?", new String[]{id.toString()});
        
        if(count>0) {
            for (int i = 0; i < accountingListData.size(); i++) {
                //AccountingListData accountingListData = accountingListData.get(i);
                if (accountingListData.get(i).getId() == id)
                {
                    accountingListData.remove(i);
                    Log.d(LOG_TAG, "tr removeD accountingList "+ id);
                    return true;
                }
            }

            Log.d(LOG_TAG, "tr removeD only from DB accountingList "+ id);

        }
        return false;

    }

    public boolean updateAccointingListData(AccountingListData accountingListData) {


        ContentValues values = new ContentValues();
        Integer id = accountingListData.getId();
        values.put("listName", accountingListData.getName());
        values.put("_order", accountingListData.getOrder());
        long count = dbHelper.update(tableName, values, "id=?", new String[]{id.toString()});
        //check list in another tables

        if (count > 0) {
            return true;

        }
        return false;

    }


    public String getTableName() {
        return tableName;
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
