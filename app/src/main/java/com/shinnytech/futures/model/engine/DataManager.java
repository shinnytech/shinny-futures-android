package com.shinnytech.futures.model.engine;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.model.bean.accountinfobean.AccountBean;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.LoginEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.DiffEntity;
import com.shinnytech.futures.model.bean.futureinfobean.FutureBean;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;
import com.shinnytech.futures.view.activity.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_ORDER;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_TRADE;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST_TRANSACTION;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDown;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;

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
    public static final AccountBean ACCOUNT = new AccountBean();
    /**
     * date: 7/9/17
     * description: 单例模式，本解析类实例
     */
    private static final DataManager INSTANCE = new DataManager();
    /**
     * date: 7/9/17
     * description: 单例模式，行情数据类实例
     */
    private static final FutureBean RTN_DATA = new FutureBean();
    /**
     * date: 7/9/17
     * description: 账户登录返回信息实例
     */
    private static final LoginEntity LOGIN = new LoginEntity();
    private DataManager() {
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

    public AccountBean getAccountBean() {
        return ACCOUNT;
    }

    public LoginEntity getLogin() {
        return LOGIN;
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
            if (chartEntity == null) {
                chartEntity = new ChartEntity();
            }
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

    private void parseKlines(JSONObject dataObject, DiffEntity diffEntity) throws JSONException{
        JSONObject futureKlineObjects = dataObject.getJSONObject("klines");
        Map<String, Map<String, KlineEntity>> futureKlineEntities = diffEntity.getKlines();
        Iterator<String> iterator = futureKlineObjects.keys();
        while (iterator.hasNext()) {
            String key = iterator.next(); //future "cu1601"
            JSONObject futureKlineObject = futureKlineObjects.getJSONObject(key);
            Iterator<String> iterator1 = futureKlineObject.keys();
            Map<String, KlineEntity> futureKlineEntity = futureKlineEntities.get(key);
            if (futureKlineEntity == null) {
                futureKlineEntity = new HashMap<>();
            }
            while (iterator1.hasNext()) {
                String key1 = iterator1.next(); //kline"M3"
                JSONObject klineObject = futureKlineObject.getJSONObject(key1);
                KlineEntity klineEntity = futureKlineEntity.get(key1);
                if (klineEntity == null) {
                    klineEntity = new KlineEntity();
                }
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
                                if (dataEntity == null) {
                                    dataEntity = new KlineEntity.DataEntity();
                                }
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
    public void refreshAccountBean(String msg) throws JSONException {
        final JSONObject accountBeanObject = new JSONObject(msg);
        switch (accountBeanObject.optString("aid")) {
            case "rtn_brokers":
                LoginEntity brokerInfo = new Gson().fromJson(msg, LoginEntity.class);
                LOGIN.setBrokers(brokerInfo.getBrokers());
                if (BaseApplicationLike.getWebSocketService() != null)
                    BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_BROKER_INFO, BROADCAST_TRANSACTION);
                break;
            case "rtn_data":
                parseRtnData(accountBeanObject);
                break;
            default:
                break;
        }
    }

    private void parseRtnData(JSONObject accountBeanObject) {
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
                                final String code = notify.optString("code");
                                LOGIN.setContent(content);
                                LOGIN.setCode(code);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), content);
                                    }
                                });
                            }
                            if (BaseApplicationLike.getWebSocketService() != null
                                    && !LoginActivity.isIsLogin() && LOGIN.getContent().contains("登录"))
                                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_LOGIN, BROADCAST_TRANSACTION);
                            break;
                        case "trade":
                            Iterator<String> tradeIterator = data.keys();
                            while (tradeIterator.hasNext()) {
                                String tradeKey = tradeIterator.next();
                                JSONObject trade = data.getJSONObject(tradeKey);
                                Iterator<String> tradeDataIterator = trade.keys();
                                while (tradeDataIterator.hasNext()) {
                                    String tradeDataKey = tradeDataIterator.next();
                                    switch (tradeDataKey) {
                                        case "accounts":
                                            parseAccount(trade);
                                            break;
                                        case "orders":
                                            parseOrder(trade);
                                            break;
                                        case "positions":
                                            parsePosition(trade);
                                            break;
                                        case "trades":
                                            parseTrade(trade);
                                            break;
                                        default:
                                            break;
                                    }
                                }
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

    private void parseTrade(JSONObject trade) {
        try {
            JSONObject tradeData = trade.getJSONObject("trades");
            Map<String, TradeEntity> tradeEntities = ACCOUNT.getTrade();
            Iterator<String> tradeIterator = tradeData.keys();
            while (tradeIterator.hasNext()) {
                String key = tradeIterator.next();
                JSONObject tradeObject = tradeData.getJSONObject(key);
                Iterator<String> iterator1 = tradeObject.keys();
                TradeEntity tradeEntity = new TradeEntity();
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
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_TRADE, BROADCAST_TRANSACTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePosition(JSONObject trade) {
        try {
            JSONObject positionData = trade.getJSONObject("positions");
            Map<String, PositionEntity> positionEntities = ACCOUNT.getPosition();
            Iterator<String> positionIterator = positionData.keys();
            while (positionIterator.hasNext()) {
                String key = positionIterator.next();
                JSONObject positionObject = positionData.getJSONObject(key);
                Iterator<String> iterator1 = positionObject.keys();
                PositionEntity positionEntity = new PositionEntity();
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
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_POSITION, BROADCAST_TRANSACTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseOrder(JSONObject trade) {
        try {
            JSONObject orderData = trade.getJSONObject("orders");
            final Map<String, OrderEntity> orderEntities = ACCOUNT.getOrder();
            Iterator<String> orderIterator = orderData.keys();
            while (orderIterator.hasNext()) {
                String key = orderIterator.next();
                final JSONObject orderObject = orderData.getJSONObject(key);
                Iterator<String> iterator1 = orderObject.keys();
                final OrderEntity orderEntity = new OrderEntity();
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
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_ORDER, BROADCAST_TRANSACTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAccount(JSONObject trade) {
        try {
            JSONObject accountData = trade.getJSONObject("accounts");
            Map<String, AccountEntity> accountEntities = ACCOUNT.getAccount();
            Iterator<String> accountIterator = accountData.keys();
            while (accountIterator.hasNext()) {
                String key = accountIterator.next();
                JSONObject accountObject = accountData.getJSONObject(key);
                AccountEntity accountEntity = new AccountEntity();
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
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendMessage(MESSAGE_ACCOUNT, BROADCAST_TRANSACTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
