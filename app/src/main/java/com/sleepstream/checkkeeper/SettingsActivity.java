package com.sleepstream.checkkeeper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import worker8.com.github.radiogroupplus.RadioGroupPlus;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private android.app.FragmentTransaction fTrans;
    private Context context;



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

        Intent intent = getIntent();
        Bundle extraData = intent.getExtras();
        String settingsPage = "";
        if(extraData != null)
            settingsPage = intent.getExtras().getString("settingsPage");
        switch(settingsPage)
        {
            case "UsersDataPreferenceFragment": {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                UsersDataPreferenceFragment fragment = new UsersDataPreferenceFragment(context);
                fTrans.replace(R.id.pager, fragment);
                fTrans.commit();
                fragment.setRetainInstance(true);
                break;
            }
            case "AppSettingsPage": {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                AppSettingsPage fragment = new AppSettingsPage(context);
                fTrans.replace(R.id.pager, fragment);
                fTrans.commit();
                fragment.setRetainInstance(true);
                break;
            }
            default: {
                fTrans = ((Activity) context).getFragmentManager().beginTransaction();
                MainSettingsPage fragment = new MainSettingsPage(context);
                fTrans.replace(R.id.pager, fragment);
                fTrans.commit();
                fragment.setRetainInstance(true);
                break;
            }
        }



    }


    public static class MainSettingsPage extends Fragment
    {
        private final Context context;
        private RelativeLayout userSettings;
        private RelativeLayout appSettings;

        public MainSettingsPage(Context context){ this.context = context;};

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
                    android.app.FragmentTransaction fTrans =getActivity().getFragmentManager().beginTransaction();
                    UsersDataPreferenceFragment fragment = new UsersDataPreferenceFragment(context);
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
                    AppSettingsPage fragment = new AppSettingsPage(context);
                    fTrans.replace(R.id.pager, fragment);
                    fTrans.addToBackStack(null);
                    fTrans.commit();
                    fragment.setRetainInstance(true);
                }
            });
            return view;
        }
    }

    public static class UsersDataPreferenceFragment extends Fragment
    {

        private final Context context;
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
        private personalData personalData = MainActivity.user;

        public UsersDataPreferenceFragment (Context context) {this.context = context;}
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.activity_settings_main, null);
            name = view.findViewById(R.id.NameField);
            surname = view.findViewById(R.id.SurnameField);
            phone =  view.findViewById(R.id.phone);
            e_mail = view.findViewById(R.id.e_mail);
            //password = view.findViewById(R.id.password);

            name_summary = view.findViewById(R.id.NameField_summary);
            surname_summary = view.findViewById(R.id.SurnameField_summary);
            phone_summary =  view.findViewById(R.id.phone_summary);
            e_mail_summary = view.findViewById(R.id.e_mail_summary);
            //password_summary = view.findViewById(R.id.password_summary);

            setData();
            final Pattern PnameSurnmae = Pattern.compile("[A-Za-zА-Яа-я ]+");
            final Pattern PPhone = Pattern.compile("^(\\+[0-9]{11})$");
            final Pattern PE_mail = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,6}$");






            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
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
                            String name = input.getText().toString().trim();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                Matcher MName = PnameSurnmae.matcher(name);
                                RelativeLayout NameField_status_layout = view.findViewById(R.id.NameField_status_layout);
                                ImageView NameField_status = view.findViewById(R.id.NameField_status);
                                if(MName.matches())
                                {
                                    NameField_status_layout.setVisibility(View.VISIBLE);
                                    NameField_status.setImageResource(R.drawable.ic_done_black_24dp);
                                    personalData.name = name;
                                    onResume();
                                }
                                else
                                {
                                    NameField_status_layout.setVisibility(View.VISIBLE);
                                    NameField_status.setImageResource(R.drawable.ic_error_black_24dp);
                                    name_summary.setText(context.getString(R.string.settings_input_data_incorrect));
                                    name_summary.setTextColor(Color.RED);
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
            surname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
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
                            String name = input.getText().toString().trim();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                Matcher MSurnmae = PnameSurnmae.matcher(name);
                                RelativeLayout SurnameField_status_layout = view.findViewById(R.id.SurnameField_status_layout);
                                ImageView SurnameField_status = view.findViewById(R.id.SurnameField_status);
                                if(MSurnmae.matches())
                                {
                                    SurnameField_status_layout.setVisibility(View.VISIBLE);
                                    SurnameField_status.setImageResource(R.drawable.ic_done_black_24dp);
                                    personalData.surname = name;
                                    onResume();
                                }
                                else
                                {
                                    SurnameField_status_layout.setVisibility(View.VISIBLE);
                                    SurnameField_status.setImageResource(R.drawable.ic_error_black_24dp);
                                    surname_summary.setText(context.getString(R.string.settings_input_data_incorrect));
                                    surname_summary.setTextColor(Color.RED);
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
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
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
                            String name = input.getText().toString().trim();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                if(!name.substring(0, 2).equals("+7") || name.length()<12)
                                    Toast.makeText(context, context.getString(R.string.settings_field_input_error), Toast.LENGTH_LONG).show();
                                else
                                {
                                    Matcher MPhone = PPhone.matcher(name);
                                    RelativeLayout phone_status_layout = view.findViewById(R.id.phone_status_layout);
                                    ImageView phone_status = view.findViewById(R.id.phone_status);
                                    if(MPhone.matches())
                                    {
                                        phone_status_layout.setVisibility(View.VISIBLE);
                                        phone_status.setImageResource(R.drawable.ic_done_black_24dp);
                                        personalData.telephone_number = name;
                                        onResume();
                                    }
                                    else
                                    {
                                        phone_status_layout.setVisibility(View.VISIBLE);
                                        phone_status.setImageResource(R.drawable.ic_error_black_24dp);
                                        phone_summary.setText(context.getString(R.string.settings_input_data_incorrect));
                                        phone_summary.setTextColor(Color.RED);
                                    }

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
                public void onClick(final View view) {
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
                            String name = input.getText().toString().trim();
                            if(name.length()==0)
                                Toast.makeText(context, context.getString(R.string.settings_field_empty_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                Matcher ME_mail= PE_mail.matcher(name);
                                RelativeLayout e_mail_status_layout = view.findViewById(R.id.e_mail_status_layout);
                                ImageView e_mail_status = view.findViewById(R.id.e_mail_status);
                                if(ME_mail.matches()) {
                                    personalData.e_mail = name;
                                    e_mail_status_layout.setVisibility(View.VISIBLE);
                                    e_mail_status.setImageResource(R.drawable.ic_done_black_24dp);
                                    onResume();
                                }
                                else
                                {
                                    e_mail_status_layout.setVisibility(View.VISIBLE);
                                    e_mail_status.setImageResource(R.drawable.ic_error_black_24dp);
                                    e_mail_summary.setText(context.getString(R.string.settings_input_data_incorrect));
                                    e_mail_summary.setTextColor(Color.RED);
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
/*            password.setOnClickListener(new View.OnClickListener() {
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
*/

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
        public void onDestroy() {
            if(personalData.name != null && personalData.surname != null && personalData.telephone_number!= null && personalData.e_mail!= null) {
                if(personalData.id == null)
                {
                    GetFnsData getFnsData = new GetFnsData(Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.ANDROID_ID));
                    //get password from FNS
                    getFnsData.registerNewUser(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            personalData._status = -1;
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.message().toLowerCase().equals("conflict"))
                            {
                                personalData._status = 0;
                            }
                            else if(response.message().toLowerCase().equals("bad request"))
                            {
                                personalData._status = -1;
                            }
                            else {
                                personalData._status = 1;
                            }


                        }
                    });
                }
                personalData.setPersonalData();
            }



            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            setData();
        }

    }

    public static class AppSettingsPage extends Fragment
    {
        private final Context context;
        private RelativeLayout ThemeSettings;
        private RelativeLayout appSettings;
        public AppSettingsPage(Context context){ this.context = context;};
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

            return view;
        }
    }
}
