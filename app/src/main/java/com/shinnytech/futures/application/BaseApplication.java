package com.shinnytech.futures.application;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.ConfirmActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.service.WebSocketService;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.umeng.commonsdk.UMConfigure;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

import static com.shinnytech.futures.constants.CommonConstants.BACKGROUND;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.FOREGROUND;
import static com.shinnytech.futures.constants.CommonConstants.JSON_FILE_URL;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_1;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_2;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_3;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_4;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_5;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_6;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_7;
import static com.shinnytech.futures.constants.CommonConstants.MD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.MD_ONLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_ONLINE;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * Created on 12/21/17.
 * Created by chenli.
 * Description: Tinker框架下的application类的具体实现
 */

public class BaseApplication extends Application implements ServiceConnection {

    private static Context sContext;
    private static WebSocketService sWebSocketService;
    private static List<String> sMDURLs = new ArrayList<>();
    private static int index = 0;
    private boolean mServiceBound = false;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTransaction;
    private BroadcastReceiver mReceiverNetwork;
    private BroadcastReceiver mReceiverScreen;
    private boolean mIsBackground = false;
    private MyHandler mMyHandler = new MyHandler();

    public static int getIndex() {
        return index;
    }

    public static void setIndex(int index) {
        BaseApplication.index = index;
    }

    public static Context getContext() {
        return sContext;
    }

    @NonNull
    public static WebSocketService getWebSocketService() {
        return sWebSocketService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sContext == null) sContext = getApplicationContext();

        //获取版本号
        initAppVersion();

        //初始化行情服务器地址
        initTMDUrl();

        //OkHttp网络框架初始化
        initOkGo();

        //下载合约列表文件
        downloadLatestJsonFile();

        //注册活动生命周期回调
        registerActivityLifecycleCallback();

