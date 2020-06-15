package com.renyu.imagelibrary.photopicker;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.renyu.commonlibrary.commonutils.ResourceUtils;
import com.renyu.commonlibrary.params.InitParams;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.bean.Video;
import com.renyu.imagelibrary.commonutils.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Clevo on 2016/9/1.
 */
public class VideoPickerAdapter extends RecyclerView.Adapter<VideoPickerAdapter.VideoPickerViewHolder> {
    private ArrayList<Video> models;

    private OperVideoListener listener;

    public interface OperVideoListener {
        void add(String path);

        void remove(String path);
    }

    VideoPickerAdapter(ArrayList<Video> models, OperVideoListener listener) {
        this.models = models;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_videopicker, parent, false);
        return new VideoPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoPickerViewHolder holder, final int position) {
        String imagePath = InitParams.IMAGE_PATH + File.separator + new File(models.get(position).getPath()).getName() + ".jpg";
        int duration = Integer.parseInt(models.get(position).getDuration()) / 1000;
        int minute = duration / 60;
        int second = duration - 60 * minute;
        holder.videopicker_text.setText(minute + ":" + (second < 10 ? "0" + second : "" + second));
        holder.videopicker_choice.setImageResource(models.get(position).isSelect() ? ResourceUtils.getMipmapId(holder.itemView.getContext(), "ic_choice_select") : ResourceUtils.getMipmapId(holder.itemView.getContext(), "ic_choice_normal"));
        holder.videopicker_image.setOnClickListener(v -> {
            boolean flag = models.get(position).isSelect();
            if (((VideoPickerActivity) holder.itemView.getContext()).videoPaths.size() == ((VideoPickerActivity) holder.itemView.getContext()).maxNum && !flag) {
                Toast.makeText(holder.itemView.getContext(), "您最多只能选择" + ((VideoPickerActivity) holder.itemView.getContext()).maxNum + "个视频", Toast.LENGTH_SHORT).show();
                return;
            }
            models.get(position).setSelect(!flag);
            holder.videopicker_choice.setImageResource(!flag ? ResourceUtils.getMipmapId(holder.itemView.getContext(), "ic_choice_select") : ResourceUtils.getMipmapId(holder.itemView.getContext(), "ic_choice_normal"));
            if (!flag) {
                listener.add(models.get(position).getPath());
            } else {
                listener.remove(models.get(position).getPath());
            }
        });
        Utils.loadFresco(Uri.parse("file://" + imagePath), SizeUtils.dp2px(118), SizeUtils.dp2px(118), holder.videopicker_image);
        holder.videopicker_image.setTag(models.get(position).getPath());
        if (!new File(imagePath).exists()) {
            ((VideoPickerActivity) (holder.itemView.getContext())).loadThumbImage(models.get(position).getPath());
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class VideoPickerViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView videopicker_image;
        private ImageView videopicker_choice;
        private TextView videopicker_text;

        VideoPickerViewHolder(View itemView) {
            super(itemView);

            videopicker_image = itemView.findViewById(R.id.videopicker_image);
            videopicker_choice = itemView.findViewById(R.id.videopicker_choice);
            videopicker_text = itemView.findViewById(R.id.videopicker_text);
        }
    }
}