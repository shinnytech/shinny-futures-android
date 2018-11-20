package com.shinnytech.futures.model.engine;

import android.content.Context;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
            }catch (Exception e){
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
        String latest = readLatestFile(latestFile.getName());
        if (latest == null) return;
        try {
            jsonObject = new JSONObject(latest);
            Iterator<String> instrumentIds = jsonObject.keys();
            while (instrumentIds.hasNext()) {
                String instrument_id = instrumentIds.next();
                JSONObject subObject = jsonObject.getJSONObject(instrument_id);
                String classN = subObject.optString("class");
                if (!"FUTURE_CONT".equals(classN) && !"FUTURE".equals(classN) && !"FUTURE_COMBINE".equals(classN)) {
                    continue;
                }
                String ins_name = subObject.optString("ins_name");
                String exchange_id = subObject.optString("exchange_id");
                String price_tick = subObject.optString("price_tick");
                String price_decs = subObject.optString("price_decs");
                String volume_multiple = subObject.optString("volume_multiple");
                String sort_key = subObject.optString("sort_key");
                String product_short_name = subObject.optString("product_short_name");
                String py = subObject.optString("py");
                String margin = subObject.optString("margin");
                boolean expired = subObject.optBoolean("expired");
                int pre_volume = subObject.optInt("pre_volume");
                SearchEntity searchEntity = new SearchEntity();
                searchEntity.setpTick(price_tick);
                searchEntity.setpTick_decs(price_decs);
                searchEntity.setInstrumentId(instrument_id);
                searchEntity.setInstrumentName(ins_name);
                searchEntity.setVm(volume_multiple);
                searchEntity.setExchangeId(exchange_id);
                searchEntity.setSort_key(sort_key);
                searchEntity.setPy(py);
                searchEntity.setMargin(margin);
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
                    searchEntity.setPre_volume(pre_volume_f);
                    sMainInsList.put(instrument_id, quoteEntity);
                    sMainInsListNameNav.put(instrument_id, ins_name.replace("主连", ""));
                }

                if ("FUTURE".equals(classN)) {
                    switch (exchange_id) {
                        case "SHFE"://上期所
                            if (!expired)sShangqiInsList.put(instrument_id, quoteEntity);
                            if (!sShangqiInsListNameNav.containsValue(product_short_name))
                                sShangqiInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("上海期货交易所");
                            break;
                        case "CZCE"://郑商所
                            if (!expired)sZhengzhouInsList.put(instrument_id, quoteEntity);
                            if (!sZhengzhouInsListNameNav.containsValue(product_short_name))
                                sZhengzhouInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("郑州商品交易所");
                            break;
                        case "DCE"://大商所
                            if (!expired)sDalianInsList.put(instrument_id, quoteEntity);
                            if (!sDalianInsListNameNav.containsValue(product_short_name))
                                sDalianInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("大连商品交易所");
                            break;
                        case "CFFEX"://中金所
                            if (!expired)sZhongjinInsList.put(instrument_id, quoteEntity);
                            if (!sZhongjinInsListNameNav.containsValue(product_short_name))
                                sZhongjinInsListNameNav.put(instrument_id, product_short_name);
                            searchEntity.setExchangeName("中国金融期货交易所");
                            break;
                        case "INE"://上期能源
                            if (!expired)sNengyuanInsList.put(instrument_id, quoteEntity);
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
                    JSONObject subObjectFuture = jsonObject.optJSONObject(leg1_symbol);
                    String product_short_name_leg = subObjectFuture.optString("product_short_name");
                    String py_leg = subObjectFuture.optString("py");
                    searchEntity.setPy(py_leg);
                    switch (exchange_id) {
                        case "CZCE":
                            if (!expired)sZhengzhouzuheInsList.put(instrument_id, quoteEntity);
                            if (!sZhengzhouzuheInsListNameNav.containsValue(product_short_name_leg))
                                sZhengzhouzuheInsListNameNav.put(instrument_id, product_short_name_leg);
                            searchEntity.setExchangeName("郑州商品交易所");
                            break;
                        case "DCE":
                            if (!expired)sDalianzuheInsList.put(instrument_id, quoteEntity);
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
    public static String getUpDown(String latest, String preClose) {
        return MathUtils.subtract(latest, preClose);
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

    /**
     * 保存合约列表字符串到本地文件
     */
    public static void saveInsListToFile(List<String> insList) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(BaseApplication.getContext().openFileOutput(OPTIONAL_INS_LIST, Context.MODE_PRIVATE));
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

    private static String readLatestFile(String fileName) {
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

}
