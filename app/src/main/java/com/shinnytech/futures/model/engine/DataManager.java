package com.shinnytech.futures.model.engine;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.shinnytech.futures.application.BaseApplicationLike;
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
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

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

import static com.shinnytech.futures.constants.CommonConstants.MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_TRADE;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST_TRANSACTION;

/**
 * date: 7/9/17
 * author: chenli
 * description: 服务器返回数据解析
 * version:
 * state: basically done
 */
public class DataManager {
    /**
     * date: 7/9/17
     * description: 账户信息实例
     */
    public static final TradeBean TRADE = new TradeBean();
    /**
     * date: 7/9/17
     * description: 单例模式，行情数据类实例
     */
    private static final FutureBean RTN_DATA = new FutureBean();
    /**
     * date: 7/9/17
     * description: 账户登录返回信息实例
     */
    private static final BrokerEntity BROKER = new BrokerEntity();
    /**
     * date: 7/9/17
     * description: 单例模式，本解析类实例
     */
    private static final DataManager INSTANCE = new DataManager();
    /**
     * date: 7/7/17
     * description: 用于判断是否登录成功的全局标志
     */
    public boolean IS_LOGIN = false;
    /**
     * date: 9/1/18
     * description: 账户id
     */
    public String USER_ID = "";

    /**
     * date: 9/7/18
     * description: 交易服务器路径
     */
    public String TRANSACTION_URL_FULL = TRANSACTION_URL + "0";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

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
        return TRADE;
    }

    public BrokerEntity getLogin() {
        return BROKER;
    }

    public SimpleDateFormat getSimpleDateFormat(){
        return simpleDateFormat;
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
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key0 = iterator.next();
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
                            diffEntity.setIns_list(dataObject.getString(key0));
                            break;
                        case "mdhis_more_data":
                            diffEntity.setMdhis_more_data(dataObject.getString(key0));
                            break;
                        default:
                            break;
                    }
                }
            }
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE, BROADCAST);
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
            JSONObject chartObject = chartsObject.getJSONObject(key);
            ChartEntity chartEntity = chartsEntities.get(key);
            if (chartEntity == null) chartEntity = new ChartEntity();
            Class clChart = chartEntity.getClass();
            Iterator<String> iterator1 = chartObject.keys();
            Map<String, String> stateEntity = chartEntity.getState();
            while (iterator1.hasNext()) {
                String key1 = iterator1.next();
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
                        if (!chartObject.isNull(key1)) {
                            f.set(chartEntity, chartObject.optString(key1));
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
            JSONObject futureKlineObject = futureKlineObjects.getJSONObject(key);
            Iterator<String> iterator1 = futureKlineObject.keys();
            Map<String, KlineEntity> futureKlineEntity = futureKlineEntities.get(key);
            if (futureKlineEntity == null) futureKlineEntity = new HashMap<>();

            while (iterator1.hasNext()) {
                String key1 = iterator1.next(); //kline"M3"
                JSONObject klineObject = futureKlineObject.getJSONObject(key1);
                KlineEntity klineEntity = futureKlineEntity.get(key1);
                if (klineEntity == null) klineEntity = new KlineEntity();

                Class clKline = klineEntity.getClass();
                Iterator<String> iterator2 = klineObject.keys();
                while (iterator2.hasNext()) {
                    String key2 = iterator2.next();
                    switch (key2) {
                        case "data":
                            JSONObject dataObjects = klineObject.getJSONObject(key2);
                            Iterator<String> iterator3 = dataObjects.keys();
                            Map<String, KlineEntity.DataEntity> dataEntities = klineEntity.getData();
                            while (iterator3.hasNext()) {
                                String key3 = iterator3.next();
                                JSONObject dataObjectInner = dataObjects.getJSONObject(key3);
                                KlineEntity.DataEntity dataEntity = dataEntities.get(key3);
                                Iterator<String> iterator4 = dataObjectInner.keys();
                                if (dataEntity == null) dataEntity = new KlineEntity.DataEntity();

                                Class clData = dataEntity.getClass();
                                while (iterator4.hasNext()) {
                                    String key4 = iterator4.next();
                                    try {
                                        Field f = clData.getDeclaredField(key4);
                                        f.setAccessible(true);
                                        if (!dataObjectInner.isNull(key4))
                                            f.set(dataEntity, dataObjectInner.optString(key4));
                                    } catch (NoSuchFieldException e) {
                                        continue;
                                    } catch (IllegalAccessException e) {
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
                                JSONObject bindingObject = bindingObjects.getJSONObject(key5);
                                KlineEntity.BindingEntity bindingEntity = bindingEntities.get(key5);
                                Iterator<String> iterator6 = bindingObject.keys();
                                if (bindingEntity == null) {
                                    bindingEntity = new KlineEntity.BindingEntity();
                                }
                                Class clBinding = bindingEntity.getClass();
                                while (iterator6.hasNext()) {
                                    String key6 = iterator6.next();
                                    try {
                                        Field f = clBinding.getDeclaredField(key6);
                                        f.setAccessible(true);
                                        if (!bindingObject.isNull(key6)) {
                                            f.set(bindingEntity, bindingObject.optString(key6));
                                        }
                                    } catch (NoSuchFieldException e) {
                                        continue;
                                    } catch (IllegalAccessException e) {
                                        continue;
                                    }

                                }
                                bindingEntities.put(key5, bindingEntity);
                            }
                            break;
                        default:
                            try {
                                Field field = clKline.getDeclaredField(key2);
                                field.setAccessible(true);
                                if (!klineObject.isNull(key2))
                                    field.set(klineEntity, klineObject.optString(key2));
                            } catch (NoSuchFieldException e) {
                                continue;
                            } catch (IllegalAccessException e) {
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
            if (!quoteObjects.isNull(key)) {
                JSONObject quoteObject = quoteObjects.getJSONObject(key);
                Iterator<String> iterator1 = quoteObject.keys();
                QuoteEntity quoteEntity = quoteEntities.get(key);
                if (quoteEntity == null) quoteEntity = new QuoteEntity();
                Class clQuote = quoteEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clQuote.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!quoteObject.isNull(key1))
                            f.set(quoteEntity, quoteObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
                        continue;
                    }

                }
                quoteEntities.put(key, quoteEntity);
            }
        }

    }

    /**
     * date: 6/16/17
     * author: chenli
     * description: 刷新登录信息
     */
    public void refreshTradeBean(String msg) throws JSONException {
        final JSONObject tradeBeanObject = new JSONObject(msg);
        switch (tradeBeanObject.optString("aid")) {
            case "rtn_brokers":
                BrokerEntity brokerInfo = new Gson().fromJson(msg, BrokerEntity.class);
                BROKER.setBrokers(brokerInfo.getBrokers());
                if (BaseApplicationLike.getWebSocketService() != null)
                    BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_BROKER_INFO, BROADCAST_TRANSACTION);
                break;
            case "rtn_data":
                parseTradeData(tradeBeanObject);
                break;
            default:
                break;
        }
    }

    private void parseTradeData(JSONObject accountBeanObject) {
        try {
            JSONArray dataArray = accountBeanObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                Iterator<String> iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JSONObject data = dataObject.getJSONObject(key);
                    switch (key) {
                        case "notify":
                            Iterator<String> notifyIterator = data.keys();
                            while (notifyIterator.hasNext()) {
                                String notifyKey = notifyIterator.next();
                                JSONObject notify = data.getJSONObject(notifyKey);
                                final String content = notify.optString("content");
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), content);
                                    }
                                });
                            }
                            break;
                        case "trade":
                            Map<String, UserEntity> userEntities = TRADE.getUsers();
                            Iterator<String> tradeIterator = data.keys();
                            while (tradeIterator.hasNext()) {
                                String userKey = tradeIterator.next();
                                JSONObject user = data.getJSONObject(userKey);
                                UserEntity userEntity = userEntities.get(userKey);
                                if (userEntity == null) userEntity = new UserEntity();
                                Iterator<String> tradeDataIterator = user.keys();
                                while (tradeDataIterator.hasNext()) {
                                    String tradeDataKey = tradeDataIterator.next();
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
                                            USER_ID = userId;
                                            userEntity.setUser_id(userId);
                                            if (BaseApplicationLike.getWebSocketService() != null && !IS_LOGIN )
                                                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_LOGIN, BROADCAST_TRANSACTION);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                userEntities.put(userKey, userEntity);
                                if (BaseApplicationLike.getWebSocketService() != null)
                                    BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_TRADE, BROADCAST_TRANSACTION);
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
                JSONObject transferObject = transferData.getJSONObject(key);
                Iterator<String> iterator1 = transferObject.keys();
                TransferEntity transferEntity = transferEntities.get(key);
                if (transferEntity == null) transferEntity = new TransferEntity();
                Class clTransfer = transferEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clTransfer.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!transferObject.isNull(key1)) {
                            String data = transferObject.optString(key1);
                            f.set(transferEntity, data);
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
                JSONObject bankObject = bankData.getJSONObject(key);
                Iterator<String> iterator1 = bankObject.keys();
                BankEntity bankEntity = bankEntities.get(key);
                if (bankEntity == null) bankEntity = new BankEntity();
                Class clBank = bankEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clBank.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!bankObject.isNull(key1)) {
                            String data = bankObject.optString(key1);
                            f.set(bankEntity, data);
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
                JSONObject tradeObject = tradeData.getJSONObject(key);
                Iterator<String> iterator1 = tradeObject.keys();
                TradeEntity tradeEntity = tradeEntities.get(key);
                if (tradeEntity == null) tradeEntity = new TradeEntity();
                Class clTrade = tradeEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clTrade.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!tradeObject.isNull(key1)) {
                            String data = tradeObject.optString(key1);
                            f.set(tradeEntity, data);
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
                JSONObject positionObject = positionData.getJSONObject(key);
                Iterator<String> iterator1 = positionObject.keys();
                PositionEntity positionEntity = positionEntities.get(key);
                if (positionEntity == null) positionEntity = new PositionEntity();
                Class clPosition = positionEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clPosition.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!positionObject.isNull(key1)) {
                            String data = positionObject.optString(key1);
                            f.set(positionEntity, data);
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
                JSONObject orderObject = orderData.getJSONObject(key);
                Iterator<String> iterator1 = orderObject.keys();
                OrderEntity orderEntity = orderEntities.get(key);
                if (orderEntity == null) orderEntity = new OrderEntity();
                Class clOrder = orderEntity.getClass();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clOrder.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!orderObject.isNull(key1)) {
                            String data = orderObject.optString(key1);
                            f.set(orderEntity, data);
                        }
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
                JSONObject accountObject = accountData.getJSONObject(key);
                AccountEntity accountEntity = accountEntities.get(key);
                if (accountEntity == null) accountEntity = new AccountEntity();
                Class clAccount = accountEntity.getClass();
                Iterator<String> iterator1 = accountObject.keys();
                while (iterator1.hasNext()) {
                    String key1 = iterator1.next();
                    try {
                        Field f = clAccount.getDeclaredField(key1);
                        f.setAccessible(true);
                        if (!accountObject.isNull(key1))
                            f.set(accountEntity, accountObject.optString(key1));
                    } catch (NoSuchFieldException e) {
                        continue;
                    } catch (IllegalAccessException e) {
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
