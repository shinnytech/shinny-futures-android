package com.shinnytech.futures.websocket;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.model.bean.accountinfobean.BrokerEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqControlConditionOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqInsertConditionOrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqCancelOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConfirmSettlementEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqInsertOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqLoginEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqPasswordEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqQueryConditionOrderEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqTransferEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TDUtils;
import com.shinnytech.futures.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST;
import static com.shinnytech.futures.application.BaseApplication.sendMessage;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CANCEL_ORDER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OFFSET;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_RECONNECT_SERVER_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_TD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME;
import static com.shinnytech.futures.constants.AmpConstants.AMP_INSERT_ORDER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_LOGIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_RECONNECT;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_AID;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_RTN_BROKERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_RTN_CONDITION_ORDERS;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_RTN_DATA;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_TRADE_KEY_RTN_HIS_CONDITION_ORDERS;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_SYSTEM_INFO;
import static com.shinnytech.futures.constants.ServerConstants.REQ_CANCEL_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_CHANGE_PASSWORD;
import static com.shinnytech.futures.constants.ServerConstants.REQ_CONFIRM_SETTLEMENT;
import static com.shinnytech.futures.constants.ServerConstants.REQ_INSERT_CONDITION_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_INSERT_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_LOGIN;
import static com.shinnytech.futures.constants.ServerConstants.REQ_QUERY_CONDITION_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_TRANSFER;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_FAIL;

public class TDWebSocket extends WebSocketBase {

    public TDWebSocket(List<String> urls, int index) {
        super(urls, index);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        LogUtils.e(text, false);
        sDataManager.TD_PACK_COUNT++;
        try {
            JSONObject jsonObject = new JSONObject(text);
            String aid = jsonObject.getString(PARSE_TRADE_KEY_AID);
            switch (aid) {
                case PARSE_TRADE_KEY_RTN_BROKERS:
                    mIndex = 0;
                    loginConfig(text);
                    break;
                case PARSE_TRADE_KEY_RTN_DATA:
                    sDataManager.refreshTradeBean(jsonObject);
                    break;
                case PARSE_TRADE_KEY_RTN_CONDITION_ORDERS:
                    sDataManager.refreshConditionOrderBean(jsonObject, aid);
                    break;
                case PARSE_TRADE_KEY_RTN_HIS_CONDITION_ORDERS:
                    sDataManager.refreshConditionOrderBean(jsonObject, aid);
                    break;
                default:
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!BaseApplication.issBackGround())sendPeekMessage();
    }

    @Override
    public void reConnect() {
        if (BaseApplication.issBackGround()) return;

        super.reConnect();
        sDataManager.TD_SESSION++;
        sDataManager.TD_PACK_COUNT = 0;

        LogUtils.e("reConnectTD", true);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_RECONNECT_SERVER_TYPE, AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_TD);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Amplitude.getInstance().logEventWrap(AMP_RECONNECT, jsonObject);
    }

