package com.lianwenhong.customtinker;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    public ImageView img;
    public TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.id_iv_hello);
        img.setImageResource(R.mipmap.universe);
        tv = findViewById(R.id.id_tv_hello);
        tv.setText(R.string.hello_world);

    }
}
