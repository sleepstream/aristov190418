package com.sleepstream.checkkeeper;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.joanzapata.pdfview.PDFView;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

import java.io.File;

public class PDFActivity extends AppCompatActivity {
    public static FloatingActionButton fab;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        fab= findViewById(R.id.getQr);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = findViewById(R.id.pdfview);
                Bitmap btm = screenShot(v);

                if(btm != null)
                {
                    BarcodeDetector detector =
                            new BarcodeDetector.Builder(getApplicationContext())
                                    .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                    .build();
                    if (!detector.isOperational()) {
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }

                    Frame frame = new Frame.Builder().setBitmap(btm).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    if(barcodes.valueAt(0)!= null) {
                        Barcode thisCode = barcodes.valueAt(0);
                        Intent intent = new Intent();
                        intent.putExtra("qrCode", thisCode.rawValue);
                        setResult(RESULT_OK, intent);
                        finish();
                    }


                }
                else
                {
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            }
        });
        Log.d("Start Activity", "activity_pdf");

        PDFView  pdfView = findViewById(R.id.pdfview);
        Intent intent=getIntent();
        String pdfUrl = intent.getExtras().getString("pdfUrl");
        pdfView.fromFile(new File(pdfUrl))
                .pages(0)
                .defaultPage(1)
                .showMinimap(false)
                .enableSwipe(true)
                .load();
    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
