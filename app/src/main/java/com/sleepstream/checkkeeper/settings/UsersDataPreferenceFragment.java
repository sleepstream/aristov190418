package com.sleepstream.checkkeeper.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.SettingsActivity;
import com.sleepstream.checkkeeper.userModule.PersonalData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersDataPreferenceFragment extends Fragment
{

    private Context context;
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
    public FloatingActionButton fab_get_update_passw;

    private PersonalData personalData = MainActivity.user;

    public UsersDataPreferenceFragment () {}


    public void  SetArguments (Context context) {this.context = context;}
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_user_data_fragment, null);
        name = view.findViewById(R.id.NameField);
        phone =  view.findViewById(R.id.phone);
        e_mail = view.findViewById(R.id.e_mail);
        fab_get_update_passw = view.findViewById(R.id.fab_get_update_passw);
        //password = view.findViewById(R.id.password);

        name_summary = view.findViewById(R.id.NameField_summary);
        phone_summary =  view.findViewById(R.id.phone_summary);
        e_mail_summary = view.findViewById(R.id.e_mail_summary);
        //password_summary = view.findViewById(R.id.password_summary);

        setData();
        final Pattern PnameSurnmae = Pattern.compile("[A-Za-zА-Яа-я ]+");
        final Pattern PPhone = Pattern.compile("^(\\+[0-9]{11})$");
        final Pattern PE_mail = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,6}$");


        fab_get_update_passw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_get_update_passw.setEnabled(false);
                if(personalData._status == null)
                {
                    new SettingsActivity.RegisterNewAsyncTask().execute(personalData);
                }
                else if(personalData._status ==0)
                {
                    allertDialogShowPassw();
                }
                else {
                    new SettingsActivity.RegisterNewAsyncTask().execute(personalData);
                }
            }
        });





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
                                PersonalData.password = name;
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
    public void allertDialogShowPassw()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(R.string.personal_data_request_AlreadyRegister);
        alertDialog.setPositiveButton(context.getString(R.string.btnReset), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new SettingsActivity.RegisterNewAsyncTask().execute(personalData);
            }
        });
        alertDialog.setNegativeButton(context.getString(R.string.btnManual), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(context.getString(R.string.Settings_PasswField));
                alertDialog.setMessage(R.string.Settings_PasswField_message);
                final EditText input = new EditText(context);
                input.setGravity(Gravity.CENTER);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                alertDialog.setView(input);
                alertDialog.setPositiveButton(context.getString(R.string.btnOk), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String passw = input.getText().toString();
                        Pattern p = Pattern.compile("[0-9]{6,10}");
                        Matcher m = p.matcher(passw);
                        if (m.matches()) {
                            personalData._status = 1;
                            personalData.generateAuth(passw);
                            Toast.makeText(context, context.getString(R.string.personal_data_request_PasswordSaved), Toast.LENGTH_LONG).show();
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
        alertDialog.show();
    }

    private void setData()
    {
        if(personalData.name != null)
        {
            name_summary.setText(personalData.name);
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
        if(personalData.name != null && personalData.telephone_number!= null && personalData.e_mail!= null) {
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