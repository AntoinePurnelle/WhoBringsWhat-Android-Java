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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    protected void onResume() {
        super.onResume();


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // TODO open login popup
        } else {
            FirestoreManager.initWithFirebaseUser(FirebaseAuth.getInstance().getCurrentUser());
        }
    }

    private void init() {
        Fabric.with(this);
        FirestoreManager.init();
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                updateLoginMenu();
                FirestoreManager.initWithFirebaseUser(firebaseUser);
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
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

        if (menu == null)
            return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_log_in).setVisible(true);
        } else {
            menu.findItem(R.id.action_log_out).setVisible(true);
            menu.findItem(R.id.action_log_in).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
