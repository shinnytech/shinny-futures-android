package com.shinnytech.futures.constants;

public final class CommonConstants {
    //服务器地址
    public static String TRANSACTION_URL = "ws://opentd.shinnytech.com/trade/user0";
    public static String JSON_FILE_URL = "http://openmd.shinnytech.com/t/md/symbols/latest.json";
    public static final String MARKET_URL_1 = "ws://openmd.shinnytech.com/t/md/front/mobile";
    public static final String MARKET_URL_2 = "ws://139.198.126.116/t/md/front/mobile";
    public static final String MARKET_URL_3 = "ws://139.198.122.80/t/md/front/mobile";
    public static final String MARKET_URL_4 = "ws://139.198.123.206/t/md/front/mobile";
    public static final String FEED_BACK_URL = "https://ask.shinnytech.com/src/indexm.html";
    //广播信息类型
    public static final String MD_ONLINE = "MD_ONLINE";
    public static final String MD_OFFLINE = "MD_OFFLINE";
    public static final String MD_MESSAGE = "MD_MESSAGE";
    public static final String TD_ONLINE = "TD_ONLINE";
    public static final String TD_OFFLINE = "TD_OFFLINE";
    public static final String TD_MESSAGE = "TD_MESSAGE";
    public static final String TD_MESSAGE_LOGIN = "TD_MESSAGE_LOGIN";
    public static final String TD_MESSAGE_WEAK_PASSWORD = "TD_MESSAGE_WEAK_PASSWORD";
    public static final String TD_MESSAGE_SETTLEMENT = "TD_MESSAGE_SETTLEMENT";
    public static final String TD_MESSAGE_BROKER_INFO = "TD_MESSAGE_BROKER_INFO";
    public static final String TD_MESSAGE_CHANGE_SUCCESS = "TD_MESSAGE_CHANGE_SUCCESS";
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
    public static final String BANK_IN = "资金转入";
    public static final String BANK_OUT = "资金转出";
    public static final String OPEN_ACCOUNT = "在线开户";
    public static final String FEEDBACK = "问题反馈";
    public static final String ABOUT = "关于";
    public static final String OFFLINE = "交易、行情网络未连接！";
    public static final String BROKER_LIST = "期货公司";
    //行情图类型
    public static final String CHART_ID = "CHART_ID";
    public static final String CURRENT_DAY_FRAGMENT = "CURRENT_DAY_FRAGMENT";
    public static final String DAY_FRAGMENT = "DAY_FRAGMENT";
    public static final String HOUR_FRAGMENT = "HOUR_FRAGMENT";
    public static final String MINUTE_FRAGMENT = "MINUTE_FRAGMENT";
    public static final String SECOND_FRAGMENT = "SECOND_FRAGMENT";
    public static final String CURRENT_DAY = "60000000000";
    public static final String KLINE_3_SECOND = "3000000000";
    public static final String KLINE_5_SECOND = "5000000000";
    public static final String KLINE_10_SECOND = "10000000000";
    public static final String KLINE_15_SECOND = "15000000000";
    public static final String KLINE_20_SECOND = "20000000000";
    public static final String KLINE_30_SECOND = "30000000000";
    public static final String KLINE_1_MINUTE = "60000000000";
    public static final String KLINE_2_MINUTE = "120000000000";
    public static final String KLINE_3_MINUTE = "180000000000";
    public static final String KLINE_5_MINUTE = "300000000000";
    public static final String KLINE_10_MINUTE = "600000000000";
    public static final String KLINE_15_MINUTE = "900000000000";
    public static final String KLINE_30_MINUTE = "1800000000000";
    public static final String KLINE_1_HOUR = "3600000000000";
    public static final String KLINE_2_HOUR = "7200000000000";
    public static final String KLINE_4_HOUR = "14400000000000";
    public static final String KLINE_1_DAY = "86400000000000";
    public static final String KLINE_7_DAY = "604800000000000";
    public static final String KLINE_28_DAY = "2419200000000000";
    //缩放大小
    public static final String SCALE_X = "mScaleX";
    //加载柱子个数
    public static final int VIEW_WIDTH = 200;
    //订阅合约数
    public static final int LOAD_QUOTE_NUM = 24;
    public static final int LOAD_QUOTE_RECOMMEND_NUM = 10;
    //页面跳转标志
    public static final int JUMP_TO_SEARCH_ACTIVITY = 1;
    public static final int JUMP_TO_FUTURE_INFO_ACTIVITY = 2;
    public static final int LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY = 3;
    public static final int KLINE_DURATION_ACTIVITY_TO_ADD_DURATION_ACTIVITY = 4;
    public static final int LOGIN_JUMP_TO_CHANGE_PASSWORD_ACTIVITY = 5;
    public static final int FUTURE_INFO_ACTIVITY_TO_COMMON_SWITCH_ACTIVITY = 6;
    public static final int MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY = 7;
    public static final String BACK_TO_ACCOUNT_DETAIL = "checkAccount";
    public static final String INS_BETWEEN_ACTIVITY = "instrument_id";
    //app名称
    public static final String KUAI_QI_XIAO_Q = "快期小Q";
    //本地自选合约文件名
    public static final String OPTIONAL_INS_LIST = "optionalInsList";
    //银期转帐方向
    public static final String TRANSFER_DIRECTION = "transferDirection";

