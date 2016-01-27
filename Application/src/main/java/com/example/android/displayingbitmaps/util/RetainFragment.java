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

import android.os.Bundle;
import android.support.v4.app.Fragment;


/**
 * A simple non-UI Fragment that stores a single Object and is retained over configuration
 * changes. It will be used to retain the ImageCache object.
 */
public class RetainFragment extends Fragment implements ImageCache.ObjectHolder {
    private Object mObject;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public RetainFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure this Fragment is retained over a configuration change
        setRetainInstance(true);
    }

    /**
     * Store a single object in this Fragment.
     *
     * @param object The object to store
     */
    @Override
    public void setObject(Object object) {
        mObject = object;
    }

    /**
     * Get the stored object.
     *
     * @return The stored object
     */
    @Override
    public Object getObject() {
        return mObject;
    }
}
