package qiqi.love.you;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by iscod on 2016/4/28.
 */
public class SildeCutListView extends ListView {
    /**
     * 当前滑动的listview position
     */
    private int slidePosition;
    /**
     * 手指按下X坐标
     */
    private int downX;
    /**
     * 手指按下Y坐标
     */
    private int downY;
    /**
     * 屏幕宽度
     */
    private int screenWidth;
    /**
     * listview的item
     */
    private View itemView;
    /**
     * 滑动类
     */
    private Scroller scroller;
    /**
     * 默认加速值
     */
    private static final int SNAP_VELOCITY = 600;
    /**
     * 速度追踪对象
     */
    private VelocityTracker velocityTracker;
    /**
     * 是否响应滑动，默认不响应
     */
    private boolean isSlide = false;
    /**
     * 认为是用户滑动的最小距离
     */
    private int mTouchSlop;
    /**
     * 移除item后的回调接口
     */
    private RemoveListener mRemoveListener;
    /**
     * 用来指示item滑出屏幕的方向，向左向右，用一个枚举值来标记
     */
    private RemoveDirection removeDirection;

    public enum RemoveDirection {
        RIGHT, LEFT;
    }

    public SildeCutListView(Context context) {
        super(context);
        initView(context);
    }

    public SildeCutListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SildeCutListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        screenWidth = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 设置滑动删除回调
     *
     * @param listener
     */
    public void setRemoveListener(RemoveListener listener) {
        mRemoveListener = listener;
    }

    /**
     * 分发事件，主要的是判断点击的是哪个item，以及通过postDelayed来设置响应左右滑动事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                addVelocityTracker(ev);
                //假如scroller滚动还没有结束，我们直接返回
                if (!scroller.isFinished()) {
                    return super.dispatchTouchEvent(ev);
                }
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                //根据点击的X,Y坐标利用pointToPosition(int x, int y)来获取点击的是ListView的哪一个position
                slidePosition = pointToPosition(downX, downY);
                //无效的position，不做处理
                if (slidePosition == AdapterView.INVALID_POSITION) {
                    return dispatchTouchEvent(ev);
                }
                //获取我们点击的itemView
                itemView = getChildAt(slidePosition - getFirstVisiblePosition());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                        || (Math.abs(ev.getX() - downX)) > mTouchSlop &&
                        Math.abs(ev.getY() - downY) < mTouchSlop) {
                    isSlide = true;
                }
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 往右滑动，getScrollX（）返回的是左边缘的距离，
     * 就是以view左边缘为原点到开始滑动的距离，所以向右边滑动为负值
     */
    private void scrollRight() {
        removeDirection = RemoveDirection.RIGHT;
        final int delta = (screenWidth + itemView.getScrollX());
        //调用startScroll方法来设置一些滚动参数，我们在computeScroll()方法中调用ScrollTo来滚动item
        scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
        postInvalidate();//刷新itmeview
    }

    /**
     * 向左滑动，向左滑动为正值。
     */
    private void scrollLeft() {
        removeDirection = RemoveDirection.LEFT;
        final int delta = (screenWidth - itemView.getScrollX());
        scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
        postInvalidate();
    }

    /**
     * 根据手指滚动itemView的距离来判断是滚动到开始的位置还是向左向右滚动
     */
    private void scrollByDistanceX() {
        //如果向左滚动的距离大于屏幕的二分之一，就让其删除
        if (itemView.getScrollX() >= screenWidth / 2) {
            scrollLeft();
        } else if (itemView.getScrollX() <= -screenWidth / 2) {
            scrollRight();
        } else {
            itemView.scrollTo(0, 0);
        }
    }

    /**
     * 处理我们拖动listView item的逻辑
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            requestDisallowInterceptTouchEvent(true);//不允许父布局拦截事件
            addVelocityTracker(ev);
            final int action = ev.getAction();
            int x = (int) ev.getX();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE: {
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                            | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);
                    int deltaX = downX - x;
                    downX = x;
                    //手指拖动itemView滚动，deltaX大于0向左滚动，小于0向右滚。
                    itemView.scrollBy(deltaX, 0);
                    return true;//拖动的时候listView不滚动
                }
                case MotionEvent.ACTION_UP: {
                    int velocityX = getScrollVelocity();
                    if (velocityX > SNAP_VELOCITY) {
                        scrollRight();
                    } else if (velocityX < -SNAP_VELOCITY) {
                        scrollLeft();
                    } else {
                        scrollByDistanceX();
                    }
                    recycleVelocityTracker();
                    //手指离开的时候就不响应左右滚动
                    isSlide = false;
                    break;
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        //调用startScroll的时候scroller.computeScrollOffset()返回true
        if (scroller.computeScrollOffset()) {
            //让listview item根据当前滚动的偏移量进行滚动
            itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
            if (scroller.isFinished()) {
                if (mRemoveListener == null) {
                    throw new NullPointerException("mRemoveListener不能为空，必须调用setRemoveListener（）");
                }
                itemView.scrollTo(0, 0);
                mRemoveListener.removeItem(removeDirection, slidePosition);
            }
        }
        super.computeScroll();
    }

    /**
     * 添加用户的速度追踪器
     *
     * @param event
     */
    private void addVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = velocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    /**
     * 移除速度追踪器
     */
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * 获取x方向的滑动速度，大于0向右滑动，反之向左。
     *
     * @return
     */
    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) velocityTracker.getXVelocity();
        return velocity;
    }

    public interface RemoveListener {
        public void removeItem(RemoveDirection direction, int position);
    }
}
