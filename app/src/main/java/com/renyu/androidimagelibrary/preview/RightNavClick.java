package com.renyu.androidimagelibrary.preview;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.renyu.androidimagelibrary.preview.impl.RightNavClickImpl;

/**
 * Created by Administrator on 2017/7/12.
 */

public class RightNavClick implements RightNavClickImpl, Parcelable {
    public RightNavClick() {
        super();
    }

    protected RightNavClick(Parcel in) {
    }

    public static final Creator<RightNavClick> CREATOR = new Creator<RightNavClick>() {
        @Override
        public RightNavClick createFromParcel(Parcel in) {
            return new RightNavClick(in);
        }

        @Override
        public RightNavClick[] newArray(int size) {
            return new RightNavClick[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @Override
    public void click(Context context) {
        Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
    }
}
