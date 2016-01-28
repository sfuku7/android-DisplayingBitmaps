package com.example.android.displayingbitmaps.util;

/**
 * Created by sfuku on 2016/01/29.
 */
public class AndroidMemoryImageCacheFactory implements MemoryImageCacheFactory {
    @Override
    public MemoryImageCache createMemoryImageCache(int maxSize) {
        return new AndroidMemoryImageCache(maxSize);
    }
}
