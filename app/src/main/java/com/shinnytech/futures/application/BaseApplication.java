package com.shinnytech.futures.application;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.sfit.ctp.info.DeviceInfoManager;
import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.amplitude.api.Identify;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.RedrawEvent;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.service.ForegroundService;
import com.shinnytech.futures.service.WebSocketService;
import com.shinnytech.futures.utils.Base64;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

import static com.shinnytech.futures.constants.CommonConstants.AMP_BACKGROUND;
import static com.shinnytech.futures.constants.CommonConstants.AMP_CRASH;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CRASH_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ERROR_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ERROR_STACK;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ERROR_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_FOREGROUND;
import static com.shinnytech.futures.constants.CommonConstants.AMP_INIT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BALANCE_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BALANCE_LAST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_INIT_TIME_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_PACKAGE_ID_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_PACKAGE_ID_LAST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_SURVIVAL_TIME_TOTAL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_TYPE_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_TYPE_FIRST_PURE_NEWBIE_VALUE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_TYPE_FIRST_TRADER_VALUE;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_CANCEL_ORDER_CONFIRM;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_INSERT_ORDER_CONFIRM;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_KLINE_DURATION_DEFAULT;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_PARA_MA;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_RECOMMEND;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_SYSTEM_INFO;
import static com.shinnytech.futures.constants.CommonConstants.JSON_FILE_URL;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_1;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_2;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_3;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_4;
import static com.shinnytech.futures.constants.CommonConstants.MD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL_INS_LIST;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;
import static com.shinnytech.futures.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * Created on 12/21/17.
 * Created by chenli.
 * Description: Tinker框架下的application类的具体实现
 */

public class BaseApplication extends Application implements ServiceConnection {

    private static Context sContext;
    private static List<String> sMDURLs = new ArrayList<>();
    private static int sIndex = 0;
    private static LOGClient sLOGClient;
    private static boolean sBackGround = false;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTransaction;
    private AppLifecycleObserver mAppLifecycleObserver = new AppLifecycleObserver();
    private Context mSettlementContext;
    private int mBindingCount = 0;

    public static int getsIndex() {
        return sIndex;
    }

    public static void setsIndex(int sIndex) {
        BaseApplication.sIndex = sIndex;
    }

    public static List<String> getsMDURLs() {
        return sMDURLs;
    }

    public static Context getContext() {
        return sContext;
    }

    public static boolean issBackGround() {
        return sBackGround;
    }

    public static LOGClient getLOGClient() {
        return sLOGClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sContext == null) sContext = getApplicationContext();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(mAppLifecycleObserver);

        //获取版本号
        initAppVersion();

        //初始化行情服务器地址
        initTMDUrl();

        //初始化默认配置
        initDefaultConfig();

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
     * date: 2019/3/24
     * author: chenli
     * description: 配置阿里日志服务
     */
    private void initAliLog(String ak, String sk) {
        SLSDatabaseManager.getInstance().setupDB(sContext);

        PlainTextAKSKCredentialProvider credentialProvider =
                new PlainTextAKSKCredentialProvider(ak, sk);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setCachable(false);
        conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        SLSLog.enableLog(); // log打印在控制台

        String endpoint = "https://cn-shanghai.log.aliyuncs.com";
        sLOGClient = new LOGClient(sContext, endpoint, credentialProvider, conf);
    }

