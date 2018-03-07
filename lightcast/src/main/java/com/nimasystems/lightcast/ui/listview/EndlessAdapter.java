package com.nimasystems.lightcast.ui.listview;

import android.widget.ListAdapter;

import java.util.List;

public interface EndlessAdapter extends ListAdapter {

    void addItems(List<?> items);
}
