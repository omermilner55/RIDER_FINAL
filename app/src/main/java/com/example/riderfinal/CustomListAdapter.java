package com.example.riderfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomListAdapter extends BaseAdapter {
    Context context;
    String[] UserListView;
    LayoutInflater inflater;

    public CustomListAdapter(Context context, String[] UserListView) {
        this.context = context;
        this.UserListView = UserListView;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return UserListView.length;
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
        convertView = inflater.inflate(R.layout.activity_custom_list_view, null);
        TextView txtView = convertView.findViewById(R.id.textview);
        txtView.setText(UserListView[position]);
        return convertView;
    }
}
