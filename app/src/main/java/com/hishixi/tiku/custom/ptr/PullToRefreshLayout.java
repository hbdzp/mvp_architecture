package com.hishixi.tiku.custom.ptr;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.hishixi.tiku.R;
import com.hishixi.tiku.custom.sticky.StickyHeaderLayout;
import com.hishixi.tiku.utils.ActivityUtils;
import com.hishixi.tiku.utils.StringUtils;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

public class PullToRefreshLayout extends PtrFrameLayout implements PtrUIHandler,
        PtrHandler {

    private int mRefreshLayoutId;//headerlayout
    private float mRefreshLayoutHeight;//头部的高度
    private TextView mRefreshText;//下拉刷新时加载文字
    private View mRefreshView;//下拉刷新时有可以旋转的view
    private OnRefreshListener mOnRefreshListener;
    private OnStatusPrefresh mOnStatusPrefresh;

    private String mPrepareText;//准备刷新时的文字
    private String mRecycleText;//可以释放时的文字
    private String mLoadingText;//正在加载时的文字
    private String mCompleteText;//加载完成时的文字

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void setOnStatusPrefresh(OnStatusPrefresh onStatusPrefresh) {
        mOnStatusPrefresh = onStatusPrefresh;
    }

    public PullToRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initStyleable(context, attrs);
        initAnimation();
        initView(context);
    }

    /**
     * 初始化view
     *
     * @param context 上下文
     */
    private void initView(Context context) {
        View parentView = LayoutInflater.from(context).inflate(mRefreshLayoutId, null);
        parentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT
                , ActivityUtils.dip2px(this.getContext(), mRefreshLayoutHeight)));
        //设置刷新头部文字
//        mRefreshText = (TextView) parentView.findViewById(R.id.refresh_text);
//        mRefreshView = parentView.findViewById(R.id.refresh_view);
        setHeaderView(parentView);
        setPtrHandler(this);
        addPtrUIHandler(this);
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {

    }

    /**
     * 初始化属性
     *
     * @param context 上下文
     * @param attrs   属性值
     */
    private void initStyleable(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable
                .PullToRefreshLayout);
        mRefreshLayoutId = array.getResourceId(R.styleable
                .PullToRefreshLayout_refresh_header_layout, 0);
        mRefreshLayoutHeight = array.getInt(R.styleable
                .PullToRefreshLayout_refresh_layout_height, 0);
        mPrepareText = array.getString(R.styleable
                .PullToRefreshLayout_refresh_prepare_text);
        if (StringUtils.isEmpty(mPrepareText))
            mPrepareText = context.getResources().getString(R.string
                    .pullToRefresh_prepare);
        mRecycleText = array.getString(R.styleable
                .PullToRefreshLayout_refresh_recycle_text);
        if (StringUtils.isEmpty(mRecycleText))
            mRecycleText = context.getResources().getString(R.string
                    .pullToRefresh_recycle);
        mLoadingText = array.getString(R.styleable
                .PullToRefreshLayout_refresh_loading_text);
        if (StringUtils.isEmpty(mLoadingText))
            mLoadingText = context.getResources().getString(R.string
                    .pullToRefresh_loading);
        mCompleteText = array.getString(R.styleable
                .PullToRefreshLayout_refresh_complete_text);
        if (StringUtils.isEmpty(mCompleteText))
            mCompleteText = context.getResources().getString(R.string
                    .pullToRefresh_complete);
        array.recycle();

    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        if (content instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) content;
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();
            int firstVisiblePosition = manager.findFirstCompletelyVisibleItemPosition();
            if (firstVisiblePosition == 0)
                return true;
        } else if (content instanceof StickyHeaderLayout) {
            StickyHeaderLayout layout = (StickyHeaderLayout) content;
            RecyclerView recyclerView = (RecyclerView) layout.getChildAt(3);
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();
            int firstVisiblePosition = manager.findFirstCompletelyVisibleItemPosition();
            if (firstVisiblePosition == 0 && layout.isScrollTop())
                return true;
        }
//        else if (content instanceof FrameLayout) {
//            FrameLayout layout = (FrameLayout) content;
//            RecyclerView recyclerView = (RecyclerView) layout.getChildAt(0);
//            if (recyclerView == null)
//                throw new RuntimeException("第一个view不是RecyclerView");
//            LinearLayoutManager manager = (LinearLayoutManager) recyclerView
//                    .getLayoutManager();
//            int firstVisiblePosition = manager.findFirstCompletelyVisibleItemPosition();
//            if (firstVisiblePosition == 0)
//                return true;
//        }
        return checkContentCanBePulledDown(frame, content, header);
    }

    public static boolean checkContentCanBePulledDown(PtrFrameLayout frame, View
            content, View header) {
        return !canChildScrollUp(content);
    }

    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {
        if (mOnRefreshListener != null)
            mOnRefreshListener.onRefresh();
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {

    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte
            status, PtrIndicator ptrIndicator) {
        int currentPos = ptrIndicator.getCurrentPosY();
        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        if (null != mOnStatusPrefresh)
            mOnStatusPrefresh.onPtrStatusPrefresh(status);
        switch (status) {
            case PtrFrameLayout.PTR_STATUS_PREPARE://下拉时
                //开始下拉
//                mRefreshText.setText(mPrepareText);
//                mRefreshView.setRotation(currentPos);
                if (currentPos >= mOffsetToRefresh) {//可以刷新了
//                    mRefreshText.setText(mRecycleText);
                }
                break;
            case PtrFrameLayout.PTR_STATUS_LOADING://释放加载时
//                mRefreshText.setText(mLoadingText);
//                mRefreshView.startAnimation(mRotateAnimation);
                break;
            case PtrFrameLayout.PTR_STATUS_COMPLETE://更新完成时
//                mRefreshText.setText(mCompleteText);
//                mRefreshView.clearAnimation();
                break;
            case PtrFrameLayout.PTR_STATUS_INIT://初始化
                break;
        }
    }

    /**
     * 设置content是否固定 默认为true
     *
     * @param pinContent true 移动 false 固定
     */
    @Override
    public void setPinContent(boolean pinContent) {
        super.setPinContent(pinContent);
    }

    /**
     * 下拉刷新回调
     */
    public interface OnRefreshListener {

        void onRefresh();
    }

    /**
     * 下拉回调
     */
    public interface OnStatusPrefresh {
        void onPtrStatusPrefresh(byte status);
    }
}
