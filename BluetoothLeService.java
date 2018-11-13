package com.example.admin.alarmapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecificBuilder;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.Eddystone;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

// A service that interacts with the BLE device via the Android BLE API.
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLeService extends Service{
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.example.admin.alarmapplication";
    private final static String TAG = "d4";
    private final IBinder binder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;
    protected ResultReceiver mReceiver;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int authorizationDisplay = 0;
    private boolean mScanning;
    private ScanSettings mScanSettings;
    private ScanFilter mScanFilterTest;
    private ArrayList<ScanFilter> mScanFilter;
    private Handler mHandler;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private IBeacon mIBeacon;
    private static final long SCAN_PERIOD = 1000;
    public static final String BT_RECEIVER = PACKAGE_NAME + ".BT_RECEIVER";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private static final String ID = "id";
    private static final String DEVICE_ID = "uuid";
    private static final String DEVICE_NAME = "name";
    private static final String DEVICE_ADDRESS = "address";
    private static final String LIST_TABLE_NAME = "Device_List";
    private static final String DEVICE_MAJOR = "major";
    private static final String DEVICE_MINOR = "minor";
    private SQLiteDatabase device_table;
    private SQLiteOpenHelper device_table_Helper;
    private SQLiteDatabase contact_table;
    private SQLiteOpenHelper contact_table_Helper;
    private Cursor contactSqLiteCursor;
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String CONTACTS_LIST_TABLE = "Contact_List";
    private static int sendTimes = 0;
    private Cursor sqLiteCursor;
    private MyIntentService myIntentService;
    private boolean bound = false;
    private static scanCallbackInterface scanListner;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            MyIntentService.LocalBinder binder = (MyIntentService.LocalBinder) service;
            myIntentService = binder.getService();
            bound = true;
            Log.i("test1", "BT_GPSserver");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            myIntentService = null;
        }
    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }
        public List<BluetoothGattService> getSupportedGattServices() {
            if (mBluetoothGatt == null) return null;
            return mBluetoothGatt.getServices();
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "get data: "+characteristic.getValue().toString());
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        }
    };
    @Override
    public void onCreate(){
        Log.i("test2", "service3");
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        Intent intentService = new Intent(BluetoothLeService.this, MyIntentService.class);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.

        if (mBluetoothAdapter == null) {
            Log.i("null", "null");
            Toast.makeText(this,R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
        }
        else if(!mBluetoothAdapter.isEnabled()){
                authorizationDisplay = 1;
                mReceiver = intent.getParcelableExtra(BT_RECEIVER);
                mReceiver.send(FAILURE_RESULT,null);
        }
        else{
            Log.i("scan", "scan");
            authorizationDisplay = 1;
            scanLeDevice(true);
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // Unbind from service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void scanLeDevice(final boolean enable) {
        mScanFilterTest = new ScanFilter.Builder().build();
        mScanFilter = new ArrayList<ScanFilter>();
        mScanFilter.add(mScanFilterTest);
        mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(0)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        int apiVersion = android.os.Build.VERSION.SDK_INT;
        if (apiVersion > android.os.Build.VERSION_CODES.KITKAT) {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mScanning = true;
                Log.i("bluetoothLeScanner1", bluetoothLeScanner.toString());
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bluetoothLeScanner.stopScan(mLeScanCallback);
                            }
                        }, 3*SCAN_PERIOD/2);
                        bluetoothLeScanner.startScan(mScanFilter, mScanSettings, mLeScanCallback);
                        mHandler.postDelayed(this, SCAN_PERIOD*2);
                    }
                };
                runnable.run();

            } else {
                mScanning = false;
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }
        else {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mScanning = true;
                Log.i("bluetoothLeScanner1", bluetoothLeScanner.toString());
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothAdapter.stopLeScan(ScanCallback);
                            }
                        }, 3*SCAN_PERIOD / 2);

                        mBluetoothAdapter.startLeScan(ScanCallback);
                        mHandler.postDelayed(this, SCAN_PERIOD*2);
                    }
                };
                runnable.run();

            } else {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(ScanCallback);
            }
        }
    }
    public void saveDevice(BluetoothDevice device,IBeacon iBeacon){
        device_table_Helper = new DeviceListOpen(getApplicationContext());
        device_table = device_table_Helper.getWritableDatabase();
        Log.i("data", "4");
        try {
            device_table.execSQL("INSERT INTO " + LIST_TABLE_NAME + " (" + ID + "," +
                    DEVICE_ADDRESS + "," + DEVICE_NAME + "," + DEVICE_ID + "," + DEVICE_MAJOR + "," + DEVICE_MINOR + ")"
                    + " VALUES( NULL,'" + device.getAddress() + "','" + device.getName() + "','" + iBeacon.getUUID().toString() + "','" + Integer.toString(iBeacon.getMajor()) + "','" + Integer.toString(iBeacon.getMinor()) + "');");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.i("data", iBeacon.getUUID().toString()+":"+device.getName()+":"+device.getAddress());
    }
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
    public static void setOnDisplayRefreshListener(scanCallbackInterface myListener) {
        scanListner = myListener;
    }
    // Device scan callback.
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    byte[] scanRecord = result.getScanRecord().getBytes();
                    super.onScanResult(callbackType, result);
                    int startByte = 2;
                    boolean patternFound = false;
                    // find ibeacon
                    while (startByte <= 5) {
                        if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                                // an
                                // iBeacon
                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                            // correct
                            // data
                            // length
                            patternFound = true;
                            break;
                        }
                        startByte++;
                    }
                    // if found
                    if (patternFound) {
                        // covert to hex
                        byte[] uuidBytes = new byte[16];
                        System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                        String hexString = bytesToHex(uuidBytes);

                        // UUID value of ibeacon
                        String uuid = hexString.substring(0, 8) + "-"
                                + hexString.substring(8, 12) + "-"
                                + hexString.substring(12, 16) + "-"
                                + hexString.substring(16, 20) + "-"
                                + hexString.substring(20, 32);

                        // Major value of ibeacon
                        int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                                + (scanRecord[startByte + 21] & 0xff);

                        // Minor value of ibeacon
                        int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                                + (scanRecord[startByte + 23] & 0xff);

                        String ibeaconName = result.getDevice().getName();
                        String mac = result.getDevice().getAddress();
                        int txPower = (scanRecord[startByte + 24]);
                        Log.i("BLE",bytesToHex(scanRecord));
                        Log.i("BLE", "Name：" + ibeaconName + "\nMac：" + mac
                                + " \nUUID：" + uuid + "\nMajor：" + major + "\nMinor："
                                + minor + "\nTxPower：" + txPower + "\nrssi：" + result.getRssi());

                        Log.i("BLE","distance："+calculateAccuracy(txPower,result.getRssi()));
                    }
                        List<ADStructure> structures =
                                ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                        // For each AD structure contained in the advertising packet.
                        Log.i("scan_callback",result.toString());
                        if(sendTimes<=30&&sendTimes!=0) {
                            sendTimes+=1;
                        }
                        else{
                            sendTimes = 0;
                        }
                        for (ADStructure structure : structures)
                        {
                            if (structure instanceof IBeacon)
                            {
                                // iBeacon packet was found.
                                if(matchData((IBeacon)structure,result.getDevice()))
                                {
                                    //connDevice(result.getDevice());
                                    if(sendTimes==0 && scanListner!=null)
                                    {
                                        Log.i("device5","test");
                                        scanListner.setPos(sendSMS());
                                        //Log.i("device6",sendSMS().toString());
                                        sendTimes+=1;
                                    }

                                }
                                addIBeacon((IBeacon)structure);
                            }

                        }

                        addDevice(result.getDevice());
                }

            };
    private BluetoothAdapter.LeScanCallback ScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    // Parse the payload of the advertising packet.
                    List<ADStructure> structures =
                            ADPayloadParser.getInstance().parse(scanRecord);
                    if(sendTimes<=30&&sendTimes!=0) {
                        sendTimes+=1;
                    }
                    else{
                        sendTimes = 0;
                    }
                    // For each AD structure contained in the advertising packet.
                    for (ADStructure structure : structures)
                    {
                        if (structure instanceof IBeacon)
                        {
                            // iBeacon packet was found.
                            if(matchData((IBeacon)structure,device))
                            {
                                //connDevice(device);
                                if(sendTimes==0)
                                {
                                    scanListner.setPos(sendSMS());
                                    sendTimes+=1;
                                }

                            }
                            addIBeacon((IBeacon)structure);
                        }

                    }
                    addDevice(device);
                }
            };
    private boolean matchData(IBeacon iBeacon, BluetoothDevice device){
        UUID uuid = iBeacon.getUUID();
        int major = iBeacon.getMajor();
        int minor = iBeacon.getMinor();
        Log.i("device2", uuid.toString());
        Log.i("device3", Integer.toString(iBeacon.getMajor()));
        Log.i("device4", iBeacon.toString());
        device_table_Helper = new DeviceListOpen(getApplicationContext());
        device_table = device_table_Helper.getReadableDatabase();
        try {
            sqLiteCursor = device_table.rawQuery("select * from " + LIST_TABLE_NAME +
                    " where " + DEVICE_ID + " = '" + uuid.toString() + "' and " + DEVICE_MAJOR + " = '" + Integer.toString(major) + "' and " + DEVICE_MINOR + " = '" + Integer.toString(minor) + "'", null);

            if (sqLiteCursor.getCount() > 0) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BluetoothLeService.this, R.string.founddevice, Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(BluetoothLeService.this, R.string.notfounddevice, Toast.LENGTH_SHORT).show();
                        Log.i("no_alarm", "No device alarm");
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    public boolean connDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice.getAddress()!=null) {
            // Previously connected device.  Try to reconnect.
            if (mBluetoothDeviceAddress != null && bluetoothDevice.getAddress().equals(mBluetoothDeviceAddress)
                    && mBluetoothGatt != null) {
                Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
            if(bluetoothDevice == null)
            {
                Log.i(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            Log.i(TAG, "connection:"+bluetoothDevice.getAddress());
            mBluetoothGatt = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress()).connectGatt(this, false, mGattCallback);
            mBluetoothGatt.connect();
            Log.i(TAG, "Trying to create a new connection.");
            mBluetoothDeviceAddress = bluetoothDevice.getAddress();
            mConnectionState = STATE_CONNECTING;
            return true;
        }
        else {
            return false;
        }
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }


    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            // Return this instance of MyService so clients can call public methods
            return BluetoothLeService.this;
        }
    }

    public int callDisplay() {
        return authorizationDisplay;
    }
    private void addDevice(BluetoothDevice device){
        bluetoothDevice = device;
    }
    private void addIBeacon(IBeacon iBeacon) { mIBeacon = iBeacon;}
    public IBeacon getIBeacon(){
        return mIBeacon;
    }
    public BluetoothDevice getDeivce() {
        return bluetoothDevice;

    }

    public BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;

    }
    private String[] sendSMS(){
        String pos[] = new String[2];
        String longitude=null;
        String latitude=null;
        String SENT = "SMS_SENT";
        PendingIntent sentPI;
        contact_table_Helper=new NameListOpen(getApplicationContext());
        contact_table = contact_table_Helper.getReadableDatabase();
        //sqLiteCursor= contact_table.rawQuery("select * from "+CONTACTS_LIST_TABLE,null);

        if(myIntentService!=null) {
            longitude= myIntentService.getPos().get("longitude").toString();
            latitude = myIntentService.getPos().get("latitude").toString();
        }
        pos[0] = latitude;
        pos[1] = longitude;
        /**
        if(sqLiteCursor.getCount()>0) {
            sqLiteCursor.moveToFirst();
            do {
                SmsManager smsManager = SmsManager.getDefault();
                if(longitude!=null&&latitude!=null){
                    try {
                        sentPI = PendingIntent.getBroadcast(this,0,new Intent(SENT),0);
                        smsManager.sendTextMessage(sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_PHONE)), null, "I am in emergency situation, please try contacting me" +
                                "\n"+ "My Location: "+"http://maps.google.com/maps?q=" + latitude + "," + longitude, sentPI, null);
                        Log.i(TAG, sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_PHONE)));

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        // Log.i(TAG, "send text");
                        smsManager.sendTextMessage(sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_PHONE)), null, "I am in emergency situation, please try contacting me", null, null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }while(sqLiteCursor.moveToNext());
            sqLiteCursor.close();
        }
         **/
        return pos;
    }

}
class DeviceListOpen extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String ID = "id";
    private static final String DEVICE_ID = "uuid";
    private static final String DEVICE_NAME = "name";
    private static final String DEVICE_ADDRESS = "address";
    private static final String DEVICE_MAJOR = "major";
    private static final String DEVICE_MINOR = "minor";
    private static final String LIST_TABLE_NAME = "Device_List";
    private static final String LIST_TABLE_CREATE =
            "CREATE TABLE "+LIST_TABLE_NAME+"("+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DEVICE_ADDRESS + " text NOT NULL UNIQUE,"  +   DEVICE_NAME + " text ,"+
                    DEVICE_ID+ " text ,"  +DEVICE_MAJOR+ " text ,"  +DEVICE_MINOR + " text);";
    DeviceListOpen(Context context){
        super(context, LIST_TABLE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(LIST_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}