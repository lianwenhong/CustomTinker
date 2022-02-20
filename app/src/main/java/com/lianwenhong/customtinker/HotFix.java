package com.lianwenhong.customtinker;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HotFix {
    public static void installPatch(Application application, File patch) {
        if (patch == null || !patch.exists()) {
            return;
        }
        try {
            /**
             * 1.获取当前的类加载器z
             */
            ClassLoader classLoader = application.getClassLoader();

            /**
             * 2.获取到dexElements属性以便后续向前追加patch.dex
             */
            Field pathListField = ReflectUtils.findField(classLoader, "pathList");
            Object pathList = pathListField.get(classLoader);
            Field dexElementsField = ReflectUtils.findField(pathList, "dexElements");
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            /**
             * 3.通过反射调用DexPathList类中的makePathElements()方法将patch.dex最终转换为Element[]数组，
             * DexPathList一系列方法都是用来将补丁包转换为Element[]数组的，如makePathElements，makeDexElements..
             * 具体的API根据真实API的版本不同方法参数等可能会有出入，所以这里在使用过程中实际上应该通过判断去兼容各个版本，
             * 此处因为是示例所以没做兼容
             */
            List<File> files = new ArrayList<>();
            files.add(patch);
            Method method = ReflectUtils.findMethod(pathList, "makePathElements", List.class, File.class, List.class);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            Object[] patchElements = (Object[]) method.invoke(pathList, files, application.getCacheDir(), suppressedExceptions);

            /**
             * 4.合并patchElements+dexElements,将补丁包的.dex文件插入数组最前面，后续在加载类的时候会优先从第一个开始遍历查找类
             */
            Object[] newElements = (Object[]) Array.newInstance(dexElements.getClass().getComponentType(), dexElements.length + patchElements.length);
            System.arraycopy(patchElements, 0, newElements, 0, patchElements.length);
            System.arraycopy(dexElements, 0, newElements, patchElements.length, dexElements.length);

            /**
             * 5.将新数组置换掉BaseDexClassLoader -> pathList -> dexElements属性，至此工作完成
             */
            dexElementsField.set(pathList, newElements);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