    /**
     * date: 2018/11/20
     * author: chenli
     * description: 初始化默认配置
     */
    private void initDefaultConfig() {
        //初始化自选合约列表文件
        try {
            BaseApplication.getContext().openFileInput(OPTIONAL_INS_LIST);
        } catch (FileNotFoundException e) {
            LatestFileManager.saveInsListToFile(new ArrayList<String>());
        }

        if (!SPUtils.contains(sContext, CONFIG_KLINE_DURATION_DEFAULT)) {
            SPUtils.putAndApply(sContext, CONFIG_KLINE_DURATION_DEFAULT, CommonConstants.KLINE_DURATION_DEFAULT);
        } else {
            //覆盖之前的配置
            String kline = (String) SPUtils.get(sContext, CONFIG_KLINE_DURATION_DEFAULT, "");
            kline = kline.replace("钟", "");
            kline = kline.replace("小", "");
            SPUtils.putAndApply(sContext, CONFIG_KLINE_DURATION_DEFAULT, kline);
        }

        if (!SPUtils.contains(sContext, CONFIG_IS_FIRM)) {
            if (SPUtils.contains(sContext, CONFIG_BROKER)) {
                String broker = (String) SPUtils.get(sContext, CONFIG_BROKER, "");
                if (!broker.isEmpty()) {
                    if (broker.equals(BROKER_ID_SIMULATION))
                        SPUtils.putAndApply(sContext, CONFIG_IS_FIRM, false);
                    else SPUtils.putAndApply(sContext, CONFIG_IS_FIRM, true);
                } else SPUtils.putAndApply(sContext, CONFIG_IS_FIRM, true);
            } else SPUtils.putAndApply(sContext, CONFIG_IS_FIRM, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_INIT_TIME)) {
            SPUtils.putAndApply(sContext, CONFIG_INIT_TIME, System.currentTimeMillis());
        }

        if (!SPUtils.contains(sContext, CONFIG_RECOMMEND)) {
            SPUtils.putAndApply(sContext, CONFIG_RECOMMEND, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_PARA_MA)) {
            SPUtils.putAndApply(sContext, CONFIG_PARA_MA, CommonConstants.PARA_MA);
        }

        if (!SPUtils.contains(sContext, CONFIG_INSERT_ORDER_CONFIRM)) {
            SPUtils.putAndApply(sContext, CONFIG_INSERT_ORDER_CONFIRM, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_CANCEL_ORDER_CONFIRM)) {
            SPUtils.putAndApply(sContext, CONFIG_CANCEL_ORDER_CONFIRM, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_POSITION_LINE)) {
            SPUtils.putAndApply(sContext, CONFIG_POSITION_LINE, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_ORDER_LINE)) {
            SPUtils.putAndApply(sContext, CONFIG_ORDER_LINE, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_AVERAGE_LINE)) {
            SPUtils.putAndApply(sContext, CONFIG_AVERAGE_LINE, true);
        }

        if (!SPUtils.contains(sContext, CONFIG_MD5)) {
            SPUtils.putAndApply(sContext, CONFIG_MD5, true);
        }

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
        try {
            Class cl = Class.forName("com.shinnytech.futures.constants.LocalCommonConstants");
            String MARKET_URL_5 = (String) cl.getMethod("getMarketUrl5").invoke(null);
            String MARKET_URL_6 = (String) cl.getMethod("getMarketUrl6").invoke(null);
            String MARKET_URL_7 = (String) cl.getMethod("getMarketUrl7").invoke(null);
            String MARKET_URL_8 = (String) cl.getMethod("getMarketUrl8").invoke(null);
            String TRANSACTION_URL_L = (String) cl.getMethod("getTransactionUrl").invoke(null);
            String JSON_FILE_URL_L = (String) cl.getMethod("getJsonFileUrl").invoke(null);
            String BUGLY_KEY = (String) cl.getMethod("getBuglyKey").invoke(null);
            String AMP_KEY = (String) cl.getMethod("getAmpKey").invoke(null);
            String AK = (String) cl.getMethod("getAK").invoke(null);
            String SK = (String) cl.getMethod("getSK").invoke(null);
            final String user_agent = (String) cl.getMethod("getUserAgent").invoke(null);
            MDUrlGroup.add(MARKET_URL_5);
            MDUrlGroup.add(MARKET_URL_6);
            MDUrlGroup.add(MARKET_URL_7);
            sMDURLs.add(MARKET_URL_8);
            TRANSACTION_URL = TRANSACTION_URL_L;
            JSON_FILE_URL = JSON_FILE_URL_L;
            Amplitude.getInstance().initialize(this, AMP_KEY).enableForegroundTracking(this);
            Identify identify = new Identify()
                    .setOnce(AMP_USER_PACKAGE_ID_FIRST, BuildConfig.FLAVOR)
                    .set(AMP_USER_PACKAGE_ID_LAST, BuildConfig.FLAVOR)
                    .setOnce(AMP_USER_INIT_TIME_FIRST, TimeUtils.getNowTimeSecond());
            Amplitude.getInstance().identify(identify);
            Amplitude.getInstance().logEvent(AMP_INIT);
            LogUtils.e("AMP_INIT", true);
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(sContext);
            strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
                public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                              String errorMessage, String errorStack) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(AMP_EVENT_CRASH_TYPE, crashType);
                        jsonObject.put(AMP_EVENT_ERROR_TYPE, errorType);
                        jsonObject.put(AMP_EVENT_ERROR_MESSAGE, errorMessage);
                        jsonObject.put(AMP_EVENT_ERROR_STACK, errorStack);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Amplitude.getInstance().logEvent(AMP_CRASH, jsonObject);
                    LinkedHashMap<String, String> map = new LinkedHashMap<>();
                    map.put("user-agent", user_agent);
                    return map;
                }
            });
            Beta.enableHotfix = false;
            Bugly.init(sContext, BUGLY_KEY, false, strategy);
            initAliLog(AK, SK);
            DataManager.getInstance().USER_AGENT = user_agent;
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
        Collections.shuffle(MDUrlGroup);
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
                if (activity.getParent() != null) {//如果这个视图是嵌入的子视图
                    mSettlementContext = activity.getParent();
                } else {
                    mSettlementContext = activity;
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity.getParent() != null) {//如果这个视图是嵌入的子视图
                    mSettlementContext = activity.getParent();
                } else {
                    mSettlementContext = activity;
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity.getParent() != null) {//如果这个视图是嵌入的子视图
                    mSettlementContext = activity.getParent();
                } else {
                    mSettlementContext = activity;
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
            }
        });
    }


    /**
     * date: 7/21/17
     * author: chenli
     * description: 前台任务--连接服务器
     */
    private void notifyForeground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] info = DeviceInfoManager.getCollectInfo(sContext);
                String encodeInfo = Base64.encode(info);
                SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, encodeInfo);
            }
        }).start();

        EventBus.getDefault().post(new RedrawEvent());
        sBackGround = false;
        WebSocketService.sendPeekMessage();
        WebSocketService.sendPeekMessageTransaction();

        Amplitude.getInstance().logEvent(AMP_FOREGROUND);

        Intent intent = new Intent(sContext, ForegroundService.class);
        stopService(intent);
    }

    /**
     * date: 7/21/17
     * author: chenli
     * description: 后台任务--关闭服务器
     */
    private void notifyBackground() {
        //后台
        sBackGround = true;
        long currentTime = System.currentTimeMillis();
        long initTime = (long) SPUtils.get(sContext, CONFIG_INIT_TIME, currentTime);
        long survivalTime = currentTime - initTime;
        Identify identify = new Identify();
        identify.set(AMP_USER_SURVIVAL_TIME_TOTAL, survivalTime);
        UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().LOGIN_USER_ID);
        if (userEntity != null) {
            AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
            if (accountEntity != null) {
                String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                String pre_balance = MathUtils.round(accountEntity.getPre_balance(), 2);
                if (!"-".equals(static_balance)) {
                    identify.setOnce(AMP_USER_BALANCE_FIRST, static_balance)
                            .set(AMP_USER_BALANCE_LAST, static_balance);
                }
                String settlement = DataManager.getInstance().getBroker().getSettlement();
                if (!"-".equals(pre_balance)) {
                    if (MathUtils.isZero(pre_balance) && settlement == null) {
                        identify.setOnce(AMP_USER_TYPE_FIRST, AMP_USER_TYPE_FIRST_PURE_NEWBIE_VALUE);
                    } else {
                        identify.setOnce(AMP_USER_TYPE_FIRST, AMP_USER_TYPE_FIRST_TRADER_VALUE);
                    }
                }
            }
        }
        Amplitude.getInstance().identify(identify);
        Amplitude.getInstance().logEvent(AMP_BACKGROUND);

        Intent intent = new Intent(sContext, ForegroundService.class);
        startService(intent);

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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LatestFileManager.initInsList(response.body());
                                Intent intent = new Intent(sContext, WebSocketService.class);
                                sContext.bindService(intent, BaseApplication.this, Context.BIND_AUTO_CREATE);
                            }
                        }).start();

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
                    case MD_OFFLINE:
                        //断线重连
                        LogUtils.e("行情服务器连接断开，正在重连...", true);

                        if (NetworkUtils.isNetworkConnected(sContext))
                            WebSocketService.reConnectMD(sMDURLs.get(sIndex));
                        else
                            ToastUtils.showToast(sContext, "无网络，请检查网络设置");
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
                    case TD_OFFLINE:
                        //断线重连
                        LogUtils.e("交易服务器连接断开，正在重连...", true);

                        if (NetworkUtils.isNetworkConnected(sContext))
                            WebSocketService.reConnectTD();
                        else
                            ToastUtils.showToast(sContext, "无网络，请检查网络设置");
                        break;
                    case TD_MESSAGE_SETTLEMENT:
                        if (mSettlementContext != null) {
                            final Dialog dialog = new Dialog(mSettlementContext, R.style.responsibilityDialog);
                            View viewDialog = View.inflate(mSettlementContext, R.layout.view_dialog_confirm, null);
                            dialog.setContentView(viewDialog);
                            TextView settlement = viewDialog.findViewById(R.id.settlement_info);
                            settlement.setText(DataManager.getInstance().getBroker().getSettlement());
                            dialog.setCanceledOnTouchOutside(false);
                            viewDialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    WebSocketService.sendReqConfirmSettlement();
                                    dialog.dismiss();
                                }
                            });
                            if (!dialog.isShowing()) {
                                dialog.show();
                            }
                        }
                        break;
                    default:
                        break;

                }
            }
        };
        LocalBroadcastManager.getInstance(sContext).registerReceiver(mReceiverTransaction, new IntentFilter(TD_BROADCAST_ACTION));

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mBindingCount++;
        //连接行情服务器
        WebSocketService.connectMD(sMDURLs.get(sIndex));
        //连接交易服务器
        WebSocketService.connectTD();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (mBindingCount < 5) {
            Intent intent = new Intent(sContext, WebSocketService.class);
            sContext.bindService(intent, BaseApplication.this, Context.BIND_AUTO_CREATE);
        } else {
            ToastUtils.showToast(sContext, "系统异常，即将退出，请重新打开");
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 2000);
        }
    }

    @Override
    public void onBindingDied(ComponentName name) {
        if (mBindingCount < 5) {
            Intent intent = new Intent(sContext, WebSocketService.class);
            sContext.bindService(intent, BaseApplication.this, Context.BIND_AUTO_CREATE);
        } else {
            ToastUtils.showToast(sContext, "系统异常，即将退出，请重新打开");
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 2000);
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {

    }

    class AppLifecycleObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onEnterForeground() {
            LogUtils.e("onEnterForeground", true);
            notifyForeground();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onEnterBackground() {
            LogUtils.e("onEnterBackground", true);
            notifyBackground();
        }
    }

}
