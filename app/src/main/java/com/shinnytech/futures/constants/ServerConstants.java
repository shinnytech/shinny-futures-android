package com.shinnytech.futures.constants;

public final class ServerConstants {
    //行情指令
    public static final String REQ_SUBSCRIBE_QUOTE = "subscribe_quote";
    public static final String REQ_SET_CHART = "set_chart";
    public static final String PARSE_MARKET_KEY_AID = "aid";
    public static final String PARSE_MARKET_KEY_RSP_LOGIN = "rsp_login";
    public static final String PARSE_MARKET_KEY_RTN_DATA = "rtn_data";
    public static final String PARSE_MARKET_KEY_DATA = "data";
    public static final String PARSE_MARKET_KEY_QUOTES = "quotes";
    public static final String PARSE_MARKET_KEY_KLINES = "klines";
    public static final String PARSE_MARKET_KEY_KLINES_DATA = "data";
    public static final String PARSE_MARKET_KEY_KLINES_BINDING = "binding";
    public static final String PARSE_MARKET_KEY_TICKS = "ticks";
    public static final String PARSE_MARKET_KEY_CHARTS = "charts";
    public static final String PARSE_MARKET_KEY_CHARTS_STATE = "state";
    public static final String PARSE_MARKET_KEY_INS_LIST = "ins_list";
    public static final String PARSE_MARKET_KEY_MDHIS_MORE_DATA = "mdhis_more_data";
    //下单指令
    public static final String REQ_LOGIN = "req_login";
    public static final String REQ_CONFIRM_SETTLEMENT = "confirm_settlement";
    public static final String REQ_INSERT_ORDER = "insert_order";
    public static final String REQ_CANCEL_ORDER = "cancel_order";
    public static final String REQ_TRANSFER = "req_transfer";
    public static final String REQ_CHANGE_PASSWORD = "change_password";
    public static final String REQ_INSERT_CONDITION_ORDER = "insert_condition_order";
    public static final String REQ_CANCEL_CONDITION_ORDER = "cancel_condition_order";
    public static final String REQ_PAUSE_CONDITION_ORDER = "pause_condition_order";
    public static final String REQ_RESUME_CONDITION_ORDER = "resume_condition_order";
    public static final String REQ_QUERY_CONDITION_ORDER = "qry_his_condition_order";
    public static final String PARSE_TRADE_KEY_AID = "aid";
    public static final String PARSE_TRADE_KEY_RTN_BROKERS = "rtn_brokers";
    public static final String PARSE_TRADE_KEY_RTN_DATA = "rtn_data";
    public static final String PARSE_TRADE_KEY_RTN_CONDITION_ORDERS = "rtn_condition_orders";
    public static final String PARSE_TRADE_KEY_RTN_HIS_CONDITION_ORDERS = "rtn_his_condition_orders";
    public static final String PARSE_TRADE_KEY_DATA = "data";
    public static final String PARSE_TRADE_KEY_NOTIFY = "notify";
    public static final String PARSE_TRADE_KEY_CONTENT = "content";
    public static final String PARSE_TRADE_KEY_LEVEL = "level";
    public static final String PARSE_TRADE_KEY_CODE = "code";
    public static final String PARSE_TRADE_KEY_TRADE = "trade";
    public static final String PARSE_TRADE_KEY_ACCOUNTS = "accounts";
    public static final String PARSE_TRADE_KEY_ORDERS = "orders";
    public static final String PARSE_TRADE_KEY_POSITIONS = "positions";
    public static final String PARSE_TRADE_KEY_TRADES = "trades";
    public static final String PARSE_TRADE_KEY_BANKS = "banks";
    public static final String PARSE_TRADE_KEY_TRANSFERS = "transfers";
    public static final String PARSE_TRADE_KEY_SESSION = "session";
    public static final String PARSE_TRADE_KEY_USER_ID = "user_id";
    public static final String PARSE_CONDITION_KEY_USER_ID = "user_id";
    public static final String PARSE_CONDITION_KEY_TRADING_DAY = "trading_day";
    public static final String PARSE_CONDITION_KEY_RTN_CONDITION_ORDERS = "rtn_condition_orders";
    public static final String PARSE_CONDITION_KEY_CONDITION_ORDERS = "condition_orders";
    public static final String PARSE_CONDITION_KEY_HIS_CONDITION_ORDERS = "his_condition_orders";
    public static final String PARSE_CONDITION_KEY_ORDER_ID = "order_id";
    public static final String PARSE_CONDITION_KEY_CONDITION_LIST = "condition_list";
    public static final String PARSE_CONDITION_KEY_ORDER_LIST = "order_list";

    //notify code
    public static final int CODE_CONDITION_FAIL_LEFT = 501;
    public static final int CODE_CONDITION_FAIL_RIGHT = 515;
    public static final int CODE_CONDITION_SUCCEED = 516;
    public static final int CODE_CHANGE_PASSWORD_SUCCEED = 326;
    public static final int CODE_CHANGE_PASSWORD_INIT = 140;
    public static final int CODE_CHANGE_PASSWORD_WEAK = 131;
    public static final int CODE_SETTLEMENT = 325;
    public static final int CODE_CHECK_UNREADY_CTP = 90;
    public static final int CODE_LOGIN_SUCCEED_CTP = 324;
    public static final int CODE_LOGIN_FAIL_CTP_LEFT = 340;
    public static final int CODE_LOGIN_FAIL_CTP_RIGHT = 346;
    public static final int CODE_LOGIN_TIMEOUT_CTP = 347;
    public static final int CODE_LOGIN_SUCCEED_SIMULATOR = 401;
    public static final int CODE_LOGIN_FAIL_SIMULATOR = 403;
    public static final int CODE_LOGIN_PASSWORD_MISMATCH_CTP = 339;
    public static final int CODE_LOGIN_PASSWORD_MISMATCH_SIMULATOR = 402;

}
