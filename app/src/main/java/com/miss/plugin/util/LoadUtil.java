package com.miss.plugin.util;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class LoadUtil {

    private static final String dexPath = "/data/data/com.miss.plugin/cache/myplugin-debug.apk";

    public static void loadUtil(Context context) {

        /**
         * 宿主dexElements = 宿主dexElements + 插件dexElements
         *
         * 1.获取宿主dexElements
         * 2.获取插件dexElements
         * 3.合并两个dexElements
         * 4.将新的dexElements 赋值到 宿主dexElements
         *
         * 目标：dexElements  -- DexPathList类的对象 -- BaseDexClassLoader的对象，类加载器
         *
         * 获取的是宿主的类加载器  --- 反射 dexElements  宿主
         *
         * 获取的是插件的类加载器  --- 反射 dexElements  插件
         */

        try {
            //  BaseDexClassLoader的对象
            Class<?> dexClazz = Class.forName("dalvik.system.BaseDexClassLoader");
            Field fieldPathList = dexClazz.getDeclaredField("pathList");
            fieldPathList.setAccessible(true);

            Class<?> pathListClazz = Class.forName("dalvik.system.DexPathList");
            Field fieldElements = pathListClazz.getDeclaredField("dexElements");
            fieldElements.setAccessible(true);

            //  宿主的类加载器
            ClassLoader hostClassLoader = context.getClassLoader();
            //  宿主的 pathList
            Object hostPathList = fieldPathList.get(hostClassLoader);
            //  宿主 dexElements
            Object[] hostElements = (Object[]) fieldElements.get(hostPathList);


            //  插件的类加载器
            ClassLoader pluginClassLoader = new DexClassLoader(dexPath,
                    context.getCacheDir().getAbsolutePath(), null, hostClassLoader);
            //  插件的 pathList
            Object pluginPathList = fieldPathList.get(pluginClassLoader);
            //  插件的 dexElements
            Object[] pluginElements = (Object[]) fieldElements.get(pluginPathList);


            // 宿主dexElements = 宿主dexElements + 插件dexElements

            Object[] newElements = (Object[]) Array.newInstance(hostElements.getClass().getComponentType(),
                    pluginElements.length + hostElements.length);

            System.arraycopy(hostElements, 0, newElements, 0, hostElements.length);
            System.arraycopy(pluginElements, 0, newElements, hostElements.length, pluginElements.length);

            fieldElements.set(hostPathList, newElements);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
