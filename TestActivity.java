package com.example.admin.alarmapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textView = (TextView)findViewById( R.id.textView5);
        //listener listen the change of
        BluetoothLeService.setOnDisplayRefreshListener(new scanCallbackInterface() {
            @Override
            public void setPos(String[] pos) {
                textView.setText("emergency longitude: "+pos[0]+" latitude:"+pos[1]);
            }
        });
    }

}
