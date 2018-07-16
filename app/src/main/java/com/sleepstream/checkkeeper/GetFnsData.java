package com.sleepstream.checkkeeper;

import android.support.multidex.MultiDexApplication;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sleepstream.checkkeeper.qrmanager.QRManager;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GetFnsData {

    OkHttpClient client;// = new OkHttpClient().retryOnConnectionFailure();
    private String urlGet;
    private String urlRestorePsw;
    private String urlRegisterUser;
    private Map<String, String> headerGet;
    private Map<String, String> headerRestorePsw;
    private Map<String, String> headerRegistorUser;
    private String FP;
    private String FD;
    private String FN;
    public String requestStr;
    public String body;
    public RequestBody bodyRec;
    public String Auth;
    private String android_id;
    public Obj dataFromReceipt;
    private String phoneNumber;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");



    public GetFnsData(String android_id)
    {
        client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        this.android_id=android_id;
    }

    public void setHeaders( QRManager QrObject)
    {
        //constract for get ticket
        headerGet= new HashMap();
        headerGet.put("Authorization","Basic "+MainActivity.user.password+"");
        headerGet.put("Device-Id", android_id);
        headerGet.put("Device-OS", "Adnroid 7.0");
        headerGet.put("Version", "2");
        headerGet.put("ClientVersion","1.4.4.1");
        headerGet.put("Host","proverkacheka.nalog.ru:9999");
        headerGet.put("Connection","Keep-Alive");
        headerGet.put("Accept-Encoding","gzip");
        headerGet.put("User-Agent","okhttp/3.0.1");
        this.urlGet = "https://proverkacheka.nalog.ru:9999/v1/inns/*/kkts/*/fss/"+QrObject.FN+"/tickets/"+QrObject.FD+"?fiscalSign="+QrObject.FP+"&sendToEmail=no";
    }
    public void setHeaders( int key)
    {
        switch(key)
        {
            case 1:
                //constract for get resetPsw
                headerRestorePsw= new HashMap();
                headerRestorePsw.put("Device-Id",new String("fhyAcINu8VA:APA91bF4QLf6NlcGvchdUuxQD0CSEBP4UfuBUz_z4gcA9nPDlY6Zvmxx63PnJioK4En2f1drC-TSpEK3cShmBBjB4aT9t8xTdxxxtijUzutvWbey3bbR0X3eeunoC9weEseFWskAC3OS"));
                headerRestorePsw.put("Device-OS", new String("Adnroid 7.0"));
                headerRestorePsw.put("Version", new String("2"));
                headerRestorePsw.put("ClientVersion",new String("1.4.4.1"));
                headerRestorePsw.put("Host",new String("proverkacheka.nalog.ru:9999"));
                headerRestorePsw.put("Connection",new String("Keep-Alive"));
                headerRestorePsw.put("Accept-Encoding",new String("gzip"));
                headerRestorePsw.put("User-Agent",new String("okhttp/3.0.1"));
                headerRestorePsw.put("Content-Type",new String("application/json; charset=UTF-8"));
                //headerRestorePsw.put("Content-Length",new String("24"));
                this.urlRestorePsw = "https://proverkacheka.nalog.ru:9999/v1/mobile/users/restore";
                break;
            case 2:
                //construct for registartion new user
                headerRegistorUser= new HashMap();
                headerRegistorUser.put("Device-Id",new String("fhyAcINu8VA:APA91bF4QLf6NlcGvchdUuxQD0CSEBP4UfuBUz_z4gcA9nPDlY6Zvmxx63PnJioK4En2f1drC-TSpEK3cShmBBjB4aT9t8xTdxxxtijUzutvWbey3bbR0X3eeunoC9weEseFWskAC3OS"));
                headerRegistorUser.put("Device-OS", new String("Adnroid 7.0"));
                headerRegistorUser.put("Version", new String("2"));
                headerRegistorUser.put("ClientVersion",new String("1.4.4.1"));
                headerRegistorUser.put("Host",new String("proverkacheka.nalog.ru:9999"));
                headerRegistorUser.put("Connection",new String("Keep-Alive"));
                headerRegistorUser.put("Accept-Encoding",new String("gzip"));
                headerRegistorUser.put("User-Agent",new String("okhttp/3.0.1"));
                headerRegistorUser.put("Content-Type",new String("application/json; charset=UTF-8"));
                //headerRestorePsw.put("Content-Length",new String("24"));
                this.urlRegisterUser = "https://proverkacheka.nalog.ru:9999/v1/mobile/users/signup";
                break;
        }



    }
    public void runGet(Callback callback) {

        Headers geaderBuild=Headers.of(headerGet);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(urlGet)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        client.newCall(request).enqueue(callback);
    }
    public Response  runGet() throws IOException {

        Headers geaderBuild=Headers.of(headerGet);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(urlGet)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        return client.newCall(request).execute();
    }
    public void resetPassword(Callback callback) {

        setHeaders(1);
        JsonObject json = new JsonObject();
        json.addProperty("phone", MainActivity.user.telephone_number);
        String jsonStr = json.toString();

         bodyRec = RequestBody.create(JSON, jsonStr);


        Headers geaderBuild=Headers.of(headerRestorePsw);
        okhttp3.Request request = new Request.Builder()
                .url(urlRestorePsw)
                .post(bodyRec)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        client.newCall(request).enqueue(callback);
    }

    public Response resetPassword() throws IOException {

        setHeaders(1);
        JsonObject json = new JsonObject();
        json.addProperty("phone", MainActivity.user.telephone_number);
        String jsonStr = json.toString();

        bodyRec = RequestBody.create(JSON, jsonStr);


        Headers geaderBuild=Headers.of(headerRestorePsw);
        okhttp3.Request request = new Request.Builder()
                .url(urlRestorePsw)
                .post(bodyRec)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        return client.newCall(request).execute();
    }

    public void registerNewUser(Callback callback) {

        setHeaders(2);
        JsonObject json = new JsonObject();
        json.addProperty("phone", MainActivity.user.telephone_number);
        json.addProperty("email", MainActivity.user.e_mail);
        json.addProperty("name", MainActivity.user.name);
        String jsonStr = json.toString();

        bodyRec = RequestBody.create(JSON, jsonStr);


        Headers geaderBuild=Headers.of(headerRegistorUser);
        okhttp3.Request request = new Request.Builder()
                .url(urlRegisterUser)
                .post(bodyRec)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        client.newCall(request).enqueue(callback);
    }

    public Response registerNewUser() throws IOException {

        setHeaders(2);
        JsonObject json = new JsonObject();
        json.addProperty("phone", MainActivity.user.telephone_number);
        json.addProperty("email", MainActivity.user.e_mail);
        json.addProperty("name", MainActivity.user.name);
        String jsonStr = json.toString();

        bodyRec = RequestBody.create(JSON, jsonStr);


        Headers geaderBuild=Headers.of(headerRegistorUser);
        okhttp3.Request request = new Request.Builder()
                .url(urlRegisterUser)
                .post(bodyRec)
                .headers(geaderBuild)
                .build();
        requestStr = request.headers().toString();

        return client.newCall(request).execute();
    }

    public void bodyJsonParse()
    {
        Gson gSon = new Gson();
        dataFromReceipt = gSon.fromJson(this.body, Obj.class);

    }

    public void setPhoneNumber(String phone)
    {
        this.phoneNumber = phone;
    }

    public void generateAuth(String messageText) {
        String data = phoneNumber + ":" + messageText;
        try {
            Auth = BaseEncoding.base64().encode(data.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    class Obj
    {
        public Document document;

    }

    class Document
    {
        public Receipt receipt;
    }

    public class Receipt
    {
        public String fiscalDriveNumber;
        public String userInn;
        public String retailPlaceAddress;
        public String taxationType;
        public String requestNumber;
        public String totalSum;
        public String fiscalDocumentNumber;
        public String cashTotalSum;
        public String kktRegId;
        public String operator;
        public String ecashTotalSum;
        public String user;
        public String nds18;
        public String receiptCode;
        public String dateTime;
        public String shiftNumber;
        public String fiscalSign;
        public ArrayList<Items> items;


    }
    public class Items
    {
        public Float price;
        public Float sum;
        String nds18;
        public Float quantity;
        public String name;
    }



}


