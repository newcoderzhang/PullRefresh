package com.example.huangjie.mypullrefesh.ViewRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.huangjie.mypullrefesh.R;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by huangjie on 2016/4/7.
 */
public class ViewPullRrfresh extends ListView implements AbsListView.OnScrollListener {
    private static final String TAG = "ViewPullRrfresh";
    private View headview;
    private View footerView;
    private ImageView iv_arrow;
    private ProgressBar pb_rotate;
    private TextView tv_state;
    private TextView tv_time;

    private int Headviewheight;
    private int footerViewHeight;
    private RotateAnimation upAnimation;
    private RotateAnimation downAnimation;


    private int downY;//按下时y坐标

    private final int PULL_REFRESH = 0;//下拉
    private final int RELEASE_REFRESH = 1;//松开
    private final int REFRESHING = 2;//正在刷新的状态
    private int currentState = PULL_REFRESH;
    private boolean isLoadingMore = false;//当前是否正在处于加载更多


    public ViewPullRrfresh(Context context) {
        super(context);
        initView();
    }

    public ViewPullRrfresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setOnScrollListener(this);
        initHeadView();
        initFooterView();
        initRotateAnimation();

    }

    public void initHeadView(){
        headview = View.inflate(getContext(), R.layout.layout_header,null);
        iv_arrow = (ImageView) headview.findViewById(R.id.iv_arrow);
        pb_rotate = (ProgressBar) headview.findViewById(R.id.pb_rotate);
        tv_state = (TextView) headview.findViewById(R.id.tv_state);
        tv_time = (TextView) headview.findViewById(R.id.tv_time);
        headview.measure(0,0);   //主动调用测量
        Headviewheight = headview.getMeasuredHeight();
        headview.setPadding(0, -Headviewheight, 0, 0);
        Log.v(TAG, "Headviewheight is " + Headviewheight);
        addHeaderView(headview);   //添加到头部
    }
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.layout_footer, null);
        footerView.measure(0, 0);//主动通知系统去测量该view;
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        addFooterView(footerView);
    }


    /**
     * 初始化旋转动画
     */
    private void initRotateAnimation() {
        upAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(300);
        upAnimation.setFillAfter(true);
        downAnimation = new RotateAnimation(-180, -360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(300);
        downAnimation.setFillAfter(true);
    }

    public void completeRefresh(){
        if(isLoadingMore){
            //重置footerView状态
            footerView.setPadding(0, -footerViewHeight, 0, 0);
            isLoadingMore = false;
        }else {
            //重置headerView状态
            headview.setPadding(0, -Headviewheight, 0, 0);
            currentState = PULL_REFRESH;
            pb_rotate.setVisibility(View.INVISIBLE);
            iv_arrow.setVisibility(View.VISIBLE);
            tv_state.setText("下拉刷新");
            tv_time.setText("最后刷新："+getCurrentTime());
        }
    }
    private OnRefreshListener listener;
    public void setOnRefreshListener(OnRefreshListener listener){
        this.listener = listener;
    }
    public interface OnRefreshListener{
        void onPullRefresh();
        void onLoadingMore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();                                    //一开始就触摸屏幕的坐标
                break;
            case MotionEvent.ACTION_MOVE:
                if(currentState==REFRESHING){
                    break;
                }
                int deltaY = (int) (ev.getY() - downY);
                int paddingTop = -Headviewheight + deltaY;                      //实际下拉的距离
                if(paddingTop>-Headviewheight && getFirstVisiblePosition()==0){ //当完全把headview拉下来以后才开始进行刷新
                    headview.setPadding(0, paddingTop, 0, 0);
                    Log.d(TAG,"padding is " + paddingTop);
                    if(paddingTop>=0 && currentState==PULL_REFRESH){           //当大于headview后就进入松开刷新状态
                        //从下拉刷新进入松开刷新状态
                        currentState = RELEASE_REFRESH;
                        refreshHeaderView();
                    }else if (paddingTop<0 && currentState==RELEASE_REFRESH) {  //当开始上划动作时候还能继续下滑  //避免划回去还是更新状态
                        //进入下拉刷新状态
                        currentState = PULL_REFRESH;
                        refreshHeaderView();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(currentState==PULL_REFRESH){
                    //隐藏headerView
                    headview.setPadding(0, -Headviewheight, 0, 0);
                }else if (currentState==RELEASE_REFRESH) {
                    headview.setPadding(0, 0, 0, 0);
                    currentState = REFRESHING;
                    refreshHeaderView();

                    if(listener!=null){
                        listener.onPullRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 获取当前系统时间，并格式化
     * @return
     */
    private String getCurrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 根据currentState来更新headerView
     */
    private void refreshHeaderView(){
        switch (currentState) {
            case PULL_REFRESH:
                tv_state.setText("下拉刷新");
                iv_arrow.startAnimation(downAnimation);
                break;
            case RELEASE_REFRESH:
                tv_state.setText("松开刷新");
                iv_arrow.startAnimation(upAnimation);
                break;
            case REFRESHING:
                iv_arrow.clearAnimation();//因为向上的旋转动画有可能没有执行完
                iv_arrow.setVisibility(View.INVISIBLE);
                pb_rotate.setVisibility(View.VISIBLE);
                tv_state.setText("正在刷新...");
                break;
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState==OnScrollListener.SCROLL_STATE_IDLE
                && getLastVisiblePosition()==(getCount()-1) &&!isLoadingMore){
            isLoadingMore = true;

            footerView.setPadding(0, -Headviewheight, 0, 0);//显示出footerView
            setSelection(getCount());//让listview最后一条显示出来

            if(listener!=null){
                listener.onLoadingMore();
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
