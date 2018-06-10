package com.sleepstream.checkkeeper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.sleepstream.checkkeeper.userModule.PersonalData;
import okhttp3.Response;

import java.io.IOException;

import static com.sleepstream.checkkeeper.MainActivity.getThemeColor;
import static com.sleepstream.checkkeeper.MainActivity.user;

public class Greetings extends Activity {

    Integer backPressed = 0;

    TextView link_to_FNS_app;
    TextInputLayout password_fns_layout;
    TextInputLayout e_mail_layout;
    TextInputLayout personalData_layout;
    TextInputLayout phone_number_layout;

    RelativeLayout registration_layout;
    RelativeLayout greetings_layout;

    CheckBox auto_registration;

    EditText password_fns;
    EditText e_mail;
    EditText personalData;
    EditText phone_number;

    Button button_registration;
    Button cancel_registration_btn;
    Button show_registration_btn;

    RelativeLayout progressBar;

    Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.greetings_layout);
        link_to_FNS_app = findViewById(R.id.link_to_FNS_app);
        password_fns_layout = findViewById(R.id.password_fns_layout);
        auto_registration = findViewById(R.id.auto_registration);
        personalData_layout= findViewById(R.id.personalData_layout);
        e_mail_layout = findViewById(R.id.e_mail_layout);
        phone_number = findViewById(R.id.phone_number);
        e_mail  = findViewById(R.id.e_mail);
        personalData = findViewById(R.id.personalData);
        phone_number_layout = findViewById(R.id.phone_number_layout);
        password_fns = findViewById(R.id.password_fns);
        button_registration = findViewById(R.id.button_registration);
        registration_layout = findViewById(R.id.registration_layout);
        greetings_layout = findViewById(R.id.greetings_layout);
        show_registration_btn = findViewById(R.id.show_registration_btn);
        cancel_registration_btn = findViewById(R.id.cancel_registration_btn);
        progressBar = findViewById(R.id.progressBar);

        cancel_registration_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

        show_registration_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registration_layout.setVisibility(View.VISIBLE);
                greetings_layout.setVisibility(View.GONE);
            }
        });

        personalData.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(personalData.getText().length()==0)
                    {
                        personalData_layout.setError(getString(R.string.requested_field_error));
                        personalData_layout.setErrorEnabled(true);
                    }
                }
                else
                {
                    personalData_layout.setError(null);
                    personalData_layout.setErrorEnabled(false);
                }
            }
        });

        e_mail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(e_mail.getText().length()==0)
                    {
                        e_mail_layout.setError(getString(R.string.requested_field_error));
                        e_mail_layout.setErrorEnabled(true);
                    }
                }
                else
                {
                    e_mail_layout.setError(null);
                    e_mail_layout.setErrorEnabled(false);
                }
            }
        });

        phone_number.setSelection(phone_number.getText().length());

        phone_number.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(phone_number.getText().length()!=12)
                    {
                        phone_number_layout.setError(getString(R.string.requested_field_error));
                        phone_number_layout.setErrorEnabled(true);
                    }
                }
                else
                {
                    phone_number_layout.setError(null);
                    phone_number_layout.setErrorEnabled(false);
                }
            }
        });

        password_fns.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(password_fns.getText().length()<6)
                    {
                        password_fns_layout.setError(getString(R.string.requested_field_error));
                        password_fns_layout.setErrorEnabled(true);
                    }
                }
                else
                {
                    password_fns_layout.setError(null);
                    password_fns_layout.setErrorEnabled(false);
                }
            }
        });


        auto_registration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    password_fns_layout.setVisibility(View.GONE);
                    e_mail_layout.setVisibility(View.VISIBLE);
                    personalData_layout.setVisibility(View.VISIBLE);

                }
                else
                {
                    password_fns_layout.setVisibility(View.VISIBLE);
                    e_mail_layout.setVisibility(View.GONE);
                    personalData_layout.setVisibility(View.GONE);
                }
            }
        });

        link_to_FNS_app.setPaintFlags(link_to_FNS_app.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        link_to_FNS_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorOld = link_to_FNS_app.getDrawingCacheBackgroundColor();
                link_to_FNS_app.setBackgroundColor(getThemeColor(context, R.attr.link_color_pressed));
                String url = "market://details?id=ru.fns.billchecker";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                link_to_FNS_app.setBackgroundColor(colorOld);
            }
        });

        button_registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFields();
                if(auto_registration.isChecked())
                {
                    if(!phone_number_layout.isErrorEnabled() && !e_mail_layout.isErrorEnabled() && !personalData_layout.isErrorEnabled())
                    {
                        user.e_mail = e_mail.getText().toString().trim();
                        user.name = personalData.getText().toString().trim();
                        user.telephone_number = phone_number.getText().toString();
                        user._status = 0;
                        user.setPersonalData();
                        new RegisterNewAsyncTask().execute(user);
                    }
                    else
                    {
                        Toast.makeText(context, getString(R.string.error_fill_form), Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    if(!phone_number_layout.isErrorEnabled() && !password_fns_layout.isErrorEnabled())
                    {
                        user.telephone_number = phone_number.getText().toString();
                        user.generateAuth(password_fns.getText().toString());
                        user._status = 1;
                        user.setPersonalData();
                        Toast.makeText(context, getString(R.string.personal_data_request_PasswordSaved), Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(context, getString(R.string.error_fill_form), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }



    private void checkFields()
    {
        if(password_fns.getText().length()<6)
        {
            password_fns_layout.setError(getString(R.string.requested_field_error));
            password_fns_layout.setErrorEnabled(true);
        }
        if(phone_number.getText().length()!=12)
        {
            phone_number_layout.setError(getString(R.string.requested_field_error));
            phone_number_layout.setErrorEnabled(true);
        }
        if(e_mail.getText().length()==0)
        {
            e_mail_layout.setError(getString(R.string.requested_field_error));
            e_mail_layout.setErrorEnabled(true);
        }
        if(personalData.getText().length()==0)
        {
            personalData_layout.setError(getString(R.string.requested_field_error));
            personalData_layout.setErrorEnabled(true);
        }
    }


    private class RegisterNewAsyncTask extends AsyncTask<PersonalData, Void, Integer>
    {
        private PersonalData personalData=null;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer != null) {
                switch (integer) {
                    case -1:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_BadRequest), Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_UserRegistered), Toast.LENGTH_LONG).show();
                        user._status = 1;
                        progressBar.setVisibility(View.GONE);
                        finish();
                        break;
                    case 2:
                        Toast.makeText(context, context.getString(R.string.personal_data_request_PasswordRequested), Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                Toast.makeText(context, context.getString(R.string.connectionError), Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.GONE);

        }
        @Override
        protected Integer doInBackground(PersonalData... params) {
            if (params[0] != null) {
                personalData = params[0];
                if (personalData.name != null  && personalData.telephone_number != null && personalData.e_mail != null) {
                    if (personalData.id != null && personalData._status == 0) {
                        GetFnsData getFnsData = new GetFnsData(Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                        try {
                            Response response = getFnsData.registerNewUser();
                            if (response.message().toLowerCase().equals("conflict")) {
                                personalData._status = 0;

                                try {
                                    response = getFnsData.resetPassword();
                                    if(response.code() == 200 ||response.code() == 204)
                                    {
                                        personalData._status = 1;
                                        return 1;
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
                }

            }
            return null;
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        if(backPressed == 0) {
            Toast.makeText(context, context.getString(R.string.press_back_to_exit), Toast.LENGTH_LONG).show();
            backPressed+=1;
        }
        else
        {
            backPressed= 0;
            finishAffinity();
            System.exit(0);
        }
    }
}
