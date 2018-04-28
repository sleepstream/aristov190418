package com.sleepstream.checkkeeper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.ArrayMap;
import android.view.*;
import android.widget.*;
import com.sleepstream.checkkeeper.modules.SettingsApp;
import com.sleepstream.checkkeeper.userModule.personalData;
import worker8.com.github.radiogroupplus.RadioGroupPlus;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private android.app.FragmentTransaction fTrans;
    private Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.settings != null) {
            String themeId = MainActivity.settings.settings.get("theme");
            if (themeId.length() > 0)
                setTheme(Integer.valueOf(themeId));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_page);
        context = this;
        fTrans =((Activity) context).getFragmentManager().beginTransaction();
        MainSettingsPage fragment = new MainSettingsPage();
        fTrans.replace(R.id.pager, fragment);
        fTrans.commit();
        fragment.setRetainInstance(true);
    }

    @SuppressLint("ValidFragment")
    public class MainSettingsPage extends Fragment
    {
        private RelativeLayout userSettings;
        private RelativeLayout appSettings;
        public MainSettingsPage(){};

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


            userSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fTrans =((Activity) context).getFragmentManager().beginTransaction();
                    UsersDataPreferenceFragment fragment = new UsersDataPreferenceFragment();
                    fTrans.replace(R.id.pager, fragment);
                    fTrans.addToBackStack(null);
                    fTrans.commit();
                    fragment.setRetainInstance(true);
                }
            });
            appSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fTrans =((Activity) context).getFragmentManager().beginTransaction();
                    AppSettingsPage fragment = new AppSettingsPage();
                    fTrans.replace(R.id.pager, fragment);
                    fTrans.addToBackStack(null);
                    fTrans.commit();
                    fragment.setRetainInstance(true);
                }
            });
            return view;
        }
    }
    @SuppressLint("ValidFragment")
    public class UsersDataPreferenceFragment extends Fragment
    {

        private RelativeLayout name;
        private RelativeLayout surname;
        private RelativeLayout phone;
        private RelativeLayout e_mail;
        private RelativeLayout password;

        private TextView name_summary;
        private TextView surname_summary;
        private TextView phone_summary;
        private TextView e_mail_summary;
        private TextView password_summary;
        private personalData personalData;

        public UsersDataPreferenceFragment () {}
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            personalData = new personalData(context);

        }
        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.activity_settings_main, null);
            name = view.findViewById(R.id.NameField);
            surname = view.findViewById(R.id.SurnameField);
            phone =  view.findViewById(R.id.phone);
            e_mail = view.findViewById(R.id.e_mail);
            password = view.findViewById(R.id.password);

            name_summary = view.findViewById(R.id.NameField_summary);
            surname_summary = view.findViewById(R.id.SurnameField_summary);
            phone_summary =  view.findViewById(R.id.phone_summary);
            e_mail_summary = view.findViewById(R.id.e_mail_summary);
            password_summary = view.findViewById(R.id.password_summary);

            setData();



            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.Settings_NameField));
                    alertDialog.setMessage(R.string.Settings_NameField_message);
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    if(personalData.name!= null)
                        input.setText(personalData.name);
                    alertDialog.setView(input);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                personalData.name = name;
                                personalData.updatePersonalData();
                                onResume();
                            }

                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            });
            surname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.SurnameField));
                    alertDialog.setMessage(R.string.Settings_SurnameField_message);
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    if(personalData.surname!= null)
                        input.setText(personalData.surname);
                    alertDialog.setView(input);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                personalData.surname = name;
                                personalData.updatePersonalData();
                                onResume();
                            }

                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            });
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.settings_phone_number));
                    alertDialog.setMessage(R.string.Settings_phone_number_message);
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    if(personalData.telephone_number!= null)
                        input.setText(personalData.telephone_number);
                    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(12) });
                    input.setInputType(InputType.TYPE_CLASS_PHONE);
                    alertDialog.setView(input);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                if(!name.substring(0, 2).equals("+7") || name.length()<12)
                                    Toast.makeText(context, context.getString(R.string.settings_field_input_error), Toast.LENGTH_LONG).show();
                                else
                                {
                                    //save number
                                    personalData.telephone_number = name;
                                    personalData.updatePersonalData();
                                    onResume();
                                }
                            }

                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            });
            e_mail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.E_mailAdress));
                    alertDialog.setMessage(R.string.Settings_E_mailAdress_message);
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    if(personalData.e_mail!=null)
                        input.setText(personalData.e_mail);
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    alertDialog.setView(input);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                personalData.e_mail = name;
                                personalData.updatePersonalData();
                                onResume();
                            }

                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            });
            password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.settings_password_FNS_text));
                    alertDialog.setMessage(R.string.Settings_password_FNS_message);
                    final EditText input = new EditText(context);
                    input.setGravity(Gravity.CENTER);
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    alertDialog.setView(input);
                    alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String name = input.getText().toString();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                personalData.password = name;
                                personalData.updatePersonalData();
                                onResume();
                            }

                        }
                    });
                    alertDialog.setNegativeButton(context.getString(R.string.btnCancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    alertDialog.show();
                }
            });


            return view;
        }

        private void setData()
        {
            if(personalData.name != null)
            {
                name_summary.setText(personalData.name);
            }
            if(personalData.surname!= null)
            {
                surname_summary.setText(personalData.surname);
            }
            if(personalData.telephone_number!= null)
            {
                phone_summary.setText(personalData.telephone_number);
            }
            if(personalData.e_mail!= null)
            {
                e_mail_summary.setText(personalData.e_mail);
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            setData();
        }
    }
    @SuppressLint("ValidFragment")
    public class AppSettingsPage extends Fragment
    {
        private RelativeLayout ThemeSettings;
        private RelativeLayout appSettings;
        public AppSettingsPage(){};
        public String themeId="";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.settings_app_settings_fragment, null);
            ThemeSettings = view.findViewById(R.id.ThemeSettings);
            ThemeSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(context.getString(R.string.Settings_ThemesChoice));
                    alertDialog.setMessage(R.string.Settings_ThemesChoice_message);
                    View viewDialog = inflater.inflate(R.layout.theme_choice_dialog, null);
                    int theme =Integer.valueOf(MainActivity.settings.settings.get("theme"));
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

            return view;
        }
    }
}
