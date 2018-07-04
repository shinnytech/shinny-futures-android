package com.shinnytech.futures.utils;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.view.activity.FutureInfoActivity;

import java.lang.reflect.Method;

import static android.content.Context.AUDIO_SERVICE;

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

    public KeyboardUtils(Activity activity, int idKeyboard, String instrument_id) {
        mActivity = (FutureInfoActivity) activity;
        mInstrumentId = instrument_id;
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
                            if (!"排队价".equals(text) && !"对手价".equals(text) && !"市价".equals(text) && !"最新价".equals(text)) {
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
                        editable.insert(0, "排队价");
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_opponent_price)) {
                        editable.clear();
                        editable.insert(0, "对手价");
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_last_price)) {
                        editable.clear();
                        editable.insert(0, "最新价");
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_market_price)) {
                        editable.clear();
                        editable.insert(0, "市价");
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_add)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case "排队价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(quoteEntity.getBid_price1(), quoteEntity.getAsk_price1()).contains("-") ? quoteEntity.getBid_price1() : quoteEntity.getAsk_price1());
                                }
                                break;
                            case "对手价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(quoteEntity.getBid_price1(), quoteEntity.getAsk_price1()).contains("-") ? quoteEntity.getAsk_price1() : quoteEntity.getBid_price1());
                                }
                                break;
                            case "最新价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, quoteEntity.getLast_price());
                                }
                                break;
                            case "市价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, quoteEntity.getLast_price());
                                }
                                break;
                            default:
                                String data = MathUtils.add(editable.toString(), "1");
                                editable.clear();
                                editable.insert(0, data);
                                break;
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_sub)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case "排队价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, MathUtils.divide(quoteEntity.getBid_price1(), quoteEntity.getAsk_price1()).contains("-") ? quoteEntity.getBid_price1() : quoteEntity.getAsk_price1());
                                }
                                break;
                            case "对手价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, MathUtils.divide(quoteEntity.getBid_price1(), quoteEntity.getAsk_price1()).contains("-") ? quoteEntity.getAsk_price1() : quoteEntity.getBid_price1());
                                }
                                break;
                            case "最新价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, quoteEntity.getLast_price());
                                }
                                break;
                            case "市价":
                                if (quoteEntity != null) {
                                    editable.clear();
                                    editable.insert(0, quoteEntity.getLast_price());
                                }
                                break;
                            default:
                                //添加负号功能
                                if (mEditText.getSelectionStart() == 0){
                                    editable.insert(0, "-");
                                }else {
                                    String data = MathUtils.subtract(editable.toString(), "1");
                                    editable.clear();
                                    editable.insert(0, data);
                                }
                                break;
                        }
                    } else {
                        String insertStr = Character.toString((char) primaryCode);
                        String str = editable.toString();
                        if ("排队价".equals(text) || "对手价".equals(text) || "市价".equals(text) || "最新价".equals(text) || mIsInit) {
                            //添加负号功能
                            if ("-".equals(str)){
                                if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                                    editable.insert(start, insertStr);
                            }else {
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

    /**
     * 隐藏系统键盘
     */
    private static void hideSystemSoftKeyboard(EditText editText) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= 11) {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            editText.setInputType(InputType.TYPE_NULL);
        }
    }

    public void attachTo(EditText editText) {
        this.mEditText = editText;
        this.mEditText.setSelectAllOnFocus(true);
        hideSystemSoftKeyboard(this.mEditText);
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) BaseApplicationLike.getContext().getSystemService(AUDIO_SERVICE);
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
        Animation mShowAction = AnimationUtils.loadAnimation(BaseApplicationLike.getContext(), R.anim.keyboard_in);
        View tab = mActivity.findViewById(R.id.rg_tab_up);
        tab.setVisibility(View.VISIBLE);
        tab.startAnimation(mShowAction);
        View content = mActivity.findViewById(R.id.fl_content_up);
        content.setVisibility(View.VISIBLE);
        content.startAnimation(mShowAction);
        mView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showKeyboard() {
        Animation mShowAction = AnimationUtils.loadAnimation(BaseApplicationLike.getContext(), R.anim.keyboard_in);
        View tab = mActivity.findViewById(R.id.rg_tab_up);
        tab.setVisibility(View.GONE);
        View content = mActivity.findViewById(R.id.fl_content_up);
        content.setVisibility(View.GONE);
        mView.setVisibility(View.VISIBLE);
        mView.startAnimation(mShowAction);
        mKeyboardView.setEnabled(true);
    }

    public boolean isVisible() {
        return View.VISIBLE == mView.getVisibility();
    }

}
