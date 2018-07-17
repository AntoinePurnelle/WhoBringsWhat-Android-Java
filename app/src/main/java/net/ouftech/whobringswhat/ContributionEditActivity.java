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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.state.State;

import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.model.Contribution;
import net.ouftech.whobringswhat.model.Event;
import net.ouftech.whobringswhat.model.FirestoreManager;
import net.ouftech.whobringswhat.model.RealTimeDBManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_APPETIZER;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_DESSERT;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_MAIN;
import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPE_STARTER;

public class ContributionEditActivity extends BaseActivity {

    public static final String CONTRIBUTION_EXTRA = "CONTRIBUTION_EXTRA";
    public static final String TYPE_EXTRA = "TYPE_EXTRA";
    public static final String EVENT_EXTRA = "EVENT_EXTRA";

    @BindView(R.id.contribution_edit_name_et)
    EditText nameEt;
    @BindView(R.id.contribution_edit_contributor_et)
    EditText contributorEt;
    @BindView(R.id.contribution_edit_servings_et)
    EditText servingsEt;
    @BindView(R.id.contribution_edit_quantity_et)
    EditText quantityEt;
    @BindView(R.id.contribution_edit_unit_et)
    EditText unitEt;
    @BindView(R.id.contribution_edit_course_et)
    EditText courseEt;
    @BindView(R.id.contribution_edit_comment_et)
    EditText commentEt;
    @BindView(R.id.event_edit_save_button)
    TextView saveButton;
    @BindView(R.id.contribution_edit_drink_cb)
    CheckBox drinkCb;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;

    @State
    private Contribution contribution;
    @State
    private Event event;
    @State // Whether it's a new contribution or the update of an existing one
    private boolean contributionCreation = false;
    @State
    @StringRes
    private int titleRes;
    @State
    @StringRes
    private int buttonRes;

    List<String> types;
    String courses[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            event = getIntent().getParcelableExtra(EVENT_EXTRA);
            int position = getIntent().getIntExtra(CONTRIBUTION_EXTRA, -1);
            if (position >= 0)
                contribution = event.getContributionsList().get(position);


            if (contribution != null) { // First onCreate with an existing Contribution
                buttonRes = R.string.save_contribution;
                titleRes = R.string.edit_contribution;
            } else {
                // No event in Intent --> new event
                contributionCreation = true;
                buttonRes = R.string.create_contribution;
                titleRes = R.string.new_contribution;
            }
        }

        if (contribution == null) {
            contribution = new Contribution();
            contribution.setType(getIntent().getStringExtra(TYPE_EXTRA));
            contribution.setContributor(RealTimeDBManager.getCurrentUser().getName());
        }

        nameEt.setText(contribution.getName());
        contributorEt.setText(contribution.getContributor());
        if (contribution.getServings() > 0)
            servingsEt.setText(String.valueOf(contribution.getServings()));
        if (contribution.getQuantity() > 0)
            quantityEt.setText(String.valueOf(contribution.getQuantity()));
        unitEt.setText(contribution.getUnit());
        courseEt.setText(contribution.getTypePrint(this));
        commentEt.setText(contribution.getComment());
        drinkCb.setChecked(contribution.isDrink());

        types = new ArrayList<>();
        if (event.hasAppetizer())
            types.add(CONTRIBUTION_TYPE_APPETIZER);
        if (event.hasStarter())
            types.add(CONTRIBUTION_TYPE_STARTER);
        if (event.hasMain())
            types.add(CONTRIBUTION_TYPE_MAIN);
        if (event.hasDessert())
            types.add(CONTRIBUTION_TYPE_DESSERT);

        courses = new String[types.size()];
        for (int i = 0; i< types.size(); i++)
            courses[i] = Contribution.getTypePrint(this, types.get(i));


        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(titleRes);
        saveButton.setText(buttonRes);

        courseEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                onCourseEtClicked();
        });
    }

    @OnClick(R.id.contribution_edit_course_et)
    public void onCourseEtClicked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.courses)
                .setSingleChoiceItems(courses,
                        types.indexOf(contribution.getType()),
                        (dialog, which) -> {
                            contribution.setType(types.get(which));
                            courseEt.setText(contribution.getTypePrint(this));
                            dialog.dismiss();
                        })
                .create().show();
    }

    @OnClick(R.id.contribution_edit_drink_cb)
    public void onDrinkCheckBoxClicked() {
        contribution.setIsDrink(!contribution.isDrink());
    }

    @OnClick(R.id.event_edit_save_button)
    public void onSaveButtonClicked() {
        if (checkValidity()) {
            contribution.setName(nameEt.getText().toString());
            contribution.setContributor(contributorEt.getText().toString());
            if (!TextUtils.isEmpty(servingsEt.getText().toString()))
                contribution.setServings(Integer.parseInt(servingsEt.getText().toString()));
            if (!TextUtils.isEmpty(quantityEt.getText().toString()))
                contribution.setQuantity(Integer.parseInt(quantityEt.getText().toString()));
            contribution.setUnit(unitEt.getText().toString());
            contribution.setComment(commentEt.getText().toString());

            saveContribution();
        }
    }

    private void saveContribution() {
        setProgressBarVisible(true);
        if (contributionCreation) {
            Logger.d(getLogTag(), String.format("Creating contribution %s", contribution));
            RealTimeDBManager.addContribution(event, contribution, new FirestoreManager.SimpleQueryListener() {
                @Override
                public void onSuccess(Void aVoid) {
                    Logger.d(getLogTag(), "Contribution created");
                    setProgressBarVisible(false);
                    Toast.makeText(ContributionEditActivity.this, R.string.contribution_created, Toast.LENGTH_LONG).show();
                    finishWithSuccess();
                }

                @Override
                public void onFailure(Exception e) {
                    setProgressBarVisible(false);
                    showWarning(R.string.an_error_occurred);
                }
            });
            /*FirestoreManager.addContribution(event, contribution, new FirestoreManager.SimpleQueryListener() {
                @Override
                public void onSuccess(Void aVoid) {
                    Logger.d(getLogTag(), "Contribution created");
                    setProgressBarVisible(false);
                    Toast.makeText(ContributionEditActivity.this, R.string.contribution_created, Toast.LENGTH_LONG).show();
                    finishWithSuccess();
                }

                @Override
                public void onFailure(Exception e) {
                    setProgressBarVisible(false);
                    showWarning(R.string.an_error_occurred);
                }
            });*/
        } else {
            Logger.d(getLogTag(), String.format("Updating contribution %s", contribution));
            RealTimeDBManager.updateEvent(event, new FirestoreManager.SimpleQueryListener() {
                @Override
                public void onSuccess(Void aVoid) {
                    Logger.d(getLogTag(), "Contribution saved");
                    setProgressBarVisible(false);
                    Toast.makeText(ContributionEditActivity.this, R.string.contribution_saved, Toast.LENGTH_LONG).show();
                    finishWithSuccess();
                }

                @Override
                public void onFailure(Exception e) {
                    setProgressBarVisible(false);
                    showWarning(R.string.an_error_occurred);
                }
            });
            /*FirestoreManager.updateContribution(event, contribution, new FirestoreManager.SimpleQueryListener() {
                @Override
                public void onSuccess(Void aVoid) {
                    Logger.d(getLogTag(), "Contribution saved");
                    setProgressBarVisible(false);
                    Toast.makeText(ContributionEditActivity.this, R.string.contribution_saved, Toast.LENGTH_LONG).show();
                    finishWithSuccess();
                }

                @Override
                public void onFailure(Exception e) {
                    setProgressBarVisible(false);
                    showWarning(R.string.an_error_occurred);
                }
            });*/
        }
    }

    private void finishWithSuccess() {
        getIntent().putExtra(CONTRIBUTION_EXTRA, contribution);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private boolean checkValidity() {
        // Checks all mandatory fields are filled
        if (nameEt.getText().length() == 0
                || contributorEt.getText().length() == 0
                || courseEt.getText().length() == 0) {
            showWarning(R.string.please_fill_non_optional);
            return false;
        }

        // Checks that the value in the servings EditText is an integer
        if (!TextUtils.isEmpty(servingsEt.getText().toString())) {
            try {
                Integer.parseInt(servingsEt.getText().toString());
            } catch (NumberFormatException e) {
                showWarning(R.string.please_enter_valid_servings);
                return false;
            }
        }

        // Checks that the value in the quantity EditText is an integer
        if (!TextUtils.isEmpty(quantityEt.getText().toString())) {
            try {
                Integer.parseInt(quantityEt.getText().toString());
            } catch (NumberFormatException e) {
                showWarning(R.string.please_enter_valid_servings);
                return false;
            }
        }

        return true;
    }

    /**
     * Displays a Snackbar with the given message
     *
     * @param message Message to display
     */
    private void showWarning(@StringRes int message) {
        Snackbar.make(saveButton, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contribution_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_contribution) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_contribution)
                    .setMessage(getString(R.string.are_you_sure_delete_contribution))
                    .setPositiveButton(R.string.delete_contribution, (dialog, which) -> {
                        deleteContribution();
                        dialog.dismiss();
                    }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteContribution() {
        setProgressBarVisible(true);

        event.getContributionsList().remove(contribution);
        RealTimeDBManager.updateEvent(event, new FirestoreManager.SimpleQueryListener() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.d(getLogTag(), "Contribution removed");
                setProgressBarVisible(false);
                finishWithSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                setProgressBarVisible(false);
                showWarning(R.string.an_error_occurred);
            }
        });

        /*FirestoreManager.deleteContribution(event, contribution, new FirestoreManager.SimpleQueryListener() {
            @Override
            public void onSuccess(Void aVoid) {
                setProgressBarVisible(false);
                finishWithSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(nameEt, R.string.an_error_occurred, Snackbar.LENGTH_LONG).show();
                setProgressBarVisible(false);
            }
        });*/
    }

    private void setProgressBarVisible(final boolean visible) {
        if (isRunning() && progressBar != null)
            runOnUiThread(() -> progressBar.setVisibility(visible ? View.VISIBLE : View.GONE));
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isContributionCreation() {
        return contributionCreation;
    }

    public void setContributionCreation(boolean contributionCreation) {
        this.contributionCreation = contributionCreation;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
    }

    public int getButtonRes() {
        return buttonRes;
    }

    public void setButtonRes(int buttonRes) {
        this.buttonRes = buttonRes;
    }

    @NonNull
    @Override
    protected String getLogTag() {
        return "ContributionEditActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_contribution_edit;
    }
}
