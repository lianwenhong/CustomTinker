package com.lianwenhong.customtinker;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

//        try {
////            HotFix.installResource2(getApplicationContext(), "/sdcard/patch_resource.apk");
//            HotFix.installResource1(getApplication(), "/sdcard/patch_resource.apk");
//        } /*catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } */catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        Utils.doLogic(this);
    }
}