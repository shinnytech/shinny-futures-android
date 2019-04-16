package com.shinnytech.futures.model.engine;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL_INS_LIST;

/**
 * date: 3/30/17
 * author: chenli
 * description: 合约列表latest.json文件帮助类
 * version:
 * state: done
 */
public class LatestFileManager {

    /**
     * date: 7/9/17
     * description: 自选合约列表名称
     */
    private static JSONObject jsonObject = new JSONObject();
    private static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String instrumentId1, String instrumentId2) {
            try {
                if (instrumentId1 == null || instrumentId2 == null) return 0;
                JSONObject jsonObject1 = jsonObject.optJSONObject(instrumentId1);
                JSONObject jsonObject2 = jsonObject.optJSONObject(instrumentId2);
                if (jsonObject1 == null || jsonObject2 == null) {
                    return instrumentId1.compareTo(instrumentId2);
                }
                int sort_key1 = jsonObject1.optInt("sort_key");
                int sort_key2 = jsonObject2.optInt("sort_key");
                if (sort_key1 == sort_key2) {
                    return instrumentId1.compareTo(instrumentId2);
                } else {
                    return sort_key1 - sort_key2;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    };
    /**
     * date: 7/9/17
     * description: 搜索列表实例
     */
    private static final Map<String, SearchEntity> SEARCH_ENTITIES = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 自选合约列表
     */
    private static Map<String, QuoteEntity> sOptionalInsList = new LinkedHashMap<>();

    /**
     * date: 7/9/17
     * description: 主力合约列表
     */
    private static Map<String, QuoteEntity> sMainInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 主力合约导航
     */
    private static Map<String, String> sMainInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 上期合约列表
     */
    private static Map<String, QuoteEntity> sShangqiInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 上期导航
     */
    private static Map<String, String> sShangqiInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 大连合约列表
     */
    private static Map<String, QuoteEntity> sDalianInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 大连合约导航
     */
    private static Map<String, String> sDalianInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 郑州合约列表
     */
    private static Map<String, QuoteEntity> sZhengzhouInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 郑州合约列表导航
     */
    private static Map<String, String> sZhengzhouInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 中金合约列表
     */
    private static Map<String, QuoteEntity> sZhongjinInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 中金合约列表导航
     */
    private static Map<String, String> sZhongjinInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 3/27/18
     * description: 能源合约列表
     */
    private static Map<String, QuoteEntity> sNengyuanInsList = new TreeMap<>(comparator);

    /**
     * date: 3/27/18
     * description: 能源合约列表导航
     */
    private static Map<String, String> sNengyuanInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 大连组合列表
     */
    private static Map<String, QuoteEntity> sDalianzuheInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 大连组合列表导航
     */
    private static Map<String, String> sDalianzuheInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 郑州组合列表
     */
    private static Map<String, QuoteEntity> sZhengzhouzuheInsList = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 郑州组合中文名列表
     */
    private static Map<String, String> sZhengzhouzuheInsListNameNav = new TreeMap<>(comparator);

    /**
     * date: 7/9/17
     * description: 搜索列表实例历史
     */
    private static Map<String, SearchEntity> SEARCH_ENTITIES_HISTORY = new TreeMap<>(comparator);


    /**
     * date: 7/9/17
     * author: chenli
     * description: 开机初始化合约列表
     */
    public static void initInsList(File latestFile) {
        LogUtils.e("合约列表解析开始", true);
        String latest = readFile(latestFile.getName());
        if (latest == null) return;
        try {
            jsonObject = new JSONObject(latest);
            Iterator<String> instrumentIds = jsonObject.keys();
            while (instrumentIds.hasNext()) {
                String instrument_id = instrumentIds.next();
                JSONObject subObject = jsonObject.getJSONObject(instrument_id);
                String classN = subObject.optString("class");
                if (!"FUTURE_CONT".equals(classN) && !"FUTURE".equals(classN) &&
                        !"FUTURE_COMBINE".equals(classN) && !"FUTURE_OPTION".equals(classN)) {
                    continue;
                }
                String ins_name = subObject.optString("ins_name");
                String ins_id = subObject.optString("ins_id");
                String product_id = subObject.optString("product_id");
                String exchange_id = subObject.optString("exchange_id");
                String price_tick = subObject.optString("price_tick");
                String price_decs = subObject.optString("price_decs");
                String volume_multiple = subObject.optString("volume_multiple");
                String sort_key = subObject.optString("sort_key");
                String product_short_name = subObject.optString("product_short_name");
                String py = subObject.optString("py");
                boolean expired = subObject.optBoolean("expired");
                int pre_volume = subObject.optInt("pre_volume");
                SearchEntity searchEntity = new SearchEntity();
                searchEntity.setIns_id(ins_id);
                searchEntity.setProduct_id(product_id);
                searchEntity.setpTick(price_tick);
                searchEntity.setpTick_decs(price_decs);
                searchEntity.setInstrumentId(instrument_id);
                searchEntity.setInstrumentName(ins_name);
                searchEntity.setVm(volume_multiple);
                searchEntity.setExchangeId(exchange_id);
                searchEntity.setSort_key(sort_key);
                searchEntity.setPy(py);
                searchEntity.setExpired(expired);
                searchEntity.setPre_volume(pre_volume);
                QuoteEntity quoteEntity = new QuoteEntity();
                quoteEntity.setInstrument_id(instrument_id);

                if ("FUTURE_CONT".equals(classN)) {
                    String underlying_symbol = subObject.optString("underlying_symbol");
                    if ("".equals(underlying_symbol)) continue;
                    searchEntity.setUnderlying_symbol(underlying_symbol);
                    JSONObject subObjectFuture = jsonObject.optJSONObject(underlying_symbol);
                    int pre_volume_f = subObjectFuture.optInt("pre_volume");
                    String product_id_f = subObjectFuture.optString("product_id");
                    String ins_id_f = subObjectFuture.optString("ins_id");
                    searchEntity.setPre_volume(pre_volume_f);
                    searchEntity.setProduct_id(product_id_f);
                    searchEntity.setIns_id(ins_id_f);
                    //主力合约页直接显示标的合约
                    quoteEntity.setInstrument_id(underlying_symbol);
                    sMainInsList.put(underlying_symbol, quoteEntity);
                    sMainInsListNameNav.put(underlying_symbol, ins_name.replace("主连", ""));
                }

                if ("FUTURE".equals(classN)) {
                    switch (exchange_id) {
                        case "SHFE"://上期所
                            if (!expired) sShangqiInsList.put(instrument_id, quoteEntity);
                            if (!sShangqiInsListNameNav.containsValue(product_short_name))
                                sShangqiInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("上海期货交易所");
                            break;
                        case "CZCE"://郑商所
                            if (!expired) sZhengzhouInsList.put(instrument_id, quoteEntity);
                            if (!sZhengzhouInsListNameNav.containsValue(product_short_name))
                                sZhengzhouInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("郑州商品交易所");
                            break;
                        case "DCE"://大商所
                            if (!expired) sDalianInsList.put(instrument_id, quoteEntity);
                            if (!sDalianInsListNameNav.containsValue(product_short_name))
                                sDalianInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("大连商品交易所");
                            break;
                        case "CFFEX"://中金所
                            if (!expired) sZhongjinInsList.put(instrument_id, quoteEntity);
                            if (!sZhongjinInsListNameNav.containsValue(product_short_name))
                                sZhongjinInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("中国金融期货交易所");
                            break;
                        case "INE"://上期能源
                            if (!expired) sNengyuanInsList.put(instrument_id, quoteEntity);
                            if (!sNengyuanInsListNameNav.containsValue(product_short_name))
                                sNengyuanInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("上海国际能源交易中心");
                            break;
                        default:
                            break;
                    }
                }

                if ("FUTURE_COMBINE".equals(classN)) {
                    String leg1_symbol = subObject.optString("leg1_symbol");
                    String leg2_symbol = subObject.optString("leg2_symbol");
                    searchEntity.setLeg1_symbol(leg1_symbol);
                    searchEntity.setLeg2_symbol(leg2_symbol);
                    JSONObject subObjectFuture = jsonObject.optJSONObject(leg1_symbol);
                    String product_short_name_leg = subObjectFuture.optString("product_short_name");
                    String py_leg = subObjectFuture.optString("py");
                    searchEntity.setPy(py_leg);
                    switch (exchange_id) {
                        case "CZCE":
                            if (!expired) sZhengzhouzuheInsList.put(instrument_id, quoteEntity);
                            if (!sZhengzhouzuheInsListNameNav.containsValue(product_short_name_leg))
                                sZhengzhouzuheInsListNameNav.put(instrument_id, product_short_name_leg);
                            searchEntity.setExchangeName("郑州商品交易所");
                            break;
                        case "DCE":
                            if (!expired) sDalianzuheInsList.put(instrument_id, quoteEntity);
                            if (!sDalianzuheInsListNameNav.containsValue(product_short_name_leg))
                                sDalianzuheInsListNameNav.put(instrument_id, product_short_name_leg);
                            searchEntity.setExchangeName("大连商品交易所");
                            break;
                        default:
                            break;
                    }
                }

                SEARCH_ENTITIES.put(instrument_id, searchEntity);
            }
            LogUtils.e("合约列表解析结束", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约列表
     */
    public static Map<String, QuoteEntity> getOptionalInsList() {
        List<String> insList = readInsListFromFile();
        sOptionalInsList.clear();
        for (String ins :
                insList) {
            QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(ins);
            if (quoteEntity == null) {
                quoteEntity = new QuoteEntity();
                quoteEntity.setInstrument_id(ins);
            }
            sOptionalInsList.put(ins, quoteEntity);
        }
        return sOptionalInsList;
    }

    public static Map<String, QuoteEntity> getMainInsList() {
        return sMainInsList;
    }

    public static Map<String, QuoteEntity> getShangqiInsList() {
        return sShangqiInsList;
    }

    public static Map<String, QuoteEntity> getDalianInsList() {
        return sDalianInsList;
    }

    public static Map<String, QuoteEntity> getZhengzhouInsList() {
        return sZhengzhouInsList;
    }

    public static Map<String, QuoteEntity> getZhongjinInsList() {
        return sZhongjinInsList;
    }

    public static Map<String, QuoteEntity> getNengyuanInsList() {
        return sNengyuanInsList;
    }

    public static Map<String, QuoteEntity> getDalianzuheInsList() {
        return sDalianzuheInsList;
    }

    public static Map<String, QuoteEntity> getZhengzhouzuheInsList() {
        return sZhengzhouzuheInsList;
    }

    public static Map<String, String> getMainInsListNameNav() {
        return sMainInsListNameNav;
    }

    public static Map<String, String> getShangqiInsListNameNav() {
        return sShangqiInsListNameNav;
    }

    public static Map<String, String> getDalianInsListNameNav() {
        return sDalianInsListNameNav;
    }

    public static Map<String, String> getZhengzhouInsListNameNav() {
        return sZhengzhouInsListNameNav;
    }

    public static Map<String, String> getZhongjinInsListNameNav() {
        return sZhongjinInsListNameNav;
    }

    public static Map<String, String> getNengyuanInsListNameNav() {
        return sNengyuanInsListNameNav;
    }

    public static Map<String, String> getDalianzuheInsListNameNav() {
        return sDalianzuheInsListNameNav;
    }

    public static Map<String, String> getZhengzhouzuheInsListNameNav() {
        return sZhengzhouzuheInsListNameNav;
    }

    public static Map<String, SearchEntity> getSearchEntities() {
        return SEARCH_ENTITIES;
    }

    public static Map<String, SearchEntity> getSearchEntitiesHistory() {
        return SEARCH_ENTITIES_HISTORY;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据最新价与昨收获取合约涨跌幅
     */
    public static String getUpDownRate(String latest, String preClose) {
        return MathUtils.round(MathUtils.multiply(MathUtils.divide(MathUtils.subtract(latest, preClose), preClose), "100"), 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据最新价与昨收获取合约涨跌
     */
    public static String getUpDown(String latest, String preSettlement) {
        return MathUtils.subtract(latest, preSettlement);
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据ptick保存对应的小数点后位数, 为了不给quote中的整数添加小数位, 用"."来判断原数是小数还是整数
     */
    public static String saveScaleByPtick(String data, String instrumentId) {
        SearchEntity searchEntity = SEARCH_ENTITIES.get(instrumentId);
        if (searchEntity == null || data == null || !data.matches("-?\\d+(\\.\\d+)?"))
            return MathUtils.round(data, 1);
        try {
            int pTick_decs = Integer.valueOf(searchEntity.getpTick_decs());
            return MathUtils.round(data, pTick_decs);
        } catch (Exception e) {
            e.printStackTrace();
            return MathUtils.round(data, 1);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据ptick加一位保存对应的小数点后位数,对应开仓均价
     */
    public static String saveScaleByPtickA(String data, String instrumentId) {
        SearchEntity searchEntity = SEARCH_ENTITIES.get(instrumentId);
        if (searchEntity == null) return MathUtils.round(data, 2);
        try {
            int pTick_decs = Integer.valueOf(searchEntity.getpTick_decs()) + 1;
            return MathUtils.round(data, pTick_decs);
        } catch (Exception e) {
            e.printStackTrace();
            return MathUtils.round(data, 2);
        }
    }

    public static int setTextViewColor(String data, String pre_settlement) {
        try {
            float value = Float.parseFloat(data) - Float.parseFloat(pre_settlement);
            if (value < 0) return ContextCompat.getColor(BaseApplication.getContext(), R.color.ask);
            else if (value > 0)
                return ContextCompat.getColor(BaseApplication.getContext(), R.color.bid);
            else return ContextCompat.getColor(BaseApplication.getContext(), R.color.white);
        } catch (Exception e) {
            e.printStackTrace();
            return ContextCompat.getColor(BaseApplication.getContext(), R.color.white);
        }
    }


    /**
     * date: 2019/2/27
     * author: chenli
     * description: 获取组合两腿行情
     */
    public static List<String> getCombineInsList(List<String> data) {
        List<String> insList = new ArrayList<>();
        for (String ins : data) {
            insList.add(ins);
            if (ins.contains("&") && ins.contains(" ")) {
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
                if (searchEntity == null) continue;
                String leg1_symbol = searchEntity.getLeg1_symbol();
                String leg2_symbol = searchEntity.getLeg2_symbol();
                insList.add(leg1_symbol);
                insList.add(leg2_symbol);
            }
        }
        return insList;
    }


    /**
     * date: 2019/1/10
     * author: chenli
     * description: 计算组合部分行情
     */
    public static QuoteEntity calculateCombineQuotePart(QuoteEntity quoteEntity) {
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(quoteEntity.getInstrument_id());
        if (searchEntity == null) return quoteEntity;
        String leg1_symbol = searchEntity.getLeg1_symbol();
        String leg2_symbol = searchEntity.getLeg2_symbol();
        QuoteEntity quoteEntity_leg1 = DataManager.getInstance().getRtnData().getQuotes().get(leg1_symbol);
        QuoteEntity quoteEntity_leg2 = DataManager.getInstance().getRtnData().getQuotes().get(leg2_symbol);
        if (quoteEntity_leg1 == null || quoteEntity_leg2 == null) return quoteEntity;
        String last_leg1 = quoteEntity_leg1.getLast_price();
        String last_leg2 = quoteEntity_leg2.getLast_price();
        String ask_price_leg1 = quoteEntity_leg1.getAsk_price1();
        String ask_price_leg2 = quoteEntity_leg2.getAsk_price1();
        String ask_volume_leg1 = quoteEntity_leg1.getAsk_volume1();
        String ask_volume_leg2 = quoteEntity_leg2.getAsk_volume1();
        String bid_price_leg1 = quoteEntity_leg1.getBid_price1();
        String bid_price_leg2 = quoteEntity_leg2.getBid_price1();
        String bid_volume_leg1 = quoteEntity_leg1.getBid_volume1();
        String bid_volume_leg2 = quoteEntity_leg2.getBid_volume1();
        String pre_settlement_leg1 = quoteEntity_leg1.getPre_settlement();
        String pre_settlement_leg2 = quoteEntity_leg2.getPre_settlement();
        String volume_leg1 = quoteEntity_leg1.getVolume();
        String volume_leg2 = quoteEntity_leg2.getVolume();
        String open_interest_leg1 = quoteEntity_leg1.getOpen_interest();
        String open_interest_leg2 = quoteEntity_leg2.getOpen_interest();
        String last = MathUtils.subtract(last_leg1, last_leg2);
        String ask_price = MathUtils.subtract(ask_price_leg1, bid_price_leg2);
        String ask_volume = MathUtils.min(ask_volume_leg1, bid_volume_leg2);
        String bid_price = MathUtils.subtract(bid_price_leg1, ask_price_leg2);
        String bid_volume = MathUtils.min(bid_volume_leg1, ask_volume_leg2);
        String pre_settlement = MathUtils.subtract(pre_settlement_leg1, pre_settlement_leg2);
        String volume = MathUtils.min(volume_leg1, volume_leg2);
        String open_interest = MathUtils.min(open_interest_leg1, open_interest_leg2);
        quoteEntity.setLast_price(last);
        quoteEntity.setAsk_price1(ask_price);
        quoteEntity.setBid_price1(bid_price);
        quoteEntity.setAsk_volume1(ask_volume);
        quoteEntity.setBid_volume1(bid_volume);
        quoteEntity.setPre_settlement(pre_settlement);
        quoteEntity.setVolume(volume);
        quoteEntity.setOpen_interest(open_interest);
        return quoteEntity;
    }

    /**
     * date: 2019/1/10
     * author: chenli
     * description: 计算组合完整行情
     */
    public static QuoteEntity calculateCombineQuoteFull(QuoteEntity quoteEntity) {
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(quoteEntity.getInstrument_id());
        if (searchEntity == null) return quoteEntity;
        String leg1_symbol = searchEntity.getLeg1_symbol();
        String leg2_symbol = searchEntity.getLeg2_symbol();
        QuoteEntity quoteEntity_leg1 = DataManager.getInstance().getRtnData().getQuotes().get(leg1_symbol);
        QuoteEntity quoteEntity_leg2 = DataManager.getInstance().getRtnData().getQuotes().get(leg2_symbol);
        if (quoteEntity_leg1 == null || quoteEntity_leg2 == null) return quoteEntity;
        String last_leg1 = quoteEntity_leg1.getLast_price();
        String last_leg2 = quoteEntity_leg2.getLast_price();
        String ask_price_leg1 = quoteEntity_leg1.getAsk_price1();
        String ask_price_leg2 = quoteEntity_leg2.getAsk_price1();
        String ask_volume_leg1 = quoteEntity_leg1.getAsk_volume1();
        String ask_volume_leg2 = quoteEntity_leg2.getAsk_volume1();
        String bid_price_leg1 = quoteEntity_leg1.getBid_price1();
        String bid_price_leg2 = quoteEntity_leg2.getBid_price1();
        String bid_volume_leg1 = quoteEntity_leg1.getBid_volume1();
        String bid_volume_leg2 = quoteEntity_leg2.getBid_volume1();
        String open_leg1 = quoteEntity_leg1.getOpen();
        String open_leg2 = quoteEntity_leg2.getOpen();
        String average_leg1 = quoteEntity_leg1.getAverage();
        String average_leg2 = quoteEntity_leg2.getAverage();
        String pre_close_leg1 = quoteEntity_leg1.getPre_close();
        String pre_close_leg2 = quoteEntity_leg2.getPre_close();
        String pre_settlement_leg1 = quoteEntity_leg1.getPre_settlement();
        String pre_settlement_leg2 = quoteEntity_leg2.getPre_settlement();
        String settlement_leg1 = quoteEntity_leg1.getSettlement();
        String settlement_leg2 = quoteEntity_leg2.getSettlement();
        String volume_leg1 = quoteEntity_leg1.getVolume();
        String volume_leg2 = quoteEntity_leg2.getVolume();
        String open_interest_leg1 = quoteEntity_leg1.getOpen_interest();
        String open_interest_leg2 = quoteEntity_leg2.getOpen_interest();
        String last = MathUtils.subtract(last_leg1, last_leg2);
        String ask_price = MathUtils.subtract(ask_price_leg1, bid_price_leg2);
        String ask_volume = MathUtils.min(ask_volume_leg1, bid_volume_leg2);
        String bid_price = MathUtils.subtract(bid_price_leg1, ask_price_leg2);
        String bid_volume = MathUtils.min(bid_volume_leg1, ask_volume_leg2);
        String open = MathUtils.subtract(open_leg1, open_leg2);
        String average = MathUtils.subtract(average_leg1, average_leg2);
        String pre_close = MathUtils.subtract(pre_close_leg1, pre_close_leg2);
        String pre_settlement = MathUtils.subtract(pre_settlement_leg1, pre_settlement_leg2);
        String settlement = MathUtils.subtract(settlement_leg1, settlement_leg2);
        String volume = MathUtils.min(volume_leg1, volume_leg2);
        String open_interest = MathUtils.min(open_interest_leg1, open_interest_leg2);
        quoteEntity.setLast_price(last);
        quoteEntity.setAsk_price1(ask_price);
        quoteEntity.setBid_price1(bid_price);
        quoteEntity.setAsk_volume1(ask_volume);
        quoteEntity.setBid_volume1(bid_volume);
        quoteEntity.setOpen(open);
        quoteEntity.setAverage(average);
        quoteEntity.setPre_close(pre_close);
        quoteEntity.setSettlement(settlement);
        quoteEntity.setPre_settlement(pre_settlement);
        quoteEntity.setVolume(volume);
        quoteEntity.setOpen_interest(open_interest);
        return quoteEntity;
    }

    public static Map<String, KlineEntity> getCombineLeg1KLines(String ins) {
        Map<String, KlineEntity> klineEntities = new HashMap<>();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
        if (searchEntity == null) return klineEntities;
        String leg1_symbol = searchEntity.getLeg1_symbol();
        Map<String, KlineEntity> klineEntities_leg1 = DataManager.getInstance().getRtnData().getKlines().get(leg1_symbol);
        if (klineEntities_leg1 == null) return klineEntities;
        return klineEntities_leg1;
    }

    public static Map<String, KlineEntity.DataEntity> calculateCombineKLineData(String ins, String klineType) {
        Map<String, KlineEntity.DataEntity> dataEntities = new HashMap<>();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
        if (searchEntity == null) return dataEntities;
        String leg1_symbol = searchEntity.getLeg1_symbol();
        String leg2_symbol = searchEntity.getLeg2_symbol();
        Map<String, KlineEntity> klineEntities_leg1 = DataManager.getInstance().getRtnData().getKlines().get(leg1_symbol);
        Map<String, KlineEntity> klineEntities_leg2 = DataManager.getInstance().getRtnData().getKlines().get(leg2_symbol);
        if (klineEntities_leg1 == null || klineEntities_leg2 == null) return dataEntities;
        KlineEntity klineEntity_leg1 = klineEntities_leg1.get(klineType);
        KlineEntity klineEntity_leg2 = klineEntities_leg2.get(klineType);
        if (klineEntity_leg1 == null || klineEntity_leg2 == null) return dataEntities;
        Map<String, KlineEntity.DataEntity> dataEntities_leg1 = klineEntity_leg1.getData();
        Map<String, KlineEntity.DataEntity> dataEntities_leg2 = klineEntity_leg2.getData();
        if (dataEntities_leg1.isEmpty() || dataEntities_leg2.isEmpty()) return dataEntities;
        KlineEntity.BindingEntity bindingEntity = klineEntity_leg1.getBinding().get(leg2_symbol);
        if (bindingEntity == null) return dataEntities;
        Map<String, String> bindingData = bindingEntity.getBindingData();
        if (bindingData.isEmpty()) return dataEntities;
        for (Map.Entry<String, KlineEntity.DataEntity> entry : dataEntities_leg1.entrySet()) {
            String key_leg1 = entry.getKey();
            String key_leg2 = bindingData.get(key_leg1);
            KlineEntity.DataEntity dataEntity_leg2 = dataEntities_leg2.get(key_leg2);
            KlineEntity.DataEntity dataEntity_leg1 = entry.getValue();
            KlineEntity.DataEntity dataEntity = new KlineEntity.DataEntity();
            String datetime = dataEntity_leg1.getDatetime();
            String open = MathUtils.subtract(dataEntity_leg1.getOpen(), dataEntity_leg2.getOpen());
            String high = MathUtils.subtract(dataEntity_leg1.getHigh(), dataEntity_leg2.getHigh());
            String low = MathUtils.subtract(dataEntity_leg1.getLow(), dataEntity_leg2.getLow());
            String close = MathUtils.subtract(dataEntity_leg1.getClose(), dataEntity_leg2.getClose());
            String volume = MathUtils.min(dataEntity_leg1.getVolume(), dataEntity_leg2.getVolume());
            dataEntity.setDatetime(datetime);
            dataEntity.setOpen(open);
            dataEntity.setHigh(high);
            dataEntity.setLow(low);
            dataEntity.setClose(close);
            dataEntity.setVolume(volume);
            dataEntities.put(key_leg1, dataEntity);
        }
        return dataEntities;
    }

    /**
     * 保存合约列表字符串到本地文件
     */
    public static void saveInsListToFile(List<String> insList) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(BaseApplication.getContext().openFileOutput(OPTIONAL_INS_LIST, MODE_PRIVATE));
            out.writeObject(insList);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取本地文件合约列表
     */
    private static List<String> readInsListFromFile() {
        List<String> insList = new ArrayList<>();
        //打开文件输入流
        //读取文件内容
        try {
            ObjectInputStream in = new ObjectInputStream(BaseApplication.getContext().openFileInput(OPTIONAL_INS_LIST));
            insList.addAll((ArrayList<String>) in.readObject());
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return insList;
    }

    /**
     * 读取本地文件
     */

    public static String readFile(String fileName) {
        StringBuilder sb = new StringBuilder("");
        try {
            //打开文件输入流
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(BaseApplication.getContext().openFileInput(fileName), "UTF-8"));
            //读取文件内容:
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            //关闭输入流
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //写数据
    public static void writeFile(String fileName, String data) {
        try {

            FileOutputStream fout = BaseApplication.getContext().openFileOutput(fileName, Context.MODE_APPEND);

            byte[] bytes = data.getBytes();

            fout.write(bytes);

            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 根据不同软件版本获取期货公司列表
     */
    public static List<String> getBrokerIdFromBuildConfig(String[] brokerIdOrigin) {
        List<String> brokerList = new ArrayList<>();
        if (brokerIdOrigin != null) {
            if (CommonConstants.KUAI_QI_XIAO_Q.equals(BuildConfig.BROKER_ID)) {
                brokerList.addAll(Arrays.asList(brokerIdOrigin));
            } else {
                for (int i = 0; i < brokerIdOrigin.length; i++) {
                    if (brokerIdOrigin[i] != null && brokerIdOrigin[i].contains(BuildConfig.BROKER_ID)) {
                        brokerList.add(brokerIdOrigin[i]);
                    }
                }
            }
        } else if (BaseApplication.getWebSocketService() != null)
            BaseApplication.getWebSocketService().reConnectTD();

        return brokerList;
    }

    public static void insertLogToDB(String jsonString) {
        LogEntity entity = new LogEntity();
        entity.setJsonString(jsonString);
        Date date = new Date();
        entity.setTimestamp(new Long(date.getTime()));
        SLSDatabaseManager.getInstance().insertRecordIntoDB(entity);
    }

    public static void deleteLogDB() {
        Context context = BaseApplication.getContext();
        if (SPUtils.contains(context, CommonConstants.CONFIG_LOGIN_DATE)) {
            String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
            if (!TimeUtils.getNowTime().equals(date)) {
                List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
                for (LogEntity logEntity : list) {
                    SLSDatabaseManager.getInstance().deleteRecordFromDB(logEntity);
                }
            }
        }
    }

}
