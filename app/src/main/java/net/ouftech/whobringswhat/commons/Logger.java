/*
 * Copyright 2018 Antoine PURNELLE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ouftech.whobringswhat.commons;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

/**
 * Created by antoine.purnelle@ouftech.net on 25-02-18.
 */

public class Logger {

    /**
     * Logs a message to the logcat in the {@link Log#DEBUG} channel
     *
     * @param tag Tag for the log
     * @param msg Message to log
     */
    public static void d(@NonNull String tag, @NonNull String msg) {
        logToCrashlytics(Log.DEBUG, tag, msg);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and and reports the exception to and Crashlytics
     *
     * @param tag Tag for the log
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public static void w(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        w(tag, msg, tr, true);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag    Tag for the log
     * @param msg    Message to log
     * @param tr     Exception to be reported
     * @param report if true, the exception will be reported to Crashlytics
     */
    public static void w(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr, boolean report) {
        msg = String.format("*** %s ***: %s - [%s]", report ? "WARNING" : "WARNING (NO-REPORT)", msg, tr);
        logToCrashlytics(Log.WARN, tag, msg);
        if (report)
            reportToCrashlytics(tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag Tag for the log
     * @param tr  Exception to be reported
     */
    public static void e(@NonNull String tag, @NonNull Throwable tr) {
        e(tag, tr.getMessage(), tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tag Tag for the log
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public static void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        msg = String.format("*** ERROR ***: %s - [%s]", msg, tr);
        logToCrashlytics(Log.ERROR, tag, msg);
        reportToCrashlytics(tr);
    }

    /**
     * Logs a message to the logcat, Crashlytics in the right channel
     *
     * @param priority Priority (channel) in which to log
     * @param tag      Tag for the logcat
     * @param msg      Message to log
     */
    private static void logToCrashlytics(int priority, @NonNull String tag, @NonNull String msg) {
        Crashlytics.log(priority, tag, msg);
    }

    /**
     * Reports an exception to Crashlytics
     *
     * @param tr Exception to reportToCrashlytics
     */
    private static void reportToCrashlytics(@NonNull Throwable tr) {
        Crashlytics.logException(tr);
    }
}
