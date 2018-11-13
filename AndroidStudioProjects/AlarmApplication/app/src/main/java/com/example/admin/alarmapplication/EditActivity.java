package com.example.admin.alarmapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends AppCompatActivity{

    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String LIST_TABLE_NAME = "Contact_List";
    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;
    private Button editButton;
    private Button editBackButton;
    private Button editSaveButton;
    private Button editDeleteButton;
    private Button editCancelButton;
    private SQLiteDatabase contact_table;
    private SQLiteOpenHelper contact_table_Helper;
    private Cursor sqLiteCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        final Bundle bundle=getIntent().getExtras().getBundle("bundle");
        editName = (EditText)findViewById(R.id.edit_editText);
        editPhone = (EditText)findViewById(R.id.edit_editText2);
        editEmail = (EditText)findViewById(R.id.edit_editText3);
        editButton = (Button)findViewById(R.id.edit_edit_button);
        editDeleteButton=(Button)findViewById(R.id.edit_delete_button);
        editCancelButton=(Button)findViewById(R.id.edit_cancel_button);
        editSaveButton=(Button)findViewById(R.id.edit_save_button);
        editBackButton=(Button)findViewById(R.id.edit_back_button);
        editName.setText(bundle.getString(KEY_NAME));
        editPhone.setText(bundle.getString(KEY_PHONE));
        editEmail.setText(bundle.getString(KEY_EMAIL));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBackButton.setVisibility(View.INVISIBLE);
                editBackButton.setClickable(false);
                editButton.setVisibility(View.INVISIBLE);
                editButton.setClickable(false);
                editSaveButton.setVisibility(View.VISIBLE);
                editSaveButton.setClickable(true);
                editCancelButton.setVisibility(View.VISIBLE);
                editCancelButton.setClickable(true);
                editDeleteButton.setVisibility(View.VISIBLE);
                editDeleteButton.setClickable(true);
                editName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                editEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                editPhone.setInputType(InputType.TYPE_CLASS_PHONE);
            }
        });
        editCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBackButton.setVisibility(View.VISIBLE);
                editBackButton.setClickable(true);
                editButton.setVisibility(View.VISIBLE);
                editButton.setClickable(true);
                editSaveButton.setVisibility(View.INVISIBLE);
                editSaveButton.setClickable(false);
                editCancelButton.setVisibility(View.INVISIBLE);
                editCancelButton.setClickable(false);
                editDeleteButton.setVisibility(View.INVISIBLE);
                editDeleteButton.setClickable(false);
                editName.setInputType(InputType.TYPE_NULL);
                editEmail.setInputType(InputType.TYPE_NULL);
                editPhone.setInputType(InputType.TYPE_NULL);
            }
        });

        editBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this,NameActivity.class);
                startActivity(intent);
            }
        });
        editDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact_table_Helper = new NameListOpen(getApplicationContext());
                contact_table=contact_table_Helper.getWritableDatabase();
                contact_table.execSQL("DELETE FROM "+LIST_TABLE_NAME+
                        " WHERE "+KEY_ID+" = "+bundle.getInt(KEY_ID));
                Intent intent = new Intent(EditActivity.this,NameActivity.class);
                startActivity(intent);
            }
        });
        editSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact_table_Helper = new NameListOpen(getApplicationContext());
                contact_table=contact_table_Helper.getWritableDatabase();
                contact_table.execSQL("UPDATE "+LIST_TABLE_NAME+" SET "+KEY_NAME+ " = '"+editName.getText()+
                    "', "+KEY_EMAIL+ " = '"+editEmail.getText()+"', "+KEY_PHONE+" = '"+editPhone.getText()+
                    "' WHERE "+KEY_ID+" = "+bundle.getInt(KEY_ID));
                editBackButton.setVisibility(View.VISIBLE);
                editBackButton.setClickable(true);
                editButton.setVisibility(View.VISIBLE);
                editButton.setClickable(true);
                editSaveButton.setVisibility(View.INVISIBLE);
                editSaveButton.setClickable(false);
                editCancelButton.setVisibility(View.INVISIBLE);
                editCancelButton.setClickable(false);
                editDeleteButton.setVisibility(View.INVISIBLE);
                editDeleteButton.setClickable(false);
                editName.setInputType(InputType.TYPE_NULL);
                editEmail.setInputType(InputType.TYPE_NULL);
                editPhone.setInputType(InputType.TYPE_NULL);
            }
        });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(EditActivity.this,NameActivity.class);
        startActivity(intent);
    }

}
