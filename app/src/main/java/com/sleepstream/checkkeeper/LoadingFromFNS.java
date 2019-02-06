package com.sleepstream.checkkeeper;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.*;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.modules.InvoicesPageFragment;
import com.sleepstream.checkkeeper.qrmanager.QRManager;
import okhttp3.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class LoadingFromFNS extends Service {

    private static final int MAX_ITERATIONS = 2;
    final String LOG_TAG = "LoadingFromFNS_Service";
    private GetFnsData getFnsData;
    public String android_id;
    private Invoice invoice;
    private Context context;
    private final Integer PauseIfError = 30000;
    private final Integer PauseService = 10000;
    private final String CHANNEL_ID = "7777";

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder mBuilder;
    private final int notificationId = 1177;
    private final static Integer limitRepeatLoading = 200;

    @Override
    public void onCreate() {
        log.info(LOG_TAG+"\n"+"onCreate");
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        getFnsData = new GetFnsData(android_id);
        context = getApplicationContext();

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Загружен новый чек!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
        notificationManager = NotificationManagerCompat.from(this);



        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info(LOG_TAG+"\n"+"onStartCommand");
        //loadingFromFNS();
        if(user != null && user._status!= null && user._status == 1) {
            AsyncLoadDataInvoice asyncLoadDataInvoice = new AsyncLoadDataInvoice();
            asyncLoadDataInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mFirebase = FirebaseDatabase.getInstance().getReference();

           /* mFirebase.child(tableNameInvoice).getRef().addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(LOG_TAG, "added new invoice "+s);
                   /* Iterable<DataSnapshot> child = dataSnapshot.getChildren();
                    InvoiceData invoiceData = dataSnapshot.getValue(InvoiceData.class);
                    try {
                        MainActivity.invoice.addInvoice(null, invoiceData, null);
                        MainActivity.invoice.writeInvoiceDataFromServer(invoiceData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(LOG_TAG, " invoice changed "+s);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(LOG_TAG, "remooved invoice ");
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

*/


            //AsyncLoadDataFromGoogle asyncLoadDataFromGoogle = new AsyncLoadDataFromGoogle();
            //asyncLoadDataInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        else
        {
            stopService(MainActivity.intentService);
        }
        //asyncLoadDataInvoice.execute();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        log.info(LOG_TAG+"\n"+"onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        log.info(LOG_TAG+"\n"+"onBind");
        return null;
    }



    private void fillData(Response response, InvoiceData finalInvoiceData) throws Exception {
        getFnsData.body = response.body().string();
        //save data from FNS
        finalInvoiceData.jsonData = getFnsData.body;
        invoice.addJsonData(finalInvoiceData);

        //prase data from FNS
        getFnsData.bodyJsonParse();

        //get GPS from address and full address


       /* if(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress == null &&
                (getFnsData.dataFromReceipt.document.receipt.user != null &&
                        (getFnsData.dataFromReceipt.document.receipt.user.toLowerCase().contains("г.") ||
                                getFnsData.dataFromReceipt.document.receipt.user.toLowerCase().contains("д.")))) {
            getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress = getFnsData.dataFromReceipt.document.receipt.user;
        }*/


        if(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress != null)
        {
            if(finalInvoiceData.store_on_map == null) {
                finalInvoiceData.store_on_map = new InvoiceData.Store_on_map();
            }
            if(finalInvoiceData.store_on_map.latitude == null || finalInvoiceData.store_on_map.longitude == null ) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                GeocodingResult[] results = getLocationInfo(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress);
                if(results != null && results.length>0) {
                    finalInvoiceData.store_on_map.latitude = Double.valueOf(gson.toJson(results[0].geometry.location.lat));
                    finalInvoiceData.store_on_map.longitude = Double.valueOf(gson.toJson(results[0].geometry.location.lng));
                    finalInvoiceData.store_on_map.update = true;
                }
            }
        }
        final int count = invoice.fillReceiptData(getFnsData.dataFromReceipt.document.receipt, finalInvoiceData);

        //run activity with map to confirm address
    }

    public GeocodingResult[]  getLocationInfo(String address) {

        String apiKey = getString(R.string.google_maps_key);
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        address = address.trim().replaceAll("[0-9]{6,}", "");

        GeocodingResult[] results = null;
        try {
            results =  GeocodingApi.geocode(context,address).await();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return results;
    }


    public class AsyncLoadDataInvoice extends AsyncTask<Void, InvoiceData, Void> {
        @Override
        protected void onProgressUpdate(InvoiceData... values) {

            if(InvoicesPageFragment.invoiceListAdapter != null) {
                int status = values[0].get_status();
                //MainActivity.invoice.reLoadInvoice();
                InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
                //int position = InvoicesPageFragment.placeChooserAdapter.findPosition(values[0]);
                //InvoicesPageFragment.llm.scrollToPosition(position);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            log.info(LOG_TAG+"\n"+"loadingFromFNS");
            int count = 0;
            boolean loop = true;
            while (loop && user._status == 1)
            {

                if(settings.settings.containsKey("LimitExceed"))
                {
                    long limitDate = Long.valueOf(Objects.requireNonNull(settings.settings.get("LimitExceed")));
                    long time =new Date().getTime();
                    if((time-limitDate)< 86400)
                    {
                        log.info(LOG_TAG+"\n"+" limit time now  " + (time-limitDate));
                        continue;
                    }
                }
                //status 0 - just loaded waiting for loading
                //status 3 - loading in progress
                //status -1 - error loading from FNS not exist
                //status 1 - loaded from fns
                //status 2 - confirmed by user
                //-3 Status Not Found from Server
                // status -5 - just added, check server
                //status 999 - block invoice to plaice on map
                QRManager qrItem;
                invoice = new Invoice(null);
                invoice.setfilter("_status", count == 0 ? new String[]{"0", "-1", "-2", "-4", "3", "-3", "999", "5","-6"} : new String[]{"0", "-1", "-2", "-4","-3", "5", "-6"});
                invoice.reLoadInvoice();
                log.info(LOG_TAG+"\n"+"reloaded from DB " + invoice.invoices.size());
                if(invoice.invoices.size()<=0)
                {
                    try
                    {
                        Thread.sleep(PauseService);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }


                for (int i = 0; i < invoice.invoices.size(); i++)
                {

                    final InvoiceData invoiceData = invoice.invoices.get(i);
                    if(invoiceData.repeatCount < limitRepeatLoading)
                    {
                        invoiceData.set_status(3);

                        try
                        {
                            invoice.updateInvoice(invoiceData);
                            publishProgress(invoiceData);
                            qrItem = new QRManager(null);
                            qrItem.FP = invoiceData.FP;
                            qrItem.FD = invoiceData.FD;
                            qrItem.FN = invoiceData.FN;
                            qrItem.date = invoiceData.getDateInvoice(2);
                            qrItem.totalSum = String.valueOf(invoiceData.getFullPrice());
                            invoiceData.repeatCount += 1;
                            boolean onServer = false;
                            if (On_line && user.google_id != null)
                            {
                                if (invoiceData.google_id != null)
                                    onServer = invoice.getInvoiceFromServer(invoiceData, this);
                                else
                                    onServer = invoice.getInvoiceFromServer(invoiceData, null);
                            }

                            if (!onServer)
                            {
                                if (!invoiceData.get_status().equals(5))
                                {
                                    //if excepted - get data
                                    getFnsData.setHeaders(qrItem, 1);
                                }
                                else
                                {
                                    getFnsData.setHeaders(qrItem, 2);
                                }
                                log.info(LOG_TAG + "\n" + "check new invoice " + invoiceData.getId());
                                Response response = null;
                                try
                                {
                                    response = getFnsData.runGet();
                                    if (response.code() == 204)
                                    {
                                        log.info(LOG_TAG + "\n" + "Status OK, update invoice\n" + getFnsData.requestStr + " code " + response.code());
                                        invoiceData.set_status(5);
                                        invoice.updateInvoice(invoiceData);
                                        getFnsData.setHeaders(qrItem, 2);
                                        log.info(LOG_TAG + "\n" + "load checked invoice " + invoiceData.getId());
                                        response = null;
                                        response = getFnsData.runGet();


                                        if (response.message().toLowerCase().equals("unauthorized"))
                                        {
                                            //need to notify user to update password
                                            log.info(LOG_TAG + "\n" + response.message() + " \nerror\n" + getFnsData.requestStr + " code " + response.code());
                                            invoiceData.set_status(-2);
                                            invoice.updateInvoice(invoiceData);
                                            user._status = -1;
                                            user.updatePersonalData();
                                            return null;
                                        }
                                        else if (response.message().toLowerCase().equals("forbidden"))
                                        {
                                            log.info(LOG_TAG + "\n" + response.message() + " \nerror\n" + getFnsData.requestStr + " code " + response.code());
                                            invoiceData.set_status(-2);
                                            invoice.updateInvoice(invoiceData);
                                            user._status = 0;
                                            user.updatePersonalData();
                                            return null;
                                        }
                                        else if (response.code() == 200)//(response.message().toLowerCase().equals("ok"))
                                        {
                                            log.info(LOG_TAG + "\n" + "Status OK, update invoice\n" + getFnsData.requestStr + " code " + response.code());
                                            invoiceData.set_status(1);
                                            invoiceData.fromFNS = true;
                                            fillData(response, invoiceData);
                                            // notificationId is a unique int for each notification that you must define
                                            notificationManager.notify(notificationId, mBuilder.build());
                                        }
                                        else if (response.code() == 202)//(response.message().toLowerCase().equals("accepted"))
                                        {
                                            log.info(LOG_TAG + "\n" + "Status Accepted, reload from FNS\n" + getFnsData.requestStr + " code " + response.code());
                                            Response respRepeat = getFnsData.runGet();
                                            if (response.code() == 200)//(respRepeat.message().toLowerCase().equals("ok"))
                                            {
                                                log.info(LOG_TAG + "\n" + "Status OK, update invoice\n" + getFnsData.requestStr + " code " + respRepeat.code());
                                                invoiceData.set_status(1);
                                                invoiceData.fromFNS = true;
                                                fillData(respRepeat, invoiceData);

                                                // notificationId is a unique int for each notification that you must define
                                                notificationManager.notify(notificationId, mBuilder.build());
                                            }
                                            else
                                            {
                                                log.info(LOG_TAG + "\n" + "Status Accepted, reload error\n" + respRepeat.message() + "\n" + "message\n" + getFnsData.requestStr + " code " + respRepeat.code());
                                                invoiceData.set_status(-1);
                                                invoice.updateInvoice(invoiceData);
                                            }
                                        } else if (response.code() == 406)//response.message().toLowerCase().equals("not acceptable"))
                                        {
                                            log.info(LOG_TAG + "\n" + "Status Not Acceptable from Server\n" + getFnsData.requestStr + "\n" + response.message() + "\ncode " + response.code());
                                            invoiceData.set_status(-4);
                                            invoice.updateInvoice(invoiceData);
                                        }
                                        else if (response.message().toLowerCase().equals("not found"))
                                        {
                                            log.info(LOG_TAG + "\n" + "Status Not Found from Server\n" + getFnsData.requestStr + " code " + response.code());
                                            invoiceData.set_status(-3);
                                            invoice.updateInvoice(invoiceData);
                                        } else if (response.code() == 402)
                                        {
                                            log.info(LOG_TAG + "\n" + "Dayly limit exceed " + response.code() + "\n" + response.message() + "\n" + getFnsData.requestStr);
                                            settings.settings.put("LimitExceed", String.valueOf(new Date().getTime()));
                                            settings.setSettings(settings.settings);
                                        } else {
                                            log.info(LOG_TAG + "\n" + "UnKnown answer from Server " + response.code() + "\n" + response.message() + "\n" + getFnsData.requestStr);
                                            invoiceData.set_status(-1);
                                            invoice.updateInvoice(invoiceData);
                                        }
                                    } else {
                                        log.info(LOG_TAG + "\n" + response.message() + " \nerror\n" + getFnsData.requestStr + " message " + response.message() + " code " + response.code());
                                        invoiceData.set_status(-2);
                                        invoice.updateInvoice(invoiceData);
                                    }
                                } catch (SocketTimeoutException ex) {
                                    log.info(LOG_TAG + "\n" + Arrays.toString(ex.getStackTrace()) + "\nerror\n" + getFnsData.requestStr + " code ");
                                    invoiceData.set_status(-1);
                                    invoice.updateInvoice(invoiceData);
                                    Log.d(LOG_TAG, "SocketTimeoutException  line 252 \n");
                                    ex.printStackTrace();
                                } catch (IOException e) {
                                    log.info(LOG_TAG + "\n" + Arrays.toString(e.getStackTrace()) + "\nerror\n" + getFnsData.requestStr + " code ");
                                    invoiceData.set_status(-1);
                                    invoice.updateInvoice(invoiceData);
                                    Log.d(LOG_TAG, "IOException line 259\n");
                                    e.printStackTrace();
                                } finally {
                                    if (response != null)
                                        response.close();
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            log.info(LOG_TAG + "\n" + Arrays.toString(ex.getStackTrace()) + "error\nException");
                            invoiceData.set_status(-1);
                            invoice.updateInvoice(invoiceData);
                            ex.printStackTrace();
                        }
                        MainActivity.invoice.reLoadInvoice();
                        publishProgress(invoiceData);
                        log.info(LOG_TAG + "\n" + "check new invoice Stop " + invoiceData.getId());
                    }

                    try {
                        Thread.sleep(PauseService);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }


                }
                count+=1;
                try {
                    Thread.sleep(PauseIfError);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }

        public void doProgress(InvoiceData value){
            publishProgress(value);
        }
        @Override
        protected void onPostExecute(Void param) {
            /*if (InvoicesPageFragment.placeChooserAdapter!=null) {
                invoice.reLoadInvoice();
                InvoicesPageFragment.placeChooserAdapter.notifyDataSetChanged();
            }*/
            stopService(MainActivity.intentService);
        }
    }

    /*
    private class AsyncLoadDataFromGoogle extends AsyncTask<Void, InvoiceData, Void>{

        Source source = Source.SERVER;
        private Task<QuerySnapshot> result;

        @Override
        protected Void doInBackground(Void... voids) {
            Long last_invoice_update = Long.valueOf(settings.settings.get("last_invoice_update"));
            if(last_invoice_update != null)
            {
                result = mFirestore.collection(tableNameInvoice).whereGreaterThan("date_add", last_invoice_update).whereEqualTo("user_google_id", user.stores_from_fns_google_id).get(source);
            }
            else
            {
                result = mFirestore.collection(tableNameInvoice).whereEqualTo("user_google_id", user.stores_from_fns_google_id).get(source);
            }

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
                for (DocumentSnapshot documentSnapshot : documents)
                {
                    //check in local sql base
                    InvoiceData invoiceData = documentSnapshot.toObject(InvoiceData.class);
                    if(invoiceData != null) {
                        Cursor cur_invoice = dbHelper.query(tableNameInvoice, null, "FP=? AND FD=? AND FN=?", new String[]{invoiceData.FP, invoiceData.FD, invoiceData.FN}, null, null, null, null);
                        if (cur_invoice.moveToFirst()) {
                        } else
                            invoice.addInvoice(null, invoiceData, null);
                    }
                }
            }


            return null;
        }
    }
    */
}