package com.nimasystems.lightcast.ui.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import java.util.List;

public class EndlessListView extends ListView implements OnScrollListener {

    private View footer;
    private boolean isLoading;
    private EndlessListener listener;
    private EndlessAdapter adapter;

    public EndlessListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
    }

    public EndlessListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }

    public EndlessListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        int l = visibleItemCount + firstVisibleItem;
        if (totalItemCount > 0 && l >= totalItemCount && !isLoading) {

            if (getAdapter() == null)
                return;

            if (getAdapter().getCount() == 0)
                return;

            if (listener == null || !listener.canLoadMore()) {
                return;
            }

            // It is time to add new data. We call the listener

            if (footer != null) {
                footer.setVisibility(View.VISIBLE);
            }

            isLoading = true;
            listener.loadData();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void setLoadingView(int resId) {
        LayoutInflater inflater = (LayoutInflater) super.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // warning - this must be set BEFORE setting the adapter!
        footer = inflater.inflate(resId, null);
        footer.setVisibility(View.GONE);
        this.addFooterView(footer);
    }

    public void setAdapter(EndlessAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }

    public void addNewData(List<?> data) {
        if (adapter == null) {
            return;
        }
        adapter.addItems(data);
        cancelLoading();
    }

    public void cancelLoading() {
        if (!isLoading) {
            return;
        }

        if (footer != null) {
            footer.setVisibility(View.GONE);
        }

        isLoading = false;
    }

    public EndlessListener getListener() {
        return listener;
    }

    public void setListener(EndlessListener listener) {
        this.listener = listener;
    }

    public static interface EndlessListener {
        public void loadData();

        public boolean canLoadMore();
    }

}
