package com.example.android.displayingbitmaps.util;

public interface AbstractImageView {

    boolean hasOwner();

    void setImageDrawable(AbstractBitmapDrawable d);

    void setFadeInImageDrawable(AbstractBitmap from, AbstractBitmapDrawable to, int durationMillis);

    void setAsyncDrawable(AbstractBitmap loadingBitmap, ImageWorker.BitmapWorkerTask bitmapWorkerTask);

    ImageWorker.BitmapWorkerTask getBitmapWorkerTask();
}
