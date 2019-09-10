package com.shinnytech.futures.constants;

public final class CommonConstants {
    //服务器地址
    public static String TRANSACTION_URL = "wss://opentd.shinnytech.com/trade/user0";
    public static String JSON_FILE_URL = "https://openmd.shinnytech.com/t/md/symbols/latest.json";
    public static final String MARKET_URL_1 = "wss://openmd.shinnytech.com/t/md/front/mobile";
    public static final String MARKET_URL_2 = "wss://139.198.126.116/t/md/front/mobile";
    public static final String MARKET_URL_3 = "wss://139.198.122.80/t/md/front/mobile";
    public static final String MARKET_URL_4 = "wss://139.198.123.206/t/md/front/mobile";
    public static final String FEED_BACK_URL = "https://ask.shinnytech.com/src/indexm.html";
    //导航栏
    public static final String OPTIONAL = "自选合约";
    public static final String DOMINANT = "主力合约";
    public static final String SHANGHAI = "上期所";
    public static final String NENGYUAN = "上期能源";
    public static final String DALIAN = "大商所";
    public static final String ZHENGZHOU = "郑商所";
    public static final String ZHONGJIN = "中金所";
    public static final String DALIANZUHE = "大连组合";
    public static final String ZHENGZHOUZUHE = "郑州组合";
    public static final String ACCOUNT_DETAIL = "账户详情";
    public static final String LOGIN = "登录交易";
    public static final String LOGOUT = "退出交易";
    public static final String SETTING = "选项设置";
    public static final String OPTIONAL_SETTING = "自选管理";
    public static final String ACCOUNT = "资金详情";
    public static final String PASSWORD = "修改密码";
    public static final String TRANSFER_IN = "资金转入";
    public static final String TRANSFER_OUT = "资金转出";
    public static final String OPEN_ACCOUNT = "在线开户";
    public static final String CONDITIONAL_ORDER = "云条件单";
    public static final String MANAGER_CONDITIONAL_ORDER = "条件单管理";
    public static final String NEW_CONDITIONAL_ORDER = "条件单";
    public static final String HISTORY_CONDITIONAL_ORDER = "历史条件";
    public static final String BREAK_EVEN_CONDITIONAL_ORDER = "止盈止损";
    public static final String FEEDBACK = "问题反馈";
    public static final String SHINNYTECH = "信易科技";
    public static final String ABOUT = "关于";
    public static final String OFFLINE = "网络故障";
    public static final String BROKER_LIST = "期货公司";
    public static final String MENU_TITLE_NAVIGATION = "navigation";
    public static final String MENU_TITLE_COLLECT = "collect";

    //页面跳转标志
    public static final int MAIN_ACTIVITY_TO_SEARCH_ACTIVITY = 1;
    public static final int MAIN_ACTIVITY_TO_SETTING_ACTIVITY = 2;
    public static final int MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY = 3;
    public static final int MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY = 4;
    public static final int LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY = 5;
    public static final int LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY = 6;
    public static final int KLINE_DURATION_ACTIVITY_TO_ADD_DURATION_ACTIVITY = 7;
    public static final int FUTURE_INFO_FRAGMENT_TO_CHART_SETTING_ACTIVITY = 8;
    public static final int CONDITION_ORDER_ACTIVITY_TO_SEARCH_ACTIVITY = 9;
    public static final String INS_BETWEEN_ACTIVITY = "instrument_id";
    public static final String DIRECTION_BETWEEN_ACTIVITY = "direction";
    public static final String VOLUME_BETWEEN_ACTIVITY = "volume";
    public static final String SOURCE_ACTIVITY = "source_activity";
    public static final String SOURCE_ACTIVITY_MAIN = "source_activity_main";
    public static final String SOURCE_ACTIVITY_FUTURE_INFO = "source_activity_future_info";
    public static final String SOURCE_ACTIVITY_CONDITION_ORDER = "source_activity_condition";
    //推荐标的
    public static final String RECOMMEND_INS = "螺纹,豆粕,菜粕,甲醇,铁矿,沥青,焦炭," +
            "PP,PTA,白糖,燃油,橡胶";
    //app名称
    public static final String KUAI_QI_XIAO_Q = "快期小Q";
    //本地自选合约文件名
    public static final String OPTIONAL_INS_LIST = "optionalInsList";
    //银期转帐方向
    public static final String TRANSFER_DIRECTION = "transferDirection";

    //登录页
    public static final String BROKER_ID_VISITOR = "游客";
    public static final String BROKER_ID_SIMULATION = "快期模拟";
    public static final String BROKER_ID_SIMNOW = "simnow";
}
