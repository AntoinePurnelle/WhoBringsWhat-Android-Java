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
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.state.State;

import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.model.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class EventCreationActivity extends BaseActivity {

    public static final String EVENT_EXTRA = "EVENT_EXTRA";

    @BindView(R.id.event_creation_name_et)
    EditText nameEt;
    @BindView(R.id.event_creation_description_et)
    EditText descriptionEt;
    @BindView(R.id.event_creation_start_time_et)
    EditText startTimeEt;
    @BindView(R.id.event_creation_end_time_et)
    EditText endTimeEt;
    @BindView(R.id.event_creation_location_et)
    EditText locationEt;
    @BindView(R.id.event_creation_servings_et)
    EditText servingsEt;
    @BindView(R.id.event_creation_courses_et)
    EditText coursesEt;
    @BindView(R.id.event_creation_budget_et)
    EditText budgetEt;
    @BindView(R.id.event_creation_save_button)
    TextView saveButton;

    @State
    private Event event;
    private boolean[] courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            event = getIntent().getParcelableExtra(EVENT_EXTRA);

        if (event == null)
            event = new Event();

        nameEt.setText(event.getName());
        descriptionEt.setText(event.getDescription());
        String startDate = event.getTime() > 0 ? DateFormat.getLongDateFormat(this).format(new Date(event.getTime())) : "";
        startTimeEt.setText(startDate);
        String endDate = event.getTime() > 0 ? DateFormat.getLongDateFormat(this).format(new Date(event.getEndTime())) : "";
        endTimeEt.setText(endDate);
        locationEt.setText(event.getLocation());
        courses = new boolean[]{event.hasAppetizer(), event.hasStarter(), event.hasMain(), event.hasDessert()};
        updateSelectedCourses();
        budgetEt.setText(event.getBudget());
    }

    @OnClick(R.id.event_creation_start_time_et)
    public void onStartTimeEtClicked() {
    }

    @OnClick(R.id.event_creation_end_time_et)
    public void onEndTimeEtClicked() {
    }

    @OnClick(R.id.event_creation_courses_et)
    public void onCoursesEtClicked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.courses)
                .setMultiChoiceItems(getResources().getStringArray(R.array.planets_array), courses, (dialog, which, isChecked) -> {
                    courses[which] = isChecked;
                    updateSelectedCourses();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void updateSelectedCourses() {
        List<String> selectedCourses = new ArrayList<>();
        for (int i = 0; i < courses.length; i++) {
            if (courses[i])
                selectedCourses.add(getResources().getStringArray(R.array.planets_array)[i]);
        }
        coursesEt.setText(TextUtils.join(", ", selectedCourses));
    }

    @OnClick(R.id.event_creation_save_button)
    public void onSaveButtonClicked() {
        checkValidity();
    }

    private boolean checkValidity() {
        if (nameEt.getText().length() == 0
                || startTimeEt.getText().length() == 0
                || locationEt.getText().length() == 0
                || coursesEt.getText().length() == 0) {
            warnInvalidFields();
            return false;
        }
        return true;
    }

    private void warnInvalidFields() {
        Toast.makeText(this, R.string.please_fill_non_optional, Toast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    protected String getLogTag() {
        return "EventCreationActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.event_creation;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