    //登录页
    public static final String BROKER_ID_VISITOR = "游客";
    public static final String BROKER_ID_SIMULATION = "快期模拟";
    public static final String[] BROKERS_LOCAL = new String[]{"A安粮期货", "B渤海期货", "B宝城期货", "B北京首创", "B倍特期货", "C长安期货", "C长城期货", "C长江期货", "D大地期货", "D大越期货", "D东航期货", "D大陆期货", "D德盛期货", "D东吴期货", "D东证期货", "D东华期货", "D东方财富", "F方正中期", "G广发期货", "G光大期货", "G国际期货", "G国投安信", "G国富期货", "G国金期货", "G国元期货", "G广金期货", "G国贸期货", "G国泰君安", "G广州期货", "H华安期货", "H华泰期货", "H海通期货", "H海证期货", "H华西期货", "H混沌天成", "H华鑫期货", "H华信期货", "H和合期货", "H恒泰期货", "H弘业期货", "H徽商期货", "H宏源期货", "H海航期货", "J金石期货", "J金元期货", "J建信期货", "J金瑞期货", "J金信期货", "J锦泰期货", "J江海汇鑫", "L良运期货", "L鲁证期货", "M迈科期货", "M美尔雅期货", "N南华期货", "Q前海期货", "S申万期货", "S上海中期", "S上海东方", "S上海东亚", "S盛达期货", "S山西三立期货", "R瑞达期货", "T铜冠金源", "T天鸿期货_主席", "T天鸿期货_次席", "T天富期货", "T天风期货_主席", "T天风期货_二席", "T通惠期货", "W五矿经易", "X先锋期货", "X兴证期货", "X兴业期货", "X新湖期货", "X新世纪期货", "X先融期货", "X西部期货", "Y银河期货", "Y一德期货CTP", "Y英大期货", "Y永安期货", "Z中信建投", "Z中融汇信", "Z招金期货", "Z中财期货", "Z中钢期货", "Z中银国际", "Z中辉期货", "Z中信期货", "Z中天期货", "Z中粮期货", "Z中州期货", "simnow", "中信模拟", "海通股指仿真"};
    //设置页
    public static final String CONFIG_KLINE_DURATION_DEFAULT = "klineDurationDefault";
    public static final String CONFIG_PARA_MA = "ma";
    public static final String CONFIG_INSERT_ORDER_CONFIRM = "orderConfirm";
    public static final String CONFIG_CANCEL_ORDER_CONFIRM = "cancelOrderConfirm";
    public static final String CONFIG_POSITION_LINE = "isPosition";
    public static final String CONFIG_ORDER_LINE = "isPending";
    public static final String CONFIG_AVERAGE_LINE = "isAverage";
    public static final String CONFIG_MD5 = "isMD5";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_ACCOUNT = "phone";
    public static final String CONFIG_BROKER = "brokerName";
    public static final String CONFIG_LOGIN_DATE = "loginDate";
    public static final String CONFIG_RECOMMEND_OPTIONAL = "recommendOptional";
    //设置页信息
    public static final String CHART_SETTING = "图表设置";
    public static final String TRANSACTION_SETTING = "交易设置";
    public static final String SYSTEM_SETTING = "系统设置";
    public static final String SUB_SETTING_TYPE = "子菜单类型";
    public static final String PARA_CHANGE = "指标参数修改";
    public static final String KLINE_DURATION_SETTING = "K线周期设置";
    public static final String COMMON_SWITCH_SETTING = "常用开关";
    public static final String KLINE_DURATION_ADD = "添加常用周期";
    public static final String INSERT_ORDER_CONFIRM = "下单确认";
    public static final String CANCEL_ORDER_CONFIRM = "撤单确认";
    public static final String UPLOAD_LOG = "上传运行日志";
    public static final String COMMON_SWITCH = "常用开关";
    public static final String PARA_MA = "5,10,20,60,0,0";
    public static final String KLINE_DURATION_DEFAULT = "1分,5分,15分,30分,1日,1周";
    public static final String KLINE_DURATION_ALL = "3秒,5秒,10秒,15秒,20秒,30秒,1分,2分," +
            "3分,5分,10分,15分,30分,1时,2时,4时,1日,1周,4周";
    public static final String KLINE_DURATION_DAY = "分时";