    /**
     * date: 2019/3/17
     * author: chenli
     * description: 登录设置，自动登录
     */
    private void loginConfig(String message) {
        BrokerEntity brokerInfo = JSON.parseObject(message, BrokerEntity.class);
        sDataManager.getBroker().setBrokers(brokerInfo.getBrokers());
        sendMessage(TD_MESSAGE_BROKER_INFO, TD_BROADCAST);

        Context context = BaseApplication.getContext();
        if (SPUtils.contains(context, SettingConstants.CONFIG_LOGIN_DATE)) {
            String date = (String) SPUtils.get(context, SettingConstants.CONFIG_LOGIN_DATE, "");
            String name = (String) SPUtils.get(context, SettingConstants.CONFIG_ACCOUNT, "");
            String password = (String) SPUtils.get(context, SettingConstants.CONFIG_PASSWORD, "");
            String broker = (String) SPUtils.get(context, SettingConstants.CONFIG_BROKER, "");
            boolean isPermissionDenied = ContextCompat.checkSelfPermission(BaseApplication.getContext(),
                Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(BaseApplication.getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(BaseApplication.getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED;
            boolean notLogin = password.isEmpty() || isPermissionDenied ||
                    (TDUtils.isVisitor(name, password) && !date.isEmpty() && !TimeUtils.getNowTime().equals(date));
            if (!notLogin){
                sDataManager.BROKER_ID = broker;
                sDataManager.USER_ID = name;
                //处理切换账号断线重连的情况
                if (date.isEmpty()) sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
                else sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_LOGIN_TYPE, AMP_EVENT_LOGIN_TYPE_VALUE_AUTO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEventWrap(AMP_LOGIN, jsonObject);
                sendReqLogin(broker, name, password);
            }else BaseApplication.sendMessage(TD_MESSAGE_LOGIN_FAIL, TD_BROADCAST);
        }else BaseApplication.sendMessage(TD_MESSAGE_LOGIN_FAIL, TD_BROADCAST);

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 用户登录
     */
    public void sendReqLogin(String bid, String user_name, String password) {
        String systemInfo = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_SYSTEM_INFO, "");
        ReqLoginEntity reqLoginEntity = new ReqLoginEntity();
        reqLoginEntity.setAid(REQ_LOGIN);
        reqLoginEntity.setBid(bid);
        reqLoginEntity.setUser_name(user_name);
        reqLoginEntity.setPassword(password);
        reqLoginEntity.setClient_system_info(systemInfo);
        reqLoginEntity.setClient_app_id(sDataManager.CLIENT_APP_ID);
        String reqLogin = JSON.toJSONString(reqLoginEntity);
        mWebSocketClient.sendText(reqLogin);
        LogUtils.e(reqLogin, true);
        LatestFileManager.insertLogToDB(reqLogin);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 确认结算单
     */
    public void sendReqConfirmSettlement() {
        ReqConfirmSettlementEntity reqConfirmSettlementEntity = new ReqConfirmSettlementEntity();
        reqConfirmSettlementEntity.setAid(REQ_CONFIRM_SETTLEMENT);
        String confirmSettlement = JSON.toJSONString(reqConfirmSettlementEntity);
        mWebSocketClient.sendText(confirmSettlement);
        LogUtils.e(confirmSettlement, true);
        LatestFileManager.insertLogToDB(confirmSettlement);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 下单
     */
    public void sendReqInsertOrder(String exchange_id, String instrument_id, String direction,
                                   String offset, int volume, String price_type, double price, String amp_price_type) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_PRICE, price);
            jsonObject.put(AMP_EVENT_PRICE_TYPE, amp_price_type);
            jsonObject.put(AMP_EVENT_INSTRUMENT_ID, exchange_id + "." + instrument_id);
            jsonObject.put(AMP_EVENT_VOLUME, volume);
            jsonObject.put(AMP_EVENT_DIRECTION, direction);
            jsonObject.put(AMP_EVENT_OFFSET, offset);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.e(amp_price_type, true);
        Amplitude.getInstance().logEventWrap(AMP_INSERT_ORDER, jsonObject);
        String user_id = DataManager.getInstance().USER_ID;
        ReqInsertOrderEntity reqInsertOrderEntity = new ReqInsertOrderEntity();
        reqInsertOrderEntity.setAid(REQ_INSERT_ORDER);
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
        String reqInsertOrder = JSON.toJSONString(reqInsertOrderEntity);
        mWebSocketClient.sendText(reqInsertOrder);
        LogUtils.e(reqInsertOrder, true);
        LatestFileManager.insertLogToDB(reqInsertOrder);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 撤单
     */
    public void sendReqCancelOrder(String order_id) {
        String user_id = DataManager.getInstance().USER_ID;
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(user_id);
        if (userEntity != null) {
            OrderEntity orderEntity = userEntity.getOrders().get(order_id);
            if (orderEntity != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_PRICE, orderEntity.getLimit_price());
                    jsonObject.put(AMP_EVENT_INSTRUMENT_ID, orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id());
                    jsonObject.put(AMP_EVENT_VOLUME, orderEntity.getVolume_left());
                    jsonObject.put(AMP_EVENT_DIRECTION, orderEntity.getDirection());
                    jsonObject.put(AMP_EVENT_OFFSET, orderEntity.getOffset());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEventWrap(AMP_CANCEL_ORDER, jsonObject);
            }
        }
        ReqCancelOrderEntity reqCancelOrderEntity = new ReqCancelOrderEntity();
        reqCancelOrderEntity.setAid(REQ_CANCEL_ORDER);
        reqCancelOrderEntity.setUser_id(user_id);
        reqCancelOrderEntity.setOrder_id(order_id);
        String reqInsertOrder = JSON.toJSONString(reqCancelOrderEntity);
        mWebSocketClient.sendText(reqInsertOrder);
        LogUtils.e(reqInsertOrder, true);
        LatestFileManager.insertLogToDB(reqInsertOrder);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 银期转帐
     */
    public void sendReqTransfer(String future_account, String future_password, String bank_id,
                                String bank_password, String currency, float amount) {
        ReqTransferEntity reqTransferEntity = new ReqTransferEntity();
        reqTransferEntity.setAid(REQ_TRANSFER);
        reqTransferEntity.setFuture_account(future_account);
        reqTransferEntity.setFuture_password(future_password);
        reqTransferEntity.setBank_id(bank_id);
        reqTransferEntity.setBank_password(bank_password);
        reqTransferEntity.setCurrency(currency);
        reqTransferEntity.setAmount(amount);
        String reqTransfer = JSON.toJSONString(reqTransferEntity);
        mWebSocketClient.sendText(reqTransfer);
        LogUtils.e(reqTransfer, true);
        LatestFileManager.insertLogToDB(reqTransfer);
    }

    /**
     * date: 2019/1/3
     * author: chenli
     * description: 修改密码
     */
    public void sendReqPassword(String new_password, String old_password) {
        ReqPasswordEntity reqPasswordEntity = new ReqPasswordEntity();
        reqPasswordEntity.setAid(REQ_CHANGE_PASSWORD);
        reqPasswordEntity.setNew_password(new_password);
        reqPasswordEntity.setOld_password(old_password);
        String reqPassword = JSON.toJSONString(reqPasswordEntity);
        mWebSocketClient.sendText(reqPassword);
        LogUtils.e(reqPassword, true);
        LatestFileManager.insertLogToDB(reqPassword);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 新建条件单
     */
    public void sendReqInsertConditionOrder(ReqConditionEntity[] condition_list,
                                            ReqConditionOrderEntity[] order_list,
                                            String time_condition_type,
                                            int GTD_date,
                                            boolean is_cancel_ori_close_order,
                                            String condition_logic) {
        String user_id = DataManager.getInstance().USER_ID;
        ReqInsertConditionOrderEntity orderEntity = new ReqInsertConditionOrderEntity();
        orderEntity.setAid(REQ_INSERT_CONDITION_ORDER);
        orderEntity.setUser_id(user_id);
        orderEntity.setCondition_list(condition_list);
        orderEntity.setOrder_list(order_list);
        orderEntity.setTime_condition_type(time_condition_type);
        orderEntity.setGtd_date(GTD_date);
        orderEntity.setIs_cancel_ori_close_order(is_cancel_ori_close_order);
        orderEntity.setConditions_logic_oper(condition_logic);
        String reqInsertConditionOrder = JSON.toJSONString(orderEntity);
        mWebSocketClient.sendText(reqInsertConditionOrder);
        LogUtils.e(reqInsertConditionOrder, true);
        LatestFileManager.insertLogToDB(reqInsertConditionOrder);
    }

    /**
     * date: 2019/8/10
     * author: chenli
     * description: 撤销、暂停、重启条件单
     */
    public void sendReqControlConditionOrder(String aid, String order_id){
        String user_id = DataManager.getInstance().USER_ID;
        ReqControlConditionOrderEntity orderEntity = new ReqControlConditionOrderEntity();
        orderEntity.setAid(aid);
        orderEntity.setUser_id(user_id);
        orderEntity.setOrder_id(order_id);
        String reqControlConditionOrder = JSON.toJSONString(orderEntity);
        mWebSocketClient.sendText(reqControlConditionOrder);
        LogUtils.e(reqControlConditionOrder, true);
        LatestFileManager.insertLogToDB(reqControlConditionOrder);
    }

    /**
     * date: 2019/8/11
     * author: chenli
     * description: 查询历史条件单
     */
    public void sendReqQueryConditionOrder(int action_day){
        String user_id = DataManager.getInstance().USER_ID;
        ReqQueryConditionOrderEntity orderEntity = new ReqQueryConditionOrderEntity();
        orderEntity.setAid(REQ_QUERY_CONDITION_ORDER);
        orderEntity.setUser_id(user_id);
        orderEntity.setAction_day(action_day);
        String reqQueryConditionOrder = JSON.toJSONString(orderEntity);
        mWebSocketClient.sendText(reqQueryConditionOrder);
        LogUtils.e(reqQueryConditionOrder, true);
        LatestFileManager.insertLogToDB(reqQueryConditionOrder);
    }

    /**
     * date: 2019/7/14
     * author: chenli
     * description: 在splash页判断是否链接成功
     */
    public boolean isOpen(){
        if (mWebSocketClient == null)return false;
        else return mWebSocketClient.isOpen();
    }
}
