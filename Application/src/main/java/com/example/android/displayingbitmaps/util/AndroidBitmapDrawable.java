package com.example.android.displayingbitmaps.util;

import android.graphics.drawable.BitmapDrawable;

public class AndroidBitmapDrawable implements AbstractBitmapDrawable {

    public final BitmapDrawable bitmapDrawable;

    private final AndroidBitmap mAndroidBitmap;

    public AndroidBitmapDrawable(BitmapDrawable d) {
        bitmapDrawable = d;
        mAndroidBitmap = new AndroidBitmap(bitmapDrawable.getBitmap());
    }

    @Override
    public AbstractBitmap getBitmap() {
        return mAndroidBitmap;
    }
}
