package com.fyang21117.yyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    private List<HelpItem> itemList;
    private LayoutInflater inflater;
    public MyAdapter() {}

    public MyAdapter(List<HelpItem> itemList, Context context) {
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList==null?0:itemList.size();
    }

    @Override
    public HelpItem getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //加载布局为一个视图
        View view = inflater.inflate(R.layout.simple_list_item_2,null);

        HelpItem item=getItem(position);
        //在view视图中查找id为image_photo的控件
        ImageView image_photo=   view.findViewById(R.id.image_photo);
        TextView tv_name=   view.findViewById(R.id.text1);
        TextView tv_age=   view.findViewById(R.id.text2);

        image_photo.setImageResource(item.getImage_photo());
        tv_name.setText(item.getAppname());
        tv_age.setText(String.valueOf(item.getPackagename()));
        //tv_name.setText(String.valueOf(item.getName()));
        //tv_age.setText(String.valueOf(item.getInfo()));
        return view;
    }
}
