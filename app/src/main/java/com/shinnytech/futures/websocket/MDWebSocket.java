package com.shinnytech.futures.websocket;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSetChartKlineEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqSubscribeQuoteEntity;
import com.shinnytech.futures.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_RECONNECT_SERVER_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_MD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_RECONNECT;
import static com.shinnytech.futures.constants.MarketConstants.CHART_ID;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_AID;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_RSP_LOGIN;
import static com.shinnytech.futures.constants.ServerConstants.PARSE_MARKET_KEY_RTN_DATA;
import static com.shinnytech.futures.constants.ServerConstants.REQ_SET_CHART;
import static com.shinnytech.futures.constants.ServerConstants.REQ_SUBSCRIBE_QUOTE;

public class MDWebSocket extends WebSocketBase {

    public MDWebSocket(List<String> urls, int index) {
        super(urls, index);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        LogUtils.e(text, false);
        sDataManager.MD_PACK_COUNT++;
        try {
            JSONObject jsonObject = new JSONObject(text);
            String aid = jsonObject.getString(PARSE_MARKET_KEY_AID);
            switch (aid) {
                case PARSE_MARKET_KEY_RSP_LOGIN:
                    mIndex = 0;
                    sendSubscribeAfterConnect();
                    break;
                case PARSE_MARKET_KEY_RTN_DATA:
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
        if (BaseApplication.issBackGround()) return;

        super.reConnect();
        sDataManager.MD_SESSION++;
        sDataManager.MD_PACK_COUNT = 0;

        LogUtils.e("reConnectMD", true);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_RECONNECT_SERVER_TYPE, AMP_EVENT_RECONNECT_SERVER_TYPE_VALUE_MD);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Amplitude.getInstance().logEventWrap(AMP_RECONNECT, jsonObject);
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
        if (insList == null)return;
        ReqSubscribeQuoteEntity reqSubscribeQuoteEntity = new ReqSubscribeQuoteEntity();
        reqSubscribeQuoteEntity.setAid(REQ_SUBSCRIBE_QUOTE);
        reqSubscribeQuoteEntity.setIns_list(insList);
        String subScribeQuote = JSON.toJSONString(reqSubscribeQuoteEntity);
        //首次打开，交易服务器已经登录完成，行情服务器还没有链接成功
        if (mWebSocketClient != null)mWebSocketClient.sendText(subScribeQuote);
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
        reqSetChartEntity.setAid(REQ_SET_CHART);
        reqSetChartEntity.setChart_id(CHART_ID);
        reqSetChartEntity.setIns_list(ins_list);
        reqSetChartEntity.setDuration(60000000000l);
        reqSetChartEntity.setTrading_day_start(0);
        reqSetChartEntity.setTrading_day_count(86400000000000l);
        String setChart = JSON.toJSONString(reqSetChartEntity);
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
            reqSetChartKlineEntity.setAid(REQ_SET_CHART);
            reqSetChartKlineEntity.setChart_id(CHART_ID);
            reqSetChartKlineEntity.setIns_list(ins_list);
            reqSetChartKlineEntity.setView_width(view_width);
            reqSetChartKlineEntity.setDuration(duration_l);
            String setChart = JSON.toJSONString(reqSetChartKlineEntity);
            sDataManager.CHARTS = setChart;
            mWebSocketClient.sendText(setChart);
            LogUtils.e(setChart, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
