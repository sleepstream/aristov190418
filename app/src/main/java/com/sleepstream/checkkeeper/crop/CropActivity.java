package com.sleepstream.checkkeeper.crop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.sleepstream.checkkeeper.MainActivity;
import com.sleepstream.checkkeeper.R;
import com.sleepstream.checkkeeper.invoiceObjects.InvoiceData;
import com.takusemba.cropme.CropView;
import com.takusemba.cropme.OnCropListener;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends AppCompatActivity {



    private ImageView backButton;
    private ImageView cropButton;
    private RecyclerView recyclerView;
    private RelativeLayout parent;
    private CropView cropView;
    private ProgressBar progressBar;
    public String photoreference;

    private static final int REQUEST_CODE_PERMISSION = 100;
    private String place_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        findViewsByIds();

        loadAlbums();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropView.crop(new OnCropListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        saveBitmapAndStartActivity(bitmap);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAlbums();
            } else {
                Snackbar.make(parent, R.string.connectionError, Snackbar.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void findViewsByIds() {
        backButton = findViewById(R.id.cross);
        cropButton = findViewById(R.id.crop);
        cropView = findViewById(R.id.crop_view);
        parent = findViewById(R.id.container);
        progressBar = findViewById(R.id.progress);
    }

    private void saveBitmapAndStartActivity(final Bitmap bitmap) {
        final String filepath = Environment.getExternalStorageDirectory() + "/PriceKeeper/storeImage/";
        progressBar.setVisibility(View.VISIBLE);
        cropView.setEnabled(false);
        File file = new File(filepath, "IMG_" + place_id + ".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(View.GONE);
        InvoiceData.Store store = new InvoiceData.Store();
        store.photoreference = this.photoreference;
        store.place_id =place_id;
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.store = store;
        MainActivity.invoice.setStoreData(invoiceData);
        finish();
    }

    private void loadAlbums() {
        Intent intent = getIntent();
        photoreference= intent.getStringExtra("photoreference");
        place_id = intent.getStringExtra("place_id");
        String key = intent.getStringExtra("key");
        loadImage(photoreference, key);
    }

    public  void loadImage(String photo_reference, String key)
    {
        if(key == null) {
            String urlGet = "https://maps.googleapis.com/maps/api/place/photo?maxheight=5000&photoreference=" + photo_reference + "&key=" + getString(R.string.google_maps_key);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(urlGet)
                    .build();

            progressBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        response.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                cropView.setBitmap(bitmap);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.connectionError), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                }
            }).start();
        }
        else
        {
            cropView.setBitmap(BitmapFactory.decodeFile(photo_reference));
        }

    }

}
