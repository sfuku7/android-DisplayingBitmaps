package com.example.android.displayingbitmaps.util;

import java.io.FileDescriptor;

/**
 * Created by sfuku on 2016/01/29.
 */
public interface AbstractBitmapFactory {

    AbstractBitmap decodeResource(String resId);

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    AbstractBitmap decodeSampledBitmapFromResource(String resId,
                                                   int reqWidth, int reqHeight,
                                                   ImageCache cache);

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    AbstractBitmap decodeSampledBitmapFromFile(String filename,
                                               int reqWidth, int reqHeight,
                                               ImageCache cache);


    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    AbstractBitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor,
                                                     int reqWidth, int reqHeight,
                                                     ImageCache cache);
}
