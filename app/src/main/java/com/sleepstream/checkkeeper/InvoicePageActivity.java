package com.sleepstream.checkkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.sleepstream.checkkeeper.accountinglistObject.AccountingList;
import com.sleepstream.checkkeeper.invoiceObjects.Invoice;

public class InvoicePageActivity  extends AppCompatActivity{

    public static Invoice invoice;
    public static AccountingList accountingList;
    private String invoiceId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        this.invoice = MainActivity.invoice;
        this.accountingList = MainActivity.accountingList;
        Intent intent = getIntent();
        intent.getStringExtra("invoiceId");
    }
}
