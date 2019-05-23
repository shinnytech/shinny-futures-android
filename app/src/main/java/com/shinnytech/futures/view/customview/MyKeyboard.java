package com.shinnytech.futures.view.customview;

import android.content.Context;
import android.inputmethodservice.Keyboard;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.utils.DensityUtils;

public class MyKeyboard extends Keyboard {
    public MyKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public MyKeyboard(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public MyKeyboard(Context context, int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
    }

    public MyKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    public void setKeyHeight() {
        setKeyHeight(DensityUtils.dp2px(BaseApplication.getContext(), 900));
    }


}