        //广播注册
        registerBroaderCast();

    }

    /**
     * date: 2018/11/7
     * author: chenli
     * description: 获取app版本号
     */
    private void initAppVersion() {
        try {
            DataManager.getInstance().APP_CODE = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
            DataManager.getInstance().APP_VERSION = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 绑定回调，获取service实例
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            WebSocketService.LocalBinder binder = (WebSocketService.LocalBinder) service;
            sWebSocketService = binder.getService();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        //连接行情服务器
        sWebSocketService.connectMD(sMDURLs.get(index));
        //连接交易服务器
        sWebSocketService.connectTD();
        mServiceBound = true;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 解绑回调
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        sWebSocketService = null;
        mServiceBound = false;
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: Home键监听器
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Beta.unInit();
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            notifyBackground();
        }

    }

    private void initOkGo() {
        //构建OkHttpClient.Builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //配置log
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        //配置超时时间
        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        //配置Cookie
        //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new com.lzy.okgo.cookie.CookieJarImpl(new SPCookieStore(sContext)));

        //配置OkGo
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("Accept", "application/json");    //header不支持中文，不允许有特殊字符
        //-------------------------------------------------------------------------------------//
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers);                      //全局公共头
    }

    /**
     * date: 8/6/18
     * author: chenli
     * description: 初始化服务器地址
     */
    private void initTMDUrl() {
        List<String> MDUrlGroup = new ArrayList<>();
        MDUrlGroup.add(MARKET_URL_2);
        MDUrlGroup.add(MARKET_URL_3);
        MDUrlGroup.add(MARKET_URL_4);
        MDUrlGroup.add(MARKET_URL_5);
        MDUrlGroup.add(MARKET_URL_6);
        MDUrlGroup.add(MARKET_URL_7);
        Collections.shuffle(MDUrlGroup);
        try {
            Class cl = Class.forName("com.shinnytech.futures.constants.LocalCommonConstants");
            String MARKET_URL_8 = (String) cl.getMethod("getMarketUrl8").invoke(null);
            String TRANSACTION_URL_L = (String) cl.getMethod("getTransactionUrl").invoke(null);
            String JSON_FILE_URL_L = (String) cl.getMethod("getJsonFileUrl").invoke(null);
            String BUGLY_KEY = (String) cl.getMethod("getBuglyKey").invoke(null);
            String UMENG_KEY = (String) cl.getMethod("getUmengKey").invoke(null);
            String BAIDU_KEY = (String) cl.getMethod("getBaiduKey").invoke(null);
            sMDURLs.add(MARKET_URL_8);
            TRANSACTION_URL = TRANSACTION_URL_L;
            JSON_FILE_URL = JSON_FILE_URL_L;
            Bugly.init(sContext, BUGLY_KEY, false);
            UMConfigure.init(sContext, UMENG_KEY, "ShinnyTech", UMConfigure.DEVICE_TYPE_PHONE, "");
            StatService.setAppKey(BAIDU_KEY);
            StatService.start(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            sMDURLs.add(MARKET_URL_1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            sMDURLs.add(MARKET_URL_1);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            sMDURLs.add(MARKET_URL_1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            sMDURLs.add(MARKET_URL_1);
        }
        sMDURLs.addAll(MDUrlGroup);
    }


    /**
     * date: 7/21/17
     * author: chenli
     * description: 应用前台监听器
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void registerActivityLifecycleCallback() {
        this.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                notifyForeground();
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
                if (activity instanceof MainActivity) {
                    LogUtils.e("App彻底销毁", true);
                    LocalBroadcastManager.getInstance(sContext).unregisterReceiver(mReceiverMarket);
                    LocalBroadcastManager.getInstance(sContext).unregisterReceiver(mReceiverTransaction);
                    sContext.unregisterReceiver(mReceiverNetwork);
                    sContext.unregisterReceiver(mReceiverScreen);
                    if (sWebSocketService != null) {
                        sWebSocketService.disConnectMD();
                        sWebSocketService.disConnectTD();
                        LogUtils.e("连接断开", true);
                    }
                    if (mServiceBound) {
                        sContext.unbindService(BaseApplication.this);
                        LogUtils.e("解除绑定", true);
                        mServiceBound = false;
                    }
                    System.exit(0);
                }
            }
        });
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: 前台任务--连接服务器
     */
    private void notifyForeground() {
        if (mIsBackground) {
            mIsBackground = false;
            //前台
            EventBus.getDefault().post(FOREGROUND);
        }
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: 后台任务--关闭服务器
     */
    private void notifyBackground() {
        if (!mIsBackground) {
            mIsBackground = true;
            //后台
            EventBus.getDefault().post(BACKGROUND);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 下载合约列表文件
     */
    private void downloadLatestJsonFile() {
        OkGo.<File>get(CommonConstants.JSON_FILE_URL)
                .tag(this)
                .execute(new FileCallback(sContext.getFilesDir().getAbsolutePath(), "latest.json") {
                    @Override
                    public void onSuccess(final com.lzy.okgo.model.Response<File> response) {
                        LogUtils.e("下载latest文件结束onResponse: " + response.body().getAbsolutePath(), true);
                        LatestFileManager.initInsList(response.body());
                        // 通知mainActivity和quoteFragment刷新列表
                        EventBus.getDefault().post(DOMINANT);
                        Intent intent = new Intent(sContext, WebSocketService.class);
                        sContext.bindService(intent, BaseApplication.this, Context.BIND_AUTO_CREATE);
                    }
                });
    }

    /**
     * date: 7/9/17
     * description: 注册广播
     */
    private void registerBroaderCast() {
        //行情服务器断线重连广播
        mReceiverMarket = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_ONLINE:
                        //不给用户造成干扰，此条暂且不发
//                        ToastNotificationUtils.showToast(sContext, "行情服务器连接成功");
                        break;
                    case MD_OFFLINE:
                        //断线重连
                        LogUtils.e("行情服务器连接断开，正在重连...", true);

                        if (NetworkUtils.isNetworkConnected(sContext))
                            mMyHandler.sendEmptyMessage(0);
                        else
                            ToastNotificationUtils.showToast(sContext, "无网络，请检查网络设置");
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(sContext).registerReceiver(mReceiverMarket, new IntentFilter(MD_BROADCAST_ACTION));

        //交易服务器断线重连广播
        mReceiverTransaction = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_ONLINE:
                        //不给用户造成干扰，此条暂且不发
//                        ToastNotificationUtils.showToast(sContext, "交易服务器连接成功");
                        break;
                    case TD_OFFLINE:
                        DataManager.getInstance().IS_LOGIN = false;
                        //断线重连
                        LogUtils.e("交易服务器连接断开，正在重连...", true);

                        if (NetworkUtils.isNetworkConnected(sContext))
                            mMyHandler.sendEmptyMessage(1);
                        else
                            ToastNotificationUtils.showToast(sContext, "无网络，请检查网络设置");
                        break;
                    case TD_MESSAGE_SETTLEMENT:
                        Intent intent1 = new Intent(context, ConfirmActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sContext.startActivity(intent1);
                        break;
                    default:
                        break;

                }
            }
        };
        LocalBroadcastManager.getInstance(sContext).registerReceiver(mReceiverTransaction, new IntentFilter(TD_BROADCAST_ACTION));

        //网络状态变化监听广播
        mReceiverNetwork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        LogUtils.e("连接断开", true);
                        break;
                    case 1:
                        if (sWebSocketService != null) {
                            //连接行情服务器
                            sWebSocketService.reConnectMD(sMDURLs.get(index));
                            //连接交易服务器
                            sWebSocketService.reConnectTD();
                            LogUtils.e("连接打开", true);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        sContext.registerReceiver(mReceiverNetwork, new IntentFilter(NETWORK_STATE));

        //屏幕亮起后重连广播
        mReceiverScreen = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyBackground();
            }
        };
        sContext.registerReceiver(mReceiverScreen, new IntentFilter(Intent.ACTION_SCREEN_OFF));

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 行情交易服务器重连
     */
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (sWebSocketService != null) sWebSocketService.reConnectMD(sMDURLs.get(index));
                    break;
                case 1:
                    if (sWebSocketService != null) sWebSocketService.reConnectTD();
                    break;
                default:
                    break;
            }
        }
    }

}
