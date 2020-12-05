package com.miss.plugin.util;

import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtil {

    private static final String TAG = "TAG";

    public static void hook() {



        //  hook 加载 可以加载 Activity ，但不能有资源文件

        /**
         *      TODO : hook 点
         *int result = ActivityManager.getService()
         *                 .startActivity(whoThread, who.getBasePackageName(), intent,
         *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
         *                         token, target != null ? target.mEmbeddedID : null,
         *                         requestCode, 0, null, options);
         *
         *      利用反射拿到对象，再用动态代理修改 startActivity 的逻辑  =====》ActivityManager.getService()
         *
         *      getService()  返回的是 IActivityManagerSingleton.get()  ===》  IActivityManager 是个接口
         *
         *
         *      private static final Singleton<IActivityManager> IActivityManagerSingleton =    ...
         *
         *      利用反射，可以拿到 属性 IActivityManagerSingleton（这是个静态类，是我们需要的）
         *
         *      拿到 IActivityManagerSingleton 再利用反射 获得 IActivityManager ，有了他再用动态代理修改startActivity的实现逻辑
         *
         *
         */

        try {
            Class<?> classActivityManager = Class.forName("android.app.ActivityManager");
            Field fieldIActivityManagerSingleton = classActivityManager.getField("IActivityManagerSingleton");
            fieldIActivityManagerSingleton.setAccessible(true);
            //  这是个静态变量，所以传入 null
            Object singleton = fieldIActivityManagerSingleton.get(null);

            Class<?> classSingleton = Class.forName("android.util.Singleton");
            Field fieldMInstance = classSingleton.getDeclaredField("mInstance");
            fieldMInstance.setAccessible(true);

            //  实现动态代理的类
            Object mInstance = fieldMInstance.get(singleton);

            //  动态代理要用的接口
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            /**
             *
             *          ActivityManager.getService()
             *                 .startActivity(whoThread, who.getBasePackageName(), intent,
             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
             *                         token, target != null ? target.mEmbeddedID : null,
             *                         requestCode, 0, null, options);
             *
             */

            Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iActivityManagerClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                            /***
                             *      在此做逻辑更改
                             *
                             *
                             */

                            //  方法过滤
                            if ("startActivity".equals(method.getName())) {

                                //  获取
                                int index = -1;

                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }

                                Intent intent = (Intent) args[index];

                                Intent intentProxy = new Intent();
                                intentProxy.setClassName("com.miss.plugin", "com.miss.plugin.ProxyActivity");
                                intentProxy.putExtra(TAG, intent);

                                //  狸猫换太子，嘴里叼着太子
                                args[index] = intentProxy;

                            }


                            return method.invoke(mInstance, args);
                        }
                    });


            fieldMInstance.set(singleton, o);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    private static final String TARGET_INTENT = "target_intent";

    public static void hookAMS() {
        try {
            // 获取 singleton 对象
            Field singletonField = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // 小于8.0
                Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
                singletonField = clazz.getDeclaredField("gDefault");
            } else {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                singletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            }

            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);

            // 获取 系统的 IActivityManager 对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            // 创建动态代理对象
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // do something
                            // Intent的修改 -- 过滤
                            /**
                             * IActivityManager类的方法
                             * startActivity(whoThread, who.getBasePackageName(), intent,
                             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                             *                         token, target != null ? target.mEmbeddedID : null,
                             *                         requestCode, 0, null, options)
                             */
                            // 过滤
                            if ("startActivity".equals(method.getName())) {
                                int index = -1;

                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                // 启动插件的
                                Intent intent = (Intent) args[index];

                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.miss.plugin",
                                        "com.miss.plugin.ProxyActivity");

                                proxyIntent.putExtra(TARGET_INTENT, intent);

                                args[index] = proxyIntent;
                            }

                            // args  method需要的参数  --- 不改变原有的执行流程
                            // mInstance 系统的 IActivityManager 对象
                            return method.invoke(mInstance, args);
                        }
                    });

            // ActivityManager.getService() 替换成 proxyInstance
            mInstanceField.set(singleton, proxyInstance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
