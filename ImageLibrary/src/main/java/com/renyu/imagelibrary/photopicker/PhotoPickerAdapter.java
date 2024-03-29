package com.renyu.imagelibrary.photopicker;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.renyu.commonlibrary.commonutils.ResourceUtils;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.bean.Photo;
import com.renyu.imagelibrary.commonutils.Utils;

import java.util.ArrayList;

/**
 * Created by Clevo on 2016/9/1.
 */
public class PhotoPickerAdapter extends RecyclerView.Adapter<PhotoPickerAdapter.PhotoPickerViewHolder> {
    private Context context;
    private ArrayList<Photo> models;

    private OperImageListener listener;

    public interface OperImageListener {
        void add(Uri path);

        void remove(Uri path);

        void showPreview(Uri path, SimpleDraweeView simpleDraweeView);
    }

    PhotoPickerAdapter(Context context, ArrayList<Photo> models, OperImageListener listener) {
        this.context = context;
        this.models = models;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_photopicker, parent, false);
        return new PhotoPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PhotoPickerViewHolder holder, final int position) {
        Utils.loadFresco(models.get(position).getPath(), SizeUtils.dp2px(118), SizeUtils.dp2px(118), holder.photopicker_image);
        holder.photopicker_choice.setOnClickListener(v -> {
            boolean flag = models.get(position).isSelect();
            if (((PhotoPickerActivity) context).imagePaths.size() == ((PhotoPickerActivity) context).maxNum && !flag) {
                Toast.makeText(context, "您最多只能选择" + ((PhotoPickerActivity) context).maxNum + "张图片", Toast.LENGTH_SHORT).show();
                return;
            }
            models.get(position).setSelect(!flag);
            holder.photopicker_choice.setImageResource(!flag ? ResourceUtils.getMipmapId(context, "ic_choice_select") : ResourceUtils.getMipmapId(context, "ic_choice_normal"));
            if (!flag) {
                listener.add(models.get(position).getPath());
            } else {
                listener.remove(models.get(position).getPath());
            }
        });
        holder.photopicker_choice.setImageResource(models.get(position).isSelect() ? ResourceUtils.getMipmapId(context, "ic_choice_select") : ResourceUtils.getMipmapId(context, "ic_choice_normal"));
        holder.photopicker_image.setOnClickListener(v -> {
            listener.showPreview(models.get(position).getPath(), holder.photopicker_image);
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    static class PhotoPickerViewHolder extends RecyclerView.ViewHolder {
        private final SimpleDraweeView photopicker_image;
        private final ImageView photopicker_choice;

        PhotoPickerViewHolder(View itemView) {
            super(itemView);

            photopicker_image = itemView.findViewById(R.id.photopicker_image);
            photopicker_choice = itemView.findViewById(R.id.photopicker_choice);
        }
    }
}