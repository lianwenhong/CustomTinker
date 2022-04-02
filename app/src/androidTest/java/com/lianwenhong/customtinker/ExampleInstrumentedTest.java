package com.lianwenhong.customtinker;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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


        // 1.获取ContextImpl
        // 2.的LoadedApk
        // 3.替换LoadedApk.mResDir属性


        Context appContext = InstrumentationRegistry.getTargetContext();

        appContext.getCacheDir().getAbsolutePath();

        try {

            // 得到ContextImpl
            Class contextImplClz = Class.forName("android.app.ContextImpl");
            Method getImplMethod = contextImplClz.getDeclaredMethod("getImpl", Context.class);
            getImplMethod.setAccessible(true);
            Object contextImpl = getImplMethod.invoke(appContext, appContext);

            // 得到Context下的mPackageInfo
            Field packageInfoField = contextImplClz.getDeclaredField("mPackageInfo");
            packageInfoField.setAccessible(true);
            Object packageInfo = packageInfoField.get(contextImpl);

            // 得到mPackageInfo下的mResDir属性
            Class loadedApkClz = Class.forName("android.app.LoadedApk");
            Field resDirField = loadedApkClz.getDeclaredField("mResDir");
            resDirField.setAccessible(true);

            //替换掉LoadedApk中的mResDir属性
            resDirField.set(packageInfo, "");

            Log.e("lianwenhong", " >>> mResDir:" + resDirField.get(packageInfo));

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}