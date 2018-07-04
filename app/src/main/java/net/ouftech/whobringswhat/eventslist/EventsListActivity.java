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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.model.Event;
import net.ouftech.whobringswhat.model.FirestoreManager;
import net.ouftech.whobringswhat.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class EventsListActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);

        init();

        fab.setOnClickListener(view -> {
            FirestoreManager.test();
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null)
            // If user is logged in (can be anonymously)
            onLoggedIn(firebaseUser);
        else
            // If no user is logged in
            displayLoginDialog();

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        eventsListRv.setLayoutManager(new LinearLayoutManager(this));
        eventsListRv.setAdapter(sectionedAdapter);
    }

    private void init() {
        Fabric.with(this);
        FirestoreManager.init();
    }

    /**
     * Create the FirebaseUI Auth Activity
     */
    private void launchSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

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
                    Logger.e(getLogTag(), new NullPointerException("Firebase user is null but result is OK"));
                    displayLoginError();
                } else {
                    Logger.d(getLogTag(), String.format("Logged in user %s-%s", firebaseUser.getUid(), firebaseUser.getEmail()));
                    onLoggedIn(firebaseUser);
                }
            } else {
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
        updateLoginMenu();
        Crashlytics.setUserIdentifier(firebaseUser.getUid());

        FirestoreManager.initWithFirebaseUser(firebaseUser, new FirestoreManager.UserQueryListener() {
            @Override
            public void onSuccess(@NonNull User user) {
                Logger.d(getLogTag(), "Firestore login finished");
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), String.format("Error while fetching or creating user %s", firebaseUser.getUid()), e);
            }
        });


        FirestoreManager.fetchEventsForUser(firebaseUser.getUid(), new FirestoreManager.EventsQueryListener() {
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
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), String.format("Error while fetching events for user %s", firebaseUser.getUid()), e);
            }
        });
    }

    private void displayEvents(@NonNull List<Event> pastEvents, @NonNull List<Event> upcomingEvents) {
        sectionedAdapter.removeAllSections();
        if (!upcomingEvents.isEmpty())
            sectionedAdapter.addSection(new EventsSection(upcomingEvents, "Upcoming"));
        if (!pastEvents.isEmpty())
            sectionedAdapter.addSection(new EventsSection(pastEvents, "Past"));

        runOnUiThread(() -> {

            if (upcomingEvents.isEmpty() && pastEvents.isEmpty() && emptyMessageTextView != null && isRunning()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                emptyMessageTextView.setText((user == null || user.isAnonymous()) ? R.string.events_list_empty_message_not_logged_in : R.string.events_list_empty_message);
                emptyMessageTextView.setVisibility(View.VISIBLE);
            } else {
                emptyMessageTextView.setVisibility(View.GONE);
            }
        });

        sectionedAdapter.notifyDataSetChanged();
        setProgressBarVisible(false);
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
