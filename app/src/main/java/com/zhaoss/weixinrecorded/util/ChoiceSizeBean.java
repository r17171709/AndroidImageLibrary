package com.zhaoss.weixinrecorded.util;

public class ChoiceSizeBean implements Comparable<ChoiceSizeBean> {
    private int height;
    private int width;

    public ChoiceSizeBean(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public int compareTo(ChoiceSizeBean o) {
        return width * height - o.getWidth() * o.getHeight();
    }
}
