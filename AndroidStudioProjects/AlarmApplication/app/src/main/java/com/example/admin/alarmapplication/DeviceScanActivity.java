package com.example.admin.alarmapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.util.ArrayList;

/**
 * Activity for scanning and displaying available BLE devices.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DeviceScanActivity extends ListActivity{
    private boolean bound = false;
    private BluetoothLeService bluetoothLeService;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BluetoothLeService mServer;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothDevice mbluetoothDevice;
    private Handler mHandler;
    private static final long REFRESH_PERIOD =10000;
    private static final long SCAN_PERIOD = 1000;
    private SQLiteDatabase device_table;
    private SQLiteOpenHelper device_table_Helper;
    private Cursor sqLiteCursor;
    //set scanning periods to 1 seconds.
    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            bluetoothLeService = binder.getService();
            bound = true;
            BluetoothLeService.LocalBinder mLocalBinder = (BluetoothLeService.LocalBinder)service;
            mServer = mLocalBinder.getService();
            if(mServer!=null) {
                Log.i("test0", "server");
                if (mServer.callDisplay() == 1) {
                    mLeDeviceListAdapter = new LeDeviceListAdapter();
                    setListAdapter(mLeDeviceListAdapter);
                }

                if (mLeDeviceListAdapter != null && mbluetoothDevice != null) {
                    mLeDeviceListAdapter.addDevice(mbluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            mServer = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mServer!=null) {
                    mbluetoothDevice = mServer.getDeivce();
                }
                if (mLeDeviceListAdapter != null && mbluetoothDevice != null) {
                    Log.i("0", "ss");
                    mLeDeviceListAdapter.addDevice(mbluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
                mHandler.postDelayed(this,SCAN_PERIOD);
            }
        };
    runnable.run();

    }
    @Override
    protected void onStart() {
        super.onStart();
        // bind to Service
        Intent intent = new Intent(this, BluetoothLeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(DeviceScanActivity.this,MainActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        private ViewHolder viewHolder;
        private TextView device_address;
        private TextView device_name;
        private CheckBox checkBox;
        private Button connButton;
        private Switch aSwitch;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            if(mLeDevices.size()!=0)
                return mLeDevices.size();
            else
                return 1;
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            final int position = i;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_device_scan, null);
                viewHolder = new ViewHolder();
                checkBox = (CheckBox)view.findViewById(R.id.rembercheckBox);
                connButton = (Button)view.findViewById(R.id.connbutton);
                device_address = (TextView) view.findViewById(R.id.address);
                device_name = (TextView) view.findViewById(R.id.name);
                viewHolder.deviceAddress = device_address;
                viewHolder.deviceName = device_name;
                view.setTag(viewHolder);
                Log.i("layout3", "view3");
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device;
            if(!mLeDevices.isEmpty())
            {device = mLeDevices.get(i);}
            else
            {device = null;}
            Log.i("layout2", "view2");
            if(device!=null) {
                connButton.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.VISIBLE);
                Log.i("display", "display1");
                final String deviceName = device.getName();
                if (deviceName != null && deviceName.length() > 0) {
                    Log.i("layout4", device.getName().toString());
                    viewHolder.deviceName.setText(deviceName);
                }
                else {
                    viewHolder.deviceName.setText(R.string.unknown_device);
                }
                viewHolder.deviceAddress.setText(device.getAddress());
                connButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                        final IBeacon iBeacon = mServer.getIBeacon();
                        if (device == null) return;
                        else
                        {
                            if(checkBox.isChecked()) {
                                mServer.saveDevice(device, iBeacon);
                                //mServer.connDevice(device);
                            }
                            else{
                                //mServer.connDevice(device);
                            }
                        }
                        if (mScanning) {

                            mScanning = false;
                        }
                        //startActivity(intent);
                    }
                });
            }
            else {
                viewHolder.deviceName.setText(R.string.no_result);
                viewHolder.deviceAddress.setText(R.string.nothing);
                connButton.setVisibility(View.INVISIBLE);
                checkBox.setVisibility(View.INVISIBLE);
            }
            return view;
        }
    }

        static class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }

}
