package com.example.android.displayingbitmaps.util;

import android.graphics.drawable.BitmapDrawable;

public class AndroidBitmapDrawable implements AbstractBitmapDrawable {

    public final BitmapDrawable bitmapDrawable;

    public AndroidBitmapDrawable(BitmapDrawable d) {
        bitmapDrawable = d;
    }

    @Override
    public AbstractBitmap getBitmap() {
        return new AndroidBitmap(bitmapDrawable.getBitmap());
    }
}
