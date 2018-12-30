package com.shinnytech.futures.model.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.model.bean.accountinfobean.BrokerEntity;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.constants.CommonConstants.BACKGROUND;
import static com.shinnytech.futures.constants.CommonConstants.CHART_ID;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.FOREGROUND;
import static com.shinnytech.futures.constants.CommonConstants.LOAD_QUOTE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.MD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.VIEW_WIDTH;

/**
 * date: 7/9/17
 * author: chenli
 * description: webSocket后台服务，分别控制与行情服务器和交易服务器的连接
 * version:
 * state: done
 */
public class WebSocketService extends Service {

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
     * date: 7/9/17
     * description: 行情广播信息
     */
    public static final String MD_BROADCAST_ACTION = WebSocketService.class.getName() + "." + MD_BROADCAST;

    /**
     * date: 7/9/17
     * description: 交易广播信息
     */
    public static final String TD_BROADCAST_ACTION = WebSocketService.class.getName() + "." + TD_BROADCAST;

    private static final int TIMEOUT = 500;
    private final IBinder mBinder = new LocalBinder();
    private boolean mBackground = false;
    private WebSocket mWebSocketClientMD;

    private WebSocket mWebSocketClientTD;

    private DataManager sDataManager = DataManager.getInstance();

    private LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    private long mMDLastPong = System.currentTimeMillis() / 1000;

    private long mTDLastPong = System.currentTimeMillis() / 1000;

