package com.example.android.displayingbitmaps.util;

import java.io.OutputStream;

public interface AbstractBitmap {
    enum CompressFormat {
        JPEG,
        PNG
    }

    boolean compress(CompressFormat format, int quality, OutputStream stream);
}
