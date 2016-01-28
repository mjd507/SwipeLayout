package com.fighting.qqview_swipelayout;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.fighting.qqview_swipelayout.adapter.MyAdapter;
import com.fighting.qqview_swipelayout.util.Cheeses;

/**
 * 描述：
 * 作者 mjd
 * 日期：2016/1/28 20:08
 */
public class MainActivity extends Activity {


    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
    }

    private void initData() {
        MyAdapter adapter = new MyAdapter(this, Cheeses.NAMES);
        lv.setAdapter(adapter);
    }

    private void initListener() {

    }
}
