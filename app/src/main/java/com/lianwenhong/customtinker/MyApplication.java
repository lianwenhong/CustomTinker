package com.lianwenhong.customtinker;

import android.app.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 类加载机制的缓存操作：
 * 之所以要在本类中做补丁包的安装是因为怕如果在后面的流程中做安装会造成有些带bug的类如果已经被系统加载的话，后续补丁包安装之后
 * 补丁包中的类得不到执行，因为类加载有缓存机制，系统会将加载过的类做一份内存缓存。
 * <p>
 * 关于生效时机：
 * 因为本方案使用的是类加载过程中往dexElements前追加布丁包的方式实现，所以不支持实时生效，只能下次启动时生效。
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 此处为加载补丁包，其实真实应用场景是从服务端下载补丁文件，因为本工程为示例所以直接将其放在raw资源文件目录下去读取，省去了下载过程
        File patchFile = new File(getCacheDir().getAbsolutePath() + File.separator + "patch.dex");
        File patchResource = new File(getCacheDir().getAbsolutePath() + File.separator + "patch_resource.apk");


        try {
            /**
             * 为了高版本Android访问外部存储需要分区等问题，因为此处仅做示例讲解所以就直接将包拷贝到私有目录中
             * 拷贝到私有目录中还有一个好处是避免在补丁包安装过程中包文件被删除造成安装失败
             */
            // 拷贝补丁包
            copyFile(R.raw.patch, patchFile);
            // 拷贝资源补丁包
            copyFile(R.raw.patch_resource, patchResource);

            // 安装补丁包
            HotFix.installPatch(this, patchFile);
//            HotFix.installResource1(this, patchResource.getAbsolutePath());
            HotFix.installResource2(this, patchResource.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (ClassNotFoundException e) {
            e.printStackTrace();
        } */ catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(int id, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            // 由于本项目是测试功能，所以这里直接将补丁包放在工程内，真实开发环境中应该是放在服务器然后下载下来
            input = getResources().openRawResource(id);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
