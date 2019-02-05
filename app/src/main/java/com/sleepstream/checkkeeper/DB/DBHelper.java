package com.sleepstream.checkkeeper.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Map;




public class DBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase myDataBase;
    final String LOG_TAG = "DBHelper";
    private static String DB_PATH;// = "/data/data/com.sleepstream.aristov.checkkeeper/databases/";


    private static String DB_NAME = "priceCeeper.db";
    private final Context myContext;
    private final String DATABASE_CREATE_personalData="CREATE TABLE \"PersonalData\" ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `surname` TEXT, `e_mail` TEXT NOT NULL, `telephone_number` TEXT NOT NULL, `password` TEXT )";

    public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, DB_NAME, null, DATABASE_VERSION);
            this.myContext= context;
        DB_PATH= myContext.getDatabasePath(DB_NAME).getAbsolutePath();
        backUpDataBase(false);
    }

    public void createDataBase() {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase(null);

            } catch (IOException e) {

                throw new Error("Error copying database path "+myContext.getDatabasePath(DB_NAME).getAbsolutePath() +"     \n" + e.toString());

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(Exception ex){
            ex.printStackTrace();

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    public void copyDataBase(Uri input) throws IOException{

        //Open your local db as the input stream
        InputStream myInput;
        if(input == null)
            myInput = myContext.getAssets().open(DB_NAME);
        else
            myInput = myContext.getContentResolver().openInputStream(input);

        // Path to the just created empty db
        String outFileName = DB_PATH;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }
    public void backUpDataBase( Boolean key) {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                String  currentDBPath= DB_PATH;
                String backupDBPath;
                if(key)
                    backupDBPath = Environment.getExternalStorageDirectory()+"/PriceKeeper/BackUp/"+new Date().getTime()+"-"+DB_NAME;
                else
                    backupDBPath  = Environment.getExternalStorageDirectory()+"/PriceKeeper/BackUp/"+DB_NAME;

                File currentDB = new File(currentDBPath);
                File backupDB = new File(backupDBPath);
                File pathDir = new File(Environment.getExternalStorageDirectory()+"/PriceKeeper/BackUp/");

                if(!backupDB.exists())
                {
                    pathDir.mkdirs();
                    backupDB.createNewFile();
                }

                if(currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                //Toast.makeText(myContext, myContext.getString(R.string.backUpReady)+backupDB.getName().toString(), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(myContext, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
                db.execSQL(DATABASE_CREATE_personalData);
                // we want both updates, so no break statement here...
        }

    }

    public void insert_Tickets()
    {
        final String Insert_Data="INSERT INTO invoice VALUES(datetime(), 1,1, 1)";
        myDataBase.execSQL(Insert_Data);

    }

    public void insert_personalData(Map<String, String> data)
    {
        String columns="";
        String values="";
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            columns+=entry.getKey()+",";
            values+="'"+entry.getValue()+"',";
        }
        //remoove finish ,
        columns = columns.substring(0, columns.length()-1);
        values=values.substring(0, values.length()-1);
        final String Insert_Data="INSERT INTO PersonalData ("+columns+") VALUES("+values+")";
        myDataBase.execSQL(Insert_Data);

    }

    public Cursor query(String tableName, String[] columns,  String selection,  String[] selectionArgs, String groupBy,String having,String orderBy,String limit)
    {
        if(!myDataBase.isOpen())
            this.openDataBase();
        Cursor cur = myDataBase.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        return cur;
    }

    public Cursor rawQuery(String sql, String[] args)
    {
        Cursor cur = myDataBase.rawQuery(sql, args);
        return cur;
    }

    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs)
    {
        if(!myDataBase.isOpen())
            this.openDataBase();
        int cur = myDataBase.update(tableName, values, whereClause, whereArgs);
        return cur;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        if(!myDataBase.isOpen())
            this.openDataBase();
        long id = myDataBase.insert(table, nullColumnHack, values);
        return id;

    }

    public long delete(String table, String whereClause, String[] whereArgs) {
        if(!myDataBase.isOpen())
            this.openDataBase();
        long id = myDataBase.delete(table,whereClause,  whereArgs);
        return id;

    }


    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}
