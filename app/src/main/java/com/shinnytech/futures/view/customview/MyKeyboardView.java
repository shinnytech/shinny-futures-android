package com.shinnytech.futures.view.customview;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class MyKeyboardView extends KeyboardView {
    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @Override
//    public void onDraw(Canvas canvas) {
//        List<Keyboard.Key> keys = getKeyboard().getKeys();
//        for (Keyboard.Key key : keys) {
//            if (key.codes[0] == 7) {
//                Drawable dr = (Drawable) context.getResources().getDrawable(R.drawable.red_tint);
//                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                dr.draw(canvas);
//
//            } else {
//                Drawable dr = (Drawable) context.getResources().getDrawable(R.drawable.blue_tint);
//                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                dr.draw(canvas);
//            }
//        }
//    }
}
