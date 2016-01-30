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

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.example.android.common.logger.Log;

public class AndroidThreadOperation implements AsyncTask.ThreadOperation {

    private static final String TAG = "AndroidThreadOperation";
    @Override
    public void setThreadPriority(Priority priority) {
        Process.setThreadPriority(convPriority(priority));
    }

    @Override
    public void publishProgress(final AsyncTask task, final Object... values) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                task.onProgressUpdate(values);
            }
        });
    }

    @Override
    public void postResult(final AsyncTask task, final Object result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                task.finish(result);
            }
        });
    }

    private static void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private static int convPriority(Priority priority) {
        switch (priority) {
            default:
                Log.w(TAG, "invalid priority " + priority);
                /* FALLTHROUGH */
            case THREAD_PRIORITY_BACKGROUND:
                return Process.THREAD_PRIORITY_BACKGROUND;
        }
    }
}