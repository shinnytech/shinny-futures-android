package com.shinnytech.futures.model.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY;
import static com.shinnytech.futures.constants.CommonConstants.ERROR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.LOAD_QUOTE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.OPEN;
import static com.shinnytech.futures.constants.CommonConstants.SWITCH;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;
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
    public static final String BROADCAST = "BROADCAST";

    /**
     * date: 7/9/17
     * description: 交易广播类型
     */
    public static final String BROADCAST_TRANSACTION = "BROADCAST_TRANSACTION";

    /**
     * date: 7/9/17
     * description: 行情广播信息
     */
    public static final String BROADCAST_ACTION = WebSocketService.class.getName() + ".BROADCAST";

    /**
     * date: 7/9/17
     * description: 交易广播信息
     */
    public static final String BROADCAST_ACTION_TRANSACTION = WebSocketService.class.getName() + ".TRANSACTION.BROADCAST";

    private static final int TIMEOUT = 5000;

    private final IBinder binder = new LocalBinder();

    private WebSocket webSocketClient;

    private WebSocket webSocketClientTransaction;

    private DataManager sDataManager = DataManager.getInstance();

    private LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    public WebSocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
            case BROADCAST:
                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra("msg", message);
                mLocalBroadcastManager.sendBroadcast(intent);
                break;
            case BROADCAST_TRANSACTION:
                Intent intentTransaction = new Intent(BROADCAST_ACTION_TRANSACTION);
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
    public void connect(String url) {
        try {
            webSocketClient = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(url)
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                            sendMessage(OPEN, BROADCAST);
                            LogUtils.e("行情服务器打开", true);
                        }

                        // A text message arrived from the server.
                        public void onTextMessage(WebSocket websocket, String message) {
                            LogUtils.e(message, false);
                            try {
                                JSONObject jsonObject = new JSONObject(message);
                                String aid = jsonObject.getString("aid");
                                switch (aid) {
                                    case "rsp_login":
                                        //首次连接行情服务器与断开重连的行情订阅处理
                                        String ins_list = sDataManager.getRtnData().getIns_list();
                                        if (ins_list != null) sendSubscribeQuote(ins_list);
                                        else
                                            sendSubscribeQuote(TextUtils.join(",", new ArrayList(LatestFileManager.getMainInsList().keySet()).subList(0, LOAD_QUOTE_NUM)));

                                        Map<String, ChartEntity> chartEntityMap = sDataManager.getRtnData().getCharts();
                                        if (chartEntityMap.size() != 0) {
                                            for (String key :
                                                    chartEntityMap.keySet()) {
                                                ChartEntity chartEntity = chartEntityMap.get(key);
                                                String duration = chartEntity.getState().get("duration");
                                                String ins = chartEntity.getState().get("ins_list");
                                                switch (duration) {
                                                    case CURRENT_DAY:
                                                        sendSetChart(ins);
                                                        break;
                                                    case KLINE_DAY:
                                                        sendSetChartDay(ins, VIEW_WIDTH);
                                                        break;
                                                    case KLINE_HOUR:
                                                        sendSetChartHour(ins, VIEW_WIDTH);
                                                        break;
                                                    case KLINE_MINUTE:
                                                        sendSetChartMin(ins, VIEW_WIDTH);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }
                                        break;
                                    case "rtn_data":
                                        BaseApplicationLike.setIndex(0);
                                        sDataManager.refreshFutureBean(jsonObject);
                                        break;
                                    default:
                                        sendMessage(SWITCH, BROADCAST);
                                        break;
                                }
                                sendPeekMessage();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                            sendMessage(CLOSE, BROADCAST);
                            LogUtils.e("onDisconnected", true);
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            ByteArrayOutputStream error = new ByteArrayOutputStream();
                            cause.printStackTrace(new PrintStream(error));
                            String exception = error.toString();
                            sendMessage(ERROR, BROADCAST);
                            LogUtils.e(exception, true);
                        }
                    })
                    .addHeader("User-Agent", "shinnyfutures-Android")
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connectAsynchronously();
            BaseApplicationLike.setIndex(BaseApplicationLike.getIndex() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disConnect() {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 行情订阅
     */
    public void sendSubscribeQuote(String insList) {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String subScribeQuote = "{\"aid\":\"subscribe_quote\",\"ins_list\":\"" + insList + "\"}";
            LogUtils.e(subScribeQuote, true);
            webSocketClient.sendText(subScribeQuote);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约信息
     */
    public void sendPeekMessage() {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String peekMessage = "{\"aid\":\"peek_message\"}";
            webSocketClient.sendText(peekMessage);
            LogUtils.e("PeekMessage", false);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 分时图
     */
    public void sendSetChart(String ins_list) {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + CURRENT_DAY + "\",\"ins_list\":\"" + ins_list + "\",\"duration\":\"60000000000\",\"trading_day_start\":\"0\",\"trading_day_count\":\"86400000000000\"}";
            LogUtils.e(setChart, true);
            webSocketClient.sendText(setChart);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 日线
     */
    public void sendSetChartDay(String ins_list, int view_width) {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + KLINE_DAY + "\",\"ins_list\":\"" + ins_list + "\",\"duration\":\"86400000000000\",\"view_width\":\"" + view_width + "\"}";
            LogUtils.e(setChart, true);
            webSocketClient.sendText(setChart);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 小时线
     */
    public void sendSetChartHour(String ins_list, int view_width) {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + KLINE_HOUR + "\",\"ins_list\":\"" + ins_list + "\",\"duration\":\"3600000000000\",\"view_width\":\"" + view_width + "\"}";
            LogUtils.e(setChart, true);
            webSocketClient.sendText(setChart);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 分钟线
     */
    public void sendSetChartMin(String ins_list, int view_width) {
        if (webSocketClient != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String setChart = "{\"aid\":\"set_chart\",\"chart_id\":\"" + CommonConstants.KLINE_MINUTE + "\",\"ins_list\":\"" + ins_list + "\",\"duration\":\"300000000000\",\"view_width\":\"" + view_width + "\"}";
            LogUtils.e(setChart, true);
            webSocketClient.sendText(setChart);
        }
    }

    /**
     * date: 6/28/17
     * author: chenli
     * description: 连接交易服务器
     */
    public void connectTransaction() {
        try {
            webSocketClientTransaction = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(TRANSACTION_URL)
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                            sendMessage(OPEN, BROADCAST_TRANSACTION);
                            LogUtils.e("交易服务器打开", true);
                        }

                        // A text message arrived from the server.
                        public void onTextMessage(WebSocket websocket, String message) {
                            LogUtils.e(message, true);
                            try {
                                sDataManager.refreshAccountBean(message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                            sendMessage(CLOSE, BROADCAST_TRANSACTION);
                            LogUtils.e("onDisconnected", true);
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            cause.printStackTrace();
                            sendMessage(ERROR, BROADCAST_TRANSACTION);
                        }
                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .addHeader("User-Agent", "shinnyfutures-Android")
                    .connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disConnectTransaction() {
        if (webSocketClientTransaction != null && webSocketClient.getState() == WebSocketState.OPEN) {
            webSocketClientTransaction.disconnect();
            webSocketClientTransaction = null;
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 用户登录
     */
    public void sendReqLogin(String bid, String user_name, String password) {
        if (webSocketClientTransaction != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String reqLogin = "{\"aid\":\"req_login\",\"bid\":\"" + bid + "\",\"user_name\":\"" + user_name + "\",\"password\":\"" + password + "\"}";
            LogUtils.e(reqLogin, true);
            webSocketClientTransaction.sendText(reqLogin);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 确认结算单
     */
    public void sendReqConfirmSettlement(String req_id, String msg) {
        if (webSocketClientTransaction != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String confirmSettlement = "{\"aid\":\"MobileConfirmSettlement\",\"req_id\":\"" + req_id + "\",\"msg\":\"" + msg + "\"}";
            LogUtils.e(confirmSettlement, true);
            webSocketClientTransaction.sendText(confirmSettlement);
        }
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 下单
     */
    public void sendReqInsertOrder(String order_id, String exchange_id, String instrument_id, String direction, String offset, int volume, String price_type, double price) {
        if (webSocketClientTransaction != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String reqInsertOrder = "{\"aid\":\"insert_order\",\"order_id\":\"" + order_id + "\",\"exchange_id\":\"" + exchange_id + "\",\"instrument_id\":\"" + instrument_id + "\",\"direction\":\"" + direction + "\",\"offset\":\"" + offset + "\",\"volume\":" + volume + ",\"price_type\":\"" + price_type + "\",\"limit_price\":" + price + "}";
            LogUtils.e(reqInsertOrder, true);
            webSocketClientTransaction.sendText(reqInsertOrder);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 撤单
     */
    public void sendReqCancelOrder(String order_id) {
        if (webSocketClientTransaction != null && webSocketClient.getState() == WebSocketState.OPEN) {
            String reqInsertOrder = "{\"aid\":\"cancel_order\",\"order_id\":\"" + order_id + "\"}";
            LogUtils.e(reqInsertOrder, true);
            webSocketClientTransaction.sendText(reqInsertOrder);
        }
    }

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }
}
