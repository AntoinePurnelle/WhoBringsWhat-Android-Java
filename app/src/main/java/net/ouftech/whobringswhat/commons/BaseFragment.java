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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evernote.android.state.StateSaver;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by antoine.purnelle@ouftech.net on 25-02-18.
 */

public abstract class BaseFragment extends Fragment {

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
     * @param tr Exception to be reported
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

    // region Fragment Lifecycle

    @LayoutRes
    protected abstract int getLayoutId();

    @CallSuper
    protected void initView(View view) {
        logd(String.format("initView %s (from %s)", this, getBaseActivity()));
    }

    @CallSuper
    @Override
    public void onAttach(Context context) {
        logd(String.format("onAttach %s (from %s)", this, getBaseActivity()));
        super.onAttach(context);

        setRunning(true);
    }

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        logd(String.format("onCreate %s (from %s)", this, getBaseActivity()));
        super.onCreate(savedInstanceState);
        StateSaver.restoreInstanceState(this, savedInstanceState);

        setRunning(true);
    }

    @CallSuper
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logd(String.format("onCreateView %s (from %s)", this, getBaseActivity()));
        setRunning(true);
        try {
            View view = inflater.inflate(getLayoutId(), container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        } catch (InflateException e) {
            loge(String.format("Error while inflating layout %s with ViewGroup container %s", getLayoutId(), container), e);
            return null;
        }
    }

    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        logd(String.format("onViewCreated %s (from %s)", this, getBaseActivity()));
        setRunning(true);
        super.onViewCreated(view, savedInstanceState);
    }

    @CallSuper
    @Override
    public void onStart() {
        logd(String.format("onStart %s (from %s)", this, getBaseActivity()));
        super.onStart();

        setRunning(true);
    }

    @CallSuper
    @Override
    public void onResume() {
        logd(String.format("onResume %s (from %s)", this, getBaseActivity()));
        super.onResume();

        setRunning(true);
    }

    @CallSuper
    @Override
    public void onPause() {
        logd(String.format("onPause %s (from %s)", this, getBaseActivity()));
        super.onPause();

        setRunning(false);
    }

    @CallSuper
    @Override
    public void onStop() {
        logd(String.format("onStop %s (from %s)", this, getBaseActivity()));
        super.onStop();

        setRunning(false);
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        logd(String.format("onDestroyView %s (from %s)", this, getBaseActivity()));
        super.onDestroyView();

        setRunning(false);
        unbinder.unbind();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        logd(String.format("onDestroy %s (from %s)", this, getBaseActivity()));
        super.onDestroy();

        setRunning(false);
    }

    @CallSuper
    @Override
    public void onDetach() {
        logd(String.format("onDetach %s (from %s)", this, getBaseActivity()));
        super.onDetach();

        setRunning(false);
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        logd(String.format("onSaveInstanceState %s (from %s)", this, getBaseActivity()));
        StateSaver.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Tries and casts the result of {@link Fragment#getActivity()} to {@link BaseActivity}
     *
     * @return The {@link BaseActivity} object if possible. Null if could not cast
     */
    @Nullable
    public BaseActivity getBaseActivity() {
        if (getActivity() instanceof BaseActivity)
            return (BaseActivity) getActivity();
        else
            return null;
    }

    public boolean isRunning() {
        return running && isActivityRunning();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isActivityRunning() {
        return getBaseActivity() != null && getBaseActivity().isRunning();
    }

    // endregion Fragment Lifecycle


}
