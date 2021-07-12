package com.renyu.androidimagelibrary;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.FrescoImageViewFactory;
import com.github.piasy.biv.view.ImageViewFactory;
import com.renyu.imagelibrary.R;
import com.renyu.imagelibrary.preview.ProgressPieIndicator;

import java.util.List;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {
    private List<Uri> imageUrls;
    private ImageViewFactory viewFactory = new FrescoImageViewFactory();

    PreviewAdapter(List<Uri> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.setIsRecyclable(false);
        holder.bind(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clear();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.hasNoImage()) {
            holder.rebind();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private BigImageView itemImage;
        private Uri imageUrl;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.biv_preview);
            itemImage.setProgressIndicator(new ProgressPieIndicator());
            itemImage.setTapToRetry(true);
            itemImage.setImageViewFactory(viewFactory);
        }

        void bind(Uri imageUrl) {
            this.imageUrl = imageUrl;
            itemImage.showImage(Uri.EMPTY, imageUrl);
        }

        void rebind() {
            itemImage.showImage(imageUrl);
        }

        void clear() {
            SubsamplingScaleImageView ssiv = itemImage.getSSIV();
            if (ssiv != null) {
                ssiv.recycle();
            }
        }

        boolean hasNoImage() {
            SubsamplingScaleImageView ssiv = itemImage.getSSIV();
            return ssiv == null || !ssiv.hasImage();
        }
    }
}
