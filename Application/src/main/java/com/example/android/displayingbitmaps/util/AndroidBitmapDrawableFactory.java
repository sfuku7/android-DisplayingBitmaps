package com.example.android.displayingbitmaps.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ResourceCursorAdapter;

import android.util.Log;

/**
 * Created by sfuku on 2016/01/29.
 */
public class AndroidBitmapDrawableFactory implements AbstractBitmapDrawableFactory {

    private static final String TAG = "ABitmapDrawableFactory";
    private Resources mResources;

    public AndroidBitmapDrawableFactory(Resources res) {
        mResources = res;
    }
    @Override
    public AbstractBitmapDrawable createAbstractBitmapDrawable(AbstractBitmap bitmap) {

        if (!(bitmap instanceof AndroidBitmap)) {
            Log.w(TAG, "invalid type, createAbstractBitmapDrawable");
        }

        Bitmap androidBitmap = ((AndroidBitmap)bitmap).bitmap;
        BitmapDrawable bitmapDrawable;

        if (Utils.hasHoneycomb()) {
            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
            bitmapDrawable = new BitmapDrawable(mResources, androidBitmap);
        } else {
            // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
            // which will recycle automagically
            bitmapDrawable = new RecyclingBitmapDrawable(mResources, androidBitmap);
        }

        return new AndroidBitmapDrawable(bitmapDrawable);
    }
}
