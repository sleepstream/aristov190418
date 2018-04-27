package com.sleepstream.checkkeeper;


import android.app.Activity;
import android.app.Fragment;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.ArrayMap;
import android.view.*;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sleepstream.checkkeeper.modules.InvoicesPageFragment;
import com.sleepstream.checkkeeper.modules.SettingsApp;
import com.sleepstream.checkkeeper.userModule.UserDataActivity;
import com.sleepstream.checkkeeper.userModule.personalData;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private android.app.FragmentTransaction fTrans;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_page);
        context = this;
        fTrans =((Activity) context).getFragmentManager().beginTransaction();
        MainSettingsPage fragment = new MainSettingsPage();
        fTrans.replace(R.id.pager, fragment);
        fTrans.commit();
    }


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
                    fTrans.commit();
                }
            });
            appSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            return view;
        }
    }

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

            View view = inflater.inflate(R.layout.activity_getnewpassword, null);
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
}