    public WebSocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String NOTIFICATION_CHANNEL_ID = "com.shinnytech.futures";
            String channelName = "WebSocketService";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("快期小Q下单软件正在运行")
                    .setContentText("点击返回程序")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        } else{
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(this, "service")
                    .setContentTitle("快期小Q下单软件正在运行")
                    .setContentText("点击返回程序")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        }


        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if ((System.currentTimeMillis() / 1000 - mMDLastPong) >= 20) {
                    sendMessage(MD_OFFLINE, MD_BROADCAST);
                } else {
                    mWebSocketClientMD.sendPing();
                }

                if ((System.currentTimeMillis() / 1000 - mTDLastPong) >= 20) {
                    sendMessage(TD_OFFLINE, TD_BROADCAST);
                } else {
                    mWebSocketClientTD.sendPing();
                }

            }
        };
        timer.schedule(timerTask, 15000, 15000);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void sendMessage(String message, String type) {
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
        }

    }

    @Subscribe
    public void onEvent(String msg) {
        if (BACKGROUND.equals(msg)) {
            mBackground = true;
        }

        if (FOREGROUND.equals(msg)) {
            mBackground = false;
            sendPeekMessage();
        }
    }

    /**
     * date: 6/28/17
     * author: chenli
     * description: 连接行情服务器
     */
    public void connectMD(String url) {
        try {
            mWebSocketClientMD = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(url)
                    .addListener(new WebSocketAdapter() {

                        // A text message arrived from the server.
                        public void onTextMessage(WebSocket websocket, String message) {
                            LogUtils.e(message, false);
                            try {
                                JSONObject jsonObject = new JSONObject(message);
                                String aid = jsonObject.getString("aid");
                                switch (aid) {
                                    case "rsp_login":
                                        //首次连接行情服务器与断开重连的行情订阅处理
                                        mWebSocketClientMD.sendPing();
                                        String ins_list = sDataManager.getRtnData().getIns_list();
                                        if (ins_list != null) sendSubscribeQuote(ins_list);
                                        else
                                            sendSubscribeQuote(TextUtils.join(",",
                                                    new ArrayList(LatestFileManager.getMainInsList().keySet()).subList(0, LOAD_QUOTE_NUM)));

                                        Map<String, ChartEntity> chartEntityMap = sDataManager.getRtnData().getCharts();
                                        if (chartEntityMap.size() != 0) {
                                            for (String key :
                                                    chartEntityMap.keySet()) {
                                                ChartEntity chartEntity = chartEntityMap.get(key);
                                                String duration = chartEntity.getState().get("duration");
                                                String ins = chartEntity.getState().get("ins_list");
                                                if (CURRENT_DAY_FRAGMENT.equals(key)){
                                                    sendSetChart(ins);
                                                }else {
                                                    sendSetChartKline(ins, VIEW_WIDTH, duration);
                                                }
                                            }
                                        }
                                        break;
                                    case "rtn_data":
                                        BaseApplication.setIndex(0);
                                        sDataManager.refreshFutureBean(jsonObject);
                                        break;
                                    default:
                                        sendMessage(MD_OFFLINE, MD_BROADCAST);
                                        return;
                                }
                                if (!mBackground) sendPeekMessage();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            super.onPongFrame(websocket, frame);
                            mMDLastPong = System.currentTimeMillis() / 1000;
                            LogUtils.e("MDPong", true);
                        }

                    })
                    .addHeader("User-Agent", "shinnyfutures-Android" + " " + sDataManager.APP_VERSION)
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connectAsynchronously();
            int index = BaseApplication.getIndex() + 1;
            if (index == 7) index = 0;
            BaseApplication.setIndex(index);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reConnectMD(String url) {
        disConnectMD();
        connectMD(url);
    }

    public void disConnectMD() {
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            mWebSocketClientMD.disconnect();
            mWebSocketClientMD = null;
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 行情订阅
     */
    public void sendSubscribeQuote(String insList) {
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            String subScribeQuote = "{\"aid\":\"subscribe_quote\",\"ins_list\":\"" + insList + "\"}";
            LogUtils.e(subScribeQuote, true);
            mWebSocketClientMD.sendText(subScribeQuote);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约信息
     */
    public void sendPeekMessage() {
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            String peekMessage = "{\"aid\":\"peek_message\"}";
            mWebSocketClientMD.sendText(peekMessage);
            LogUtils.e(peekMessage, false);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 分时图
     */
    public void sendSetChart(String ins_list) {
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            String duration = "60000000000";
            String trading_day_start = "0";
            String trading_day_count = "86400000000000";
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + CHART_ID + "\"," +
                    "\"ins_list\":\"" + ins_list + "\",\"duration\":" + duration + "," +
                    "\"trading_day_start\":" + trading_day_start + "," +
                    "\"trading_day_count\":" + trading_day_count + "}";
            LogUtils.e(setChart, true);
            mWebSocketClientMD.sendText(setChart);
        }
    }

    /**
     * date: 2018/12/14
     * author: chenli
     * description: k线图
     */
    public void sendSetChartKline(String ins_list, int view_width, String duration){
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + CHART_ID + "\"," +
                    "\"ins_list\":\"" + ins_list + "\",\"duration\":" + duration + "," +
                    "\"view_width\":" + view_width + "}";
            LogUtils.e(setChart, true);
            mWebSocketClientMD.sendText(setChart);
        }
    }

    /**
     * date: 6/28/17
     * author: chenli
     * description: 连接交易服务器
     */
    public void connectTD() {
        try {
            mWebSocketClientTD = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(CommonConstants.TRANSACTION_URL)
                    .addListener(new WebSocketAdapter() {
                        @Override

                        // A text message arrived from the server.
                        public void onTextMessage(final WebSocket websocket, String message) {
                            LogUtils.e(message, false);
                            try {
                                JSONObject jsonObject = new JSONObject(message);
                                String aid = jsonObject.getString("aid");
                                switch (aid) {
                                    case "rtn_brokers":
                                        mWebSocketClientTD.sendPing();
                                        BrokerEntity brokerInfo = new Gson().fromJson(message, BrokerEntity.class);
                                        sDataManager.getBroker().setBrokers(brokerInfo.getBrokers());
                                        sendMessage(TD_MESSAGE_BROKER_INFO, TD_BROADCAST);
                                        break;
                                    case "rtn_data":
                                        sDataManager.refreshTradeBean(jsonObject);
                                        break;
                                    default:
                                        sendMessage(TD_OFFLINE, TD_BROADCAST);
                                        return;
                                }
                                if (!mBackground) sendPeekMessageTransaction();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            super.onPongFrame(websocket, frame);
                            mTDLastPong = System.currentTimeMillis() / 1000;
                            LogUtils.e("TDPong", true);
                        }

                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .addHeader("User-Agent", "shinnyfutures-Android" + " " + sDataManager.APP_VERSION)
                    .connectAsynchronously();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reConnectTD() {
        disConnectTD();
        connectTD();
    }

    public void disConnectTD() {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            mWebSocketClientTD.disconnect();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约信息
     */
    public void sendPeekMessageTransaction() {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String peekMessage = "{\"aid\":\"peek_message\"}";
            mWebSocketClientTD.sendText(peekMessage);
            LogUtils.e(peekMessage, false);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 用户登录
     */
    public void sendReqLogin(String bid, String user_name, String password) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String reqLogin = "{\"aid\":\"req_login\",\"bid\":\"" + bid + "\",\"user_name\":\""
                    + user_name + "\",\"password\":\"" + password + "\"}";
            LogUtils.e(reqLogin, true);
            mWebSocketClientTD.sendText(reqLogin);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 确认结算单
     */
    public void sendReqConfirmSettlement() {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String confirmSettlement = "{\"aid\":\"confirm_settlement\"}";
            LogUtils.e(confirmSettlement, true);
            mWebSocketClientTD.sendText(confirmSettlement);
        }
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 下单
     */
    public void sendReqInsertOrder(String exchange_id, String instrument_id, String direction,
                                   String offset, int volume, String price_type, double price) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String user_id = DataManager.getInstance().USER_ID;
            String reqInsertOrder = "{\"aid\":\"insert_order\", \"user_id\":\"" + user_id + "\", " +
                    "\"order_id\":\"\",\"exchange_id\":\"" + exchange_id + "\",\"instrument_id\":\""
                    + instrument_id + "\",\"direction\":\"" + direction + "\",\"offset\":\"" + offset
                    + "\",\"volume\":" + volume + ",\"price_type\":\"" + price_type + "\",\"limit_price\":"
                    + price + ", \"volume_condition\":\"ANY\", \"time_condition\":\"GFD\"}";
            LogUtils.e(reqInsertOrder, true);
            mWebSocketClientTD.sendText(reqInsertOrder);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 撤单
     */
    public void sendReqCancelOrder(String order_id) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String user_id = DataManager.getInstance().USER_ID;
            String reqInsertOrder = "{\"aid\":\"cancel_order\", \"user_id\":\"" + user_id + "\"," +
                    "\"order_id\":\"" + order_id + "\"}";
            LogUtils.e(reqInsertOrder, true);
            mWebSocketClientTD.sendText(reqInsertOrder);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 银期转帐
     */
    public void sendReqTransfer(String future_account, String future_password, String bank_id,
                                String bank_password, String currency, float amount) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String reqTransfer = "{\"aid\":\"req_transfer\",\"future_account\":\"" + future_account
                    + "\",\"future_password\":\"" + future_password + "\",\"bank_id\":\"" + bank_id
                    + "\",\"bank_password\":\"" + bank_password + "\",\"currency\":\"" + currency +
                    "\",\"amount\": " + amount + "}";
            LogUtils.e(reqTransfer, true);
            mWebSocketClientTD.sendText(reqTransfer);
        }
    }

    public void sendReqPassword(String new_password, String old_password) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            String reqPassword = "{\"aid\":\"change_password\",\"new_password\":\"" + new_password
                    + "\",\"old_password\":\"" + old_password + "\"}";
            LogUtils.e(reqPassword, true);
            mWebSocketClientTD.sendText(reqPassword);
        }
    }

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }
}
