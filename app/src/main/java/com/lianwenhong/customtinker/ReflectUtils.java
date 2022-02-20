package com.lianwenhong.customtinker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static Field findField(Object instance, String name) {
        Class<?> clz = instance.getClass();
        Field field = null;
        while (clz != Object.class) {
            try {
                field = clz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            //向父类寻找属性
            clz = clz.getSuperclass();
        }
        return field;
    }

    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes) {
        Class<?> clz = instance.getClass();
        Method method = null;
        while (clz != Object.class) {
            try {
                method = clz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            //向父类寻找属性
            clz = clz.getSuperclass();
        }
        return method;
    }

    public static Method[] findAllMethods(Object instance) {
        Class<?> clz = instance.getClass();

        return clz.getDeclaredMethods();
    }
}
