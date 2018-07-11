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
import android.support.v7.app.AlertDialog;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.evernote.android.state.State;

import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.model.Contribution;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static net.ouftech.whobringswhat.model.Contribution.CONTRIBUTION_TYPES;

public class ContributionEditActivity extends BaseActivity {

    public static final String CONTRIBUTION_EXTRA = "CONTRIBUTION_EXTRA";
    public static final String TYPE_EXTRA = "TYPE_EXTRA";

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

    @State
    private Contribution contribution;
    @State // Whether it's a new contribution or the update of an existing one
    private boolean contributionCreation = false;
    @State
    @StringRes
    private int titleRes;

    List<String> types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            contribution = getIntent().getParcelableExtra(CONTRIBUTION_EXTRA);

            if (contribution != null) { // First onCreate with an existing Contribution
                saveButton.setText(R.string.save_contribution);
                titleRes = R.string.edit_contribution;
            } else {
                // No event in Intent --> new event
                contributionCreation = true;
                saveButton.setText(R.string.create_contribution);
                titleRes = R.string.new_contribution;
            }
        }

        if (contribution == null) {
            contribution = new Contribution();
            contribution.setType(getIntent().getStringExtra(TYPE_EXTRA));
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

        types = Arrays.asList(CONTRIBUTION_TYPES);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(titleRes);
    }

    @OnClick(R.id.contribution_edit_course_et)
    public void onCourseEtClicked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.courses)
                .setSingleChoiceItems(getResources().getStringArray(R.array.courses_array),
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

        }
    }

    private boolean checkValidity() {
        return true;
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
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
