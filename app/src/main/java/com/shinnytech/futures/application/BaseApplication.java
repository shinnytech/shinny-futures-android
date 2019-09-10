package com.shinnytech.futures.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.sfit.ctp.info.DeviceInfoManager;
import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.amplitude.api.Identify;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.service.ForegroundService;
import com.shinnytech.futures.utils.Base64;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.websocket.MDWebSocket;
import com.shinnytech.futures.websocket.TDWebSocket;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.shinnytech.futures.constants.AmpConstants.AMP_BACKGROUND;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CRASH;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CRASH_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_ERROR_MESSAGE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_ERROR_STACK;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_ERROR_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_INIT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BALANCE_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BALANCE_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_INIT_TIME_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_PACKAGE_ID_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_PACKAGE_ID_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_SURVIVAL_TIME_TOTAL;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_TYPE_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_TYPE_FIRST_PURE_NEWBIE_VALUE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_TYPE_FIRST_TRADER_VALUE;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.CommonConstants.JSON_FILE_URL;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_1;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_2;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_3;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_URL_4;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL_INS_LIST;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_CANCEL_ORDER_CONFIRM;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_INSERT_ORDER_CONFIRM;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_IS_CONDITION;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_KLINE_DURATION_DEFAULT;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_PARA_MA;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_RECOMMEND;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_SYSTEM_INFO;

/**
 * Created on 12/21/17.
 * Created by chenli.
 * Description: Tinker框架下的application类的具体实现
 */

public class BaseApplication extends Application {

    /**
     * date: 7/9/17
     * description: 行情广播类型
     */
    public static final String MD_BROADCAST = "MD_BROADCAST";

    /**
     * date: 7/9/17
     * description: 交易广播类型
     */
    public static final String TD_BROADCAST = "TD_BROADCAST";

    /**
     * date: 2019/8/10
     * description: 条件单广播类型
     */
    public static final String CO_BROADCAST = "CO_BROADCAST";

    /**
     * date: 7/9/17
     * description: 行情广播信息
     */
    public static final String MD_BROADCAST_ACTION = BaseApplication.class.getName() + "." + MD_BROADCAST;

    /**
     * date: 7/9/17
     * description: 交易广播信息
     */
    public static final String TD_BROADCAST_ACTION = BaseApplication.class.getName() + "." + TD_BROADCAST;
    /**
     * date: 2019/8/10
     * description: 条件单广播信息
     */
    public static final String CO_BROADCAST_ACTION = BaseApplication.class.getName() + "." + CO_BROADCAST;
    private static LocalBroadcastManager mLocalBroadcastManager;
    private static Context sContext;
    private static DataManager sDataManager;
    private List<String> mMDURLs;
    private List<String> mTDURLs;
    private static LOGClient sLOGClient;
    private static boolean sBackGround;
    private AppLifecycleObserver mAppLifecycleObserver;
    private static MDWebSocket mMDWebSocket;
    private static TDWebSocket mTDWebSocket;

    public static Context getContext() {
        return sContext;
    }

    public static boolean issBackGround() {
        return sBackGround;
    }

    public static LOGClient getLOGClient() {
        return sLOGClient;
    }

    public static MDWebSocket getmMDWebSocket() {
        return mMDWebSocket;
    }

    public static TDWebSocket getmTDWebSocket() {
        return mTDWebSocket;
    }

    public static void sendMessage(String message, String type) {
        switch (type) {
            case MD_BROADCAST:
                Intent intent = new Intent(MD_BROADCAST_ACTION);
                intent.putExtra("msg", message);
                mLocalBroadcastManager.sendBroadcast(intent);
                break;
            case TD_BROADCAST:
                Intent intentTransaction = new Intent(TD_BROADCAST_ACTION);
                intentTransaction.putExtra("msg", message);
                mLocalBroadcastManager.sendBroadcast(intentTransaction);
                break;
            case CO_BROADCAST:
                Intent intentCondition = new Intent(CO_BROADCAST_ACTION);
                intentCondition.putExtra("msg", message);
                mLocalBroadcastManager.sendBroadcast(intentCondition);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sDataManager = DataManager.getInstance();
        mMDURLs = new ArrayList<>();
        mTDURLs = new ArrayList<>();
        mAppLifecycleObserver = new AppLifecycleObserver();
        sBackGround = false;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(sContext);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mAppLifecycleObserver);

        initAppVersion();
        initTMDUrl();
        initDefaultConfig();
        initThirdParty();
        downloadLatestJsonFile();
    }

