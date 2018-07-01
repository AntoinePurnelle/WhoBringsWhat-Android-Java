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

package net.ouftech.whobringswhat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.model.FirestoreManager;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;

public class EventsListActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 9108;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.fab)
    protected FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);

        init();

        fab.setOnClickListener(view -> {
            FirestoreManager.test();
        });
    }

    private void init() {
        Fabric.with(this);
        FirestoreManager.init();
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
     * Update the login menu, sets the user to Crashlytics and fetches or create the Firestore {@link net.ouftech.whobringswhat.model.User} object
     * @param firebaseUser Logged in user
     */
    private void onLoggedIn(@NonNull FirebaseUser firebaseUser) {
        updateLoginMenu();
        FirestoreManager.initWithFirebaseUser(firebaseUser);
        Crashlytics.setUserIdentifier(firebaseUser.getUid());
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
