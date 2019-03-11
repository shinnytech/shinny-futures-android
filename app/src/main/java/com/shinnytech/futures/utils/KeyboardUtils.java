package com.shinnytech.futures.utils;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;

import static android.content.Context.AUDIO_SERVICE;
import static com.shinnytech.futures.constants.CommonConstants.COUNTERPARTY_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.LATEST_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.QUEUED_PRICE;

/**
 * date: 6/2/17
 * author: chenli
 * description: 自定义控件盘帮助类
 * version:
 * state: basically done
 */
public class KeyboardUtils {
    private boolean mIsInit;
    private FutureInfoActivity mActivity;
    private View mView;
    private KeyboardView mKeyboardView;
    private EditText mEditText;
    private String mInstrumentId;
    private int mIdKeyboard;

    public KeyboardUtils(Activity activity, final int idKeyboard, final String instrument_id) {
        mActivity = (FutureInfoActivity) activity;
        mInstrumentId = instrument_id;
        mIdKeyboard = idKeyboard;
        Keyboard mKeyboard = new Keyboard(mActivity, idKeyboard);
        mView = mActivity.findViewById(R.id.keyboard_layout);
        mKeyboardView = mView.findViewById(R.id.keyboard);
        TextView hideKeyboard = mView.findViewById(R.id.hide);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(true);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {

            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                playClick(primaryCode);
                if (mEditText != null) {
                    Editable editable = mEditText.getText();
                    String text = mEditText.getText().toString();
                    int start = mEditText.length();
                    if (primaryCode == Keyboard.KEYCODE_DELETE) {
                        if (editable.length() > 0) {
                            if (!QUEUED_PRICE.equals(text) && !COUNTERPARTY_PRICE.equals(text) && !MARKET_PRICE.equals(text) && !LATEST_PRICE.equals(text)) {
                                editable.delete(start - 1, start);
                            } else {
                                editable.clear();
                            }
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_empty_text)) {
                        editable.clear();
                    } else if (primaryCode == Keyboard.KEYCODE_DONE) {
                        hideKeyboard();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_line_up_price)) {
                        editable.clear();
                        editable.insert(0, QUEUED_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_opponent_price)) {
                        editable.clear();
                        editable.insert(0, COUNTERPARTY_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_last_price)) {
                        editable.clear();
                        editable.insert(0, LATEST_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_market_price)) {
                        editable.clear();
                        editable.insert(0, MARKET_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_add)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case COUNTERPARTY_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            default:
                                String data;
                                if (idKeyboard == R.xml.future_price) {
                                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
                                    String price_tick = searchEntity == null ? "0" : searchEntity.getpTick();
                                    data = MathUtils.add(editable.toString(), price_tick);
                                } else {
                                    data = MathUtils.add(editable.toString(), "1");
                                }
                                editable.clear();
                                editable.insert(0, data);
                                break;
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_sub)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case COUNTERPARTY_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            default:
                                String data;
                                if (idKeyboard == R.xml.future_price) {
                                    //添加负号功能
                                    if (mEditText.getSelectionStart() == 0 && mEditText.getSelectionEnd() == 0){
                                        editable.insert(0, "-");
                                        break;
                                    }else {
                                        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
                                        String price_tick = searchEntity == null ? "0" : searchEntity.getpTick();
                                        data = MathUtils.subtract(editable.toString(), price_tick);
                                    }
                                } else {
                                    data = MathUtils.subtract(editable.toString(), "1");
                                }
                                editable.clear();
                                editable.insert(0, data);
                                break;
                        }
                    } else {
                        String insertStr = Character.toString((char) primaryCode);
                        String str = editable.toString();
                        if (QUEUED_PRICE.equals(text) || COUNTERPARTY_PRICE.equals(text) || MARKET_PRICE.equals(text) || LATEST_PRICE.equals(text) || mIsInit) {
                            //添加负号功能
                            if ("-".equals(str)) {
                                if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                                    editable.insert(start, insertStr);
                            } else {
                                editable.clear();
                                editable.insert(0, insertStr);
                            }
                            mIsInit = false;
                        } else if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                            editable.insert(start, insertStr);
                    }
                }
            }

            @Override
            public void onText(CharSequence text) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });
        mIsInit = true;
        hideKeyboard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    public void refreshInstrumentId(String instrumentId) {
        mInstrumentId = instrumentId;
    }

    /**
     * 隐藏系统键盘
     */
    private void hideSystemSoftKeyboard() {
        View view = mActivity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void attachTo(EditText editText) {
        this.mEditText = editText;
        this.mEditText.requestFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mEditText.setShowSoftInputOnFocus(false);
        } else {
            hideSystemSoftKeyboard();
        }
        this.mEditText.setSelection(0, mEditText.getText().length());
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) BaseApplication.getContext().getSystemService(AUDIO_SERVICE);
        if (am != null)
            switch (keyCode) {
                case 32:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                    break;
                case Keyboard.KEYCODE_DONE:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                    break;
                default:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
            }
    }

    public void hideKeyboard() {
        Animation mShowAction = AnimationUtils.loadAnimation(BaseApplication.getContext(), R.anim.keyboard_in);
        View tab = mActivity.findViewById(R.id.rg_tab_up);
        tab.setVisibility(View.VISIBLE);
        tab.startAnimation(mShowAction);
        View content = mActivity.findViewById(R.id.kline_content);
        content.setVisibility(View.VISIBLE);
        content.startAnimation(mShowAction);
        mView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showKeyboard() {
        Animation mShowAction = AnimationUtils.loadAnimation(BaseApplication.getContext(), R.anim.keyboard_in);
        View tab = mActivity.findViewById(R.id.rg_tab_up);
        tab.setVisibility(View.GONE);
        View content = mActivity.findViewById(R.id.kline_content);
        content.setVisibility(View.GONE);
        mView.setVisibility(View.VISIBLE);
        mView.startAnimation(mShowAction);
        mKeyboardView.setEnabled(true);
    }

    public boolean isVisible() {
        return View.VISIBLE == mView.getVisibility();
    }

}
