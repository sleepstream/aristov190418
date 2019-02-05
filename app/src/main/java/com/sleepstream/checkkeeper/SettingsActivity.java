package com.sleepstream.checkkeeper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.sleepstream.checkkeeper.modules.SettingsApp;
import com.sleepstream.checkkeeper.settings.MainSettingsPage;
import com.sleepstream.checkkeeper.settings.UsersDataPreferenceFragment;
import com.sleepstream.checkkeeper.userModule.PersonalData;
import okhttp3.Response;
import worker8.com.github.radiogroupplus.RadioGroupPlus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sleepstream.checkkeeper.MainActivity.*;
import static com.sleepstream.checkkeeper.invoiceObjects.Invoice.tableNameInvoice;

public class SettingsActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private android.app.FragmentTransaction fTrans;
    private Context context;

    public UsersDataPreferenceFragment usersDataPreferenceFragment;
    public AppSettingsPage appSettingsPage;
    private MainSettingsPage mainSettingsPage;
    private  GoogleApiClient mGoogleApiClient;
    private final static String LOG_TAG = "SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.settings != null) {
            String themeId = MainActivity.settings.settings.get("theme");
            if (themeId!= null && themeId.length() > 0)
                setTheme(Integer.valueOf(themeId));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_page);
        context = this;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.Firebase_default_web_client_id))
                .build();
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        Intent intent = getIntent();
        Bundle extraData = intent.getExtras();
        String settingsPage = "";
        if(extraData != null)
            settingsPage = intent.getExtras().getString("settingsPage");
        switch(settingsPage)
        {
            case "UsersDataPreferenceFragment": {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                usersDataPreferenceFragment = new UsersDataPreferenceFragment();
                usersDataPreferenceFragment.SetArguments(context);
                fTrans.replace(R.id.pager, usersDataPreferenceFragment);
                fTrans.commit();
                usersDataPreferenceFragment.setRetainInstance(true);
                break;
            }
            case "AppSettingsPage": {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                appSettingsPage = new AppSettingsPage();
                appSettingsPage.SetArguments(context);
                fTrans.replace(R.id.pager, appSettingsPage);
                fTrans.commit();
                appSettingsPage.setRetainInstance(true);
                break;
            }
            default: {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                mainSettingsPage= new MainSettingsPage();
                mainSettingsPage.SetArguments(context);
                fTrans.replace(R.id.pager, mainSettingsPage);
                fTrans.commit();
                mainSettingsPage.setRetainInstance(true);
                break;
            }
        }



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public static class RegisterNewAsyncTask extends AsyncTask<PersonalData, Void, Integer> {
        private PersonalData personalData=null;
        private UsersDataPreferenceFragment usersDataPreferenceFragment;
        private Context context;

        public void setArguments(UsersDataPreferenceFragment usersDataPreferenceFragment, Context context)
        {
            this.usersDataPreferenceFragment = usersDataPreferenceFragment;
            this.context  = context;
        }


        @Override
        protected void onPostExecute(Integer integer) {
            if(usersDataPreferenceFragment!= null && usersDataPreferenceFragment.fab_get_update_passw != null)
                usersDataPreferenceFragment.fab_get_update_passw.setEnabled(true);
            if (integer != null) {
                switch (integer) {
                    case -1:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_BadRequest), Toast.LENGTH_LONG).show();
                        break;
                    case 0:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setMessage(R.string.personal_data_request_AlreadyRegister);
                        alertDialog.setPositiveButton(context.getString(R.string.btnReset), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new RegisterNewAsyncTask().execute(personalData);
                            }
                        });
                        alertDialog.setNegativeButton(context.getString(R.string.btnManual), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(usersDataPreferenceFragment != null)
                                    usersDataPreferenceFragment.allertDialogShowPassw();
                            }
                        });
                        alertDialog.show();
                        break;
                    case 1:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_UserRegistered), Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_PasswordRequested), Toast.LENGTH_LONG).show();
                        break;
                }
            }
            else
            {
                Toast.makeText(context, context.getString(R.string.connectionError), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(PersonalData... params) {
            if (params[0] != null) {
                personalData = params[0];

                if (personalData.name != null  && personalData.telephone_number != null && personalData.e_mail != null) {
                    if (personalData.id == null || personalData._status == -1) {
                        GetFnsData getFnsData = new GetFnsData(Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                        try {
                            Response response = getFnsData.registerNewUser();
                            if (response.message().toLowerCase().equals("conflict")) {
                                personalData._status = 0;
                                return 0;
                            } else if (response.message().toLowerCase().equals("bad request")) {
                                personalData._status = -1;
                                return -1;
                            } else {
                                personalData._status = 1;
                                return 1;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(personalData._status == null)
                                personalData._status = -1;
                            personalData.setPersonalData();
                        }
                    }
                    else if(personalData._status == 0 || personalData._status == 1)
                    {
                        GetFnsData getFnsData = new GetFnsData(Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                        //get password from FNS
                        Response response = null;
                        try {
                            response = getFnsData.resetPassword();
                            if(response.code() == 200 ||response.code() == 204)
                            {
                                personalData._status = 1;
                                return 2;
                            }
                            else
                            {
                                personalData._status=0;
                                return -1;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                        finally {
                            personalData.setPersonalData();
                        }
                    }
                }

            }
            return null;
        }
    }




    public static class AppSettingsPage extends Fragment
    {
        private Context context;
        private RelativeLayout ThemeSettings;
        private RelativeLayout appSettings;

        public AppSettingsPage(){}

        public void SetArguments(Context context){ this.context = context;}

        public String themeId="";
        public Switch switcher_on_line;
        public TextView On_lineSettings_summary;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.settings_app_settings_fragment, null);
            On_lineSettings_summary = view.findViewById(R.id.On_lineSettings_summary);
            ThemeSettings = view.findViewById(R.id.ThemeSettings);
            ThemeSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.Settings_ThemesChoice));
                    alertDialog.setMessage(R.string.Settings_ThemesChoice_message);
                    View viewDialog = inflater.inflate(R.layout.theme_choice_dialog, null);
                    String themeStr= MainActivity.settings.settings.get("theme");
                    int theme = 0;
                    if(themeStr!=null)
                        theme =Integer.valueOf(MainActivity.settings.settings.get("theme"));
                    switch(theme)
                    {
                        case R.style.FirstTheme:
                            ((RadioButton)viewDialog.findViewById(R.id.ThemeFirstButton)).setChecked(true);
                            break;
                        case R.style.SecondTheme:
                            ((RadioButton)viewDialog.findViewById(R.id.ThemeSecondButton)).setChecked(true);
                            break;
                    }
                    RadioGroupPlus mRadioGroupPlus = viewDialog.findViewById(R.id.radio_group_plus);
                    mRadioGroupPlus.setOnCheckedChangeListener(new RadioGroupPlus.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroupPlus radioGroupPlus, @IdRes int i) {

                            switch(i)
                            {
                                case R.id.ThemeFirstButton:
                                    themeId = String.valueOf(R.style.FirstTheme);
                                    break;
                                case R.id.ThemeSecondButton:
                                    themeId = String.valueOf(R.style.SecondTheme);
                                    break;
                            }
                        }
                    });
                    alertDialog.setView(viewDialog);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(themeId != MainActivity.settings.settings.get("theme")) {
                                SettingsApp settingsApp = new SettingsApp();
                                Map<String, String> settings = new ArrayMap<>();
                                settings.put("theme", themeId);
                                settingsApp.setSettings(settings);
                                Toast.makeText(context, context.getString(R.string.theme_updated_need_reload), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            themeId="";
                        }
                    });
                    alertDialog.show();
                }
            });

            switcher_on_line = view.findViewById(R.id.switcher_on_line);
            if(On_line)
                switcher_on_line.setChecked(true);
            else
                switcher_on_line.setChecked(false);

            switcher_on_line.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isChecked = switcher_on_line.isChecked();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.settings_On_lineSettings_text));
                    if(isChecked)
                    {
                        alertDialog.setMessage(R.string.settings_On_lineSettings_On_message);
                    }
                    else
                    {
                        alertDialog.setMessage(R.string.settings_On_lineSettings_Off_message);
                    }
                    alertDialog.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switcher_on_line.setChecked(!isChecked);

                        }
                    });
                    alertDialog.setPositiveButton(R.string.ButtonOK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            On_line = isChecked;
                            Map<String, String> values = new HashMap<>();
                            values.put("on_line", String.valueOf(isChecked));
                            settings.setSettings(values);
                            Log.d(LOG_TAG, "settings updated, on_line set to "+values.get("on_line"));

                            if(isChecked) {
                                On_lineSettings_summary.setText(R.string.settings_On_lineSettings_OFF_summary);
                                Intent intent = new Intent(context, Greetings.class);
                                intent.putExtra("from", LOG_TAG);
                                 startActivityForResult(intent, 1000);


                            }
                            else {
                                On_lineSettings_summary.setText(R.string.settings_On_lineSettings_ON_summary);
                                user.google_id = null;
                                user.mPhotoUrl = null;
                                user.signIn = false;
                                user.setPersonalData();

                                AuthUI.getInstance().signOut((FragmentActivity) getActivity());
                                /*if(mGoogleApiClient.isConnected()) {

                                    Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                                            new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(Status status) {
                                                    Log.d(LOG_TAG, "try to log out from google 2 "+status.getStatusMessage());
                                                }
                                            });
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                            new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(Status status) {
                                                    //mGoogleApiClient.disconnect();
                                                    Log.d(LOG_TAG, "try to log out from google "+status.getStatusMessage());
                                                }
                                            });
                                }*/

                            }




                        }
                    });
                    alertDialog.show();
                }
            });

            return view;
        }
        /**
         * Dispatch incoming result to the correct fragment.
         *
         * @param requestCode
         * @param resultCode
         * @param data
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            //super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 1000)
            {
                if(On_line) {
                    switcher_on_line.setChecked(true);
                    //запуск ассинхронного задания по загрузке данных на сервер
                    //приложение на время выполнения блокируется
                    //AsyncLoadToServerInvoices asyncLoadToServerInvoices = new AsyncLoadToServerInvoices();
                    //asyncLoadToServerInvoices.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                    switcher_on_line.setChecked(false);
            }
        }
    }

    public static class AsyncLoadToServerInvoices extends AsyncTask<String, InvoiceData, InvoiceData>
    {

        @Override
        protected InvoiceData doInBackground(String... strings) {
            Invoice invoisToLoad = new Invoice(null);

            invoisToLoad.reLoadInvoice();
            if(invoisToLoad.invoices.size()>0)
            {
                for(InvoiceData invoiceData : invoisToLoad.invoices)
                {
                    //invoisToLoad.addInvoiceDataServer(invoiceData);
                    //invoisToLoad.writeInvoiceDataToServer(invoiceData);
                }
            }

            Task<QuerySnapshot> result = mFirestore.collection(tableNameInvoice).whereEqualTo("user_google_id", user.google_id).get(Invoice.source);
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
                for(DocumentSnapshot documentSnapshot: documents)
                {
                    InvoiceData invoiceData = documentSnapshot.toObject(InvoiceData.class);
                    if(invoiceData!= null) {
                        invoiceData.google_id = documentSnapshot.getId();
                        invoiceData.set_status(invoiceData.server_status);
                        Cursor cur = dbHelper.query(tableNameInvoice, null, "FP=? and FD=? and FN=?",
                                new String[]{invoiceData.FP, invoiceData.FD, invoiceData.FN}, null, null, null, null);
                        if(cur.moveToFirst())
                        {
                            invoiceData.setId(cur.getInt(cur.getColumnIndex("id")));
                        }
                        else
                        {
                            invoiceData.set_status(4);
                            invoice.addInvoiceDataLocal(null, invoiceData);
                        }



                        //invoice.writeInvoiceDataFromServer(invoiceData);
                    }
                }
            }


            return null;
        }
    }


}
