package com.example.android.displayingbitmaps.util;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.FileDescriptor;

public class AndroidBitmapFactory implements AbstractBitmapFactory {

    private static final String TAG = "AndroidBitmapFactory";

    private final Resources mResources;

    public AndroidBitmapFactory(Resources res) {
        mResources = res;
    }
    @Override
    public AbstractBitmap decodeResource(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, resId);
        return bitmap != null ? new AndroidBitmap(bitmap) : null;
    }

    @Override
    public AbstractBitmap decodeSampledBitmapFromResource(int resId, int reqWidth, int reqHeight, ImageCache cache) {
        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mResources, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = ImageResizer.calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, resId, options);

        return new AndroidBitmap(bitmap);
    }

    @Override
    public AbstractBitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = ImageResizer.calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);

        return new AndroidBitmap(bitmap);
    }

    @Override
    public AbstractBitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor,
                                                            int reqWidth, int reqHeight,
                                                            ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = ImageResizer.calculateInSampleSize(
                options.outWidth, options.outHeight, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        return new AndroidBitmap(bitmap);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        //BEGIN_INCLUDE(add_bitmap_options)
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            AbstractBitmap inBitmap = cache.getBitmapFromReusableSet(options);
            if (inBitmap != null && inBitmap instanceof AndroidBitmap) {
                options.inBitmap = ((AndroidBitmap)inBitmap).bitmap;
            }
        }
        //END_INCLUDE(add_bitmap_options)
    }
}
