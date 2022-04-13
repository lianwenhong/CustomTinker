package com.lianwenhong.customtinker;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

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
            Field nativeLibraryPathElementsField = ReflectUtils.findField(pathList, "nativeLibraryPathElements");
            Field nativeLibraryDirectoriesField = ReflectUtils.findField(pathList, "nativeLibraryDirectories");
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

            Log.e("lianwenhong", " >>> nativeLibraryPathElements:" + nativeLibraryPathElementsField.get(pathList));
            Log.e("lianwenhong", " >>> nativeLibraryDirectories: " + nativeLibraryDirectoriesField.get(pathList));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个方法只做了简单的LoadedApk中的mResDir属性替换，在本例中可以实现资源替换，但是考虑到系统的复杂性，最好是将所有的与资源相关的属性都做替换
     * 所以才有installResource2()方法
     *
     * @param application
     * @param resPath
     * @throws ClassNotFoundException
     */
    public static void installResource1(Application application, String resPath) throws ClassNotFoundException {
        if (TextUtils.isEmpty(resPath)) {
            return;
        }

        try {

            // 得到ContextImpl
            Class contextImplClz = Class.forName("android.app.ContextImpl");
            Method getImplMethod = contextImplClz.getDeclaredMethod("getImpl", Context.class);
            getImplMethod.setAccessible(true);
            Object contextImpl = getImplMethod.invoke(application, application);

            // 得到Context下的mPackageInfo
            Field packageInfoField = contextImplClz.getDeclaredField("mPackageInfo");
            packageInfoField.setAccessible(true);
            Object packageInfo = packageInfoField.get(contextImpl);

            // 得到mPackageInfo下的mResDir属性
            Class loadedApkClz = Class.forName("android.app.LoadedApk");
            Field resDirField = loadedApkClz.getDeclaredField("mResDir");
            resDirField.setAccessible(true);

            //替换掉LoadedApk中的mResDir属性
            resDirField.set(packageInfo, resPath);

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

    private static Object objActivityThread;

    private static Field mPackagesField;
    private static Field mResourcePackagesField;

    private static Field mResDirField;

    private static AssetManager newAssetManager;
    private static Method addAssetPathMethod;
    private static Method ensureStringBlocksMethod;

    private static Collection<WeakReference<Resources>> references;

    private static Field mResourcesImplField;
    private static Field mAssetField;

    public static void init(Context context) {

        try {
            Class activityThreadClz = Class.forName("android.app.ActivityThread");

            try {
                mPackagesField = activityThreadClz.getDeclaredField("mPackages");
                mPackagesField.setAccessible(true);
                if (SDK_INT < 27) {
                    mResourcePackagesField = activityThreadClz.getDeclaredField("mResourcePackages");
                    mResourcePackagesField.setAccessible(true);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            Method currentActivityThreadMethod = activityThreadClz.getDeclaredMethod("currentActivityThread");
            objActivityThread = currentActivityThreadMethod.invoke(null);

            Class loadedApkClz = Class.forName("android.app.LoadedApk");
            Field mApplicationField = loadedApkClz.getDeclaredField("mApplication");
            mApplicationField.setAccessible(true);
            mResDirField = loadedApkClz.getDeclaredField("mResDir");
            mResDirField.setAccessible(true);

            AssetManager assetManager = context.getAssets();
            if (assetManager.getClass().getName().equals("android.content.res.BaiduAssetManager")) {
                newAssetManager = (AssetManager) AssetManager.class.getConstructor().newInstance();
            } else {
                newAssetManager = AssetManager.class.getConstructor().newInstance();
            }

            addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            try {
                ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
                ensureStringBlocksMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // SDK 28开始没有ensureStringBlocks这个方法了
            }

            Class resourcesManagerClz = Class.forName("android.app.ResourcesManager");
            Method getInstanceMethod = resourcesManagerClz.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            Object resourceManager = getInstanceMethod.invoke(null);

            try {
                Field mResourceReferencesField = resourcesManagerClz.getDeclaredField("mResourceReferences");
                mResourceReferencesField.setAccessible(true);
                references = (Collection<WeakReference<Resources>>) mResourceReferencesField.get(resourceManager);
            } catch (NoSuchFieldException e) {
                Log.e("lianwenhong", " e:" + e.getMessage());
                Field mActiveResourcesField = resourcesManagerClz.getDeclaredField("mActiveResources");
                mActiveResourcesField.setAccessible(true);
                ArrayMap<?, WeakReference<Resources>> mActiveResources = (ArrayMap<?, WeakReference<Resources>>) mActiveResourcesField.get(resourceManager);
                references = mActiveResources.values();
            }

            if (references == null || references.size() == 0) {
                throw new IllegalAccessException("resource references is null or empty");
            }

            if (SDK_INT >= 24) {
                // 24开始，Resource的具体实现机制都抽象到ResourceImpl中了
                Class resourcesImplClz = Class.forName("android.content.res.ResourcesImpl");
                mResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                mResourcesImplField.setAccessible(true);
                mAssetField = resourcesImplClz.getDeclaredField("mAssets");
            } else {
                // <24版本还没有ResourceImpl这个类
                mAssetField = Resources.class.getDeclaredField("mAssets");
            }
            mAssetField.setAccessible(true);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * 最终的效果是通过新增一个AssetManager并将该AssetManager的资源路径指向布丁路径来达到资源替换的目的。
     * 主要涉及修改4个属性：
     * ActivityThread -> ArrayMap<String, WeakReference<LoadedApk>> mPackages
     * ActivityThread -> ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages
     * <p>
     * ResourcesManager -> ArrayList<WeakReference<Resources>> mResourceReferences
     * LoadedApk -> mResDir
     *
     * @param context
     * @param patchResPath
     * @throws IllegalAccessException
     */
    public static void installResource2(Context context, String patchResPath) throws IllegalAccessException {
        init(context);
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        final Field[] packagesFields;
        if (SDK_INT < 27) {
            // 27以下有2个属性，mPackages和mResourcePackages
            packagesFields = new Field[]{mPackagesField, mResourcePackagesField};
        } else {
            packagesFields = new Field[]{mPackagesField};
        }
        // 1.将ActivityThread中的LoadedApk中的mResDir替换
        for (Field field : packagesFields) {
            ArrayMap<String, WeakReference<?>> value = (ArrayMap<String, WeakReference<?>>) field.get(objActivityThread);
            for (Map.Entry<String, WeakReference<?>> entry : value.entrySet()) {
                Object loadedApk = entry.getValue().get();
                if (loadedApk == null)
                    continue;
                String resDir = (String) mResDirField.get(loadedApk);
                if (!TextUtils.isEmpty(resDir) && !TextUtils.isEmpty(applicationInfo.sourceDir)
                        && resDir.equals(applicationInfo.sourceDir)) {
                    // 走到这个条件说明这个LoadedApk对象是本应用程序的LoadedApk对象，因为我们只想修复本应用的资源，所以需要做次判断
                    mResDirField.set(loadedApk, patchResPath);
                }
            }
        }

        // 2.新建一个AssetManager并将布丁资源路径添加到其资源路径中
        try {
            if (0 == (Integer) addAssetPathMethod.invoke(newAssetManager, patchResPath)) {
                throw new IllegalStateException("addAssetPath is faild");
            }
            // 参考ResourceImpl中AssetManager的创建流程，需要执行以下ensureStringBlocks()用于生成加锁创建出一个字符串资源池维护资源索引.
            if (ensureStringBlocksMethod != null)
                ensureStringBlocksMethod.invoke(newAssetManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        for (WeakReference<Resources> item : references) {
            Resources ref = item.get();
            if (ref == null) continue;

            try {
                mAssetField.set(ref, newAssetManager);
            } catch (Throwable ig) {
                // 如果走到这个异常，说明这个field是ResourceImpl中的属性
                Object resourceImpl = mResourcesImplField.get(ref);
                mAssetField.set(resourceImpl, newAssetManager);
            }

            ref.updateConfiguration(ref.getConfiguration(), ref.getDisplayMetrics());

        }
    }

    // API <=27时可以直接使用这种方式来重构AssetManager就能避免一大堆的AssetManager反射，https://www.jb51.net/article/216170.htm#_label0
    public static void installResource3(Context context, String patchResPath) throws InvocationTargetException, IllegalAccessException {
        AssetManager assetManager = context.getAssets();

        Method method = ReflectUtils.findMethod(assetManager, "addAssetPath", String.class);
        if (method != null) {
            if (0 == (Integer) method.invoke(assetManager, patchResPath)) {
                throw new IllegalStateException("addAssetPath is faild");
            }
            Method initMethod = ReflectUtils.findMethod(assetManager, "init", Boolean.class);
            Method destroyMethod = ReflectUtils.findMethod(assetManager, "destroy");

            if (destroyMethod != null) {
                destroyMethod.invoke(assetManager);
                Log.e("lianwenhong", " >>> invoke AssetManager.destroy()");
            }

            if (initMethod != null) {
                initMethod.invoke(assetManager, false);
                Log.e("lianwenhong", " >>> invoke AssetManager.init(false)");
            }
        }
    }
}
