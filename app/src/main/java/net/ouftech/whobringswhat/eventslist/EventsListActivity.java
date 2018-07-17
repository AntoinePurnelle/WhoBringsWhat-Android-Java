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

package net.ouftech.whobringswhat.eventslist;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.state.State;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import net.ouftech.whobringswhat.EventEditActivity;
import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.WhoBringsWhatWidget;
import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.CollectionUtils;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.eventcontent.EventContentActivity;
import net.ouftech.whobringswhat.model.Event;
import net.ouftech.whobringswhat.model.FirestoreManager;
import net.ouftech.whobringswhat.model.RealTimeDBManager;
import net.ouftech.whobringswhat.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class EventsListActivity extends BaseActivity {

    public static final String SHARED_PREFS_NAME = "WBW_SHARED_PREFS";
    public static final String UPCOMING_EVENTS_SHARED_PREF = "upcomingEvents";

    private static final int RC_SIGN_IN = 9108;

    @BindView(R.id.events_list_toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.fab)
    protected FloatingActionButton fab;
    @BindView(R.id.events_list_rv)
    protected RecyclerView eventsListRv;
    @BindView(R.id.pb_loading_indicator)
    protected ProgressBar progressBar;
    @BindView(R.id.empty_message_textView)
    protected TextView emptyMessageTextView;

    private SectionedRecyclerViewAdapter sectionedAdapter;

    @State
    private String pendingEvent;
    private boolean transformAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);

        init();

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        eventsListRv.setLayoutManager(new LinearLayoutManager(this));
        eventsListRv.setAdapter(sectionedAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null)
            // If user is logged in (can be anonymously)
            onLoggedIn(firebaseUser);
        else
            // If no user is logged in
            displayLoginDialog();
    }

    private void init() {
        Fabric.with(this);
        FirestoreManager.init();
        RealTimeDBManager.init();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        logd("opening with link " + deepLink);

                        String[] parts = deepLink.getPath().split("/");
                        String path, id;
                        if (TextUtils.isEmpty(parts[0])) {
                            path = parts[1];
                            id = parts[2];
                        } else {
                            path = parts[0];
                            id = parts[1];
                        }

                        if ("events".equals(path)) {
                            if (FirebaseAuth.getInstance().getCurrentUser() == null)  // Not logged in or anonymous. Wait for login
                                pendingEvent = id;
                            else
                                openPendingEvent(id);
                        }
                    }
                })
                .addOnFailureListener(this, e -> {
                    logw("getDynamicLink:onFailure", e);
                    showWarning(R.string.an_error_occurred);
                });
    }

    private void openPendingEvent(String id) {
        setProgressBarVisible(true);

        RealTimeDBManager.fetchEventById(id, new FirestoreManager.EventQueryListener() {
            @Override
            public void onSuccess(@NonNull Event event) {
                setProgressBarVisible(false);
                RealTimeDBManager.addCurrentUserToEvent(event, new FirestoreManager.SimpleQueryListener() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        logd("User added to event");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logw("Could not add user to event", e);
                    }
                });
                openEvent(event);
            }

            @Override
            public void onFailure(Exception e) {
                showWarning(R.string.an_error_occurred);
                setProgressBarVisible(false);
            }
        });

        /*FirestoreManager.fetchEventById(id, new FirestoreManager.EventQueryListener() {
            @Override
            public void onSuccess(@NonNull Event event) {
                setProgressBarVisible(false);
                event.addUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
                FirestoreManager.updateEvent(event, new FirestoreManager.SimpleQueryListener() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        logd("User added to event");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logw("Could not add user to event", e);
                    }
                });
                openEvent(event);
            }

            @Override
            public void onFailure(Exception e) {
                showWarning(R.string.an_error_occurred);
                setProgressBarVisible(false);
            }
        });*/
    }

    /**
     * Create the FirebaseUI Auth Activity
     */
    private void launchSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isAnonymous())
            transformAnonymous = true;

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .enableAnonymousUsersAutoUpgrade()
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * Signs the user out
     */
    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Logger.d(getLogTag(), "Sign out successful");
                    else
                        Logger.w(getLogTag(), "Sign out failed", task.getException());

                    updateLoginMenu();
                });

        sectionedAdapter.removeAllSections();
        sectionedAdapter.notifyDataSetChanged();
        emptyMessageTextView.setText(R.string.events_list_empty_message_not_logged_in);
        emptyMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { // FirebaseUI Auth Result
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) { // User has logged in
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    transformAnonymous = false;
                    Logger.e(getLogTag(), new NullPointerException("Firebase user is null but result is OK"));
                    displayLoginError();
                } else {
                    Logger.d(getLogTag(), String.format("Logged in user %s-%s", firebaseUser.getUid(), firebaseUser.getEmail()));
                    onLoggedIn(firebaseUser);
                }
            } else {
                transformAnonymous = false;
                if (response != null && response.getError() != null) {
                    Logger.e(getLogTag(), "Error during login with Firebase", response.getError());
                    displayLoginError();
                } else {
                    Logger.d(getLogTag(), "User cancelled login");
                }
            }
        }
    }

    /**
     * Displays the dialog offering to log in or continue anonymously
     */
    private void displayLoginDialog() {
        if (isRunning())
            new AlertDialog.Builder(this)
                    .setTitle(R.string.login_dialog_title)
                    .setMessage(R.string.login_dialog_message)
                    .setPositiveButton(R.string.login, (dialog, which) -> {
                        dialog.dismiss();
                        launchSignIn();
                    })
                    .setNegativeButton(R.string.not_now, (dialog, which) -> {
                        dialog.dismiss();
                        anonymousLogin();
                    })
                    .show();
    }

    /**
     * Displays an error dialog when login failed
     */
    private void displayLoginError() {
        if (isRunning())
            new AlertDialog.Builder(this)
                    .setTitle(R.string.login_error_dialog_title)
                    .setMessage(R.string.login_error_dialog_message)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();
    }

    /**
     * Log in anonymously to Firebase
     */
    private void anonymousLogin() {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser == null) {
                            Logger.e(getLogTag(), new NullPointerException("Firebase user is null but task is successful"));
                        } else {
                            Logger.d(getLogTag(), String.format("Logged in anonymously with user %s", firebaseUser.getUid()));
                            onLoggedIn(firebaseUser);
                        }
                    } else {
                        Logger.e(getLogTag(), "Error during anonymous login with Firebase", task.getException());
                    }
                });
    }

    /**
     * Update the login menu, sets the user to Crashlytics and fetches or create the Firestore {@link User} object
     *
     * @param firebaseUser Logged in user
     */
    private void onLoggedIn(@NonNull FirebaseUser firebaseUser) {
        setProgressBarVisible(true);

        if (pendingEvent != null) {
            // app was opened by a dynamic link --> open the pending event
            openPendingEvent(pendingEvent);
            pendingEvent = null; // reset to prevent re-open when coming back to activity
        }

        updateLoginMenu();
        Crashlytics.setUserIdentifier(firebaseUser.getUid());


        RealTimeDBManager.initWithFirebaseUser(firebaseUser, new FirestoreManager.UserQueryListener() {
            @Override
            public void onSuccess(@NonNull User user) {
                Logger.d(getLogTag(), "RealtimeDB login finished");

                RealTimeDBManager.fetchEventsForUser(user, new FirestoreManager.EventsQueryListener() {
                    @Override
                    public void onSuccess(@NonNull List<Event> events) {
                        Logger.d(getLogTag(), String.format("Fetched %s events", events.size()));
                        long now = new Date().getTime();
                        List<Event> pastEvents = new ArrayList<>();
                        List<Event> upcomingEvents = new ArrayList<>();
                        for (Event event : events) {
                            if (event.getTime() < now)
                                pastEvents.add(event);
                            else
                                upcomingEvents.add(event);
                        }

                        displayEvents(pastEvents, upcomingEvents);
                        saveUpcomingEventsList(upcomingEvents);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.e(getLogTag(), String.format("Error while fetching events for user %s", firebaseUser.getUid()), e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), String.format("Error while fetching or creating user %s", firebaseUser.getUid()), e);
            }
        }, transformAnonymous);

        FirestoreManager.initWithFirebaseUser(firebaseUser, new FirestoreManager.UserQueryListener() {
            @Override
            public void onSuccess(@NonNull User user) {
                Logger.d(getLogTag(), "Firestore login finished");
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), String.format("Error while fetching or creating user %s", firebaseUser.getUid()), e);
            }
        }, transformAnonymous);
        transformAnonymous = false;



        /*FirestoreManager.fetchEventsForUser(firebaseUser.getUid(), new FirestoreManager.EventsQueryListener() {
            @Override
            public void onSuccess(@NonNull List<Event> events) {
                Logger.d(getLogTag(), String.format("Fetched %s events", events.size()));
                long now = new Date().getTime();
                List<Event> pastEvents = new ArrayList<>();
                List<Event> upcomingEvents = new ArrayList<>();
                for (Event event : events) {
                    if (event.getTime() < now)
                        pastEvents.add(event);
                    else
                        upcomingEvents.add(event);
                }

                displayEvents(pastEvents, upcomingEvents);
                saveUpcomingEventsList(upcomingEvents);
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), String.format("Error while fetching events for user %s", firebaseUser.getUid()), e);
            }
        });*/
    }

    private void saveUpcomingEventsList(List<Event> upcomingEvents) {
        if (isRunning()) {
            String events;

            if (CollectionUtils.isEmpty(upcomingEvents)) {
                events = null;
            } else {

                StringBuilder upComingEventsString = new StringBuilder();

                upComingEventsString.append(getString(R.string.upcoming)).append("\n\n");

                for (Event upcomingEvent : upcomingEvents) {
                    upComingEventsString
                            .append("• ")
                            .append(upcomingEvent.getName());
                    if (upcomingEvent.getTime() > 0)
                        upComingEventsString
                                .append(" - ")
                                .append(DateUtils.formatDateTime(this, upcomingEvent.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME))
                                .append("\n");
                }

                events = upComingEventsString.toString();
            }

            SharedPreferences.Editor editor = getApplication().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(UPCOMING_EVENTS_SHARED_PREF, events);
            editor.apply();

            Intent intent = new Intent(EventsListActivity.this, WhoBringsWhatWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
            int[] ids = widgetManager.getAppWidgetIds(new ComponentName(getApplication(), WhoBringsWhatWidget.class));

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
        }
    }

    private void displayEvents(@NonNull List<Event> pastEvents, @NonNull List<Event> upcomingEvents) {
        sectionedAdapter.removeAllSections();
        if (!upcomingEvents.isEmpty())
            sectionedAdapter.addSection(new EventsSection(upcomingEvents, getString(R.string.upcoming), position -> openEvent(upcomingEvents.get(position))));
        if (!pastEvents.isEmpty())
            sectionedAdapter.addSection(new EventsSection(pastEvents, getString(R.string.past), position -> openEvent(pastEvents.get(position))));

        runOnUiThread(() -> {

            if (emptyMessageTextView != null && isRunning()) {
                if (upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    emptyMessageTextView.setText((user == null || user.isAnonymous()) ? R.string.events_list_empty_message_not_logged_in : R.string.events_list_empty_message);
                    emptyMessageTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyMessageTextView.setVisibility(View.GONE);
                }
            }
        });

        sectionedAdapter.notifyDataSetChanged();
        setProgressBarVisible(false);
    }

    private void openEvent(@NonNull Event event) {
        Intent intent = new Intent(EventsListActivity.this, EventContentActivity.class);
        intent.putExtra(EventContentActivity.EVENT_EXTRA, event);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events_list, menu);
        updateLoginMenu();
        return true;
    }

    private void updateLoginMenu() {
        if (toolbar == null)
            return;

        Menu menu = toolbar.getMenu();

        if (menu == null || menu.findItem(R.id.action_log_in) == null)
            return;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_log_in).setVisible(true);
        } else {
            menu.findItem(R.id.action_log_out).setVisible(true);
            menu.findItem(R.id.action_log_in).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_log_in) {
            launchSignIn();
            return true;
        } else if (id == R.id.action_log_out) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setProgressBarVisible(final boolean visible) {
        if (isRunning() && progressBar != null)
            runOnUiThread(() -> progressBar.setVisibility(visible ? View.VISIBLE : View.GONE));
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        Intent intent = new Intent(this, EventEditActivity.class);
        startActivity(intent);
    }

    private void showWarning(@StringRes int message) {
        Snackbar.make(eventsListRv, message, Snackbar.LENGTH_LONG).show();
    }

    public String getPendingEvent() {
        return pendingEvent;
    }

    public void setPendingEvent(String pendingEvent) {
        this.pendingEvent = pendingEvent;
    }

    @NonNull
    @Override
    protected String getLogTag() {
        return "EventsListActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_events_list;
    }
}
