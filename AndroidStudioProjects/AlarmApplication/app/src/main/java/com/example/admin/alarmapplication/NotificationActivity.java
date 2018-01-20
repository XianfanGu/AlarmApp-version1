package com.example.admin.alarmapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends AppCompatActivity {
    public static final String PACKAGE_NAME = "com.example.admin.alarmapplication";
    public static final String RESULT_LATITUDE = PACKAGE_NAME +
            ".RESULT_LATITUDE";
    public static final String RESULT_LONGTITUDE = PACKAGE_NAME +
            ".RESULT_LONGTITUDE";
    private TextView textView;
    private TextView LatextView;
    private Double longitude;
    private Double latitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        final Bundle resultData = getIntent().getBundleExtra("info");
        if(resultData!=null) {
            longitude = resultData.getDouble(RESULT_LONGTITUDE);
            latitude = resultData.getDouble(RESULT_LATITUDE);
            Log.i("1234", longitude.toString());
            textView = (TextView) findViewById(R.id.textView3);
            LatextView = (TextView) findViewById(R.id.textView4);
            if (longitude != null && textView != null) {
                textView.setText("longitude: "+longitude.toString());
                LatextView.setText("latitude: "+latitude.toString());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?q=" + latitude + "," + longitude));
                startActivity(intent);
            }
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(NotificationActivity.this,MainActivity.class);
        startActivity(intent);
    }

}
