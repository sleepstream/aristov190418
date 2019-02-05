package com.sleepstream.checkkeeper.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.SettingsActivity;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

import static com.sleepstream.checkkeeper.MainActivity.*;

public class MainSettingsPage extends Fragment
{
    private Context context;
    private RelativeLayout userSettings;
    private RelativeLayout appSettings;
    private RelativeLayout clearServerData;

    public MainSettingsPage(){ }
    public void SetArguments(Context context){ this.context = context;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_main_page_fragment, null);
        userSettings = view.findViewById(R.id.userSettings);
        appSettings = view.findViewById(R.id.appSettings);
        clearServerData = view.findViewById(R.id.clearServerData);


        userSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.FragmentTransaction fTrans =getActivity().getFragmentManager().beginTransaction();
                UsersDataPreferenceFragment fragment = new UsersDataPreferenceFragment();
                fragment.SetArguments(context);
                fTrans.replace(R.id.pager, fragment);
                fTrans.addToBackStack(null);
                fTrans.commit();
                fragment.setRetainInstance(true);
            }
        });
        appSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.FragmentTransaction fTrans =getActivity().getFragmentManager().beginTransaction();
                SettingsActivity.AppSettingsPage fragment = new SettingsActivity.AppSettingsPage();
                fragment.SetArguments(context);
                fTrans.replace(R.id.pager, fragment);
                fTrans.addToBackStack(null);
                fTrans.commit();
                fragment.setRetainInstance(true);
            }
        });

        clearServerData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.clearServerDataAlertTitle);
                builder.setMessage(getString(R.string.clearServerDataAlert))
                        .setCancelable(false)
                        .setNegativeButton("Cansel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AsyncDeletFromServerInvoices asyncDeletFromServerInvoices = new AsyncDeletFromServerInvoices();
                                asyncDeletFromServerInvoices.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view;
    }


    public static class AsyncDeletFromServerInvoices extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            invoice.clearPrivatInvoicesOnServer();
            return null;
        }
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            blurPlotter.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            blurPlotter.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
}

