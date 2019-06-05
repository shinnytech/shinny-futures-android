package com.shinnytech.futures.model.engine;

import android.os.Handler;
import android.os.Looper;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.amplitude.api.Identify;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.BankEntity;
import com.shinnytech.futures.model.bean.accountinfobean.BrokerEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TradeBean;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.DiffEntity;
import com.shinnytech.futures.model.bean.futureinfobean.FutureBean;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.service.WebSocketService;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_USER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_NOTIFY_CODE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_NOTIFY_CONTENT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_NOTIFY_LEVEL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_NOTIFY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGIN_FAILED;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGIN_SUCCEEDED;
import static com.shinnytech.futures.constants.CommonConstants.AMP_NOTIFY;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_ACCOUNT_ID_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_ACCOUNT_ID_LAST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BROKER_ID_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BROKER_ID_LAST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_LOGIN_SUCCESS_TIME_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMNOW;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.CommonConstants.CHANGE_PASSWORD_SUCCEED;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_FAIL;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_CHANGE_SUCCESS;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_WEAK_PASSWORD;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST;

/**
 * date: 7/9/17
 * author: chenli
 * description: 服务器返回数据解析
 * version:
 * state: basically done
 */
public class DataManager {

    private static final DataManager INSTANCE = new DataManager();
    /**
     * date: 2018/11/7
     * description: appVersion
     */
    public String APP_VERSION = "";
    /**
     * date: 2018/11/7
     * description: appCode
     */
    public int APP_CODE = 0;
    /**
     * date: 2019/4/16
     * description: user_agent
     */
    public String USER_AGENT = "";
    /**
     * date: 2018/11/7
     * description: 用户下单价格类型
     */
    public String PRICE_TYPE = "对手";
    /**
     * date: 2018/12/13
     * description: 持仓点击方向
     */
    public String POSITION_DIRECTION = "";
    /**
     * date: 2019/3/18
     * description: 是否显示副图判断
     */
    public boolean IS_SHOW_VP_CONTENT = false;
    /**
     * date: 2019/5/11
     * description: 判断是否显示登录成功弹框
     */
    public boolean IS_SHOW_LOGIN_SUCCESS = false;
    /**
     * date: 2019/6/1
     * description: 登录入口
     */
    public String LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
    /**
     * date: 2019/6/1
     * description: 主动登录事件期货公司及账号
     */
    public String LOGIN_BROKER_ID = "";
    public String LOGIN_USER_ID = "";

    /**
     * date: 2019/5/24
     * description: 判断用户是否主动切换页面
     */
    public boolean IS_POSITIVE = false;
    /**
     * date: 2019/3/18
     * description: 用户最后一次发送的订阅请求
     */
    public String QUOTES = "";
    public String CHARTS = "";
    /**
     * date: 2019/4/19
     * description: 用户最后一次切换的交易所
     */
    public String EXCHANGE_ID = "";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    /**
     * date: 7/9/17
     * description: 账户信息实例
     */
    private TradeBean TRADE_DATA = new TradeBean();
    /**
     * date: 7/9/17
     * description: 单例模式，行情数据类实例
     */
    private FutureBean RTN_DATA = new FutureBean();
    /**
     * date: 7/9/17
     * description: 账户登录返回信息实例
     */
    private BrokerEntity BROKER = new BrokerEntity();

