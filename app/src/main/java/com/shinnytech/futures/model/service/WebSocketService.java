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
import com.shinnytech.futures.model.bean.reqbean.ReqCancelOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConfirmSettlementEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqInsertOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqLoginEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqPasswordEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqPeekMessageEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartKlineEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSubscribeQuoteEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqTransferEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.constants.CommonConstants.CHART_ID;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
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
                    if (mWebSocketClientMD != null)mWebSocketClientMD.sendPing();
                }

                if ((System.currentTimeMillis() / 1000 - mTDLastPong) >= 20) {
                    sendMessage(TD_OFFLINE, TD_BROADCAST);
                } else {
                    if (mWebSocketClientTD != null)mWebSocketClientTD.sendPing();
                }

            }
        };
        timer.schedule(timerTask, 15000, 15000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                                        mWebSocketClientMD.sendPing();
                                        sendSubscribeAfterConnect();
                                        break;
                                    case "rtn_data":
                                        BaseApplication.setIndex(0);
                                        sDataManager.refreshFutureBean(jsonObject);
                                        break;
                                    default:
                                        sendMessage(MD_OFFLINE, MD_BROADCAST);
                                        return;
                                }
                                sendPeekMessage();
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

    /**
     * date: 2019/3/17
     * author: chenli
     * description: 首次连接行情服务器与断开重连的行情订阅处理
     */
    private void sendSubscribeAfterConnect() {
        if (!sDataManager.QUOTES.isEmpty() && mWebSocketClientMD != null){
            mWebSocketClientMD.sendText(sDataManager.QUOTES);
            LogUtils.e(sDataManager.QUOTES, true);
        }
        else if (LatestFileManager.getOptionalInsList().isEmpty())
            sendSubscribeQuote(TextUtils.join(",",
                    new ArrayList(LatestFileManager.getMainInsList().keySet()).subList(0, LOAD_QUOTE_NUM)));
        else {
            List<String> list = LatestFileManager.getCombineInsList(
                    new ArrayList<>(LatestFileManager.getOptionalInsList().keySet()));

            if (list.size() < LOAD_QUOTE_NUM) sendSubscribeQuote(TextUtils.join(",", list));
            else sendSubscribeQuote(TextUtils.join(",", list.subList(0, LOAD_QUOTE_NUM)));
        }

        if (!sDataManager.CHARTS.isEmpty() && mWebSocketClientMD != null){
            mWebSocketClientMD.sendText(sDataManager.CHARTS);
            LogUtils.e(sDataManager.CHARTS, true);
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
            ReqSubscribeQuoteEntity reqSubscribeQuoteEntity = new ReqSubscribeQuoteEntity();
            reqSubscribeQuoteEntity.setAid("subscribe_quote");
            reqSubscribeQuoteEntity.setIns_list(insList);
            String subScribeQuote = new Gson().toJson(reqSubscribeQuoteEntity);
            mWebSocketClientMD.sendText(subScribeQuote);
            sDataManager.QUOTES = subScribeQuote;
            LogUtils.e(subScribeQuote, true);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约信息
     */
    public void sendPeekMessage() {
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            ReqPeekMessageEntity reqPeekMessageEntity = new ReqPeekMessageEntity();
            reqPeekMessageEntity.setAid("peek_message");
            String peekMessage = new Gson().toJson(reqPeekMessageEntity);
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
            ReqSetChartEntity reqSetChartEntity = new ReqSetChartEntity();
            reqSetChartEntity.setAid("set_chart");
            reqSetChartEntity.setChart_id(CHART_ID);
            reqSetChartEntity.setIns_list(ins_list);
            reqSetChartEntity.setDuration(60000000000l);
            reqSetChartEntity.setTrading_day_start(0);
            reqSetChartEntity.setTrading_day_count(86400000000000l);
            String setChart = new Gson().toJson(reqSetChartEntity);
            sDataManager.CHARTS = setChart;
            mWebSocketClientMD.sendText(setChart);
            LogUtils.e(setChart, true);
        }
    }

    /**
     * date: 2018/12/14
     * author: chenli
     * description: k线图
     */
    public void sendSetChartKline(String ins_list, int view_width, String duration){
        if (mWebSocketClientMD != null && mWebSocketClientMD.getState() == WebSocketState.OPEN) {
            try {
                long duration_l = Long.parseLong(duration);
                ReqSetChartKlineEntity reqSetChartKlineEntity = new ReqSetChartKlineEntity();
                reqSetChartKlineEntity.setAid("set_chart");
                reqSetChartKlineEntity.setChart_id(CHART_ID);
                reqSetChartKlineEntity.setIns_list(ins_list);
                reqSetChartKlineEntity.setView_width(view_width);
                reqSetChartKlineEntity.setDuration(duration_l);
                String setChart = new Gson().toJson(reqSetChartKlineEntity);
                sDataManager.CHARTS = setChart;
                mWebSocketClientMD.sendText(setChart);
                LogUtils.e(setChart, true);
            }catch (Exception e){
                e.printStackTrace();
            }
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
//                            LatestFileManager.writeFile("TDData", message);

                            try {
                                JSONObject jsonObject = new JSONObject(message);
                                String aid = jsonObject.getString("aid");
                                switch (aid) {
                                    case "rtn_brokers":
                                        mWebSocketClientTD.sendPing();
                                        loginConfig(message);
                                        break;
                                    case "rtn_data":
                                        sDataManager.refreshTradeBean(jsonObject);
                                        break;
                                    default:
                                        sendMessage(TD_OFFLINE, TD_BROADCAST);
                                        return;
                                }
                                sendPeekMessageTransaction();
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

    /**
     * date: 2019/3/17
     * author: chenli
     * description: 登录设置，自动登录
     */
    private void loginConfig(String message){
        BrokerEntity brokerInfo = new Gson().fromJson(message, BrokerEntity.class);
        sDataManager.getBroker().setBrokers(brokerInfo.getBrokers());
        sendMessage(TD_MESSAGE_BROKER_INFO, TD_BROADCAST);

        Context context = BaseApplication.getContext();
        if (SPUtils.contains(context, CommonConstants.CONFIG_LOGIN_DATE)){
            String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
            if (TimeUtils.getNowTime().equals(date)){
                String name = (String) SPUtils.get(context, CommonConstants.CONFIG_ACCOUNT, "");
                String password = (String) SPUtils.get(context, CommonConstants.CONFIG_PASSWORD, "");
                String broker = (String) SPUtils.get(context, CommonConstants.CONFIG_BROKER, "");
                sendReqLogin(broker, name, password);
            }
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
            ReqPeekMessageEntity reqPeekMessageEntity = new ReqPeekMessageEntity();
            reqPeekMessageEntity.setAid("peek_message");
            String peekMessage = new Gson().toJson(reqPeekMessageEntity);
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
            ReqLoginEntity reqLoginEntity = new ReqLoginEntity();
            reqLoginEntity.setAid("req_login");
            reqLoginEntity.setBid(bid);
            reqLoginEntity.setUser_name(user_name);
            reqLoginEntity.setPassword(password);
            String reqLogin = new Gson().toJson(reqLoginEntity);
            mWebSocketClientTD.sendText(reqLogin);
            LogUtils.e(reqLogin, true);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 确认结算单
     */
    public void sendReqConfirmSettlement() {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            ReqConfirmSettlementEntity reqConfirmSettlementEntity = new ReqConfirmSettlementEntity();
            reqConfirmSettlementEntity.setAid("confirm_settlement");
            String confirmSettlement = new Gson().toJson(reqConfirmSettlementEntity);
            mWebSocketClientTD.sendText(confirmSettlement);
            LogUtils.e(confirmSettlement, true);
            LogUtils.w2f(confirmSettlement);
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
            ReqInsertOrderEntity reqInsertOrderEntity = new ReqInsertOrderEntity();
            reqInsertOrderEntity.setAid("insert_order");
            reqInsertOrderEntity.setUser_id(user_id);
            reqInsertOrderEntity.setOrder_id("");
            reqInsertOrderEntity.setExchange_id(exchange_id);
            reqInsertOrderEntity.setInstrument_id(instrument_id);
            reqInsertOrderEntity.setDirection(direction);
            reqInsertOrderEntity.setOffset(offset);
            reqInsertOrderEntity.setVolume(volume);
            reqInsertOrderEntity.setPrice_type(price_type);
            reqInsertOrderEntity.setLimit_price(price);
            reqInsertOrderEntity.setVolume_condition("ANY");
            reqInsertOrderEntity.setTime_condition("GFD");
            String reqInsertOrder = new Gson().toJson(reqInsertOrderEntity);
            mWebSocketClientTD.sendText(reqInsertOrder);
            LogUtils.w2f(reqInsertOrder);
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
            ReqCancelOrderEntity reqCancelOrderEntity = new ReqCancelOrderEntity();
            reqCancelOrderEntity.setAid("cancel_order");
            reqCancelOrderEntity.setUser_id(user_id);
            reqCancelOrderEntity.setOrder_id(order_id);
            String reqInsertOrder = new Gson().toJson(reqCancelOrderEntity);
            mWebSocketClientTD.sendText(reqInsertOrder);
            LogUtils.e(reqInsertOrder, true);
            LogUtils.w2f(reqInsertOrder);
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
            ReqTransferEntity reqTransferEntity = new ReqTransferEntity();
            reqTransferEntity.setAid("req_transfer");
            reqTransferEntity.setFuture_account(future_account);
            reqTransferEntity.setFuture_password(future_password);
            reqTransferEntity.setBank_id(bank_id);
            reqTransferEntity.setBank_password(bank_password);
            reqTransferEntity.setCurrency(currency);
            reqTransferEntity.setAmount(amount);
            String reqTransfer = new Gson().toJson(reqTransferEntity);
            mWebSocketClientTD.sendText(reqTransfer);
            LogUtils.e(reqTransfer, true);
            LogUtils.w2f(reqTransfer);
        }
    }

    /**
     * date: 2019/1/3
     * author: chenli
     * description: 修改密码
     */
    public void sendReqPassword(String new_password, String old_password) {
        if (mWebSocketClientTD != null && mWebSocketClientTD.getState() == WebSocketState.OPEN) {
            ReqPasswordEntity reqPasswordEntity = new ReqPasswordEntity();
            reqPasswordEntity.setAid("change_password");
            reqPasswordEntity.setNew_password(new_password);
            reqPasswordEntity.setOld_password(old_password);
            String reqPassword = new Gson().toJson(reqPasswordEntity);
            mWebSocketClientTD.sendText(reqPassword);
            LogUtils.e(reqPassword, true);
            LogUtils.w2f(reqPassword);
        }
    }

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }
}