    //交易页
    public static final String ACTION_OPEN_BUY = "买多";
    public static final String ACTION_ADD_BUY = "加多";
    public static final String ACTION_OPEN_SELL = "卖空";
    public static final String ACTION_ADD_SELL = "加空";
    public static final String ACTION_CLOSE_BUY = "平多";
    public static final String ACTION_CLOSE_SELL = "平空";
    public static final String ACTION_LOCK = "锁仓";
    public static final String STATUS_FIRST_OPEN_FIRST_CLOSE = "先开先平";
    public static final String STATUS_LOCK = "锁仓状态";
    public static final String STATUS_ALIVE = "ALIVE";
    public static final String PRICE_TYPE_LIMIT = "LIMIT";

    public static final String DIRECTION_BUY = "BUY";
    public static final String DIRECTION_SELL = "SELL";
    public static final String DIRECTION_BUY_ZN = "多";
    public static final String DIRECTION_SELL_ZN = "空";
    public static final String DIRECTION_BOTH_ZN = "双向";

    public static final String OFFSET_OPEN = "OPEN";
    public static final String OFFSET_CLOSE = "CLOSE";
    public static final String OFFSET_CLOSE_TODAY = "CLOSETODAY";
    public static final String OFFSET_CLOSE_HISTORY = "CLOSEHISTORY";
    public static final String OFFSET_CLOSE_FORCE = "FORCECLOSE";
    public static final String OFFSET_OPEN_ZN = "开仓";
    public static final String OFFSET_CLOSE_ZN = "平仓";
    public static final String OFFSET_CLOSE_TODAY_ZN = "平今";
    public static final String OFFSET_CLOSE_HISTORY_ZN = "平昨";
    public static final String OFFSET_CLOSE_FORCE_ZN = "强平";

    //下单版价格类型
    public static final String LATEST_PRICE = "最新价";
    public static final String COUNTERPARTY_PRICE = "对手价";
    public static final String MARKET_PRICE = "市价";
    public static final String QUEUED_PRICE = "排队价";
    public static final String USER_PRICE = "用户设置价";

    //交易所信息
    public static final String SHFE = "SHFE";
    public static final String SHFE_ZN = "上海期货交易所";
    public static final String CZCE = "CZCE";
    public static final String CZCE_ZN = "郑州商品交易所";
    public static final String DCE = "DCE";
    public static final String DCE_ZN = "大连商品交易所";
    public static final String CFFEX = "CFFEX";
    public static final String CFFEX_ZN = "中国金融期货交易所";
    public static final String INE = "INE";
    public static final String INE_ZN = "上海国际能源交易中心";

    //amp event type
    public static final String AMP_INIT = "amp_init";
    public static final String AMP_BACKGROUND = "amp_background";
    public static final String AMP_TRADE = "amp_trade";
    public static final String AMP_TRANSFER = "amp_transfer";
    public static final String AMP_LOGGED = "amp_logged";
    //amp user property
    public static final String AMP_USER_SCREEN_SIZE = "screen_size";
    public static final String AMP_USER_BROKER_ID = "broker_id_last";
    public static final String AMP_USER_PACKAGE_ID = "package_id_last";
}
