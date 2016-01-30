/*
 * Copyright (C) 2016 Fukuta,Shinya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.displayingbitmaps.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class AndroidImageView implements AbstractImageView {

    private static final String TAG = "AndroidImageView";

    private final Resources mResources;

    private final WeakReference<ImageView> mImageViewReference;

    public AndroidImageView(Resources res, ImageView imageView) {
        mResources = res;
        mImageViewReference = new WeakReference<ImageView>(imageView);
    }

    public void setOnClickListener(View.OnClickListener l) {

        ImageView imageView = mImageViewReference.get();
        if (imageView == null) {
            return;
        }

        imageView.setOnClickListener(l);
    }

    @Override
    public boolean hasOwner() {
        return mImageViewReference.get() != null;
    }

    @Override
    public void setImageDrawable(AbstractBitmapDrawable d) {

        ImageView imageView = mImageViewReference.get();
        if (imageView == null) {
            return;
        }

        if (!(d instanceof AndroidBitmapDrawable)) {
            Log.w(TAG, "invalid type");
            return;
        }

        imageView.setImageDrawable(((AndroidBitmapDrawable) d).bitmapDrawable);
    }

    @Override
    public void setFadeInImageDrawable(AbstractBitmap from, AbstractBitmapDrawable to, int durationMillis) {

        ImageView imageView = mImageViewReference.get();
        if (imageView == null) {
            return;
        }

        if (!(from instanceof AndroidBitmap) || !(to instanceof AndroidBitmapDrawable)) {
            Log.w(TAG, "invalid type, setFadeInImageDrawable");
            return;
        }

        // Transition drawable with a transparent drawable and the final drawable
        final TransitionDrawable td =
                new TransitionDrawable(new Drawable[] {
                        new ColorDrawable(android.R.color.transparent),
                        ((AndroidBitmapDrawable)to).bitmapDrawable
                });
        // Set background to loading bitmap
        imageView.setBackgroundDrawable(
                new BitmapDrawable(mResources, ((AndroidBitmap)from).bitmap));

        imageView.setImageDrawable(td);
        td.startTransition(durationMillis);
    }

    @Override
    public void setAsyncDrawable(AbstractBitmap loadingBitmap, ImageWorker.BitmapWorkerTask bitmapWorkerTask) {

        ImageView imageView = mImageViewReference.get();
        if (imageView == null) {
            return;
        }

        Bitmap bitmap;
        if (loadingBitmap != null) {
            if (!(loadingBitmap instanceof AndroidBitmap)) {
                Log.w(TAG, "invalid type, loadingBitmap");
                return;
            }
            bitmap = ((AndroidBitmap)loadingBitmap).bitmap;
        } else {
            bitmap = null;
        }

        final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, bitmap, bitmapWorkerTask);
        imageView.setImageDrawable(asyncDrawable);
    }

    @Override
    public ImageWorker.BitmapWorkerTask getBitmapWorkerTask() {

        ImageView imageView = mImageViewReference.get();
        if (imageView == null) {
            return null;
        }

        final Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
            return asyncDrawable.getBitmapWorkerTask();
        }
        return null;
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<ImageWorker.BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, ImageWorker.BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<ImageWorker.BitmapWorkerTask>(bitmapWorkerTask);
        }

        public ImageWorker.BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

}
