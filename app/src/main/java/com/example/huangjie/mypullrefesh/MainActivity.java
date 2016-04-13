package com.example.huangjie.mypullrefesh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.huangjie.mypullrefesh.ViewRefresh.ViewPullRrfresh;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private ViewPullRrfresh refreshListView;

    private ArrayList<String> list = new ArrayList<String>();

    private MyAdapter adapter;

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            //更新UI
            adapter.notifyDataSetChanged();
            refreshListView.completeRefresh();
        };
    };

    /**
     * 模拟向服务器请求数据
     */
    private void requestDataFromServer(final boolean isLoadingMore){
        new Thread(){
            public void run() {
                SystemClock.sleep(3000);//模拟请求服务器的一个时间长度

                if(isLoadingMore){
                    list.add("加载更多的数据-1");
                    list.add("加载更多的数据-2");
                    list.add("加载更多的数据-3");
                }else {
                    list.add(0, "下拉刷新的数据");
                }

                //在UI线程更新UI
                handler.sendEmptyMessage(0);
            };
        }.start();
    }
    private void initData() {
        for (int i = 0; i < 20; i++) {
            list.add("listview原来的数据 - "+i);
        }
        adapter = new MyAdapter();
        refreshListView.setAdapter(adapter);

        refreshListView.setOnRefreshListener(new ViewPullRrfresh.OnRefreshListener() {
            @Override
            public void onPullRefresh() {
                //需要联网请求服务器的数据，然后更新UI
                requestDataFromServer(false);
            }

            @Override
            public void onLoadingMore() {
                requestDataFromServer(true);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        refreshListView = (ViewPullRrfresh) findViewById(R.id.refreshListView);
        initData();
    }
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(18);
            textView.setText(list.get(position));
            return textView;
        }

    }
}
