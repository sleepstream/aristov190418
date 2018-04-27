package com.sleepstream.checkkeeper.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import static com.sleepstream.checkkeeper.MainActivity.dbHelper;

public class SettingsApp {

    private final String tableName = "settings";
    public Map<String, String> settings = new LinkedHashMap<>();
    public SettingsApp(){
        reloadSettings();
    }
    private void reloadSettings()
    {
        settings.clear();
        Cursor cur = dbHelper.query(tableName, null, null, null, null, null, null, null);
        if(cur.moveToFirst())
        {
            do{
                settings.put(cur.getString(cur.getColumnIndex("name")), cur.getString(cur.getColumnIndex("value")));
            }while(cur.moveToNext());
        }
    }

    private void updateSettings(Map<String, String> settings)
    {


        for(Map.Entry<String, String> entry: settings.entrySet())
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", entry.getKey());
            contentValues.put("value", entry.getValue());
            dbHelper.update(tableName, contentValues, "name=?", new String[]{entry.getKey()});
        }
    }

    public void setSettings(Map<String, String> settings)
    {
        for(Map.Entry<String, String> entry: settings.entrySet())
        {
            Cursor cur = dbHelper.query(tableName, null, "name = ?", new String[]{entry.getKey()}, null, null, null, null);
            if(cur.moveToFirst())
            {
                Map<String, String> tmp = new ArrayMap<>();
                tmp.put(entry.getKey(), entry.getValue());
                updateSettings(tmp);
            }
            else
            {
                ContentValues contentValues = new ContentValues();
                contentValues.put("name",entry.getKey());
                contentValues.put("value", entry.getValue());
                int id = (int)dbHelper.insert(tableName, null, contentValues);
                Log.d("", id+"");
            }
        }
        reloadSettings();
    }


}
