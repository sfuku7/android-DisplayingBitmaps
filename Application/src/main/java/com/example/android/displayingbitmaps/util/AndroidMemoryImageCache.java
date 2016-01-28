package com.example.android.displayingbitmaps.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class AndroidMemoryImageCache extends MemoryImageCache {

    private static final String TAG = "AndroidMemoryImageCache";

    public AndroidMemoryImageCache(int maxSize) {
        super(new MemoryCacheImpl(maxSize));
    }

    @Override
    synchronized AbstractBitmapDrawable put(String key, AbstractBitmapDrawable value) {

        if (!(value instanceof AndroidBitmapDrawable)) {
            Log.w(TAG, "invalid type");
            return value;
        }

        BitmapDrawable concreteValue = ((AndroidBitmapDrawable)value).bitmapDrawable;

        if (RecyclingBitmapDrawable.class.isInstance(concreteValue)) {
            // The removed entry is a recycling drawable, so notify it
            // that it has been added into the memory cache
            ((RecyclingBitmapDrawable) concreteValue).setIsCached(true);
        }

        return super.put(key, value);
    }

    @Override
    protected AbstractBitmap getBitmapFromReusableSet(Object optionsPlatformDepend) {
        if (!(optionsPlatformDepend instanceof BitmapFactory.Options)) {
            return null;
        }

        BitmapFactory.Options options = (BitmapFactory.Options)optionsPlatformDepend;
        Bitmap androidBitmap =  ((MemoryCacheImpl)mMemoryCache).getBitmapFromReusableSet(options);
        return androidBitmap != null ? new AndroidBitmap(androidBitmap) : null;
    }

    private static class MemoryCacheImpl extends LruCache<String, AbstractBitmapDrawable> {

        private Set<SoftReference<Bitmap>> mReusableBitmaps;

        MemoryCacheImpl(int maxSize) {
            super(maxSize);

            // If we're running on Honeycomb or newer, create a set of reusable bitmaps that can be
            // populated into the inBitmap field of BitmapFactory.Options. Note that the set is
            // of SoftReferences which will actually not be very effective due to the garbage
            // collector being aggressive clearing Soft/WeakReferences. A better approach
            // would be to use a strongly references bitmaps, however this would require some
            // balancing of memory usage between this set and the bitmap LruCache. It would also
            // require knowledge of the expected size of the bitmaps. From Honeycomb to JellyBean
            // the size would need to be precise, from KitKat onward the size would just need to
            // be the upper bound (due to changes in how inBitmap can re-use bitmaps).
            if (Utils.hasHoneycomb()) {
                mReusableBitmaps =
                        Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
            }
        }

        /**
         * Notify the removed entry that is no longer being cached
         */
        @Override
        protected void entryRemoved(boolean evicted, String key,
                AbstractBitmapDrawable oldValue, AbstractBitmapDrawable newValue) {
            if (!(oldValue instanceof AndroidBitmapDrawable)
                    || !(newValue instanceof AndroidBitmapDrawable)) {
                Log.w(TAG, "invalid type, oldValue or newValue");
                return;
            }
            BitmapDrawable concreteOldValue = ((AndroidBitmapDrawable) oldValue).bitmapDrawable;
            if (RecyclingBitmapDrawable.class.isInstance(concreteOldValue)) {
                // The removed entry is a recycling drawable, so notify it
                // that it has been removed from the memory cache
                ((RecyclingBitmapDrawable) concreteOldValue).setIsCached(false);
            } else {
                // The removed entry is a standard BitmapDrawable

                if (Utils.hasHoneycomb()) {
                    // We're running on Honeycomb or later, so add the bitmap
                    // to a SoftReference set for possible use with inBitmap later
                    mReusableBitmaps.add(new SoftReference<Bitmap>(concreteOldValue.getBitmap()));
                }
            }
        }

        /**
         * Measure item size in kilobytes rather than units which is more practical
         * for a bitmap cache
         */
        @Override
        protected int sizeOf(String key, AbstractBitmapDrawable value) {
            if (!(value instanceof AndroidBitmapDrawable)) {
                Log.w(TAG, "invalid type, value");
                return 0;
            }
            final int bitmapSize = getBitmapSize(((AndroidBitmapDrawable)value).bitmapDrawable) / 1024;
            return bitmapSize == 0 ? 1 : bitmapSize;
        }

        protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {

            Bitmap bitmap = null;

            if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
                synchronized (mReusableBitmaps) {
                    final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                    Bitmap item;

                    while (iterator.hasNext()) {
                        item = iterator.next().get();

                        if (null != item && item.isMutable()) {
                            // Check to see it the item can be used for inBitmap
                            if (canUseForInBitmap(item, options)) {
                                bitmap = item;

                                // Remove from reusable set so it can't be used again
                                iterator.remove();
                                break;
                            }
                        } else {
                            // Remove from the set if the reference has been cleared.
                            iterator.remove();
                        }
                    }
                }
            }

            return bitmap;
        }
    }


    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param value
     * @return size in bytes
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();

        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if (Utils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }

        if (Utils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * @param candidate - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     *      <code>targetOptions</code>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        //BEGIN_INCLUDE(can_use_for_inbitmap)
        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
        //END_INCLUDE(can_use_for_inbitmap)
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }
}
