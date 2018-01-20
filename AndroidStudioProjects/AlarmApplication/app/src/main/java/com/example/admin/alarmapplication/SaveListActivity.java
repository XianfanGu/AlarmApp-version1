package com.example.admin.alarmapplication;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//a class to save the packets information of ble device into SQLite database
public class SaveListActivity extends ListActivity {
    private static final String ID = "id";
    private static final String DEVICE_ID = "uuid";
    private static final String DEVICE_NAME = "name";
    private static final String DEVICE_ADDRESS = "address";
    private static final String LIST_TABLE_NAME = "Device_List";
    private static final String DEVICE_MAJOR = "major";
    private static final String DEVICE_MINOR = "minor";
    private SQLiteDatabase savelist_table;
    private SQLiteOpenHelper savelist_table_Helper;
    private Cursor sqLiteCursor;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    @Override
    protected void onResume() {
        super.onResume();
        searchList();
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String,String> devicelist = mLeDeviceListAdapter.getDevice(position);
        deleteItemFromList(devicelist);
        searchList();
    }
    private void deleteItemFromList(Map<String,String> device)
    {
        savelist_table_Helper = new SaveListOpen(getApplicationContext());
        savelist_table=savelist_table_Helper.getWritableDatabase();
        savelist_table.execSQL("DELETE FROM "+LIST_TABLE_NAME+
                " WHERE "+DEVICE_NAME+" = '"+device.get("name")+"' AND "+DEVICE_ADDRESS+" = '"+device.get("address")+"'");
    }
    private void searchList()
    {
        savelist_table_Helper = new SaveListOpen(getApplicationContext());
        savelist_table = savelist_table_Helper.getReadableDatabase();
        try {
            sqLiteCursor = savelist_table.rawQuery("select * from " + LIST_TABLE_NAME, null);

            int id[] = new int[sqLiteCursor.getCount()];
            int i = 0;
            if (sqLiteCursor.getCount() > 0) {
                mLeDeviceListAdapter = new LeDeviceListAdapter();
                setListAdapter(mLeDeviceListAdapter);
                sqLiteCursor.moveToFirst();
                do {
                    Log.i("d5", sqLiteCursor.getString(1));
                    Map<String, String> device = new HashMap<String, String>();
                    device.put("name", sqLiteCursor.getString(sqLiteCursor.getColumnIndex(DEVICE_NAME)));
                    device.put("address", sqLiteCursor.getString(sqLiteCursor.getColumnIndex(DEVICE_ADDRESS)));
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    i++;

                } while (sqLiteCursor.moveToNext());
                sqLiteCursor.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //display the devices in the List Layout
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<Map<String,String>> mLeDevices;
        private LayoutInflater mInflator;
        private ViewHolder viewHolder;
        private TextView device_address;
        private TextView device_name;
        private Switch aSwitch;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<Map<String,String>>();
            mInflator = SaveListActivity.this.getLayoutInflater();
        }

        public void addDevice(Map<String,String> device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public Map<String,String> getDevice(int position) {
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

            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_save_list, null);
                viewHolder = new ViewHolder();

                device_address = (TextView) view.findViewById(R.id.listaddress);
                device_name = (TextView) view.findViewById(R.id.listname);
                viewHolder.deviceAddress = device_address;
                viewHolder.deviceName = device_name;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Map<String,String> device;
            if(!mLeDevices.isEmpty())
            {device = mLeDevices.get(i);}
            else
            {device = null;}
            if(device!=null) {
                final String deviceName = device.get("name");
                if (deviceName != null && deviceName.length() > 0) {
                    Log.i("layout4", device.get("name").toString());
                    viewHolder.deviceName.setText(deviceName);
                }
                else {
                    viewHolder.deviceName.setText(R.string.unknown_device);
                }
                viewHolder.deviceAddress.setText(device.get("address"));
            }
            else {
                viewHolder.deviceName.setText(R.string.no_result);
                viewHolder.deviceAddress.setText(R.string.nothing);
            }
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(SaveListActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
//method to create data table
class SaveListOpen extends SQLiteOpenHelper {
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
    SaveListOpen(Context context) {
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
