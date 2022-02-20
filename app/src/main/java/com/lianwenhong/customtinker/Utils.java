package com.lianwenhong.customtinker;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    public static void doLogic(Context context) {
        int total = 0;
        // 走到这里会抛出分母为0的异常
        float percent = 5 / total;

//        int total = 1;
//        float percent = 5 / total;
//        Toast.makeText(context, "bug已修复啦", Toast.LENGTH_SHORT).show();

    }
}
