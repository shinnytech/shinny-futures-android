package com.shinnytech.futures.websocket;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.reqbean.ReqPeekMessageEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartKlineEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSubscribeQuoteEntity;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_RECONNECT_SERVER_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_MD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_RECONNECT_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_RECONNECT;
import static com.shinnytech.futures.constants.CommonConstants.CHART_ID;

public class MDWebSocket extends WebSocketBase {

    public MDWebSocket(List<String> urls, int index) {
        super(urls, index);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        LogUtils.e(text, false);
        try {
            JSONObject jsonObject = new JSONObject(text);
            String aid = jsonObject.getString("aid");
            switch (aid) {
                case "rsp_login":
                    mIndex = 0;
                    sendSubscribeAfterConnect();
                    break;
                case "rtn_data":
                    sDataManager.refreshFutureBean(jsonObject);
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
        super.reConnect();

        LogUtils.e("reConnectMD", true);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_RECONNECT_SERVER_TYPE, AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_MD);
            jsonObject.put(AMP_EVENT_RECONNECT_TIME, TimeUtils.getAmpTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Amplitude.getInstance().logEvent(AMP_RECONNECT, jsonObject);
    }

    /**
     * date: 2019/3/17
     * author: chenli
     * description: 首次连接行情服务器与断开重连的行情订阅处理
     */
    private void sendSubscribeAfterConnect() {
        if (!sDataManager.QUOTES.isEmpty()) {
            mWebSocketClient.sendText(sDataManager.QUOTES);
        }

        if (!sDataManager.CHARTS.isEmpty()) {
            mWebSocketClient.sendText(sDataManager.CHARTS);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 行情订阅
     */
    public void sendSubscribeQuote(String insList) {
        ReqSubscribeQuoteEntity reqSubscribeQuoteEntity = new ReqSubscribeQuoteEntity();
        reqSubscribeQuoteEntity.setAid("subscribe_quote");
        reqSubscribeQuoteEntity.setIns_list(insList);
        String subScribeQuote = new Gson().toJson(reqSubscribeQuoteEntity);
        mWebSocketClient.sendText(subScribeQuote);
        sDataManager.QUOTES = subScribeQuote;
        LogUtils.e(subScribeQuote, true);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 分时图
     */
    public void sendSetChart(String ins_list) {
        ReqSetChartEntity reqSetChartEntity = new ReqSetChartEntity();
        reqSetChartEntity.setAid("set_chart");
        reqSetChartEntity.setChart_id(CHART_ID);
        reqSetChartEntity.setIns_list(ins_list);
        reqSetChartEntity.setDuration(60000000000l);
        reqSetChartEntity.setTrading_day_start(0);
        reqSetChartEntity.setTrading_day_count(86400000000000l);
        String setChart = new Gson().toJson(reqSetChartEntity);
        sDataManager.CHARTS = setChart;
        mWebSocketClient.sendText(setChart);
        LogUtils.e(setChart, true);
    }

    /**
     * date: 2018/12/14
     * author: chenli
     * description: k线图
     */
    public void sendSetChartKline(String ins_list, int view_width, String duration) {
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
            mWebSocketClient.sendText(setChart);
            LogUtils.e(setChart, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
