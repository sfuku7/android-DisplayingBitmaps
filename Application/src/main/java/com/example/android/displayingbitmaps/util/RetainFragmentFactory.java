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

import android.support.v4.app.FragmentManager;

/**
 * Created by sfuku on 2016/01/27.
 */
public class RetainFragmentFactory implements ImageCache.ObjectHolderFactory {

    private static final String TAG = "RetainFragmentFactory";

    FragmentManager mFragmentManager;
    public RetainFragmentFactory(FragmentManager fm) {
        mFragmentManager = fm;
    }
    @Override
    public ImageCache.ObjectHolder getObjectHolderInstance() {
        return findOrCreateRetainFragment(mFragmentManager);
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        //BEGIN_INCLUDE(find_create_retain_fragment)
        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
        //END_INCLUDE(find_create_retain_fragment)
    }
}
