package com.shinnytech.futures.view.listener;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * date: 7/9/17
 * author: chenli
 * description: recyclerView的单击与长按事件监听器
 * version:
 * state: done
 */
public class SimpleRecyclerViewItemClickListener extends RecyclerView.SimpleOnItemTouchListener {

    private OnItemClickListener mListener;
    private GestureDetectorCompat mGestureDetector;
    private RecyclerView mRecyclerView;

    public SimpleRecyclerViewItemClickListener(RecyclerView rv, OnItemClickListener listener) {
        this.mListener = listener;
        this.mRecyclerView = rv;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (mGestureDetector == null) {
            initGestureDetector(mRecyclerView);
        }
        return mGestureDetector.onTouchEvent(e);// 把事件交给GestureDetector处理
    }

    /**
     * 初始化GestureDetector
     */
    private void initGestureDetector(final RecyclerView recyclerView) {
        mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() { // 这里选择SimpleOnGestureListener实现类，可以根据需要选择重写的方法

            /**
             * 单击事件
             */
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mListener.onItemClick(childView, recyclerView.getChildLayoutPosition(childView));
                }
                return false;
            }

            /**
             * 长按事件
             */
            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(childView, recyclerView.getChildLayoutPosition(childView));
                }
            }

        });

    }

    public interface OnItemClickListener {

        /**
         * 当ItemView的单击事件触发时调用
         */
        void onItemClick(View view, int position);

        /**
         * 当ItemView的长按事件触发时调用
         */
        void onItemLongClick(View view, int position);

    }
}