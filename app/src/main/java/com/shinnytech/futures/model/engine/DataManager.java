package com.shinnytech.futures.model.engine;

import android.os.Handler;
import android.os.Looper;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.amplitude.api.Identify;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.BankEntity;
import com.shinnytech.futures.model.bean.accountinfobean.BrokerEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TradeBean;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.COrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderBean;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.DiffEntity;
import com.shinnytech.futures.model.bean.futureinfobean.FutureBean;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.shinnytech.futures.application.BaseApplication.CO_BROADCAST;
import static com.shinnytech.futures.application.BaseApplication.MD_BROADCAST;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST;
import static com.shinnytech.futures.application.BaseApplication.sendMessage;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_FAILED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_SUCCEED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_FAIL_REASON;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_NOTIFY_CONTENT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_LOGIN_FAILED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_LOGIN_SUCCEEDED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_NOTIFY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_ACCOUNT_ID_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_ACCOUNT_ID_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BROKER_ID_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BROKER_ID_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_LOGIN_SUCCESS_TIME_FIRST;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_TIMEOUT;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMNOW;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CHANGE_PASSWORD_INIT;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CHANGE_PASSWORD_SUCCEED;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CHANGE_PASSWORD_WEAK;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CHECK_UNREADY_CTP;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CONDITION_FAIL_LEFT;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CONDITION_FAIL_RIGHT;
import static com.shinnytech.futures.constants.ServerConstants.CODE_CONDITION_SUCCEED;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_FAIL_CTP_LEFT;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_FAIL_CTP_RIGHT;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_FAIL_SIMULATOR;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_PASSWORD_MISMATCH_CTP;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_PASSWORD_MISMATCH_SIMULATOR;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_SUCCEED_CTP;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_SUCCEED_SIMULATOR;
import static com.shinnytech.futures.constants.ServerConstants.CODE_LOGIN_TIMEOUT_CTP;
import static com.shinnytech.futures.constants.ServerConstants.CODE_SETTLEMENT;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_CONDITION_LIST;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_CONDITION_ORDERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_HIS_CONDITION_ORDERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_ORDER_ID;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_ORDER_LIST;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_RTN_CONDITION_ORDERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_TRADING_DAY;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_CONDITION_KEY_USER_ID;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_CHARTS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_DATA;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_INS_LIST;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_KLINES;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_KLINES_BINDING;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_KLINES_DATA;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_MDHIS_MORE_DATA;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_QUOTES;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_CHARTS_STATE;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_TICKS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_CODE;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_CONTENT;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_DATA;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_LEVEL;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_NOTIFY;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_TRADE;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_ACCOUNTS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_BANKS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_ORDERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_POSITIONS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_SESSION;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_TRADES;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_TRANSFERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_USER_ID;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_HIS_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_CHANGE_SUCCESS;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_WEAK_PASSWORD;

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
    public String USER_AGENT = "android-github";
    /**
     * date: 2019/6/24
     * description: client_app_id
     */
    public String CLIENT_APP_ID = "android-github";
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
     * date: 2019/6/1
     * description: 登录入口
     */
    public String LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
    /**
     * date: 2019/6/1
     * description: 期货公司及账号
     */
    public String BROKER_ID = "";
    public String USER_ID = "";

    /**
     * date: 2019/7/4
     * description: 页面来源
     */
    public String SOURCE = "";
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

    /**
     * date: 2019/7/4
     * description: 进程开启时间
     */
    public long INIT_TIME = 0;

    /**
     * date: 2019/7/4
     * description: 上一个事件时间
     */
    public long LAST_TIME = 0;

    /**
     * date: 2019/7/4
     * description: 切前台时间
     */
    public long FOREGROUND_TIME = 0;

    /**
     * date: 2019/7/4
     * description: 本事件次序
     */
    public Map<String, Long> COUNT = new HashMap<>();

    /**
     * date: 2019/7/4
     * description: 行情重连次数
     */
    public int MD_SESSION = 0;

    /**
     * date: 2019/7/4
     * description: 交易重连次数
     */
    public int TD_SESSION = 0;

    /**
     * date: 2019/7/4
     * description: 行情收包数
     */
    public int MD_PACK_COUNT = 0;

    /**
     * date: 2019/7/4
     * description: 交易收包数
     */
    public int TD_PACK_COUNT = 0;

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
     * date: 2019/8/8
     * description: 条件单信息
     */
    private ConditionOrderBean CONDITION_ORDER_DATA = new ConditionOrderBean();
    /**
     * date: 2019/8/12
     * description: 历史条件单
     */
    private ConditionOrderBean CONDITION_ORDER_HIS_DATA = new ConditionOrderBean();
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

    public ConditionOrderBean getConditionOrderBean() {
        return CONDITION_ORDER_DATA;
    }

    public BrokerEntity getBroker() {
        return BROKER;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public ConditionOrderBean getHisConditionOrderBean() {
        return CONDITION_ORDER_HIS_DATA;
    }

    /**
     * date: 6/16/17
     * author: chenli
     * description: 刷新行情数据，K线数据
     */
    public void refreshFutureBean(JSONObject rtnData) {
        try {
            JSONArray dataArray = rtnData.getJSONArray(PARSE_MARKET_KEY_DATA);
            DiffEntity diffEntity = RTN_DATA.getData();
            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.isNull(i)) continue;
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key0 = iterator.next();
                    if (dataObject.isNull(key0)) continue;
                    switch (key0) {
                        case PARSE_MARKET_KEY_QUOTES:
                            parseQuotes(dataObject, diffEntity);
                            break;
                        case PARSE_MARKET_KEY_KLINES:
                            parseKlines(dataObject, diffEntity);
                            break;
                        case PARSE_MARKET_KEY_TICKS:
                            parseTicks(dataObject, diffEntity);
                            break;
                        case PARSE_MARKET_KEY_CHARTS:
                            parseCharts(dataObject, diffEntity);
                            break;
                        case PARSE_MARKET_KEY_INS_LIST:
                            diffEntity.setIns_list(dataObject.optString(key0));
                            break;
                        case PARSE_MARKET_KEY_MDHIS_MORE_DATA:
                            diffEntity.setMdhis_more_data(dataObject.optBoolean(key0));
                            break;
                        default:
                            break;
                    }
                }
            }
            sendMessage(MD_MESSAGE, MD_BROADCAST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseCharts(JSONObject dataObject, DiffEntity diffEntity) throws JSONException {
        JSONObject chartsObject = dataObject.getJSONObject(PARSE_MARKET_KEY_CHARTS);
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
                if (PARSE_MARKET_KEY_CHARTS_STATE.equals(key1)) {
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
        JSONObject futureKlineObjects = dataObject.getJSONObject(PARSE_MARKET_KEY_KLINES);
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
                        case PARSE_MARKET_KEY_KLINES_DATA:
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
                        case PARSE_MARKET_KEY_KLINES_BINDING:
                            JSONObject bindingObjects = klineObject.getJSONObject(PARSE_MARKET_KEY_KLINES_BINDING);
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
        JSONObject quoteObjects = dataObject.getJSONObject(PARSE_MARKET_KEY_QUOTES);
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
            JSONArray dataArray = accountBeanObject.getJSONArray(PARSE_TRADE_KEY_DATA);
            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.isNull(i)) continue;
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (dataObject.isNull(key)) continue;
                    JSONObject data = dataObject.getJSONObject(key);
                    switch (key) {
                        case PARSE_TRADE_KEY_NOTIFY:
                            parseNotify(data);
                            break;
                        case PARSE_TRADE_KEY_TRADE:
                            parseTrade(data);
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

    private void parseNotify(JSONObject data){
        try {
            Iterator<String> notifyIterator = data.keys();
            while (notifyIterator.hasNext()) {
                String notifyKey = notifyIterator.next();
                if (data.isNull(notifyKey)) continue;
                JSONObject notify = data.getJSONObject(notifyKey);
                final String content = notify.optString(PARSE_TRADE_KEY_CONTENT);
                String level = notify.optString(PARSE_TRADE_KEY_LEVEL);
                int code = notify.optInt(PARSE_TRADE_KEY_CODE);

                //warning通知上报
                if ("WARNING".equals(level)){
                    JSONObject jsonObject1 = new JSONObject();
                    try {
                        jsonObject1.put(AMP_EVENT_NOTIFY_CONTENT, content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Amplitude.getInstance().logEventWrap(AMP_NOTIFY, jsonObject1);
                }

                //条件单已被服务器拒绝
                if (code >= CODE_CONDITION_FAIL_LEFT && code <= CODE_CONDITION_FAIL_RIGHT){
                    Amplitude.getInstance().logEventWrap(AMP_CONDITION_FAILED, new JSONObject());
                }

                //条件单下单成功
                if (code == CODE_CONDITION_SUCCEED){
                    Amplitude.getInstance().logEventWrap(AMP_CONDITION_SUCCEED, new JSONObject());
                }

                //修改密码成功通知
                if (code == CODE_CHANGE_PASSWORD_SUCCEED) {
                    sendMessage(TD_MESSAGE_CHANGE_SUCCESS, TD_BROADCAST);
                }
                //首次登录修改密码和弱密码提示
                if ((code == CODE_CHANGE_PASSWORD_INIT) || (code == CODE_CHANGE_PASSWORD_WEAK)) {
                    sendMessage(TD_MESSAGE_WEAK_PASSWORD, TD_BROADCAST);
                }

                //实盘登陆失败
                if ((code >= CODE_LOGIN_FAIL_CTP_LEFT && code <= CODE_LOGIN_FAIL_CTP_RIGHT) ||
                        code == CODE_LOGIN_FAIL_SIMULATOR || code == CODE_LOGIN_PASSWORD_MISMATCH_CTP
                        || code == CODE_LOGIN_PASSWORD_MISMATCH_SIMULATOR){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(AMP_EVENT_LOGIN_TYPE, LOGIN_TYPE);
                        jsonObject.put(AMP_EVENT_LOGIN_FAIL_REASON, content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Amplitude.getInstance().logEventWrap(AMP_LOGIN_FAILED, jsonObject);
                    sendMessage(TD_MESSAGE_LOGIN_FAIL, TD_BROADCAST);
                }

                //实盘登陆超时
                if (code == CODE_LOGIN_TIMEOUT_CTP ){
                    sendMessage(TD_MESSAGE_LOGIN_TIMEOUT, TD_BROADCAST);
                }

                //游客模式和自动登录不弹出登陆成功提示
                if ((CODE_LOGIN_SUCCEED_CTP == code || CODE_LOGIN_SUCCEED_SIMULATOR == code) &&
                        !LOGIN_TYPE.equals(AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN)) continue;

                //实盘不提示"用户登录失败!"，显示ctp给的提示
                if (code >= CODE_LOGIN_FAIL_CTP_LEFT && code <= CODE_LOGIN_FAIL_CTP_RIGHT)continue;

                //CTP查询未就绪不显示
                if (code == CODE_CHECK_UNREADY_CTP)continue;

                //结算单
                if (code == CODE_SETTLEMENT) {
                    BROKER.setSettlement(content);
                    BROKER.setConfirmed(false);
                    sendMessage(TD_MESSAGE_SETTLEMENT, TD_BROADCAST);
                    continue;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(BaseApplication.getContext(), content);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTrade(JSONObject data){
        try {
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
                        case PARSE_TRADE_KEY_ACCOUNTS:
                            parseAccounts(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_ORDERS:
                            parseOrders(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_POSITIONS:
                            parsePositions(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_TRADES:
                            parseTrades(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_BANKS:
                            parseBanks(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_TRANSFERS:
                            parseTransfers(user, userEntity);
                            break;
                        case PARSE_TRADE_KEY_SESSION:
                            String userId = user.getJSONObject(PARSE_TRADE_KEY_SESSION).
                                    optString(PARSE_TRADE_KEY_USER_ID);
                            if (USER_ID.equals(userId)) {
                                userEntity.setUser_id(userId);
                                Identify identify = new Identify()
                                        .setOnce(AMP_USER_ACCOUNT_ID_FIRST, USER_ID)
                                        .set(AMP_USER_ACCOUNT_ID_LAST, USER_ID)
                                        .setOnce(AMP_USER_BROKER_ID_FIRST, BROKER_ID)
                                        .set(AMP_USER_BROKER_ID_LAST, BROKER_ID);
                                if (!(BROKER_ID_SIMULATION.equals(BROKER_ID) || BROKER_ID_SIMNOW.equals(BROKER_ID))) {
                                    long currentTime = System.currentTimeMillis();
                                    long initTime = (long) SPUtils.get(BaseApplication.getContext(), CONFIG_INIT_TIME, currentTime);
                                    long loginTime = currentTime - initTime;
                                    identify.setOnce(AMP_USER_LOGIN_SUCCESS_TIME_FIRST, loginTime);
                                }
                                Amplitude.getInstance().identify(identify);

                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put(AMP_EVENT_LOGIN_TYPE, LOGIN_TYPE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Amplitude.getInstance().logEventWrap(AMP_LOGIN_SUCCEEDED, jsonObject);
                                sendMessage(TD_MESSAGE_LOGIN_SUCCEED, TD_BROADCAST);
                            }
                            break;
                        default:
                            break;
                    }
                }
                userEntities.put(userKey, userEntity);
                sendMessage(TD_MESSAGE, TD_BROADCAST);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void parseTransfers(JSONObject user, UserEntity userEntity) {
        try {
            JSONObject transferData = user.getJSONObject(PARSE_TRADE_KEY_TRANSFERS);
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
            JSONObject bankData = user.getJSONObject(PARSE_TRADE_KEY_BANKS);
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
            JSONObject tradeData = user.getJSONObject(PARSE_TRADE_KEY_TRADES);
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
            JSONObject positionData = user.getJSONObject(PARSE_TRADE_KEY_POSITIONS);
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
//                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
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
            JSONObject orderData = user.getJSONObject(PARSE_TRADE_KEY_ORDERS);
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
            JSONObject accountData = user.getJSONObject(PARSE_TRADE_KEY_ACCOUNTS);
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

    /**
     * date: 2019/8/8
     * author: chenli
     * description: 刷新条件单信息
     */
    public void refreshConditionOrderBean(JSONObject conditionOrderBeanObject, String aid) {
        try {
            JSONArray dataArray;
            String user_id = conditionOrderBeanObject.optString(PARSE_CONDITION_KEY_USER_ID);
            String trading_day = conditionOrderBeanObject.optString(PARSE_CONDITION_KEY_TRADING_DAY);
            ConditionUserEntity conditionUserEntity;
            if (aid.equals(PARSE_CONDITION_KEY_RTN_CONDITION_ORDERS)){
                CONDITION_ORDER_DATA.setUser_id(user_id);
                CONDITION_ORDER_DATA.setTrading_day(trading_day);
                dataArray = conditionOrderBeanObject.getJSONArray(PARSE_CONDITION_KEY_CONDITION_ORDERS);
                conditionUserEntity = CONDITION_ORDER_DATA.getUsers().get(user_id);
            }else {
                CONDITION_ORDER_HIS_DATA.setUser_id(user_id);
                CONDITION_ORDER_HIS_DATA.setTrading_day(trading_day);
                dataArray = conditionOrderBeanObject.getJSONArray(PARSE_CONDITION_KEY_HIS_CONDITION_ORDERS);
                conditionUserEntity = CONDITION_ORDER_HIS_DATA.getUsers().get(user_id);
            }
            if (conditionUserEntity == null)conditionUserEntity = new ConditionUserEntity();
            for (int i = 0; i < dataArray.length(); i++) {
                if (dataArray.isNull(i)) continue;
                JSONObject dataObject = dataArray.getJSONObject(i);
                String order_id = dataObject.optString(PARSE_CONDITION_KEY_ORDER_ID);
                ConditionOrderEntity conditionOrderEntity = conditionUserEntity.getCondition_orders().get(order_id);
                if (conditionOrderEntity == null) conditionOrderEntity = new ConditionOrderEntity();
                Class clConditionOrder = conditionOrderEntity.getClass();
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (dataObject.isNull(key)) continue;
                    switch (key) {
                        case PARSE_CONDITION_KEY_CONDITION_LIST:
                            parseConditionList(conditionOrderEntity, dataObject, key);
                            break;
                        case PARSE_CONDITION_KEY_ORDER_LIST:
                            parseOrderList(conditionOrderEntity, dataObject, key);
                            break;
                        default:
                            try {
                                Field f = clConditionOrder.getDeclaredField(key);
                                f.setAccessible(true);
                                f.set(conditionOrderEntity, dataObject.optString(key));
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
                conditionUserEntity.getCondition_orders().put(order_id, conditionOrderEntity);
            }
            if (aid.equals(PARSE_CONDITION_KEY_RTN_CONDITION_ORDERS)){
                CONDITION_ORDER_DATA.getUsers().put(user_id, conditionUserEntity);
                sendMessage(CO_MESSAGE, CO_BROADCAST);
            }else {
                CONDITION_ORDER_HIS_DATA.getUsers().put(user_id, conditionUserEntity);
                sendMessage(CO_HIS_MESSAGE, CO_BROADCAST);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/8/8
     * author: chenli
     * description: 解析条件
     */
    private void parseConditionList(ConditionOrderEntity conditionOrderEntity, JSONObject dataObject, String key){
        try {
            JSONArray conditionArray = dataObject.optJSONArray(key);
            List<ConditionEntity> conditionList = new ArrayList<>();
            for (int conditionIndex = 0; conditionIndex < conditionArray.length(); conditionIndex++) {
                if (conditionArray.isNull(conditionIndex)) continue;
                JSONObject conditionObject = conditionArray.getJSONObject(conditionIndex);
                ConditionEntity conditionEntity = new ConditionEntity();
                Class clCondition = conditionEntity.getClass();
                Iterator<String> iterator = conditionObject.keys();
                while (iterator.hasNext()) {
                    String key1 = iterator.next();
                    if (conditionObject.isNull(key1)) continue;
                    try {
                        Field f = clCondition.getDeclaredField(key1);
                        f.setAccessible(true);
                        f.set(conditionEntity, conditionObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                conditionList.add(conditionEntity);
            }
            conditionOrderEntity.setCondition_list(conditionList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/8/8
     * author: chenli
     * description: 解析单子
     */
    private void parseOrderList(ConditionOrderEntity conditionOrderEntity, JSONObject dataObject, String key){
        try {
            JSONArray orderArray = dataObject.optJSONArray(key);
            List<COrderEntity> orderList = new ArrayList<>();
            for (int orderIndex = 0; orderIndex < orderArray.length(); orderIndex++) {
                if (orderArray.isNull(orderIndex)) continue;
                JSONObject orderObject = orderArray.getJSONObject(orderIndex);
                COrderEntity orderEntity = new COrderEntity();
                Class clOrder = orderEntity.getClass();
                Iterator<String> iterator = orderObject.keys();
                while (iterator.hasNext()) {
                    String key1 = iterator.next();
                    if (orderObject.isNull(key1)) continue;
                    try {
                        Field f = clOrder.getDeclaredField(key1);
                        f.setAccessible(true);
                        f.set(orderEntity, orderObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }

                }
                orderList.add(orderEntity);
            }
            conditionOrderEntity.setOrder_list(orderList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
