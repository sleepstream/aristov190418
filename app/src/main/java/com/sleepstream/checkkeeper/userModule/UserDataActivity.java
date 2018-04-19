package com.sleepstream.checkkeeper.userModule;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.sleepstream.checkkeeper.GetFnsData;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDataActivity extends AppCompatActivity {
    private String request;
    private EditText name;
    private EditText surname;
    private EditText phone;
    private EditText e_mail;
    private GetFnsData getFnsData;
    private Button okButton;
    private final  String LOG_TAG="UserDataActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        getFnsData = new GetFnsData(android_id);
        setContentView(R.layout.activity_getnewpassword);
        name = (EditText) findViewById(R.id.NameField);
        surname = (EditText) findViewById(R.id.SurnameField);
        phone = (EditText) findViewById(R.id.phone);
        e_mail = (EditText) findViewById(R.id.e_mail);
        okButton = (Button) findViewById(R.id.OKbtn);


        phone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(phone.getCurrentTextColor() == Color.RED)
                {
                    phone.setTextColor(Color.WHITE);

                }
                return false;
            }
        });
        name.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(name.getCurrentTextColor() == Color.RED)
                {
                    name.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
        surname.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(surname.getCurrentTextColor() == Color.RED)
                {
                    surname.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
        e_mail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(e_mail.getCurrentTextColor() == Color.RED)
                {
                    e_mail.setTextColor(Color.WHITE);
                }
                return false;
            }
        });




    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        else
            request = extras.get("requestCode").toString();

        switch(request)
        {
            case "2000":
                //get full information
                //show all fields
                name.setEnabled(true);
                surname.setEnabled(true);
                e_mail.setEnabled(true);
                name.setText(MainActivity.user.name);
                surname.setText(MainActivity.user.surname);
                e_mail.setText(MainActivity.user.e_mail);
                phone.setText(MainActivity.user.telephone_number);
                break;
            case "2001":
                //just show data
                name.setText(MainActivity.user.name);
                surname.setText(MainActivity.user.surname);
                e_mail.setText(MainActivity.user.e_mail);
                phone.setText(MainActivity.user.telephone_number);

                name.setEnabled(false);
                e_mail.setEnabled(false);
                surname.setEnabled(false);
                phone.setEnabled(false);
                break;
            default:
                name.setEnabled(true);
                surname.setEnabled(true);
                e_mail.setEnabled(true);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClick_OKBtn(View view)
    {
        Pattern PnameSurnmae = Pattern.compile("[A-Za-zА-Яа-я ]+");
        Pattern PPhone = Pattern.compile("^(\\+[0-9]{11})$");
        Pattern PE_mail = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,6}$");
        Matcher MName = PnameSurnmae.matcher(name.getText());
        Matcher MSurnmae = PnameSurnmae.matcher(surname.getText());
        Matcher MPhone = PPhone.matcher(phone.getText());
        Matcher ME_mail= PE_mail.matcher(e_mail.getText());

        switch(request)
        {
            case "2000":
                //get full information
                if(!MName.matches())
                {
                    name.setTextColor(Color.RED);
                }
                else
                    name.setTextColor(Color.WHITE);
                if(!MSurnmae.matches())
                {
                    surname.setTextColor(Color.RED);
                }
                else
                    surname.setTextColor(Color.WHITE);
                if(!MPhone.matches())
                {
                    phone.setTextColor(Color.RED);
                }
                else
                    phone.setTextColor(Color.WHITE);
                if(!ME_mail.matches())
                {
                    e_mail.setTextColor(Color.RED);
                }
                else
                    e_mail.setTextColor(Color.WHITE);
                if(ME_mail.matches() && MName.matches() && MPhone.matches() && MSurnmae.matches())
                {
                    okButton.setEnabled(false);
                    MainActivity.user.name = name.getText().toString().trim();
                    MainActivity.user.surname = surname.getText().toString().trim();
                    MainActivity.user.telephone_number = phone.getText().toString().trim();
                    MainActivity.user.e_mail = e_mail.getText().toString().trim();
                    name.setText(MainActivity.user.name);
                    surname.setText(MainActivity.user.surname);


                    //get password from FNS
                    getFnsData.registerNewUser(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            MainActivity.user._status = -1;
                            MainActivity.user.insertPersonalData();
                            Log.d(LOG_TAG, e.getMessage()+"\nerror\n"+getFnsData.requestStr);
                            Intent intent = new Intent();
                            intent.putExtra("error", "connection");
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d(LOG_TAG, response.message()+ "\n" + getFnsData.requestStr);
                            if(response.message().toLowerCase().equals("conflict"))
                            {
                                MainActivity.user._status = 0;
                                MainActivity.user.insertPersonalData();
                                Intent intent = new Intent();
                                intent.putExtra("error", "conflict");
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                            else if(response.message().toLowerCase().equals("bad request"))
                            {
                                MainActivity.user._status = -1;
                                MainActivity.user.insertPersonalData();
                                Intent intent = new Intent();
                                intent.putExtra("error", "bad request");
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                            else {
                                MainActivity.user._status = 1;
                                MainActivity.user.insertPersonalData();
                                if(MainActivity.user.id >0)
                                {
                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }

                        }
                    });

                    //save data to system DB
                    //connect here to FNS - get password
                }
            break;
            case "2001":
                okButton.setEnabled(false);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

            break;

        }
    }
}
