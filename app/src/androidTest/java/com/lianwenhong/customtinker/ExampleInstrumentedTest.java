package com.lianwenhong.customtinker;

import android.os.Environment;
import android.util.Log;

import androidx.test.filters.MediumTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {

        File directory = Environment.getExternalStorageDirectory();
        String path = directory.getAbsolutePath();

        Field pathList = ReflectUtils.findField(getClass().getClassLoader(), "pathList");
        Log.e("abc", " pathList :" + pathList.getName());
    }
}