    /**
     * date: 2018/11/7
     * author: chenli
     * description: 获取app版本号
     */
    private void initAppVersion() {
        try {
            sDataManager.APP_CODE = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
            sDataManager.APP_VERSION = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
            MDUrlGroup.add(MARKET_URL_5);
            MDUrlGroup.add(MARKET_URL_6);
            MDUrlGroup.add(MARKET_URL_7);
            mMDURLs.add(MARKET_URL_8);
            TRANSACTION_URL = TRANSACTION_URL_L;
            JSON_FILE_URL = JSON_FILE_URL_L;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            mMDURLs.add(MARKET_URL_1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            mMDURLs.add(MARKET_URL_1);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            mMDURLs.add(MARKET_URL_1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            mMDURLs.add(MARKET_URL_1);
        }
        Collections.shuffle(MDUrlGroup);
        mMDURLs.addAll(MDUrlGroup);
        mTDURLs.add(TRANSACTION_URL);
        mMDWebSocket = new MDWebSocket(mMDURLs, 0);
        mTDWebSocket = new TDWebSocket(mTDURLs, 0);
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
            SPUtils.putAndApply(sContext, CONFIG_KLINE_DURATION_DEFAULT, SettingConstants.KLINE_DURATION_DEFAULT);
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
            SPUtils.putAndApply(sContext, CONFIG_PARA_MA, SettingConstants.PARA_MA);
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

        if (!SPUtils.contains(sContext, CONFIG_IS_CONDITION)) {
            SPUtils.putAndApply(sContext, CONFIG_IS_CONDITION, false);
        }

    }

    /**
     * date: 2019/6/18
     * author: chenli
     * description: 初始化第三方框架
     */
    private void initThirdParty(){
        String BUGLY_KEY = "github";
        String AMP_KEY = "github";
        String AK = "github";
        String SK = "github";
        try {
            Class cl = Class.forName("com.shinnytech.futures.constants.LocalCommonConstants");
            BUGLY_KEY = (String) cl.getMethod("getBuglyKey").invoke(null);
            AMP_KEY = (String) cl.getMethod("getAmpKey").invoke(null);
            AK = (String) cl.getMethod("getAK").invoke(null);
            SK = (String) cl.getMethod("getSK").invoke(null);
            String user_agent = (String) cl.getMethod("getUserAgent").invoke(null);
            String client_app_id = (String) cl.getMethod("getClientAppId").invoke(null);
            sDataManager.USER_AGENT = user_agent;
            sDataManager.CLIENT_APP_ID = client_app_id;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        initAMP(AMP_KEY);
        initBugly(BUGLY_KEY);
        initAliLog(AK, SK);
    }

    /**
     * date: 2019/6/18
     * author: chenli
     * description: 配置AMP
     */
    private void initAMP(String AMP_KEY){
        Amplitude.getInstance().initialize(this, AMP_KEY).enableForegroundTracking(this);
        Identify identify = new Identify()
                .setOnce(AMP_USER_PACKAGE_ID_FIRST, BuildConfig.FLAVOR)
                .set(AMP_USER_PACKAGE_ID_LAST, BuildConfig.FLAVOR)
                .setOnce(AMP_USER_INIT_TIME_FIRST, TimeUtils.getNowTimeSecond());
        Amplitude.getInstance().identify(identify);
        sDataManager.INIT_TIME = System.currentTimeMillis();
        sDataManager.LAST_TIME =  sDataManager.INIT_TIME;
        sDataManager.SOURCE = "none";
        LogUtils.e("AMP_INIT", true);
        Amplitude.getInstance().logEventWrap(AMP_INIT, new JSONObject());
    }

    /**
     * date: 2019/6/18
     * author: chenli
     * description: 配置bugly
     */
    private void initBugly(String BUGLY_KEY){
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
                Amplitude.getInstance().logEventWrap(AMP_CRASH, jsonObject);
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put("user-agent", sDataManager.USER_AGENT);
                return map;
            }
        });
        Beta.enableHotfix = false;
        Bugly.init(sContext, BUGLY_KEY, false, strategy);
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
     * date: 7/9/17
     * author: chenli
     * description: 下载合约列表文件
     */
    private void downloadLatestJsonFile() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CommonConstants.JSON_FILE_URL)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String latest = LatestFileManager.readFile("latest.json");
                if (latest.isEmpty())return;
                LatestFileManager.initInsList(latest);
                mMDWebSocket.reConnect();
                mTDWebSocket.reConnect();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String latest = response.body().string();
                LatestFileManager.initInsList(latest);
                mMDWebSocket.reConnect();
                mTDWebSocket.reConnect();
                LatestFileManager.writeFile("latest.json", latest);
            }
        });
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


    /**
     * date: 7/21/17
     * author: chenli
     * description: 前台任务--连接服务器
     */
    private void notifyForeground() {
        sDataManager.FOREGROUND_TIME = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] info = DeviceInfoManager.getCollectInfo(sContext);
                    String encodeInfo = Base64.encode(info);
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, encodeInfo);
                }catch (Exception e){
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, "");
                }

            }
        }).start();

        if (sBackGround){
            sBackGround = false;
            mMDWebSocket.backToForegroundCheck();
            mTDWebSocket.backToForegroundCheck();
        }

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
        sDataManager.SOURCE = "none";
        long currentTime = System.currentTimeMillis();
        long initTime = (long) SPUtils.get(sContext, CONFIG_INIT_TIME, currentTime);
        long survivalTime = currentTime - initTime;
        Identify identify = new Identify();
        identify.set(AMP_USER_SURVIVAL_TIME_TOTAL, survivalTime);
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity != null) {
            AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
            if (accountEntity != null) {
                String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                String pre_balance = MathUtils.round(accountEntity.getPre_balance(), 2);
                if (!"-".equals(static_balance)) {
                    identify.setOnce(AMP_USER_BALANCE_FIRST, static_balance)
                            .set(AMP_USER_BALANCE_LAST, static_balance);
                }
                String settlement = sDataManager.getBroker().getSettlement();
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
        Amplitude.getInstance().logEventWrap(AMP_BACKGROUND, new JSONObject());

        Intent intent = new Intent(sContext, ForegroundService.class);
        startService(intent);
    }

}
