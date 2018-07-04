package com.shinnytech.futures.model.engine;

import android.content.Context;

import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String OPTIONAL_INS_LIST = "optionalInsList";
    /**
     * date: 7/9/17
     * description: 搜索列表实例
     */
    private static final Map<String, SearchEntity> SEARCH_ENTITIES = new LinkedHashMap<>(1568);


    /**
     * date: 7/9/17
     * description: 主力合约列表
     */
    private static Map<String, String> sMainInsList = new LinkedHashMap<>(64);

    /**
     * date: 7/9/17
     * description: 主力合约导航
     */
    private static List<String> sMainInsListNameNav = new ArrayList(60);

    /**
     * date: 7/9/17
     * description: 上期合约列表
     */
    private static Map<String, String> sShangqiInsList = new LinkedHashMap<>(220);

    /**
     * date: 7/9/17
     * description: 上期导航
     */
    private static List<String> sShangqiInsListNameNav = new ArrayList<>(24);

    /**
     * date: 7/9/17
     * description: 大连合约列表
     */
    private static Map<String, String> sDalianInsList = new LinkedHashMap<>(224);

    /**
     * date: 7/9/17
     * description: 大连合约导航
     */
    private static List<String> sDalianInsListNameNav = new ArrayList<>(24);

    /**
     * date: 7/9/17
     * description: 郑州合约列表
     */
    private static Map<String, String> sZhengzhouInsList = new LinkedHashMap<>(200);

    /**
     * date: 7/9/17
     * description: 郑州合约列表导航
     */
    private static List<String> sZhengzhouInsListNameNav = new ArrayList<>(24);

    /**
     * date: 7/9/17
     * description: 中金合约列表
     */
    private static Map<String, String> sZhongjinInsList = new LinkedHashMap<>(28);

    /**
     * date: 7/9/17
     * description: 中金合约列表导航
     */
    private static List<String> sZhongjinInsListNameNav = new ArrayList<>();

    /**
     * date: 3/27/18
     * description: 能源合约列表
     */
    private static Map<String, String> sNengyuanInsList = new LinkedHashMap<>(28);

    /**
     * date: 3/27/18
     * description: 能源合约列表导航
     */
    private static List<String> sNengyuanInsListNameNav = new ArrayList<>();


    /**
     * date: 7/9/17
     * description: 大连组合列表
     */
    private static Map<String, String> sDalianzuheInsList = new LinkedHashMap<>(108);

    /**
     * date: 7/9/17
     * description: 大连组合列表导航
     */
    private static List<String> sDalianzuheInsListNameNav = new ArrayList<>(40);

    /**
     * date: 7/9/17
     * description: 郑州组合列表
     */
    private static Map<String, String> sZhengzhouzuheInsList = new LinkedHashMap<>(816);

    /**
     * date: 7/9/17
     * description: 郑州组合中文名列表
     */
    private static List<String> sZhengzhouzuheInsListNameNav = new ArrayList<>(28);

    /**
     * date: 7/9/17
     * description: 搜索列表实例历史
     */
    private static Map<String, SearchEntity> SEARCH_ENTITIES_HISTORY = new LinkedHashMap<>();

    /**
     * date: 7/9/17
     * description: 合约列表json字符串
     */
    private static String sLatest;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 开机初始化合约列表
     */
    public static void initInsList(File latestFile) {
        if (latestFile != null) sLatest = readLatestFile(latestFile.getName());
        try {
            JSONObject latestObject = new JSONObject(sLatest);
            String mainIns = latestObject.getString("active");
            JSONObject dataObject = latestObject.getJSONObject("data");
            JSONObject futureObject = dataObject.getJSONObject("future");
            Iterator<String> futureIds = futureObject.keys();
            while (futureIds.hasNext()) {
                String futureId = futureIds.next();
                JSONObject nObject = futureObject.getJSONObject(futureId).getJSONObject("n");
                String sn = nObject.getString("sn");
                String vm = nObject.getString("vm");
                String py = nObject.getString("py");
                String ei = nObject.getString("ei");
                String pTick = nObject.getString("ptick");
                JSONObject insObject = futureObject.getJSONObject(futureId).getJSONObject("Ins");
                int bRepeat = 0;
                int aRepeat = 0;
                Iterator<String> instrumentIds = insObject.keys();
                while (instrumentIds.hasNext()) {
                    String instrumentId = instrumentIds.next();
                    String instrumentName = sn + instrumentId.substring(futureId.length(), instrumentId.length());
                    SearchEntity searchEntity = new SearchEntity();
                    searchEntity.setpTick(pTick);
                    searchEntity.setInstrumentId(instrumentId);
                    searchEntity.setInstrumentName(instrumentName);
                    searchEntity.setPy(py);
                    searchEntity.setVm(vm);
                    searchEntity.setExchangeId(ei);
                    if (mainIns.contains(instrumentId)) {
                        sMainInsList.put(instrumentId, instrumentName);
                        if (aRepeat++ == 0) sMainInsListNameNav.add(sn);
                    }
                    switch (ei) {
                        case "SHFE"://上期所
                            sShangqiInsList.put(instrumentId, instrumentName);
                            if (bRepeat++ == 0) sShangqiInsListNameNav.add(sn);
                            searchEntity.setExchangeName("上海期货交易所");
                            break;
                        case "CZCE"://郑商所
                            sZhengzhouInsList.put(instrumentId, instrumentName);
                            if (bRepeat++ == 0) sZhengzhouInsListNameNav.add(sn);
                            searchEntity.setExchangeName("郑州商品交易所");
                            break;
                        case "DCE"://大商所
                            sDalianInsList.put(instrumentId, instrumentName);
                            if (bRepeat++ == 0) sDalianInsListNameNav.add(sn);
                            searchEntity.setExchangeName("大连商品交易所");
                            break;
                        case "CFFEX"://中金所
                            sZhongjinInsList.put(instrumentId, instrumentName);
                            if (bRepeat++ == 0) sZhongjinInsListNameNav.add(sn);
                            searchEntity.setExchangeName("中国金融期货交易所");
                            break;
                        case "INE"://上期能源
                            sNengyuanInsList.put(instrumentId, instrumentName);
                            if (bRepeat++ == 0) sNengyuanInsListNameNav.add(sn);
                            searchEntity.setExchangeName("上海国际能源交易中心");
                            break;
                        default:
                            break;
                    }
                    SEARCH_ENTITIES.put(instrumentId, searchEntity);
                }
            }

            JSONObject combineObject = dataObject.getJSONObject("combine");
            Iterator<String> combineIds = combineObject.keys();
            while (combineIds.hasNext()) {
                String combineId = combineIds.next();
                JSONObject nObject = combineObject.getJSONObject(combineId).getJSONObject("n");
                String ei = nObject.getString("ei");
                String pTick = nObject.getString("ptick");
                String vm = nObject.getString("vm");
                JSONObject insObject = combineObject.getJSONObject(combineId).getJSONObject("Ins");
                int bRepeat = 0;
                Iterator<String> instrumentIds = insObject.keys();
                while (instrumentIds.hasNext()) {
                    String instrumentId = instrumentIds.next();
                    JSONObject oneCombineObject = insObject.getJSONObject(instrumentId);
                    if (oneCombineObject.length() == 0) {
                        SearchEntity searchEntity = new SearchEntity();
                        searchEntity.setInstrumentId(instrumentId);
                        searchEntity.setInstrumentName(instrumentId);
                        //组合拼音没有
                        searchEntity.setPy("");
                        searchEntity.setpTick(pTick);
                        searchEntity.setVm(vm);
                        searchEntity.setExchangeId(ei);
                        switch (ei) {
                            case "CZCE":
                                sZhengzhouzuheInsList.put(instrumentId, instrumentId);
                                if (bRepeat++ == 0) sZhengzhouzuheInsListNameNav.add(instrumentId);
                                searchEntity.setExchangeName("郑州商品交易所");
                                break;
                            case "DCE":
                                sDalianzuheInsList.put(instrumentId, instrumentId);
                                if (bRepeat++ == 0) sDalianzuheInsListNameNav.add(instrumentId);
                                searchEntity.setExchangeName("大连商品交易所");
                                break;
                            default:
                                break;
                        }
                        SEARCH_ENTITIES.put(instrumentId, searchEntity);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取合约列表
     */
    public static Map<String, String> getOptionalInsList() {
        return readInsListFromFile();
    }

    public static Map<String, String> getMainInsList() {
        return sMainInsList;
    }

    public static Map<String, String> getShangqiInsList() {
        return sShangqiInsList;
    }

    public static Map<String, String> getDalianInsList() {
        return sDalianInsList;
    }

    public static Map<String, String> getZhengzhouInsList() {
        return sZhengzhouInsList;
    }

    public static Map<String, String> getZhongjinInsList() {
        return sZhongjinInsList;
    }

    public static Map<String, String> getNengyuanInsList() {
        return sNengyuanInsList;
    }

    public static Map<String, String> getDalianzuheInsList() {
        return sDalianzuheInsList;
    }

    public static Map<String, String> getZhengzhouzuheInsList() {
        return sZhengzhouzuheInsList;
    }

    public static List<String> getMainInsListNameNav() {
        return sMainInsListNameNav;
    }

    public static List<String> getShangqiInsListNameNav() {
        return sShangqiInsListNameNav;
    }

    public static List<String> getDalianInsListNameNav() {
        return sDalianInsListNameNav;
    }

    public static List<String> getZhengzhouInsListNameNav() {
        return sZhengzhouInsListNameNav;
    }

    public static List<String> getZhongjinInsListNameNav() {
        return sZhongjinInsListNameNav;
    }

    public static List<String> getNengyuanInsListNameNav() {
        return sNengyuanInsListNameNav;
    }

    public static List<String> getDalianzuheInsListNameNav() {
        return sDalianzuheInsListNameNav;
    }

    public static List<String> getZhengzhouzuheInsListNameNav() {
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
        return MathUtils.round(MathUtils.multiply("100", MathUtils.divide(MathUtils.subtract(latest, preClose), preClose)), 2, BigDecimal.ROUND_HALF_EVEN);
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
        if (searchEntity != null && data.contains(".")) {
            try {
                String pTick = searchEntity.getpTick();
                DecimalFormat decimalFormat = new DecimalFormat(pTick);
                return decimalFormat.format(Float.valueOf(data));
            } catch (Exception e) {
                e.printStackTrace();
                return data;
            }
        } else return data;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据ptick加一位保存对应的小数点后位数,对应开仓均价
     */
    public static String saveScaleByPtickA(String data, String instrumentId) {
        SearchEntity searchEntity = SEARCH_ENTITIES.get(instrumentId);
        if (searchEntity != null) {
            try {
                String pTick = searchEntity.getpTick();
                int scale = 0;
                if (pTick != null) {
                    if (pTick.contains(".")) scale = pTick.length() - pTick.indexOf(".");
                    else scale = 1;
                }
                return MathUtils.round(data, scale);
            } catch (Exception e) {
                e.printStackTrace();
                return MathUtils.round(data, 2);
            }
        } else return MathUtils.round(data, 2);
        // 组合目前找不到ptick,默认保留两位
    }

    /**
     * 保存合约列表字符串到本地文件
     */
    public static void saveInsListToFile(Map<String, String> insList) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(BaseApplicationLike.getContext().openFileOutput(OPTIONAL_INS_LIST, Context.MODE_PRIVATE));
            out.writeObject(insList);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取本地文件合约列表
     */
    private static Map<String, String> readInsListFromFile() {
        Map<String, String> insList = new LinkedHashMap<>();
        //打开文件输入流
        //读取文件内容
        try {
            ObjectInputStream in = new ObjectInputStream(BaseApplicationLike.getContext().openFileInput(OPTIONAL_INS_LIST));
            insList = (LinkedHashMap<String, String>) in.readObject();
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
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(BaseApplicationLike.getContext().openFileInput(fileName), "UTF-8"));
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
