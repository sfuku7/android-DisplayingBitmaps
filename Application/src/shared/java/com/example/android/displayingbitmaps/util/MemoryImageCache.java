package com.example.android.displayingbitmaps.util;

import android.support.v4.util.LruCache;

public class MemoryImageCache {

    protected LruCache<String, AbstractBitmapDrawable> mMemoryCache;

    MemoryImageCache(LruCache<String, AbstractBitmapDrawable> cache) {
        mMemoryCache = cache;
    }

    MemoryImageCache(int maxSize) {
        mMemoryCache = new LruCache<String, AbstractBitmapDrawable>(maxSize);
    }

    synchronized AbstractBitmapDrawable put(String key, AbstractBitmapDrawable value) {
        return mMemoryCache.put(key, value);
    }

    synchronized AbstractBitmapDrawable get(String key) {
        return mMemoryCache.get(key);
    }

    synchronized void evictAll() {
        mMemoryCache.evictAll();
    }

    synchronized AbstractBitmap getBitmapFromReusableSet(Object optionsPlatformDepend) {
        return null;
    }
}