    private DataManager() {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static DataManager getInstance() {
        return INSTANCE;
    }

    /**
     * date: 6/16/17
     * author: chenli
     * description: 获取实例
     */
    public DiffEntity getRtnData() {
        return RTN_DATA.getData();
    }

    public TradeBean getTradeBean() {
        return TRADE_DATA;
    }

    public BrokerEntity getBroker() {
        return BROKER;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public void clearAccount() {
        LOGIN_USER_ID = "";
        TRADE_DATA = new TradeBean();
    }

    /**
     * date: 6/16/17
     * author: chenli
     * description: 刷新行情数据，K线数据
     */
    public void refreshFutureBean(JSONObject rtnData) {
        try {
            JSONArray dataArray = rtnData.getJSONArray("data");
            DiffEntity diffEntity = RTN_DATA.getData();
            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.isNull(i)) continue;
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key0 = iterator.next();
                    if (dataObject.isNull(key0)) continue;
                    switch (key0) {
                        case "quotes":
                            parseQuotes(dataObject, diffEntity);
                            break;
                        case "klines":
                            parseKlines(dataObject, diffEntity);
                            break;
                        case "ticks":
                            parseTicks(dataObject, diffEntity);
                            break;
                        case "charts":
                            parseCharts(dataObject, diffEntity);
                            break;
                        case "ins_list":
                            diffEntity.setIns_list(dataObject.optString(key0));
                            break;
                        case "mdhis_more_data":
                            diffEntity.setMdhis_more_data(dataObject.optBoolean(key0));
                            break;
                        default:
                            break;
                    }
                }
            }
            WebSocketService.sendMessage(MD_MESSAGE, MD_BROADCAST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseCharts(JSONObject dataObject, DiffEntity diffEntity) throws JSONException {
        JSONObject chartsObject = dataObject.getJSONObject("charts");
        Map<String, ChartEntity> chartsEntities = diffEntity.getCharts();
        Iterator<String> iterator = chartsObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (chartsObject.isNull(key)) continue;
            JSONObject chartObject = chartsObject.getJSONObject(key);
            ChartEntity chartEntity = chartsEntities.get(key);
            if (chartEntity == null) chartEntity = new ChartEntity();
            Class clChart = chartEntity.getClass();
            Iterator<String> iterator1 = chartObject.keys();
            Map<String, String> stateEntity = chartEntity.getState();
            while (iterator1.hasNext()) {
                String key1 = iterator1.next();
                if (chartObject.isNull(key1)) continue;
                if ("state".equals(key1)) {
                    JSONObject stateObject = chartObject.optJSONObject(key1);
                    Iterator<String> iterator511 = stateObject.keys();
                    while (iterator511.hasNext()) {
                        String key511 = iterator511.next();
                        stateEntity.put(key511, stateObject.optString(key511));
                    }
                } else {
                    try {
                        Field f = clChart.getDeclaredField(key1);
                        f.setAccessible(true);
                        f.set(chartEntity, chartObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            chartsEntities.put(key, chartEntity);
        }
    }

    private void parseTicks(JSONObject dataObject, DiffEntity diffEntity) throws JSONException {
//        JSONObject ticks = dataObject.getJSONObject("ticks");
//        Iterator<String> iterator = ticks.keys();
//        while (iterator.hasNext()) {
//            String key = iterator.next();
//        }
    }

    private void parseKlines(JSONObject dataObject, DiffEntity diffEntity) throws JSONException {
        JSONObject futureKlineObjects = dataObject.getJSONObject("klines");
        Map<String, Map<String, KlineEntity>> futureKlineEntities = diffEntity.getKlines();
        Iterator<String> iterator = futureKlineObjects.keys();
        while (iterator.hasNext()) {
            String key = iterator.next(); //future "cu1601"
            if (futureKlineObjects.isNull(key)) continue;
            JSONObject futureKlineObject = futureKlineObjects.getJSONObject(key);
            Iterator<String> iterator1 = futureKlineObject.keys();
            Map<String, KlineEntity> futureKlineEntity = futureKlineEntities.get(key);
            if (futureKlineEntity == null) futureKlineEntity = new HashMap<>();

            while (iterator1.hasNext()) {
                String key1 = iterator1.next(); //kline"M3"
                if (futureKlineObject.isNull(key1)) continue;
                JSONObject klineObject = futureKlineObject.getJSONObject(key1);
                KlineEntity klineEntity = futureKlineEntity.get(key1);
                if (klineEntity == null) klineEntity = new KlineEntity();

                Class clKline = klineEntity.getClass();
                Iterator<String> iterator2 = klineObject.keys();
                while (iterator2.hasNext()) {
                    String key2 = iterator2.next();
                    if (klineObject.isNull(key2)) continue;
                    switch (key2) {
                        case "data":
                            JSONObject dataObjects = klineObject.getJSONObject(key2);
                            Iterator<String> iterator3 = dataObjects.keys();
                            Map<String, KlineEntity.DataEntity> dataEntities = klineEntity.getData();
                            while (iterator3.hasNext()) {
                                String key3 = iterator3.next();
                                if (dataObjects.isNull(key3)) continue;
                                JSONObject dataObjectInner = dataObjects.getJSONObject(key3);
                                KlineEntity.DataEntity dataEntity = dataEntities.get(key3);
                                Iterator<String> iterator4 = dataObjectInner.keys();
                                if (dataEntity == null) dataEntity = new KlineEntity.DataEntity();

                                Class clData = dataEntity.getClass();
                                while (iterator4.hasNext()) {
                                    String key4 = iterator4.next();
                                    if (dataObjectInner.isNull(key4)) continue;
                                    try {
                                        Field f = clData.getDeclaredField(key4);
                                        f.setAccessible(true);
                                        f.set(dataEntity, dataObjectInner.optString(key4));
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                        continue;
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                        continue;
                                    }

                                }
                                dataEntities.put(key3, dataEntity);
                            }
                            break;
                        case "binding":
                            JSONObject bindingObjects = klineObject.getJSONObject("binding");
                            Iterator<String> iterator5 = bindingObjects.keys();
                            Map<String, KlineEntity.BindingEntity> bindingEntities = klineEntity.getBinding();
                            while (iterator5.hasNext()) {
                                String key5 = iterator5.next();
                                if (bindingObjects.isNull(key5)) continue;
                                JSONObject bindingObject = bindingObjects.getJSONObject(key5);
                                KlineEntity.BindingEntity bindingEntity = bindingEntities.get(key5);
                                Iterator<String> iterator6 = bindingObject.keys();
                                if (bindingEntity == null) {
                                    bindingEntity = new KlineEntity.BindingEntity();
                                }
                                while (iterator6.hasNext()) {
                                    String key6 = iterator6.next();
                                    if (bindingObject.isNull(key6)) continue;
                                    bindingEntity.getBindingData().put(key6, bindingObject.optString(key6));
                                }
                                bindingEntities.put(key5, bindingEntity);
                            }
                            break;
                        default:
                            try {
                                Field field = clKline.getDeclaredField(key2);
                                field.setAccessible(true);
                                field.set(klineEntity, klineObject.optString(key2));
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                                continue;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                continue;
                            }
                            break;
                    }
                }
                futureKlineEntity.put(key1, klineEntity);
            }
            futureKlineEntities.put(key, futureKlineEntity);
        }
    }

    private void parseQuotes(JSONObject dataObject, DiffEntity diffEntity) throws JSONException {
        JSONObject quoteObjects = dataObject.getJSONObject("quotes");
        Map<String, QuoteEntity> quoteEntities = diffEntity.getQuotes();
        Iterator<String> iterator = quoteObjects.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (quoteObjects.isNull(key)) continue;
            JSONObject quoteObject = quoteObjects.getJSONObject(key);
            Iterator<String> iterator1 = quoteObject.keys();
            QuoteEntity quoteEntity = quoteEntities.get(key);
            if (quoteEntity == null) quoteEntity = new QuoteEntity();
            Class clQuote = quoteEntity.getClass();
            while (iterator1.hasNext()) {
                String key1 = iterator1.next();
                if (quoteObject.isNull(key1)) continue;
                try {
                    Field f = clQuote.getDeclaredField(key1);
                    f.setAccessible(true);
                    f.set(quoteEntity, quoteObject.optString(key1));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            quoteEntities.put(key, quoteEntity);
        }

    }

    /**
     * date: 6/16/17
     * author: chenli
     * description: 刷新登录信息
     */
    public void refreshTradeBean(JSONObject accountBeanObject) {
        try {
            JSONArray dataArray = accountBeanObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.isNull(i)) continue;
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (dataObject.isNull(key)) continue;
                    JSONObject data = dataObject.getJSONObject(key);
                    switch (key) {
                        case "notify":
                            Iterator<String> notifyIterator = data.keys();
                            while (notifyIterator.hasNext()) {
                                String notifyKey = notifyIterator.next();
                                if (data.isNull(notifyKey)) continue;
                                JSONObject notify = data.getJSONObject(notifyKey);
                                final String content = notify.optString("content");
                                String type = notify.optString("type");
                                String level = notify.optString("level");
                                int code = notify.optInt("code");
                                JSONObject jsonObject1 = new JSONObject();
                                try {
                                    jsonObject1.put(AMP_EVENT_NOTIFY_CODE, code);
                                    jsonObject1.put(AMP_EVENT_NOTIFY_CONTENT, content);
                                    jsonObject1.put(AMP_EVENT_NOTIFY_LEVEL, level);
                                    jsonObject1.put(AMP_EVENT_NOTIFY_TYPE, type);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Amplitude.getInstance().logEvent(AMP_NOTIFY, jsonObject1);

                                if (content.equals(CHANGE_PASSWORD_SUCCEED)) {
                                    WebSocketService.sendMessage(TD_MESSAGE_CHANGE_SUCCESS, TD_BROADCAST);
                                }
                                if ((code == 140) || (code == 131)) {
                                    WebSocketService.sendMessage(TD_MESSAGE_WEAK_PASSWORD, TD_BROADCAST);
                                }
                                if ("SETTLEMENT".equals(type)) {
                                    LogUtils.e(content, true);
                                    BROKER.setSettlement(content);
                                    WebSocketService.sendMessage(TD_MESSAGE_SETTLEMENT, TD_BROADCAST);
                                } else {
                                    if (LOGIN_SUCCEED.equals(content)) {
                                        //游客模式不显示登录成功弹出框
                                        if (IS_SHOW_LOGIN_SUCCESS) IS_SHOW_LOGIN_SUCCESS = false;
                                        else continue;
                                    }
                                    if (LOGIN_FAIL.equals(content)) {
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, LOGIN_BROKER_ID);
                                            jsonObject.put(AMP_EVENT_LOGIN_USER_ID, LOGIN_USER_ID);
                                            jsonObject.put(AMP_EVENT_LOGIN_TIME, TimeUtils.getAmpTime());
                                            jsonObject.put(AMP_EVENT_LOGIN_TYPE, LOGIN_TYPE);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Amplitude.getInstance().logEvent(AMP_LOGIN_FAILED, jsonObject);
                                        WebSocketService.sendMessage(TD_MESSAGE_LOGIN_FAIL, TD_BROADCAST);
                                    }
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtils.showToast(BaseApplication.getContext(), content);
                                        }
                                    });
                                }
                            }
                            break;
                        case "trade":
                            Map<String, UserEntity> userEntities = TRADE_DATA.getUsers();
                            Iterator<String> tradeIterator = data.keys();
                            while (tradeIterator.hasNext()) {
                                String userKey = tradeIterator.next();
                                if (data.isNull(userKey)) continue;
                                JSONObject user = data.getJSONObject(userKey);
                                UserEntity userEntity = userEntities.get(userKey);
                                if (userEntity == null) userEntity = new UserEntity();
                                Iterator<String> tradeDataIterator = user.keys();
                                while (tradeDataIterator.hasNext()) {
                                    String tradeDataKey = tradeDataIterator.next();
                                    if (user.isNull(tradeDataKey)) continue;
                                    switch (tradeDataKey) {
                                        case "accounts":
                                            parseAccounts(user, userEntity);
                                            break;
                                        case "orders":
                                            parseOrders(user, userEntity);
                                            break;
                                        case "positions":
                                            parsePositions(user, userEntity);
                                            break;
                                        case "trades":
                                            parseTrades(user, userEntity);
                                            break;
                                        case "banks":
                                            parseBanks(user, userEntity);
                                            break;
                                        case "transfers":
                                            parseTransfers(user, userEntity);
                                            break;
                                        case "session":
                                            String userId = user.getJSONObject("session").optString("user_id");
                                            if (LOGIN_USER_ID.equals(userId)) {
                                                userEntity.setUser_id(userId);
                                                Amplitude.getInstance().setUserId(LOGIN_BROKER_ID + LOGIN_USER_ID);
                                                Identify identify = new Identify()
                                                        .setOnce(AMP_USER_ACCOUNT_ID_FIRST, LOGIN_USER_ID)
                                                        .set(AMP_USER_ACCOUNT_ID_LAST, LOGIN_USER_ID)
                                                        .setOnce(AMP_USER_BROKER_ID_FIRST, LOGIN_BROKER_ID)
                                                        .set(AMP_USER_BROKER_ID_LAST, LOGIN_BROKER_ID);
                                                if (!(BROKER_ID_SIMULATION.equals(LOGIN_BROKER_ID) || BROKER_ID_SIMNOW.equals(LOGIN_BROKER_ID))) {
                                                    long currentTime = System.currentTimeMillis();
                                                    long initTime = (long) SPUtils.get(BaseApplication.getContext(), CONFIG_INIT_TIME, currentTime);
                                                    long loginTime = currentTime - initTime;
                                                    identify.setOnce(AMP_USER_LOGIN_SUCCESS_TIME_FIRST, loginTime);
                                                }
                                                Amplitude.getInstance().identify(identify);

                                                JSONObject jsonObject = new JSONObject();
                                                try {
                                                    jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, LOGIN_BROKER_ID);
                                                    jsonObject.put(AMP_EVENT_LOGIN_USER_ID, LOGIN_USER_ID);
                                                    jsonObject.put(AMP_EVENT_LOGIN_TIME, TimeUtils.getAmpTime());
                                                    jsonObject.put(AMP_EVENT_LOGIN_TYPE, LOGIN_TYPE);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Amplitude.getInstance().logEvent(AMP_LOGIN_SUCCEEDED, jsonObject);
                                                WebSocketService.sendMessage(TD_MESSAGE_LOGIN_SUCCEED, TD_BROADCAST);
                                            }

                                            break;
                                        default:
                                            break;
                                    }
                                }
                                userEntities.put(userKey, userEntity);
                                WebSocketService.sendMessage(TD_MESSAGE, TD_BROADCAST);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTransfers(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject transferData = user.getJSONObject("transfers");
            Map<String, TransferEntity> transferEntities = userEntity.getTransfers();
            Iterator<String> transferIterator = transferData.keys();
            while (transferIterator.hasNext()) {
                String key = transferIterator.next();
                if (transferData.isNull(key)) continue;
                JSONObject transferObject = transferData.getJSONObject(key);
                Iterator<String> iterator1 = transferObject.keys();
                TransferEntity transferEntity = transferEntities.get(key);
                if (transferEntity == null) transferEntity = new TransferEntity();
                Class clTransfer = transferEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (transferObject.isNull(key1)) continue;
                    try {
                        Field f = clTransfer.getDeclaredField(key1);
                        f.setAccessible(true);
                        String data = transferObject.optString(key1);
                        f.set(transferEntity, data);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                transferEntity.setKey(key);
                transferEntities.put(key, transferEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseBanks(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject bankData = user.getJSONObject("banks");
            Map<String, BankEntity> bankEntities = userEntity.getBanks();
            Iterator<String> bankIterator = bankData.keys();
            while (bankIterator.hasNext()) {
                String key = bankIterator.next();
                if (bankData.isNull(key)) continue;
                JSONObject bankObject = bankData.getJSONObject(key);
                Iterator<String> iterator1 = bankObject.keys();
                BankEntity bankEntity = bankEntities.get(key);
                if (bankEntity == null) bankEntity = new BankEntity();
                Class clBank = bankEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (bankObject.isNull(key1)) continue;
                    try {
                        Field f = clBank.getDeclaredField(key1);
                        f.setAccessible(true);
                        String data = bankObject.optString(key1);
                        f.set(bankEntity, data);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                bankEntity.setKey(key);
                bankEntities.put(key, bankEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTrades(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject tradeData = user.getJSONObject("trades");
            Map<String, TradeEntity> tradeEntities = userEntity.getTrades();
            Iterator<String> tradeIterator = tradeData.keys();
            while (tradeIterator.hasNext()) {
                String key = tradeIterator.next();
                if (tradeData.isNull(key)) continue;
                JSONObject tradeObject = tradeData.getJSONObject(key);
                Iterator<String> iterator1 = tradeObject.keys();
                TradeEntity tradeEntity = tradeEntities.get(key);
                if (tradeEntity == null) tradeEntity = new TradeEntity();
                Class clTrade = tradeEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (tradeObject.isNull(key1)) continue;
                    try {
                        Field f = clTrade.getDeclaredField(key1);
                        f.setAccessible(true);
                        String data = tradeObject.optString(key1);
                        f.set(tradeEntity, data);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                tradeEntity.setKey(key);
                tradeEntities.put(key, tradeEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePositions(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject positionData = user.getJSONObject("positions");
            Map<String, PositionEntity> positionEntities = userEntity.getPositions();
            Iterator<String> positionIterator = positionData.keys();
            while (positionIterator.hasNext()) {
                String key = positionIterator.next();
                if (positionData.isNull(key)) continue;
                JSONObject positionObject = positionData.getJSONObject(key);
                Iterator<String> iterator1 = positionObject.keys();
                PositionEntity positionEntity = positionEntities.get(key);
                if (positionEntity == null) positionEntity = new PositionEntity();
                Class clPosition = positionEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (positionObject.isNull(key1)) continue;
                    try {
                        Field f = clPosition.getDeclaredField(key1);
                        f.setAccessible(true);
                        String data = positionObject.optString(key1);
                        f.set(positionEntity, data);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }


                }
                positionEntity.setKey(key);
                positionEntities.put(key, positionEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseOrders(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject orderData = user.getJSONObject("orders");
            final Map<String, OrderEntity> orderEntities = userEntity.getOrders();
            Iterator<String> orderIterator = orderData.keys();
            while (orderIterator.hasNext()) {
                String key = orderIterator.next();
                if (orderData.isNull(key)) continue;
                JSONObject orderObject = orderData.getJSONObject(key);
                Iterator<String> iterator1 = orderObject.keys();
                OrderEntity orderEntity = orderEntities.get(key);
                if (orderEntity == null) orderEntity = new OrderEntity();
                Class clOrder = orderEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (orderObject.isNull(key1)) continue;
                    try {
                        Field f = clOrder.getDeclaredField(key1);
                        f.setAccessible(true);
                        String data = orderObject.optString(key1);
                        f.set(orderEntity, data);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                orderEntity.setKey(key);
                orderEntities.put(key, orderEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAccounts(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject accountData = user.getJSONObject("accounts");
            Map<String, AccountEntity> accountEntities = userEntity.getAccounts();
            Iterator<String> accountIterator = accountData.keys();
            while (accountIterator.hasNext()) {
                String key = accountIterator.next();
                if (accountData.isNull(key)) continue;
                JSONObject accountObject = accountData.getJSONObject(key);
                AccountEntity accountEntity = accountEntities.get(key);
                if (accountEntity == null) accountEntity = new AccountEntity();
                Class clAccount = accountEntity.getClass();
                Iterator<String> iterator1 = accountObject.keys();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    if (accountObject.isNull(key1)) continue;
                    try {
                        Field f = clAccount.getDeclaredField(key1);
                        f.setAccessible(true);
                        f.set(accountEntity, accountObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                accountEntity.setKey(key);
                accountEntities.put(key, accountEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
