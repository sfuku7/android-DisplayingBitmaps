package com.example.android.displayingbitmaps.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.OutputStream;

public class AndroidBitmap implements AbstractBitmap {

    private static final String TAG = "AndroidBitmap";

    final Bitmap bitmap;

    AndroidBitmap(Bitmap b) {
        bitmap = b;
    }

    @Override
    public boolean compress(CompressFormat format, int quality, OutputStream stream) {
        return bitmap.compress(convCompressFormat(format), quality, stream);
    }

    private static Bitmap.CompressFormat convCompressFormat(CompressFormat format) {
        switch (format) {
            default:
                Log.w(TAG, "unknown format !!");
                /* FALLTHROUGH */
            case JPEG:
                return Bitmap.CompressFormat.JPEG;
            case PNG:
                return Bitmap.CompressFormat.PNG;
        }
    }
}
