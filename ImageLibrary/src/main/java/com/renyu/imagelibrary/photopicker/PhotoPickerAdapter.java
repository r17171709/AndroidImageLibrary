package com.renyu.imagelibrary.photopicker;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.bean.Photo;

import java.util.ArrayList;

/**
 * Created by Clevo on 2016/9/1.
 */
public class PhotoPickerAdapter extends RecyclerView.Adapter<PhotoPickerAdapter.PhotoPickerViewHolder> {

    Context context;
    ArrayList<Photo> models;

    OperImageListener listener;

    public interface OperImageListener {
        void add(String path);
        void remove(String path);
        void show(String path);
        void takePic();
    }

    public PhotoPickerAdapter(Context context, ArrayList<Photo> models, OperImageListener listener) {
        this.context = context;
        this.models = models;
        this.listener = listener;
    }

    @Override
    public PhotoPickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_photopicker, parent, false);
        return new PhotoPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PhotoPickerViewHolder holder, final int position) {
        if (models.get(position).getPath().equals("")) {
            holder.camera_image.setVisibility(View.VISIBLE);
            holder.camera_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.takePic();
                }
            });
            holder.photopicker_image.setVisibility(View.GONE);
            holder.photopicker_choice.setVisibility(View.GONE);
        }
        else {
            holder.camera_image.setVisibility(View.GONE);
            holder.photopicker_image.setVisibility(View.VISIBLE);
            holder.photopicker_choice.setVisibility(View.VISIBLE);
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://"+models.get(position).getPath()))
                .setResizeOptions(new ResizeOptions(SizeUtils.dp2px(118), SizeUtils.dp2px(118))).build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).setAutoPlayAnimations(true).build();
        holder.photopicker_image.setController(draweeController);
        holder.photopicker_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.show(models.get(position).getPath());
            }
        });
        holder.photopicker_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag=models.get(position).isSelect();
                if (((PhotoPickerActivity) context).imagePaths.size()==((PhotoPickerActivity) context).maxNum && !flag) {
                    Toast.makeText(context, "您最多只能选择" + ((PhotoPickerActivity) context).maxNum + "张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                models.get(position).setSelect(!flag);
                holder.photopicker_choice.setImageResource(!flag?R.mipmap.ic_choice_select:R.mipmap.ic_choice_normal);
                if (!flag) {
                    listener.add(models.get(position).getPath());
                }
                else {
                    listener.remove(models.get(position).getPath());
                }
            }
        });
        holder.photopicker_choice.setImageResource(models.get(position).isSelect()?R.mipmap.ic_choice_select:R.mipmap.ic_choice_normal);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class PhotoPickerViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView photopicker_image;
        ImageView photopicker_choice;
        LinearLayout camera_image;

        public PhotoPickerViewHolder(View itemView) {
            super(itemView);

            photopicker_image= (SimpleDraweeView) itemView.findViewById(R.id.photopicker_image);
            photopicker_choice= (ImageView) itemView.findViewById(R.id.photopicker_choice);
            camera_image= (LinearLayout) itemView.findViewById(R.id.camera_image);
        }
    }
}
