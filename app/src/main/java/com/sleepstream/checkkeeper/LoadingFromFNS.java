package com.sleepstream.checkkeeper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.modules.InvoicesPageFragment;
import com.sleepstream.checkkeeper.qrmanager.QRManager;
import okhttp3.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import static com.sleepstream.checkkeeper.MainActivity.log;
import static com.sleepstream.checkkeeper.MainActivity.user;

public class LoadingFromFNS extends Service {

    private static final int MAX_ITERATIONS = 2;
    final String LOG_TAG = "LoadingFromFNS_Service";
    private GetFnsData getFnsData;
    public String android_id;
    private Invoice invoice;
    private Context context;
    private final Integer PauseIfError = 30000;
    private final Integer PauseService = 10000;

    @Override
    public void onCreate() {
        log.info(LOG_TAG+"\n"+"onCreate");
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        getFnsData = new GetFnsData(android_id);
        context = getApplicationContext();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info(LOG_TAG+"\n"+"onStartCommand");
        //loadingFromFNS();
        if(user != null && user._status!= null && user._status == 1) {
            AsyncLoadDataInvoice asyncLoadDataInvoice = new AsyncLoadDataInvoice();
            asyncLoadDataInvoice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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



    private void fillData(Response response, InvoiceData finalInvoiceData) throws IOException {
        getFnsData.body = response.body().string();
        //save data from FNS
        invoice.addJsonData(getFnsData.body, finalInvoiceData.getId());

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
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress, 1);
                if(addresses.size() > 0) {
                    if(finalInvoiceData.store == null) {
                        finalInvoiceData.store = new InvoiceData.Store();
                    }
                    if(finalInvoiceData.store.latitude == null || finalInvoiceData.store.longitude == null ) {
                        //finalInvoiceData.store.address = addresses.get(0).getAddressLine(0);
                        finalInvoiceData.store.latitude = addresses.get(0).getLatitude();
                        finalInvoiceData.store.longitude = addresses.get(0).getLongitude();
                    }
                    //if(getFnsData.dataFromReceipt.document.receipt.retailPlaceAddress == getFnsData.dataFromReceipt.document.receipt.user)
                    //    getFnsData.dataFromReceipt.document.receipt.user = null;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //finalInvoiceData.set_status(2);
        //invoice.updateInvoice(finalInvoiceData);
        //check is address in
        final int count = invoice.fillReceiptData(getFnsData.dataFromReceipt.document.receipt, finalInvoiceData);

        //run activity with map to confirm address
    }

    class AsyncLoadDataInvoice extends AsyncTask<Void, InvoiceData, Void> {
        @Override
        protected void onProgressUpdate(InvoiceData... values) {

            if(InvoicesPageFragment.invoiceListAdapter != null) {
                int status = values[0].get_status();
                MainActivity.invoice.reLoadInvoice();
                InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
                //int position = InvoicesPageFragment.invoiceListAdapter.findPosition(values[0]);
                //InvoicesPageFragment.llm.scrollToPosition(position);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            log.info(LOG_TAG+"\n"+"loadingFromFNS");
            int count = 0;
            boolean loop = true;
            while (loop && user._status == 1) {
                //status 0 - just loaded waiting for loading
                //status 3 - loading in progress
                //status -1 - error loading from FNS not exist
                //status 1 - loaded from fns
                //status 2 - confirmed by user
                //-3 Status Not Found from Server
                QRManager qrItem;
                invoice = new Invoice(null);
                invoice.setfilter("_status", count == 0 ? new String[]{"0", "-1", "-2", "-4", "3", "-3"} : new String[]{"0", "-1", "-2", "-4","-3"});
                invoice.reLoadInvoice();
                log.info(LOG_TAG+"\n"+"reloaded from DB " + invoice.invoices.size());
                if(invoice.invoices.size()<=0)
                {
                    try {
                        Thread.sleep(PauseService);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }


                for (int i = 0; i < invoice.invoices.size(); i++) {

                    final InvoiceData invoiceData = invoice.invoices.get(i);
                    invoiceData.set_status(3);

                    try {
                        invoice.updateInvoice(invoiceData);
                        publishProgress(invoiceData);
                        qrItem = new QRManager(null);
                        qrItem.FP = invoiceData.getFP();
                        qrItem.FD = invoiceData.getFD();
                        qrItem.FN = invoiceData.getFN();
                        invoiceData.repeatCount+=1;
                        getFnsData.setHeaders(qrItem);
                        log.info(LOG_TAG+"\n"+"check new invoice " + invoiceData.getId());
                        Response response=null;
                        try {
                            response = getFnsData.runGet();
                            if (response.message().toLowerCase().equals("unauthorized")) {
                                //need to notify user to update password
                                log.info(LOG_TAG+"\n"+response.message() + " \nerror\n" + getFnsData.requestStr);
                                invoiceData.set_status(-2);
                                invoice.updateInvoice(invoiceData);
                                user._status = -1;
                                user.updatePersonalData();
                                return null;
                            }
                            else if(response.message().toLowerCase().equals("forbidden"))
                            {
                                log.info(LOG_TAG+"\n"+response.message() + " \nerror\n" + getFnsData.requestStr);
                                invoiceData.set_status(-2);
                                invoice.updateInvoice(invoiceData);
                                user._status = 0;
                                user.updatePersonalData();
                                return null;
                            }
                            else if (response.message().toLowerCase().equals("ok")) {
                                log.info(LOG_TAG+"\n"+"Status OK, update invoice\n"+getFnsData.requestStr);
                                invoiceData.set_status(1);
                                invoiceData.fromFNS = true;
                                fillData(response, invoiceData);
                            } else if (response.message().toLowerCase().equals("accepted")) {
                                log.info(LOG_TAG+"\n"+"Status Accepted, reload from FNS\n"+getFnsData.requestStr);
                                Response respRepeat = getFnsData.runGet();
                                if (respRepeat.message().toLowerCase().equals("ok")) {
                                    log.info(LOG_TAG+"\n"+"Status OK, update invoice\n"+getFnsData.requestStr);
                                    invoiceData.set_status(1);
                                    invoiceData.fromFNS = true;
                                    fillData(respRepeat, invoiceData);
                                }
                                else
                                {
                                    log.info(LOG_TAG+"\n"+"Status Accepted, reload error\n"+respRepeat.message()+"\n"+ "message\n"+getFnsData.requestStr);
                                    invoiceData.set_status(-1);
                                    invoice.updateInvoice(invoiceData);
                                }
                            }
                            else if(response.message().toLowerCase().equals("not acceptable"))
                            {
                                log.info(LOG_TAG+"\n"+"Status Not Acceptable from Server\n"+getFnsData.requestStr);
                                invoiceData.set_status(-4);
                                invoice.updateInvoice(invoiceData);
                            }
                            else if (response.message().toLowerCase().equals("not found"))
                            {
                                log.info(LOG_TAG+"\n"+"Status Not Found from Server\n"+getFnsData.requestStr);
                                invoiceData.set_status(-3);
                                invoice.updateInvoice(invoiceData);
                            }
                            else {
                                log.info(LOG_TAG+"\n"+"UnKnown answer from Server " + response.message()+"\n"+getFnsData.requestStr);
                                invoiceData.set_status(-1);
                                invoice.updateInvoice(invoiceData);
                            }
                        }
                        catch(SocketTimeoutException ex)
                        {
                            log.info(LOG_TAG+"\n"+ Arrays.toString(ex.getStackTrace()) + "\nerror\n" + getFnsData.requestStr);
                            invoiceData.set_status(-1);
                            invoice.updateInvoice(invoiceData);
                            Log.d(LOG_TAG, "SocketTimeoutException  line 252 \n");
                            ex.printStackTrace();
                        }
                        catch (IOException e) {
                            log.info(LOG_TAG+"\n"+ Arrays.toString(e.getStackTrace()) + "\nerror\n" + getFnsData.requestStr);
                            invoiceData.set_status(-1);
                            invoice.updateInvoice(invoiceData);
                            Log.d(LOG_TAG, "IOException line 259\n");
                            e.printStackTrace();
                        }
                        finally {
                            if(response!= null)
                                response.close();
                        }
                    }
                    catch (Exception ex)
                    {
                        log.info(LOG_TAG+"\n"+Arrays.toString(ex.getStackTrace()) + "error\nException" );
                        invoiceData.set_status(-1);
                        invoice.updateInvoice(invoiceData);
                        ex.printStackTrace();
                    }

                    publishProgress(invoiceData);
                    log.info(LOG_TAG+"\n"+"check new invoice Stop " + invoiceData.getId());


                }
                //count+=1;
                try {
                    Thread.sleep(PauseIfError);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
            /*if (InvoicesPageFragment.invoiceListAdapter!=null) {
                invoice.reLoadInvoice();
                InvoicesPageFragment.invoiceListAdapter.notifyDataSetChanged();
            }*/
            stopService(MainActivity.intentService);
        }
    }
}