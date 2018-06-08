package com.xinyi.shinnyfutures.application;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.xinyi.shinnyfutures.BuildConfig;
import com.xinyi.shinnyfutures.constants.CommonConstants;
import com.xinyi.shinnyfutures.model.service.WebSocketService;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;
import com.xinyi.shinnyfutures.utils.LogUtils;
import com.xinyi.shinnyfutures.utils.NetworkUtils;
import com.xinyi.shinnyfutures.utils.ToastNotificationUtils;
import com.xinyi.shinnyfutures.view.activity.ConfirmActivity;
import com.xinyi.shinnyfutures.view.activity.LoginActivity;
import com.xinyi.shinnyfutures.view.activity.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.DOMINANT;
import static com.xinyi.shinnyfutures.constants.CommonConstants.LOG_OUT;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_SETTLEMENT;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_SETTLEMENT_NOT_SURE;
import static com.xinyi.shinnyfutures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

/**
 * Created on 12/21/17.
 * Created by chenli.
 * Description: Tinker框架下的application类的具体实现
 */

public class BaseApplicationLike extends DefaultApplicationLike implements ServiceConnection {

    private static Application sContext;
    private static WebSocketService sWebSocketService;
    private URI mUriMarket;
    private URI mUriTransaction;
    private boolean mServiceBound = false;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTransaction;
    private BroadcastReceiver mReceiverNetwork;
    private BroadcastReceiver mReceiverScreen;
    private boolean mIsBackground = false;
    private MyHandler mMyHandler = new MyHandler();

    public BaseApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
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
        if (sContext == null) sContext = getApplication();

        //初始化bugly
        initBugly();

        //OkHttp网络框架初始化
        initOkGo();

        //广播注册
        registerBroaderCast();

        //下载合约列表文件
        downloadLatestJsonFile();

        //注册活动生命周期回调
        registerActivityLifecycleCallback();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
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
            mUriMarket = new URI(CommonConstants.MARKET_URL);
            mUriTransaction = new URI(CommonConstants.TRANSACTION_URL);
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        notifyForeground();
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
            mIsBackground = true;
            notifyBackground();
        }

    }

    private void initBugly() {
        // 设置是否开启热更新能力，默认为true
        Beta.enableHotfix = true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = true;
        // 设置开发设备，默认为false，上传补丁如果下发范围指定为“开发设备”，需要调用此接口来标识开发设备
        Bugly.setIsDevelopmentDevice(getApplication(), true);
        // 多渠道需求塞入
        String channel = BuildConfig.BROKER_ID;
        Bugly.setAppChannel(getApplication(), channel);
        Bugly.init(getApplication(), "247bd4965f", false);
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
        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
        headers.put("commonHeaderKey2", "commonHeaderValue2");
        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
        //-------------------------------------------------------------------------------------//
        OkGo.getInstance().init(sContext)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params);
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: 应用前台监听器
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void registerActivityLifecycleCallback() {
        sContext.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (mIsBackground) {
                    mIsBackground = false;
                    notifyForeground();
                }
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
                    notifyBackground();
                    if (mServiceBound) {
                        sContext.unbindService(BaseApplicationLike.this);
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
        if (sWebSocketService != null) {
            //连接行情服务器
            sWebSocketService.connect(mUriMarket, null, 2000);
            //连接交易服务器
            sWebSocketService.connectTransaction(mUriTransaction, null, 2000);
            LogUtils.e("连接打开", true);
        }
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: 后台任务--关闭服务器
     */
    private void notifyBackground() {
        // This is where you can notify listeners, handle session tracking, etc
        if (sWebSocketService != null) {
            sWebSocketService.disConnect();
            sWebSocketService.disConnectTransaction();
            LogUtils.e("连接断开", true);
            EventBus.getDefault().post(LOG_OUT);
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
                        LatestFileUtils.initInsList(response.body());
                        // 通知mainActivity和quoteFragment刷新列表
                        EventBus.getDefault().post(DOMINANT);
                        Intent intent = new Intent(sContext, WebSocketService.class);
                        sContext.bindService(intent, BaseApplicationLike.this, Context.BIND_AUTO_CREATE);
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
                    case CLOSE:
                        //每隔两秒,断线重连
                        if (!mIsBackground) {
                            if (NetworkUtils.isNetworkConnected(sContext)) {
                                Message message = new Message();
                                message.obj = mUriMarket;
                                message.what = 0;
                                mMyHandler.sendMessageDelayed(message, 2000);
                            } else
                                ToastNotificationUtils.showToast(sContext, "无网络，请检查网络设置");
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(sContext).registerReceiver(mReceiverMarket, new IntentFilter(BROADCAST_ACTION));

        //交易服务器断线重连广播
        mReceiverTransaction = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case CLOSE:
                        LoginActivity.setIsLogin(false);
                        //每隔两秒,断线重连
                        if (!mIsBackground) {
                            if (NetworkUtils.isNetworkConnected(sContext)) {
                                Message message = new Message();
                                message.obj = mUriTransaction;
                                message.what = 1;
                                mMyHandler.sendMessageDelayed(message, 2000);
                            } else
                                ToastNotificationUtils.showToast(sContext, "无网络，请检查网络设置");
                        }
                        break;
                    case MESSAGE_SETTLEMENT_NOT_SURE:
                        break;
                    case MESSAGE_SETTLEMENT:
                        Intent intent1 = new Intent(context, ConfirmActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sContext.startActivity(intent1);
                        break;
                    default:
                        break;

                }
            }
        };
        LocalBroadcastManager.getInstance(sContext).registerReceiver(mReceiverTransaction, new IntentFilter(BROADCAST_ACTION_TRANSACTION));

        //网络状态变化监听广播
        mReceiverNetwork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        if (sWebSocketService != null) {
                            LogUtils.e("连接断开", true);
                        }
                        break;
                    case 1:
                        if (sWebSocketService != null) {
                            //连接行情服务器
                            sWebSocketService.connect(mUriMarket, null, 2000);
                            //连接交易服务器
                            sWebSocketService.connectTransaction(mUriTransaction, null, 2000);
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
                mIsBackground = true;
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
                    if (sWebSocketService != null)
                        sWebSocketService.connect((URI) msg.obj, null, 2000);
                    break;
                case 1:
                    if (sWebSocketService != null)
                        sWebSocketService.connectTransaction(URI.create((String) msg.obj), null, 2000);
                    break;
                default:
                    break;
            }
        }
    }

}
