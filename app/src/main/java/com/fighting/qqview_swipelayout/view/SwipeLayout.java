package com.fighting.qqview_swipelayout.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 描述：
 * 作者 mjd
 * 日期：2016/1/28 20:10
 */
public class SwipeLayout extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private View mBackView;
    private View mFrontView;
    private int range;
    private int viewWidth;
    private int viewHeight;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //1. 创建 ViewDragHelper
        viewDragHelper = ViewDragHelper.create(this, mCallback);
    }
    //2. 转交触摸事件，拦截判断，处理触摸事件

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            //多点触摸有一些小 bug，最好 catch 一下
            viewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //消费事件，返回 true
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //默认是关闭状态
        layoutContent(false);
    }

    private void layoutContent(boolean isOpen) {
        //设置前布局位置
        Rect rect = computeFrontRect(isOpen);
        mFrontView.layout(rect.left, rect.top, rect.right, rect.bottom);
        //根据前布局位置计算后布局位置
        Rect backRect = computeBackRectViaFront(rect);
        mBackView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);
    }

    private Rect computeBackRectViaFront(Rect rect) {
        int left = rect.right;
        return new Rect(left, 0, left + range, viewHeight);
    }

    /**
     * 计算布局所在矩形区域
     */
    private Rect computeFrontRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = -range;
        }
        return new Rect(left, 0, left + viewWidth, viewHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackView = getChildAt(0);
        mFrontView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //mBackView 的宽度就是 mFrontView 的拖拽范围
        range = mBackView.getMeasuredWidth();
        //控件的宽
        viewWidth = getMeasuredWidth();
        //控件的高
        viewHeight = getMeasuredHeight();
    }

    //3. 处理回调事件
    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        //返回值决定了 child 是否可以被拖拽
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //child 被用户拖拽的孩子
            return true;
        }

        //返回拖拽的范围，返回一个大于 0 的值，计算动画执行的时长，水平方向是否可以被滑开
        @Override
        public int getViewHorizontalDragRange(View child) {
            return range;
        }

        //返回值决定将要移动到的位置，此时还没有发生真正的移动
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //left 建议移动到的位置
            if (child == mFrontView) {
                //限定前布局的拖拽范围
                if (left > 0) {
                    //前布局最大的左边位置不能大于 0
                    left = 0;
                } else if (left < -range) {
                    //前布局最小的左边位置不能小于 -range
                    left = -range;
                }
            } else if (child == mBackView) {
                //限定后布局的拖拽范围
                if (left < (viewWidth - range)) {
                    //后布局最小左边位置不能小于 viewWidth - range
                    left = viewWidth - range;
                } else if (left > viewWidth) {
                    //后布局最大的左边位置不能大于 viewWidth
                    left = viewWidth;
                }
            }
            return left;
        }

        //位置发生改变时，前后布局的变化量互相传递
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //left 最新的水平位置
            //dx 刚刚发生的水平变化量
            //位置变化时，把水平变化量传递给另一个布局
            if (changedView == mFrontView) {
                //拖拽的是前布局，把刚刚发生的变化量 dx 传递给后布局
                mBackView.offsetLeftAndRight(dx);
            } else if (changedView == mBackView) {
                //拖拽的是后布局，把刚刚发生的变化量 dx 传递给前布局
                mFrontView.offsetLeftAndRight(dx);
            }
            //更新状态及调用监听
            dispatchDragEvent();
            //兼容低版本，重绘一次界面
            invalidate();
        }

        //松手时会被调用
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //xvel 水平方向上的速度，向左为-，向右为+
            if (xvel == 0 && mFrontView.getLeft() < -range * 0.5f) {
                //xvel 变 0 时, 并且前布局的左边位置小于-mRange 的一半
                open();
            } else if (xvel < 0) {
                //xvel 为-时，打开
                open();
            } else {
                //其它情况为关闭
                close();
            }
        }
    };

    private void dispatchDragEvent() {
        //需要记录一下上次的状态，对比当前状态和上次状态，在状态改变时调用监听
        Status lastStatus = status;
        //获取更新状态
        status = updateStatus();
        //在状态改变时调用监听
        if (lastStatus != status && onSwipeListener != null) {
            if (status == Status.Open) {
                onSwipeListener.onOpen(this);
            } else if (status == Status.Close) {
                onSwipeListener.onClose(this);
            } else if (status == Status.Swiping) {
                if (lastStatus == Status.Close) {
                    //如果上一次状态为关闭，现在是拖拽状态，说明正在打开
                    onSwipeListener.onStartOpen(this);
                } else if (lastStatus == Status.Open) {
                    //如果上一次状态为打开，现在是拖拽状态，说明正在关闭
                    onSwipeListener.onStartClose(this);
                }
            }
        }
    }

    private Status updateStatus() {
        //通过前布局左边的位置可以判断当前的状态
        int left = mFrontView.getLeft();
        if (left == 0) {
            return Status.Close;
        } else if (left == -range) {
            return Status.Open;
        }
        return Status.Swiping;
    }

    private void open() {
        open(true);
    }

    private void open(boolean isSmooth) {
        int finalLeft = -range;
        if (isSmooth) {
            //开启一个平滑动画将 child移动到 finalLeft,finalTop 的位置上。此方法返回 true 说明当前位置不是最终位置需要重绘
            if (viewDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                //调用重绘方法
                //invalidate(); 可能会丢帧, 此处推荐使用 ViewCompat.postInvalidateOnAnimation()
                //参数一定要传 child 所在的容器，因为只有容器才知道 child 应该摆放在什么位置
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(true);
        }
    }

    public void close() {
        close(true);
    }

    private void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            if (viewDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(false);
        }
    }

    //重绘时 computeScroll() 方法会被调用
    @Override
    public void computeScroll() {
        super.computeScroll();
        //mHelper.continueSettling(deferCallbacks) 维持动画的继续，返回 true 表示还需要重绘
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    //控件有三种状态
    public enum Status {
        Open, Close, Swiping
    }

    //初始状态为关闭
    private Status status = Status.Close;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public interface OnSwipeListener {
        //通知外界已经打开
        void onOpen(SwipeLayout swipeLayout);

        //通知外界已经关闭
        void onClose(SwipeLayout swipeLayout);

        //通知外界将要打开
        void onStartOpen(SwipeLayout swipeLayout);

        //通知外界将要关闭
        void onStartClose(SwipeLayout swipeLayout);
    }

    private OnSwipeListener onSwipeListener;

    public OnSwipeListener getOnSwipeListener() {
        return onSwipeListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.onSwipeListener = onSwipeListener;
    }
}
