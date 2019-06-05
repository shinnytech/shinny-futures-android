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
    //广播信息类型
    public static final String MD_ONLINE = "MD_ONLINE";
    public static final String MD_OFFLINE = "MD_OFFLINE";
    public static final String MD_MESSAGE = "MD_MESSAGE";
    public static final String MD_TIMEOUT = "MD_TIMEOUT";
    public static final String TD_ONLINE = "TD_ONLINE";
    public static final String TD_OFFLINE = "TD_OFFLINE";
    public static final String TD_MESSAGE = "TD_MESSAGE";
    public static final String TD_TIMEOUT = "TD_TIMEOUT";
    public static final String TD_MESSAGE_LOGIN_SUCCEED = "TD_MESSAGE_LOGIN_SUCCEED";
    public static final String TD_MESSAGE_LOGIN_FAIL = "TD_MESSAGE_LOGIN_FAIL";
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
    public static final String TRANSFER_IN = "资金转入";
    public static final String TRANSFER_OUT = "资金转出";
    public static final String OPEN_ACCOUNT = "在线开户";
    public static final String CONDITIONAL_ORDER = "云条件单";
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
    public static final int MAIN_ACTIVITY_TO_SEARCH_ACTIVITY = 1;
    public static final int MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY = 2;
    public static final int MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY = 3;
    public static final int MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY = 4;
    public static final int LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY = 5;
    public static final int LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY = 6;
    public static final int KLINE_DURATION_ACTIVITY_TO_ADD_DURATION_ACTIVITY = 7;
    public static final int FUTURE_INFO_ACTIVITY_TO_CHART_SETTING_ACTIVITY = 8;
    public static final String BACK_TO_ACCOUNT_DETAIL = "checkAccount";
    public static final String INS_BETWEEN_ACTIVITY = "instrument_id";
    //app名称
    public static final String KUAI_QI_XIAO_Q = "快期小Q";
    //本地自选合约文件名
    public static final String OPTIONAL_INS_LIST = "optionalInsList";
    //银期转帐方向
    public static final String TRANSFER_DIRECTION = "transferDirection";
    //notify
    public static final String CHANGE_PASSWORD_SUCCEED = "修改密码成功";
    public static final String LOGIN_SUCCEED = "登录成功";
    public static final String LOGIN_FAIL = "用户登录失败!";

    //登录页
    public static final String BROKER_ID_VISITOR = "游客";
    public static final String BROKER_ID_SIMULATION = "快期模拟";
    public static final String BROKER_ID_SIMNOW = "simnow";
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
    public static final String CONFIG_RECOMMEND = "recommend";
    public static final String CONFIG_INIT_TIME = "initTime";
    public static final String CONFIG_VERSION_CODE = "versionCode";
    public static final String CONFIG_SYSTEM_INFO = "systemInfo";
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
    public static final String PARA_MA = "5,10,20,40,60,0";
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
    public static final String STATUS_ALIVE_ZN = "未成";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_FINISHED_ZN = "已成";
    public static final String STATUS_CANCELED_ZN = "已撤";
    public static final String PRICE_TYPE_LIMIT = "LIMIT";

    public static final String DIRECTION_BUY = "BUY";
    public static final String DIRECTION_SELL = "SELL";
    public static final String DIRECTION_BUY_ZN = "多";
    public static final String DIRECTION_BUY_ZN_S = "买";
    public static final String DIRECTION_SELL_ZN = "空";
    public static final String DIRECTION_SELL_ZN_S = "卖";
    public static final String DIRECTION_BOTH_ZN = "双向";

    public static final String OFFSET_OPEN = "OPEN";
    public static final String OFFSET_CLOSE = "CLOSE";
    public static final String OFFSET_CLOSE_TODAY = "CLOSETODAY";
    public static final String OFFSET_CLOSE_HISTORY = "CLOSEHISTORY";
    public static final String OFFSET_CLOSE_FORCE = "FORCECLOSE";
    public static final String OFFSET_OPEN_ZN = "开仓";
    public static final String OFFSET_OPEN_ZN_S = "开";
    public static final String OFFSET_CLOSE_ZN = "平仓";
    public static final String OFFSET_CLOSE_ZN_S = "平";
    public static final String OFFSET_CLOSE_TODAY_ZN = "平今";
    public static final String OFFSET_CLOSE_HISTORY_ZN = "平昨";
    public static final String OFFSET_CLOSE_FORCE_ZN = "强平";

    //下单版价格类型
    public static final String LATEST_PRICE = "最新";
    public static final String OPPONENT_PRICE = "对手";
    public static final String MARKET_PRICE = "市价";
    public static final String QUEUED_PRICE = "排队";
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
    public static final String AMP_SELECT_BROKER = "amp_select_broker";
    public static final String AMP_LOGIN = "amp_login";
    public static final String AMP_LOGIN_SUCCEEDED = "amp_login_succeeded";
    public static final String AMP_LOGIN_FAILED = "amp_login_failed";
    public static final String AMP_LOGIN_TIME_OUT = "amp_login_time_out";
    public static final String AMP_FOREGROUND = "amp_foreground";
    public static final String AMP_BACKGROUND = "amp_background";
    public static final String AMP_LOGOUT = "amp_logout";
    public static final String AMP_QUOTE_TAB = "amp_quote_tab";
    public static final String AMP_ACCOUNT_TAB = "amp_account_tab";
    public static final String AMP_ACCOUNT_LINK = "amp_account_link";
    public static final String AMP_CANCEL_CLOSE_CONFIRMED = "amp_cancel_close_confirmed";
    public static final String AMP_CANCEL_CLOSE_CANCELED = "amp_cancel_close_canceled";
    public static final String AMP_CANCEL_ORDER = "amp_cancel_order";
    public static final String AMP_INSERT_ORDER = "amp_insert_order";
    public static final String AMP_OPTIONAL_SEARCH = "amp_optional_search";
    public static final String AMP_OPTIONAL_RECOMMEND = "amp_optional_recommend";
    public static final String AMP_OPTIONAL_QUOTE = "amp_optional_quote";
    public static final String AMP_OPTIONAL_FUTURE_INFO = "amp_optional_future_info";
    public static final String AMP_MENU_TRANSFER_IN = "amp_menu_transfer_in";
    public static final String AMP_MENU_TRANSFER_OUT = "amp_menu_transfer_out";
    public static final String AMP_ACCOUNT_TRANSFER_IN = "amp_account_transfer_in";
    public static final String AMP_ACCOUNT_TRANSFER_OUT = "amp_account_transfer_out";
    public static final String AMP_TRANSFER_IN = "amp_transfer_in";
    public static final String AMP_TRANSFER_OUT = "amp_transfer_out";
    public static final String AMP_CONDITIONAL_ORDER = "amp_conditional_order";
    public static final String AMP_CRASH = "amp_crash";
    public static final String AMP_SWITCH_PAGE = "amp_switch_page";
    public static final String AMP_PRICE_KEY = "amp_price_key";
    public static final String AMP_VOLUME_KEY = "amp_volume_key";
    public static final String AMP_SHOW_PAGE = "amp_show_page";
    public static final String AMP_LEAVE_PAGE = "amp_leave_page";
    public static final String AMP_NOTIFY = "amp_notify";

    //amp event property
    public static final String AMP_EVENT_SELECT_BROKER_ID = "broker_id";
    public static final String AMP_EVENT_SELECT_IS_ADDED = "is_added";
    public static final String AMP_EVENT_LOGIN_BROKER_ID = "broker_id";
    public static final String AMP_EVENT_LOGIN_USER_ID = "user_id";
    public static final String AMP_EVENT_LOGIN_TIME = "login_time";
    public static final String AMP_EVENT_LOGIN_TYPE = "login_type";
    public static final String AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN = "login";
    public static final String AMP_EVENT_LOGIN_TYPE_VALUE_VISIT = "visit";
    public static final String AMP_EVENT_LOGIN_TYPE_VALUE_AUTO = "auto";
    public static final String AMP_EVENT_LOGOUT_TIME = "logout_time";

    public static final String AMP_EVENT_PRICE = "price";
    public static final String AMP_EVENT_INSTRUMENT_ID = "instrument_id";
    public static final String AMP_EVENT_VOLUME = "volume";
    public static final String AMP_EVENT_DIRECTION = "direction";
    public static final String AMP_EVENT_OFFSET = "offset";

    public static final String AMP_EVENT_OPTIONAL_INSTRUMENT_ID = "optional_instrument_id";
    public static final String AMP_EVENT_OPTIONAL_DIRECTION = "optional_direction";
    public static final String AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD = "添加";
    public static final String AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE = "删除";

    public static final String AMP_EVENT_BANK = "bank";
    public static final String AMP_EVENT_AMOUNT = "amount";
    public static final String AMP_EVENT_CURRENCY = "currency";

    public static final String AMP_EVENT_CURRENT_PAGE = "current_page";
    public static final String AMP_EVENT_TARGET_PAGE = "target_page";
    public static final String AMP_EVENT_PAGE_VALUE_MAIN = "main_page";
    public static final String AMP_EVENT_PAGE_VALUE_LOGIN = "login_page";
    public static final String AMP_EVENT_PAGE_VALUE_SETTING = "setting_page";
    public static final String AMP_EVENT_PAGE_VALUE_OPTIONAL_SETTING = "optional_setting_page";
    public static final String AMP_EVENT_PAGE_VALUE_CHART_SETTING = "chart_setting_page";
    public static final String AMP_EVENT_PAGE_VALUE_ACCOUNT = "account_page";
    public static final String AMP_EVENT_PAGE_VALUE_CHANGE_PASSWORD = "change_password_page";
    public static final String AMP_EVENT_PAGE_VALUE_TRANSFER = "transfer_page";
    public static final String AMP_EVENT_PAGE_VALUE_OPEN_ACCOUNT = "open_account_page";
    public static final String AMP_EVENT_PAGE_VALUE_FEED_BACK = "feed_back_page";
    public static final String AMP_EVENT_PAGE_VALUE_ABOUT = "about_page";
    public static final String AMP_EVENT_PAGE_VALUE_SEARCH = "search_page";
    public static final String AMP_EVENT_PAGE_VALUE_FUTURE_INFO = "future_info_page";

    public static final String AMP_EVENT_PRICE_KEY = "price_key";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_0 = "price_key_0";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_1 = "price_key_1";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_2 = "price_key_2";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_3 = "price_key_3";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_4 = "price_key_4";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_5 = "price_key_5";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_6 = "price_key_6";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_7 = "price_key_7";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_8 = "price_key_8";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_9 = "price_key_9";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_DEL = "price_key_del";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_MINUS = "price_key_minus";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_PLUS = "price_key_plus";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_POINT = "price_key_point";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_QUEUED = "price_key_queued";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_OPPONENT = "price_key_opponent";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_MARKET = "price_key_market";
    public static final String AMP_EVENT_PRICE_KEY_VALUE_LAST = "price_key_last";

    public static final String AMP_EVENT_VOLUME_KEY = "volume_key";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_0 = "volume_key_0";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_1 = "volume_key_1";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_2 = "volume_key_2";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_3 = "volume_key_3";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_4 = "volume_key_4";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_5 = "volume_key_5";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_6 = "volume_key_6";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_7 = "volume_key_7";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_8 = "volume_key_8";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_9 = "volume_key_9";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_DEL = "volume_key_del";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_MINUS = "volume_key_minus";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_PLUS = "volume_key_plus";
    public static final String AMP_EVENT_VOLUME_KEY_VALUE_CLEAR = "volume_key_clear";

    public static final String AMP_EVENT_PAGE_ID = "page_id";
    public static final String AMP_EVENT_PAGE_ID_VALUE_MAIN = "main";
    public static final String AMP_EVENT_PAGE_ID_VALUE_ACCOUNT = "account";
    public static final String AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO = "future_info";
    public static final String AMP_EVENT_SUB_PAGE_ID = "sub_page_id";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_QUOTE = "quote";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_HANDICAP = "handicap";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_POSITION = "position";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_ALIVE = "alive";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_ORDER = "order";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_TRADE = "trade";
    public static final String AMP_EVENT_SUB_PAGE_ID_VALUE_TRANSACTION = "transaction";
    public static final String AMP_EVENT_IS_POSITIVE = "is_positive";
    public static final String AMP_EVENT_IS_INS_IN_POSITION = "is_ins_in_position";
    public static final String AMP_EVENT_IS_INS_IN_OPTIONAL = "is_ins_in_optional";
    public static final String AMP_EVENT_BROKER_ID = "broker_id";
    public static final String AMP_EVENT_BALANCE = "balance";
    public static final String AMP_EVENT_POSITION_COUNT = "position_count";
    public static final String AMP_EVENT_ORDER_COUNT = "order_count";
    public static final String AMP_EVENT_PAGE_VISIBLE_TIME = "page_visible_time";

    public static final String AMP_EVENT_CRASH_TYPE = "crash_type";
    public static final String AMP_EVENT_ERROR_TYPE = "error_type";
    public static final String AMP_EVENT_ERROR_MESSAGE = "error_message";
    public static final String AMP_EVENT_ERROR_STACK = "error_stack";

    public static final String AMP_EVENT_NOTIFY_TYPE = "notify_type";
    public static final String AMP_EVENT_NOTIFY_LEVEL = "notify_level";
    public static final String AMP_EVENT_NOTIFY_CONTENT = "notify_content";
    public static final String AMP_EVENT_NOTIFY_CODE = "notify_code";

    //amp user property
    public static final String AMP_USER_PACKAGE_ID_FIRST = "package_id_first";
    public static final String AMP_USER_PACKAGE_ID_LAST = "package_id_last";
    public static final String AMP_USER_INIT_TIME_FIRST = "init_time_first";
    public static final String AMP_USER_SURVIVAL_TIME_TOTAL = "survival_time_total";
    public static final String AMP_USER_LOGIN_TIME_FIRST = "login_time_first";
    public static final String AMP_USER_LOGIN_SUCCESS_TIME_FIRST = "login_success_time_first";
    public static final String AMP_USER_ACCOUNT_ID_FIRST = "account_id_first";
    public static final String AMP_USER_ACCOUNT_ID_LAST = "account_id_last";
    public static final String AMP_USER_BROKER_ID_FIRST = "broker_id_first";
    public static final String AMP_USER_BROKER_ID_LAST = "broker_id_last";
    public static final String AMP_USER_BANK_FIRST = "bank_first";
    public static final String AMP_USER_BANK_LAST = "bank_last";
    public static final String AMP_USER_BALANCE_FIRST = "balance_first";
    public static final String AMP_USER_BALANCE_LAST = "balance_last";
    public static final String AMP_USER_TYPE_FIRST = "user_type_first";
    public static final String AMP_USER_TYPE_FIRST_PURE_NEWBIE_VALUE = "pure_newbie";
    public static final String AMP_USER_TYPE_FIRST_TRADER_VALUE = "trader";

}
