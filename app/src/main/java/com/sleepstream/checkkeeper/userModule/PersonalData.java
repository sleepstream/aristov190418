package com.sleepstream.checkkeeper.userModule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.google.common.io.BaseEncoding;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.sleepstream.checkkeeper.MainActivity.dbHelper;

public class PersonalData {

    private Map<String, String> data;
    private String tableName="PersonalData";
    public String name= null;
    public String e_mail = null;
    public String telephone_number = null;
    public String password = null;
    public Integer id;
    public Integer _status;
    final String LOG_TAG = "PersonalData";
    private Context context;


    public PersonalData(Context context)
    {
        this.context = context;
        data = new HashMap();
        
        Cursor cur = dbHelper.query(tableName, null, null, null, null, null, null, "1");
        if(cur.getCount() >0)
        {
            cur.moveToFirst();
            try {
                id = cur.getInt(cur.getColumnIndex("id"));
                name = cur.getString(cur.getColumnIndex("name"));
                e_mail = cur.getString(cur.getColumnIndex("e_mail"));
                telephone_number = cur.getString(cur.getColumnIndex("telephone_number"));
                password = cur.getString(cur.getColumnIndex("password"));
                _status = cur.getInt(cur.getColumnIndex("_status"));
                Log.d(LOG_TAG, "Select records on construct: OK");
            }
            catch (Exception ex)
            {
                Log.d(LOG_TAG, "Select records on construct: ERROR");
            }
            cur.close();
        }
        else
        {
            Log.d(LOG_TAG, "No personal data in DB: ERROR");
        }
        cur.close();


    }

    public void updatePersonalData ()
    {
        
        ContentValues data = new ContentValues();
        data.put("name", name);
        data.put("telephone_number", telephone_number);
        if(password != null)
            data.put("password", password);
        data.put("e_mail", e_mail);
        if(_status != null)
            data.put("_status", _status);
        else
            data.put("_status", 0);
        int count = dbHelper.update(tableName, data, "id=?", new String[]{id+""});
        Log.d(LOG_TAG, "Updated records: "+count);
        
    }

    public void setPersonalData()
    {
        if(id == null)
        {
            insertPersonalData();
        }
        else
        {
            updatePersonalData();
        }
    }

    public void insertPersonalData ()
    {
        ContentValues data = new ContentValues();
        data.put("name", name);
        data.put("telephone_number", telephone_number);
        if(password!= null)
            data.put("password", password);
        data.put("e_mail", e_mail);
        data.put("_status", 0);

        Cursor cur =dbHelper.query(tableName, null, "telephone_number=?", new String[]{telephone_number}, null, null, null, null);
        if(cur.getCount() == 0) {
            long count = dbHelper.insert(tableName, null, data);
            if (count > -1)
                this.id = (int) count;
            Log.d(LOG_TAG, "Inserted record id: " + count);
        }
        else
        {
            Log.d(LOG_TAG, "Insert record ERROR: record with telephone_number exist:" + telephone_number +"\n call update");
            this.updatePersonalData();
        }
        cur.close();
        
    }

    public void generateAuth(String messageText) {
        String data = telephone_number + ":" + messageText;
        try {
            this.password = BaseEncoding.base64().encode(data.getBytes("UTF-8"));
            updatePersonalData();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
