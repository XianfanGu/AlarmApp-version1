package com.example.admin.alarmapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.example.admin.alarmapplication";

    private static final String ACTION_SHOWGPS = "com.example.admin.alarmapplication.action.getGPS";
    private static final String ACTION_ALARM = "com.example.admin.alarmapplication.action.alarm";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String BT_RECEIVER = PACKAGE_NAME + ".BT_RECEIVER";
    private Bundle bundle;
    private final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_GPS = 1;
    private static final int REQUEST_CODE = 1;
    private  ImageButton add;
    private  Button contactlist;
    private Button optionButton;
    private Button bleButton;
    private Button positionButton;
    private Button testButton;
    private Button saveList;
    private Intent bleservice;
    private Intent serviceintent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add = (ImageButton)findViewById(R.id.imageAddButton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ContactsActivity.class);
                startActivity(intent);
            }
        });
        contactlist = (Button)findViewById(R.id.contact_button);
        contactlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,NameActivity.class);
                startActivity(intent);
            }
        });
        saveList = (Button)findViewById(R.id.deviceList);
        saveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SaveListActivity.class);
                startActivity(intent);
            }
        });
        positionButton = (Button)findViewById(R.id.gps);
        positionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this,NotificationActivity.class);
                if(bundle!=null)
                    intent.putExtra("info",bundle);
                startActivity(intent);
            }
        });
        optionButton = (Button)findViewById(R.id.option);
        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,OptionActivity.class);
                startActivity(intent);
            }
        });
        bleButton = (Button)findViewById(R.id.ble);
        bleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });
        testButton = (Button)findViewById(R.id.test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });
        bleservice = new Intent(MainActivity.this,BluetoothLeService.class);
        serviceintent = new Intent(MainActivity.this,MyIntentService.class);
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        }
        bleservice.putExtra(BT_RECEIVER,new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode,resultData);

                if(resultCode == FAILURE_RESULT){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                }
            }});
        serviceintent.setAction(ACTION_SHOWGPS);
        serviceintent.putExtra(RECEIVER, new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode,resultData);

                if(resultCode == SUCCESS_RESULT) {
                        bundle = resultData;
                }
                else if(resultCode == FAILURE_RESULT){
                    if(bundle==null) {
                        Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, REQUEST_ENABLE_GPS);
                    }
                    Log.i("1", "+++++++++++++RESULT_NOT_OK++++++++++++");
                }
            }});
        if(serviceintent!=null)
        {startService(serviceintent);}
        if(bleservice!=null)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            Log.i("test3", "main3");
            startService(bleservice);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            stopService(bleservice);
            return;
        }
        else if(bleservice!=null)
        {
            startService(bleservice);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
