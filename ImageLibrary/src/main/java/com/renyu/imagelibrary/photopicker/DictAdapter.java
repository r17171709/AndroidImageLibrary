package com.renyu.imagelibrary.photopicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.bean.PhotoDirectory;
import com.renyu.imagelibrary.commonutils.Utils;

import java.util.ArrayList;

/**
 * Created by Clevo on 2016/9/1.
 */
public class DictAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PhotoDirectory> models;

    public DictAdapter(Context context, ArrayList<PhotoDirectory> models) {
        this.context = context;
        this.models = models;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public Object getItem(int position) {
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DictHolder holder;
        if (convertView == null) {
            holder = new DictHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_dict, parent, false);
            holder.adapter_dict_cover = convertView.findViewById(R.id.adapter_dict_cover);
            holder.adapter_dict_name = convertView.findViewById(R.id.adapter_dict_name);
            holder.adapter_dict_count = convertView.findViewById(R.id.adapter_dict_count);
            convertView.setTag(holder);
        } else {
            holder = (DictHolder) convertView.getTag();
        }
        Utils.loadFresco("file://" + models.get(position).getCoverPath(), SizeUtils.dp2px(70), SizeUtils.dp2px(70), holder.adapter_dict_cover);
        holder.adapter_dict_name.setText(models.get(position).getName());
        holder.adapter_dict_count.setText(models.get(position).getPhotos().size() + "张");
        return convertView;
    }

    class DictHolder {
        SimpleDraweeView adapter_dict_cover;
        TextView adapter_dict_name;
        TextView adapter_dict_count;
    }
}
