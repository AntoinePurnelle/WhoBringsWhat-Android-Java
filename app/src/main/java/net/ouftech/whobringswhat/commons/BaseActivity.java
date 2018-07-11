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

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.evernote.android.state.StateSaver;

import net.ouftech.whobringswhat.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by antoine.purnelle@ouftech.net on 25-02-18.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private boolean running = false;
    private Unbinder unbinder;

    // region Crashlytics

    @NonNull
    protected abstract String getLogTag();

    /**
     * Logs a message to the logcat in the {@link Log#DEBUG} channel
     *
     * @param msg Message to log
     */
    protected void logd(@NonNull String msg) {
        Logger.d(getLogTag(), msg);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and and reports the exception to and Crashlytics
     *
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public void logw(@NonNull String msg, @NonNull Throwable tr) {
        Logger.w(getLogTag(), msg, tr, true);
    }

    /**
     * Logs a warning to the logcat in the {@link Log#WARN} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param msg    Message to log
     * @param tr     Exception to be reported
     * @param report if true, the exception will be reported to Crashlytics
     */
    public void logw(@NonNull String msg, @NonNull Throwable tr, boolean report) {
        Logger.w(getLogTag(), msg, tr, report);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param tr  Exception to be reported
     */
    public void loge(@NonNull Throwable tr) {
        Logger.e(getLogTag(), tr);
    }

    /**
     * Logs an error to the logcat in the {@link Log#ERROR} channel, Crashlytics and reports the exception to Crashlytics
     *
     * @param msg Message to log
     * @param tr  Exception to be reported
     */
    public void loge(@NonNull String msg, @NonNull Throwable tr) {
        Logger.e(getLogTag(), msg, tr);
    }

    // endregion Crashlytics


    // region Lifecycle

    @LayoutRes
    protected abstract int getLayoutId();

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        logd(String.format("onCreate %s", this));
        super.onCreate(savedInstanceState);

        StateSaver.restoreInstanceState(this, savedInstanceState);

        setRunning(true);
        setContentView(getLayoutId());
        unbinder = ButterKnife.bind(this);
    }

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        logd(String.format("onCreate (PersistableBundle) %s", this));
        super.onCreate(savedInstanceState, persistentState);

        setRunning(true);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @CallSuper
    @Override
    protected void onStart() {
        logd(String.format("onStart %s", this));
        super.onStart();

        setRunning(true);
    }

    @CallSuper
    @Override
    protected void onResume() {
        logd(String.format("onResume %s", this));
        super.onResume();

        setRunning(true);
    }

    @CallSuper
    @Override
    protected void onPause() {
        logd(String.format("onPause %s", this));
        super.onPause();

        setRunning(false);
    }

    @CallSuper
    @Override
    protected void onStop() {
        logd(String.format("onStop %s", this));
        super.onStop();

        setRunning(false);
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        logd(String.format("onDestroy %s", this));
        super.onDestroy();

        setRunning(false);
        unbinder.unbind();
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        logd(String.format("onSaveInstanceState %s", this));
        StateSaver.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        logd(String.format("onSaveInstanceState (PersistableBundle) %s", this));
        StateSaver.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    // endregion Lifecycle
}
