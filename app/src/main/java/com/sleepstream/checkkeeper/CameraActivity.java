package com.sleepstream.checkkeeper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.noob.noobcameraflash.managers.NoobCameraManager;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class CameraActivity extends AppCompatActivity {




    private String text;

    boolean hasCameraPermission = false;
    // QREader
    private SurfaceView mySurfaceView;
    private ImageView flash_button_image;
    private RelativeLayout flash_button;
    private QREader qrEader;
    private static final String cameraPerm = Manifest.permission.CAMERA;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if(MainActivity.settings != null) {
            String themeId = MainActivity.settings.settings.get("theme");
            if (themeId != null && themeId.length() > 0)
                setTheme(Integer.valueOf(themeId));
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        Log.d("Start Activity", "CameraActivity");

        hasCameraPermission = RuntimePermissionUtil.checkPermissonGranted(this, cameraPerm);
        mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);
        flash_button_image = findViewById(R.id.flash_button_image);
        flash_button = findViewById(R.id.flash_button);

        flash_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrEader.turnOnOffTorch();
                if(qrEader.tourchEnable)
                    flash_button_image.setImageResource(R.drawable.baseline_flash_off_orange_48);
                else
                    flash_button_image.setImageResource(R.drawable.baseline_flash_on_orange_48);

            }
        });

        //Rectangle box = new Rectangle(this);
        //addContentView(box, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        if (hasCameraPermission) {
            // Setup QREader
            setupQREader();
            if (!qrEader.isCameraRunning())
                qrEader.start();
        }
        else
        {
            Intent intent = new Intent();
            intent.putExtra("resultQR", "");
            intent.putExtra("resultQR", "");
            setResult(RESULT_CANCELED, intent);
            Log.d("postion", "try to finish Cameractivity");
            finish();
        }



    }

    void setupQREader() {
        // Init QREader
        // ------------
        qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QREader", "Value : " + data);
                text=data;

                Intent intent = new Intent();
                intent.putExtra("resultQR", text);
                setResult(RESULT_OK, intent);
                finish();
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(mySurfaceView.getHeight())
                .width(mySurfaceView.getWidth())
                .build();
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (hasCameraPermission) {
            Log.d("postion", "pause Cameractivity");

            // Cleanup in onPause()
            // --------------------
            qrEader.releaseAndCleanup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasCameraPermission) {
            Log.d("postion", "resume Cameractivity");
            // Init and Start with SurfaceView
            // -------------------------------
            qrEader.initAndStart(mySurfaceView);
        }
    }




}
