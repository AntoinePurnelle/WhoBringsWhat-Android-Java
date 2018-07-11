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

package net.ouftech.whobringswhat.eventcontent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.evernote.android.state.State;

import net.ouftech.whobringswhat.ContributionEditActivity;
import net.ouftech.whobringswhat.EventEditActivity;
import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.model.Contribution;
import net.ouftech.whobringswhat.model.Event;
import net.ouftech.whobringswhat.model.FirestoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_APPETIZER;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_DESSERT;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_MAIN;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_STARTER;

public class EventContentActivity extends BaseActivity {

    public static final String EVENT_EXTRA = "EVENT_EXTRA";
    public static final int EVENT_EDIT_REQUEST_CODE = 1000;
    public static final int CONTRIBUTION_EDIT_REQUEST_CODE = 1001;

    @BindView(R.id.events_content_rv)
    RecyclerView rv;
    @BindView(R.id.event_content_fab)
    FloatingActionButton fab;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    private SectionedRecyclerViewAdapter sectionedAdapter;
    @State
    private ArrayList<Contribution> appetizerContributions;
    @State
    private ArrayList<Contribution> starterContributions;
    @State
    private ArrayList<Contribution> mainContributions;
    @State
    private ArrayList<Contribution> dessertContributions;
    Map<String, ArrayList<Contribution>> contributionsLists;

    @State
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(sectionedAdapter);

        if (savedInstanceState == null)
            event = getIntent().getParcelableExtra(EVENT_EXTRA);
        else
            displayEventContent();

        loadContributions();

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(event.getName());

