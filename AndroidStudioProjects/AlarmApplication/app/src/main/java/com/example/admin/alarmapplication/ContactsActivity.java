package com.example.admin.alarmapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ContactsActivity extends AppCompatActivity {
    private Button cancelbutton;
    private Button savebutton;
    private EditText email_input;
    private EditText name_input;
    private EditText phone_input;
    private int VALUE_ID;
    private String VALUE_EMAIL,VALUE_NAME,VALUE_PHONE;
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String LIST_TABLE_NAME = "Contact_List";
    private SQLiteOpenHelper contact_table_Helper;
    private SQLiteDatabase contact_table;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        name_input = (EditText)findViewById(R.id.editText);
        phone_input = (EditText)findViewById(R.id.editText2);
        email_input=(EditText)findViewById(R.id.editText3);
        cancelbutton = (Button)findViewById(R.id.cancel_button);
        savebutton = (Button)findViewById(R.id.save_button);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact_table_Helper = new NameListOpen(getApplicationContext());
                contact_table = contact_table_Helper.getWritableDatabase();
               // contact_table.insert(LIST_TABLE_NAME,KEY_EMAIL, );
                VALUE_EMAIL = email_input.getText().toString();
                VALUE_NAME = name_input.getText().toString();
                VALUE_PHONE = phone_input.getText().toString();
                if(!(VALUE_NAME.toString().equals("")&&VALUE_EMAIL.toString().equals("")&&VALUE_PHONE.toString().equals(""))) {
                    contact_table.execSQL("INSERT INTO " + LIST_TABLE_NAME + " (" + KEY_ID + "," +
                            KEY_EMAIL + "," + KEY_NAME + "," + KEY_PHONE + ")"
                            + " VALUES( NULL,'" + VALUE_EMAIL + "','" + VALUE_NAME + "','" + VALUE_PHONE + "');");
                }
                Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(ContactsActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
class NameListOpen extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String LIST_TABLE_NAME = "Contact_List";
    private static final String LIST_TABLE_CREATE =
                "CREATE TABLE "+LIST_TABLE_NAME+"("+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_EMAIL + " text NOT NULL UNIQUE,"  +   KEY_NAME + " text NOT NULL,"+
                KEY_PHONE + " text NOT NULL UNIQUE);";
    NameListOpen(Context context){
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