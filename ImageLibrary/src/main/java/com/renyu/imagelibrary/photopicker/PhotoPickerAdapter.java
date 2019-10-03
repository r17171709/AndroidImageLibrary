package com.renyu.imagelibrary.photopicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        void add(String path);

        void remove(String path);

        void takePic();
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
        if (models.get(position).getPath().equals("")) {
            holder.camera_image.setVisibility(View.VISIBLE);
            holder.camera_image.setOnClickListener(v -> listener.takePic());
            holder.photopicker_image.setVisibility(View.GONE);
            holder.photopicker_choice.setVisibility(View.GONE);
        } else {
            holder.camera_image.setVisibility(View.GONE);
            holder.photopicker_image.setVisibility(View.VISIBLE);
            holder.photopicker_choice.setVisibility(View.VISIBLE);
        }
        Utils.loadFresco("file://" + models.get(position).getPath(), SizeUtils.dp2px(118), SizeUtils.dp2px(118), holder.photopicker_image);
        holder.photopicker_image.setOnClickListener(v -> {
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
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class PhotoPickerViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView photopicker_image;
        private ImageView photopicker_choice;
        private LinearLayout camera_image;

        PhotoPickerViewHolder(View itemView) {
            super(itemView);

            photopicker_image = itemView.findViewById(R.id.photopicker_image);
            photopicker_choice = itemView.findViewById(R.id.photopicker_choice);
            camera_image = itemView.findViewById(R.id.camera_image);
        }
    }
}