        if (!isTablet())
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 )
                    fab.hide();
                else
                    fab.show();
            }
        });
    }

    private void loadContributions() {
        setProgressBarVisible(true);

        if (contributionsLists == null)
            contributionsLists = new HashMap<>();
        else
            contributionsLists.clear();

        FirestoreManager.fetchContributionsForEvent(event, new FirestoreManager.ContributionsQueryListener() {
            @Override
            public void onSuccess(@NonNull List<Contribution> contributions) {
                Logger.d(getLogTag(), String.format("Fetched %s contributions", contributions.size()));

                appetizerContributions = new ArrayList<>();
                contributionsLists.put(CONTRIBUTION_TYPE_APPETIZER, appetizerContributions);
                starterContributions = new ArrayList<>();
                contributionsLists.put(CONTRIBUTION_TYPE_STARTER, starterContributions);
                mainContributions = new ArrayList<>();
                contributionsLists.put(CONTRIBUTION_TYPE_MAIN, mainContributions);
                dessertContributions = new ArrayList<>();
                contributionsLists.put(CONTRIBUTION_TYPE_DESSERT, dessertContributions);

                for (Contribution contribution : contributions) {
                    String type = contribution.getType();
                    switch (type) {
                        case CONTRIBUTION_TYPE_APPETIZER:
                            appetizerContributions.add(contribution);
                            break;
                        case Contribution.CONTRIBUTION_TYPE_STARTER:
                            starterContributions.add(contribution);
                            break;
                        case Contribution.CONTRIBUTION_TYPE_MAIN:
                            mainContributions.add(contribution);
                            break;
                        case Contribution.CONTRIBUTION_TYPE_DESSERT:
                            dessertContributions.add(contribution);
                            break;
                    }
                }

                displayEventContent();
            }

            @Override
            public void onFailure(Exception e) {
                setProgressBarVisible(false);
                Snackbar.make(rv, R.string.an_error_occurred, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private ContributionsSection.ContributionClickListener contributionClickListener =  new ContributionsSection.ContributionClickListener() {
        @Override
        public void onEditContributionClicked(int position, String type) {
            Intent intent = new Intent(EventContentActivity.this, ContributionEditActivity.class);
            intent.putExtra(ContributionEditActivity.TYPE_EXTRA, type);
            intent.putExtra(ContributionEditActivity.CONTRIBUTION_EXTRA, getContribution(type, position));
            intent.putExtra(ContributionEditActivity.EVENT_EXTRA, event);
            startActivityForResult(intent, CONTRIBUTION_EDIT_REQUEST_CODE);
        }

        @Override
        public void onAddContributionClicked(String type) {
            Intent intent = new Intent(EventContentActivity.this, ContributionEditActivity.class);
            intent.putExtra(ContributionEditActivity.TYPE_EXTRA, type);
            intent.putExtra(ContributionEditActivity.EVENT_EXTRA, event);
            startActivityForResult(intent, CONTRIBUTION_EDIT_REQUEST_CODE);
        }
    };

    private Contribution getContribution(String type, int position) {
        return contributionsLists.get(type).get(position);
    }

    private void displayEventContent() {
        sectionedAdapter.removeAllSections();

        sectionedAdapter.addSection(new EventDetailsSection(event));
        if (event.hasAppetizer())
            sectionedAdapter.addSection(new ContributionsSection(appetizerContributions, CONTRIBUTION_TYPE_APPETIZER, contributionClickListener));

        if (event.hasStarter())
            sectionedAdapter.addSection(new ContributionsSection(starterContributions, CONTRIBUTION_TYPE_STARTER, contributionClickListener));

        if (event.hasMain())
            sectionedAdapter.addSection(new ContributionsSection(mainContributions, CONTRIBUTION_TYPE_MAIN, contributionClickListener));

        if (event.hasDessert())
            sectionedAdapter.addSection(new ContributionsSection(dessertContributions, CONTRIBUTION_TYPE_DESSERT, contributionClickListener));

        sectionedAdapter.notifyDataSetChanged();
        setProgressBarVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_content, menu);

        boolean canDelete = event.getOwner() != null
                && FirestoreManager.getCurrentUser() != null
                && TextUtils.equals(event.getOwner().getId(), FirestoreManager.getCurrentUser().getFirebaseId());
        menu.findItem(R.id.action_delete_event).setVisible(canDelete);


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EVENT_EDIT_REQUEST_CODE && resultCode == RESULT_OK && data.hasExtra(EVENT_EXTRA)) {
            event = data.getParcelableExtra(EVENT_EXTRA);
            displayEventContent();

            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(event.getName());
        } else if (requestCode == CONTRIBUTION_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadContributions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_edit_event:
                Intent intent = new Intent(this, EventEditActivity.class);
                intent.putExtra(EventContentActivity.EVENT_EXTRA, event);
                startActivityForResult(intent, EVENT_EDIT_REQUEST_CODE);
                break;
            case R.id.action_leave_event:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.leave_event)
                        .setMessage(String.format(getString(R.string.are_you_sure_leave), event.getName()))
                        .setPositiveButton(R.string.leave_event, (dialog, which) -> {
                            leaveEvent();
                            dialog.dismiss();
                        }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
                break;
            case R.id.action_delete_event:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_event)
                        .setMessage(String.format(getString(R.string.are_you_sure_delete_event), event.getName()))
                        .setPositiveButton(R.string.delete_event, (dialog, which) -> {
                            deleteEvent();
                            dialog.dismiss();
                        }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void leaveEvent() {
        setProgressBarVisible(true);
        event.getUsers().remove(FirestoreManager.getCurrentUser().getFirebaseId());
        FirestoreManager.updateEvent(event, new FirestoreManager.SimpleQueryListener() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
                setProgressBarVisible(false);
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(rv, R.string.an_error_occurred, Snackbar.LENGTH_LONG).show();
                setProgressBarVisible(false);
            }
        });
    }

    private void deleteEvent() {
        setProgressBarVisible(true);
        FirestoreManager.deleteEvent(event, new FirestoreManager.SimpleQueryListener() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
                setProgressBarVisible(false);
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(rv, R.string.an_error_occurred, Snackbar.LENGTH_LONG).show();
                setProgressBarVisible(false);
            }
        });
    }

    @OnClick(R.id.event_content_fab)
    public void onFabClicked() {
        setProgressBarVisible(true);
        FirestoreManager.getEventLink(event, task -> {
            setProgressBarVisible(false);
            if (!task.isSuccessful()) {
                showWarning(R.string.an_error_occurred);
                return;
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, task.getResult().getShortLink().toString());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, event.getName()));
        });

    }

    private void showWarning(@StringRes int message) {
        Snackbar.make(rv, message, Snackbar.LENGTH_LONG).show();
    }

    private void setProgressBarVisible(final boolean visible) {
        if (isRunning() && progressBar != null)
            runOnUiThread(() -> progressBar.setVisibility(visible ? View.VISIBLE : View.GONE));
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public ArrayList<Contribution> getAppetizerContributions() {
        return appetizerContributions;
    }

    public void setAppetizerContributions(ArrayList<Contribution> appetizerContributions) {
        this.appetizerContributions = appetizerContributions;
    }

    public ArrayList<Contribution> getStarterContributions() {
        return starterContributions;
    }

    public void setStarterContributions(ArrayList<Contribution> starterContributions) {
        this.starterContributions = starterContributions;
    }

    public ArrayList<Contribution> getMainContributions() {
        return mainContributions;
    }

    public void setMainContributions(ArrayList<Contribution> mainContributions) {
        this.mainContributions = mainContributions;
    }

    public ArrayList<Contribution> getDessertContributions() {
        return dessertContributions;
    }

    public void setDessertContributions(ArrayList<Contribution> dessertContributions) {
        this.dessertContributions = dessertContributions;
    }

    @NonNull
    @Override
    protected String getLogTag() {
        return "EventContentActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_event_content;
    }
}
