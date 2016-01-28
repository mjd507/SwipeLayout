package com.fighting.qqview_swipelayout.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fighting.qqview_swipelayout.R;
import com.fighting.qqview_swipelayout.view.SwipeLayout;

/**
 * 描述：
 * 作者 mjd
 * 日期：2016/1/28 22:28
 */
public class MyAdapter extends BaseAdapter {
    private Context context;
    private String[] names;
    private SwipeLayout lastOpenedSwipeLayout;

    public MyAdapter(Context context, String[] names) {
        this.context = context;
        this.names = names;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(names[position]);
        SwipeLayout swipeLayout = (SwipeLayout) convertView;
        swipeLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
            @Override
            public void onOpen(SwipeLayout swipeLayout) {
                //当前 item 被打开时，记录下此 item
                lastOpenedSwipeLayout = swipeLayout;
            }

            @Override
            public void onClose(SwipeLayout swipeLayout) {
            }

            @Override
            public void onStartOpen(SwipeLayout swipeLayout) {
                //当前 item 将要打开时关闭上一次打开的 item
                if (lastOpenedSwipeLayout != null) {
                    lastOpenedSwipeLayout.close();
                }
            }

            @Override
            public void onStartClose(SwipeLayout swipeLayout) {
            }
        });
        return convertView;
    }
}
