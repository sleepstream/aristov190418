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
import static com.sleepstream.checkkeeper.MainActivity.mFirestore;

public class PersonalData {

    public String mPhotoUrl;
    public String google_id;
    public String name= null;
    public String e_mail = null;
    public String telephone_number = null;
    public String password = null;
    public Integer id;
    public Integer _status;
    public boolean signIn= false;

    private Map<String, String> data;
    private String tableName_personalData ="personalData";


    final String LOG_TAG = "PersonalData";
    private Context context;

    public PersonalData()
    {}

    public PersonalData(Context context)
    {
        this.context = context;
        data = new HashMap();
        
        Cursor cur = dbHelper.query(tableName_personalData, null, null, null, null, null, null, "1");
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
                google_id = cur.getString(cur.getColumnIndex("google_id"));
                mPhotoUrl = cur.getString(cur.getColumnIndex("mPhotoUrl"));
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
        if(google_id!= null)
            data.put("google_id", google_id);
        else
            data.putNull("google_id");
        if(mPhotoUrl != null)
            data.put("mPhotoUrl",mPhotoUrl);
        else
            data.putNull("mPhotoUrl");
        int count = dbHelper.update(tableName_personalData, data, "id=?", new String[]{id+""});
        Log.d(LOG_TAG, "Updated records: "+count);
        
    }
    public long deletePersonalData()
    {
        if(this.id != null)
            return dbHelper.delete(tableName_personalData, "id=?", new String[]{id.toString()});
        else
            return -1;
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

        //сохраняем учетку на сервере
        //проверить надо ли сохарянть или нет?
        if(google_id != null)
            mFirestore.collection("users").document(google_id).set(this);
    }

    public void insertPersonalData ()
    {
        ContentValues data = new ContentValues();
        if(name != null)
            data.put("name", name);
        if(telephone_number!= null)
            data.put("telephone_number", telephone_number);
        if(password!= null)
            data.put("password", password);
        if(e_mail != null)
            data.put("e_mail", e_mail);
        if(_status == null)
            data.put("_status", 0);
        else
            data.put("_status", _status);
        if(google_id!= null)
            data.put("google_id", google_id);
        else
            data.putNull("google_id");
        if(mPhotoUrl != null)
            data.put("mPhotoUrl",mPhotoUrl);
        else
            data.putNull("mPhotoUrl");

        if(telephone_number!= null)
        {
            Cursor cur =dbHelper.query(tableName_personalData, null, "telephone_number=?", new String[]{telephone_number}, null, null, null, null);
            if (cur.getCount() == 0) {
                long count = dbHelper.insert(tableName_personalData, null, data);
                if (count > -1)
                    this.id = (int) count;
                Log.d(LOG_TAG, "Inserted record id: " + count);
            } else {
                cur.moveToFirst();
                this.id = cur.getInt(cur.getColumnIndex("id"));
                Log.d(LOG_TAG, "Insert record ERROR: record with telephone_number exist:" + telephone_number + "\n call update");
                this.updatePersonalData();
            }
            cur.close();
        }
        else
        {
            Log.d(LOG_TAG, "Insert record ERROR: telephone_number is null");
        }
        
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
