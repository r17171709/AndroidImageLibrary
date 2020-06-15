package com.renyu.imagelibrary;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;

/**
 * Created by Administrator on 2020/6/12.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int column;

    public SpaceItemDecoration(int space, int column) {
        this.space = space;
        this.column = column;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = SizeUtils.dp2px(space);
        outRect.bottom = space;
        if (parent.getChildLayoutPosition(view) % column == 0) {
            outRect.left = 0;
        }
    }
}