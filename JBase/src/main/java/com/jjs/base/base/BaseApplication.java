package com.jjs.base.base;

/**
 * 说明：分包方案，未测试：
 * 需要init的有：SharedUtil、L、NetworkUtil
 * Created by aa on 2017/6/19.
 */

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.jjs.base.utils.UEHandler;

import java.lang.ref.WeakReference;

public abstract class BaseApplication extends Application {
    public static boolean isDebug = false;//是否是debug模式，开关log打印信息
    public boolean hasCrash = false;//是否需要全局异常捕获
    public static String BaseUrl = ""; //服务器地址
    private static Application sInstance;
    private static WeakReference<Activity> mCurrentActivityWeakRef;

    public static Application get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Utils.init(this);
        LogUtils.getConfig().setLogSwitch(isDebug);
        if (hasCrash) {
            UEHandler.init();
        }
        //监听会被add到list集合中，走生命周期时会被遍历调用
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mCurrentActivityWeakRef = new WeakReference<Activity>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public static Activity getActivity() {
        if (mCurrentActivityWeakRef != null) {
            return mCurrentActivityWeakRef.get();
        }
        return null;
    }
}
