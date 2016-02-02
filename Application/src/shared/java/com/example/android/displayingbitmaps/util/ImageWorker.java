/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.example.android.common.logger.Log;
import com.example.android.j2objcdisplayingbitmaps.BuildConfig;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private AbstractBitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    protected final AbstractBitmapFactory mBitmapFactory;
    private final AbstractBitmapDrawableFactory mBitmapDrawableFactory;
    private final MemoryImageCacheFactory mMemoryImageCacheFactory;
    private final DiskEnvironment mDiskEnvironment;
    private final AsyncTask.ThreadOperation mThreadOperation;

    protected ImageWorker(AbstractBitmapFactory bitmapFactory,
                          AbstractBitmapDrawableFactory bitmapDrawableFactory,
                          MemoryImageCacheFactory memoryImageCacheFactory,
                          DiskEnvironment env,
                          AsyncTask.ThreadOperation accessor) {
        mBitmapFactory = bitmapFactory;
        mBitmapDrawableFactory = bitmapDrawableFactory;
        mMemoryImageCacheFactory = memoryImageCacheFactory;
        mDiskEnvironment = env;
        mThreadOperation = accessor;
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener A listener that will be called back once the image has been loaded.
     */
    public void loadImage(Object data, AbstractImageView imageView, OnImageLoadedListener listener) {
        if (data == null) {
            return;
        }

        AbstractBitmapDrawable value = null;

        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (value != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(value);
            if (listener != null) {
                listener.onImageLoaded(true);
            }
        } else if (cancelPotentialWork(data, imageView)) {
            //BEGIN_INCLUDE(execute_background_task)
            final BitmapWorkerTask task = new BitmapWorkerTask(mThreadOperation, data, imageView, listener);
            imageView.setAsyncDrawable(mLoadingBitmap, task);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
            //END_INCLUDE(execute_background_task)
        }
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(Object data, AbstractImageView imageView) {
        loadImage(data, imageView, null);
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(AbstractBitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(String resId) {
        mLoadingBitmap = mBitmapFactory.decodeResource(resId);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(ImageCache.ObjectHolderFactory objectHolderFactory,
                              ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.getInstance(objectHolderFactory, mMemoryImageCacheFactory, mBitmapFactory, mImageCacheParams);
        new CacheAsyncTask(mThreadOperation).execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param activity
     * @param diskCacheDirectoryName See
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(android.content.Context, String)}.
     */
    public void addImageCache(ImageCache.ObjectHolderFactory objectHolderFactory, String diskCacheDirectoryPath) {
        mImageCacheParams = new ImageCache.ImageCacheParams(mDiskEnvironment, diskCacheDirectoryPath);
        mImageCache = ImageCache.getInstance(objectHolderFactory, mMemoryImageCacheFactory, mBitmapFactory, mImageCacheParams);
        new CacheAsyncTask(mThreadOperation).execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, android.widget.ImageView)}
     * @return The processed bitmap
     */
    protected abstract AbstractBitmap processBitmap(Object data);

    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(AbstractImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, AbstractImageView imageView) {
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(AbstractImageView imageView) {
        if (imageView != null) {
            return imageView.getBitmapWorkerTask();
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    class BitmapWorkerTask extends AsyncTask<Void, Void, AbstractBitmapDrawable> {
        private Object mData;
        private final AbstractImageView mImageView;
        private final OnImageLoadedListener mOnImageLoadedListener;

        public BitmapWorkerTask(ThreadOperation accessor, Object data, AbstractImageView imageView) {
            super(accessor);
            mData = data;
            mImageView = imageView;
            mOnImageLoadedListener = null;
        }

        public BitmapWorkerTask(ThreadOperation accessor, Object data, AbstractImageView imageView, OnImageLoadedListener listener) {
            super(accessor);
            mData = data;
            mImageView = imageView;
            mOnImageLoadedListener = listener;
        }

        /**
         * Background processing.
         */
        @Override
        protected AbstractBitmapDrawable doInBackground(Void... params) {
            //BEGIN_INCLUDE(load_bitmap_in_background)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            final String dataString = String.valueOf(mData);
            AbstractBitmap bitmap = null;
            AbstractBitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(mData);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                drawable = mBitmapDrawableFactory.createAbstractBitmapDrawable(bitmap);
                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(dataString, drawable);
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return drawable;
            //END_INCLUDE(load_bitmap_in_background)
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(AbstractBitmapDrawable value) {
            //BEGIN_INCLUDE(complete_background_work)
            boolean success = false;
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final AbstractImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                success = true;
                setImageDrawable(imageView, value);
            }
            if (mOnImageLoadedListener != null) {
                mOnImageLoadedListener.onImageLoaded(success);
            }
            //END_INCLUDE(complete_background_work)
        }

        @Override
        protected void onCancelled(AbstractBitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private AbstractImageView getAttachedImageView() {
            if (!mImageView.hasOwner()) {
                return null;
            }

            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(mImageView);

            if (this == bitmapWorkerTask) {
                return mImageView;
            }

            return null;
        }
    }

    /**
     * Interface definition for callback on image loaded successfully.
     */
    public interface OnImageLoadedListener {

        /**
         * Called once the image has been loaded.
         * @param success True if the image was loaded successfully, false if
         *                there was an error.
         */
        void onImageLoaded(boolean success);
    }


    /**
     * Called when the processing is complete and the final drawable should be 
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(AbstractImageView imageView, AbstractBitmapDrawable drawable) {
        if (mFadeInBitmap) {
            imageView.setFadeInImageDrawable(mLoadingBitmap, drawable, FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        CacheAsyncTask(ThreadOperation accessor) {
            super(accessor);
        }

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask(mThreadOperation).execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask(mThreadOperation).execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask(mThreadOperation).execute(MESSAGE_CLOSE);
    }

}
