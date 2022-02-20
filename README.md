# CustomTinker
本工程通过Android类加载机制+反射实现dexElements插队的方式实现热修复，Tinker和QZone的真正热修复原理也是如此。

使用过程：

1.将出bug的类修改正确，然后执行打包流程

2.此时取出工程目录下的/build/intermediates/javac/debug/classes/包路径/文件夹下对应的class文件，例如本例中的Utils.class

3.将包路径/整个拷贝到某个目录，然后在命令行下cd到该目录，执行dx --dex --output=patch.dex 包名路径/需要修复的类文件,此时会在当前目录下生成patch.dex文件

4.然后将patch.dex文件当成补丁包放入资源文件夹raw下即可。


例如本例中全部指令为：dx --dex --output=patch.dex com/lianwenhong/customtinker/Utils.class -> 生成patch.dex文件

这里其实也可以写成：dx --dex --output=patch.jar com/lianwenhong/customtinker/Utils.class -> 生成patch.jar文件

ClassLoader可以加载.dex文件，或者.zip、.jar、.apk中包含的.dex文件
