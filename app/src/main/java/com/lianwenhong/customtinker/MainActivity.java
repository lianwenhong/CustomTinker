package com.lianwenhong.customtinker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.*;

public class MainActivity extends AppCompatActivity {

    public ImageView img;
    public TextView tv;
//    public Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.id_iv_hello);
        img.setImageResource(R.mipmap.universe);
        tv = findViewById(R.id.id_tv_hello);
        tv.setText(R.string.hello_world);
//        btn = findViewById(R.id.id_btn_change);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                changeSkin();
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });

        Utils.doLogic(this);
    }
}