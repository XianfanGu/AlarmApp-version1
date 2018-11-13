package com.example.admin.alarmapplication;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;


public class NameActivity extends AppCompatActivity {

    private String name[];
    private String email[];
    private String phone[];
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String LIST_TABLE_NAME = "Contact_List";
    private ArrayList<ListFragment> list;
    private SQLiteDatabase contact_table;
    private FragmentManager fragmentManager;
    private SQLiteOpenHelper contact_table_Helper;
    private Cursor sqLiteCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        fragmentManager=getFragmentManager();

        contact_table_Helper=new NameListOpen(getApplicationContext());
        contact_table = contact_table_Helper.getReadableDatabase();
        sqLiteCursor= contact_table.rawQuery("select * from "+LIST_TABLE_NAME,null);
        int id[] = new int[sqLiteCursor.getCount()];
        int i = 0;
        list=new ArrayList<ListFragment>();
        if(sqLiteCursor.getCount()>0) {
            FragmentTransaction transaction;
            sqLiteCursor.moveToFirst();
            do {
                transaction=fragmentManager.beginTransaction();
                id[i] = sqLiteCursor.getInt(sqLiteCursor.getColumnIndex(KEY_ID));
                ListFragment listFragment = new ListFragment();
                Bundle bundle=new Bundle();
                bundle.putInt(KEY_ID,id[i]);
                bundle.putString(KEY_NAME,sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_NAME)));
                bundle.putString(KEY_EMAIL,sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_EMAIL)));
                bundle.putString(KEY_PHONE,sqLiteCursor.getString(sqLiteCursor.getColumnIndex(KEY_PHONE)));
                listFragment.setArguments(bundle);
                list.add(i,listFragment);
                transaction.add(R.id.Main_Linear,list.get(i));
                transaction.commit();
                i++;
            }while(sqLiteCursor.moveToNext());
            sqLiteCursor.close();
        }

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(NameActivity.this,MainActivity.class);
        startActivity(intent);
    }

}