package com.example.admin.alarmapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends Service {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.example.admin.alarmapplication";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_LATITUDE = PACKAGE_NAME +
            ".RESULT_LATITUDE";
    public static final String RESULT_LONGTITUDE = PACKAGE_NAME +
            ".RESULT_LONGTITUDE";

    private static final String ACTION_SHOWGPS = "com.example.admin.alarmapplication.action.getGPS";
    private static final String ACTION_ALARM = "com.example.admin.alarmapplication.action.alarm";
    private static final int REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final IBinder binder = new LocalBinder();
    private Location myLocation;
    private Double longitude;
    private Double latitude;
    private BluetoothAdapter mBluetoothAdapter;
    protected ResultReceiver mReceiver;
   @Override
   public void onCreate(){
       Log.i("test1", "service1");
       super.onCreate();
   }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        final Intent tempIntent = intent;

        //GPS
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLocation = location;
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude();
                if (tempIntent != null) {
                    final String action = tempIntent.getAction();
                    if (ACTION_SHOWGPS.equals(action)) {
                        mReceiver = tempIntent.getParcelableExtra(RECEIVER);
                        Log.i("gps: ","longitude: "+longitude.toString()+". latitude: "+latitude);
                        if(longitude!=null&&latitude!=null) {
                            // mReceiver = intent.getParcelableExtra(RECEIVER);
                            Bundle bundle = new Bundle();
                            bundle.putDouble(RESULT_LATITUDE, latitude);
                            bundle.putDouble(RESULT_LONGTITUDE, longitude);
                            mReceiver.send(SUCCESS_RESULT, bundle);
                        }
                        else{
                            mReceiver.send(FAILURE_RESULT, null);
                        }
                    }
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(myLocation!=null) {
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude();
            }
            else{
                Toast.makeText(this,R.string.error_GPS,Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,R.string.error_permission_GPS,Toast.LENGTH_LONG).show();
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SHOWGPS.equals(action)) {
                mReceiver = intent.getParcelableExtra(RECEIVER);
                if(longitude!=null&&latitude!=null) {
                   // mReceiver = intent.getParcelableExtra(RECEIVER);
                    Bundle bundle = new Bundle();
                    bundle.putDouble(RESULT_LATITUDE, latitude);
                    bundle.putDouble(RESULT_LONGTITUDE, longitude);
                    mReceiver.send(SUCCESS_RESULT, bundle);
                }
                else if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){
                    mReceiver.send(FAILURE_RESULT, null);
                }
            }
        }
        return Service.START_NOT_STICKY;
    }
    public Map<String,Double> getPos()
    {
        Map<String,Double> position = new HashMap<String, Double>();
        position.put("longitude",longitude);
        position.put("latitude",latitude);
        return position;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }
    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        MyIntentService getService() {
            // Return this instance of MyService so clients can call public methods
            return MyIntentService.this;
        }
    }
}
