package com.shinnytech.futures.constants;

public final class CommonConstants {
    //服务器地址
    public static final String MARKET_URL_1 = "ws://openmd.shinnytech.com/t/md/front/mobile";
    public static final String MARKET_URL_2 = "ws://139.198.126.116/t/md/front/mobile";
    public static final String MARKET_URL_3 = "ws://139.198.122.80/t/md/front/mobile";
    public static final String MARKET_URL_4 = "ws://139.198.123.206/t/md/front/mobile";
    public static final String MARKET_URL_5 = "ws://106.15.82.247/t/md/front/mobile";
    public static final String MARKET_URL_6 = "ws://106.15.82.189/t/md/front/mobile";
    public static final String MARKET_URL_7 = "ws://106.15.219.160/t/md/front/mobile";
    public static String TRANSACTION_URL = "ws://opentd.shinnytech.com/trade/user0";
    public static String JSON_FILE_URL = "http://openmd.shinnytech.com/t/md/symbols/latest.json";
    public static final String FEED_BACK_URL = "http://ask.shinnytech.com/src/indexm.html";

    //广播信息类型
    public static final String MD_ONLINE = "MD_ONLINE";
    public static final String MD_OFFLINE = "MD_OFFLINE";
    public static final String MD_MESSAGE = "MD_MESSAGE";
    public static final String TD_ONLINE = "TD_ONLINE";
    public static final String TD_OFFLINE = "TD_OFFLINE";
    public static final String TD_MESSAGE = "TD_MESSAGE";
    public static final String TD_MESSAGE_LOGIN = "TD_MESSAGE_LOGIN";
    public static final String TD_MESSAGE_SETTLEMENT = "TD_MESSAGE_SETTLEMENT";
    public static final String TD_MESSAGE_BROKER_INFO = "TD_MESSAGE_BROKER_INFO";

    //导航栏
    public static final String OPTIONAL = "自选合约";
    public static final String DOMINANT = "主力合约";
    public static final String SHANGHAI = "上海期货交易所";
    public static final String NENGYUAN = "上海能源";
    public static final String DALIAN = "大连商品交易所";
    public static final String ZHENGZHOU = "郑州商品交易所";
    public static final String ZHONGJIN = "中国金融期货交易所";
    public static final String DALIANZUHE = "大连组合";
    public static final String ZHENGZHOUZUHE = "郑州组合";
    public static final String ACCOUNT = "资金账户";
    public static final String DEAL = "成交记录";
    public static final String BANK = "银期转帐";
    public static final String FEEDBACK = "反馈";
    public static final String ABOUT = "关于";

    //行情图类型
    public static final String CURRENT_DAY_FRAGMENT = "当日";
    public static final String DAY_FRAGMENT = "日线";
    public static final String HOUR_FRAGMENT = "小时线";
    public static final String MINUTE_FRAGMENT = "5分钟线";
    public static final String KLINE_MINUTE = "300000000000";
    public static final String KLINE_HOUR = "3600000000000";
    public static final String KLINE_DAY = "86400000000000";
    public static final String CURRENT_DAY = "60000000000";

    //加载柱子个数
    public static final int VIEW_WIDTH = 200;
    //订阅合约数
    public static final int LOAD_QUOTE_NUM = 24;

    //页面跳转标志
    public static final String ACTIVITY_TYPE = "activity_type";
    public static final int POSITION_JUMP_TO_LOG_IN_ACTIVITY = 1;
    public static final int ORDER_JUMP_TO_LOG_IN_ACTIVITY = 2;
    public static final int TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY = 3;
    public static final int JUMP_TO_SEARCH_ACTIVITY = 4;
    public static final int JUMP_TO_FUTURE_INFO_ACTIVITY = 5;

    //app名称
    public static final String KUAI_QI_XIAO_Q = "快期小Q";

    //本地自选合约文件名
    public static final String OPTIONAL_INS_LIST = "optionalInsList";


    //EventBus通知
    public static final String LOG_OUT = "LOGOUT";
    public static final String BACKGROUND = "BACKGROUND";
    public static final String FOREGROUND = "FOREGROUND";

    //下单版价格类型
    public static final String LATEST_PRICE = "最新价";
    public static final String COUNTERPARTY_PRICE = "对手价";
    public static final String MARKET_PRICE = "市价";
    public static final String QUEUED_PRICE = "排队价";
    public static final String USER_PRICE = "用户设置价";

}
