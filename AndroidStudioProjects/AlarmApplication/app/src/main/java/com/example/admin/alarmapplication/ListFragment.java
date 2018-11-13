package com.example.admin.alarmapplication;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private LinearLayout linearLayout;
    private TextView textView;
    private Activity mActivity;
    private String name;
    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity=activity;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int Red,Green,Blue;
        final Bundle bundle=getArguments();
        name = bundle.getString(KEY_NAME);
        Random random=new Random();
        Red=random.nextInt(255);
        Green=random.nextInt(255);
        Blue=random.nextInt(255);

        View view=inflater.inflate(R.layout.fragment_list,container,false);
        linearLayout=(LinearLayout) view.findViewById(R.id.Linear_2);
        linearLayout.setBackgroundColor(Color.argb(100,Red,Green,Blue));
        textView = (TextView)view.findViewById(R.id.textView_fragment);
        textView.setText(name);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,EditActivity.class);
                intent.putExtra("bundle",bundle);
                startActivity(intent);
            }
        });
        return view;
    }

}
