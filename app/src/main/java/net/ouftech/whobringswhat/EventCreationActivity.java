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

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.state.State;

import net.ouftech.whobringswhat.commons.BaseActivity;
import net.ouftech.whobringswhat.commons.Logger;
import net.ouftech.whobringswhat.model.Event;
import net.ouftech.whobringswhat.model.FirestoreManager;

import java.util.ArrayList;
import java.util.Calendar;
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
    @State
    private long startDate = 0;
    @State
    private long endDate = 0;
    @State
    private boolean eventCreation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            event = getIntent().getParcelableExtra(EVENT_EXTRA);

            if (event != null) { // First onCreate with an existing Event
                // Init dates with existing event (it will be restored with the @State annotation if not first onCreate)
                startDate = event.getTime();
                endDate = event.getEndTime();
                saveButton.setText(R.string.save_event);
            } else {
                // No event in Intent --> new event
                eventCreation = true;
                saveButton.setText(R.string.create_event);
            }
        }

        if (event == null)
            event = new Event();

        nameEt.setText(event.getName());
        descriptionEt.setText(event.getDescription());
        displayDate(startDate, startTimeEt);
        displayDate(endDate, endTimeEt);
        locationEt.setText(event.getLocation());
        courses = new boolean[]{event.hasAppetizer(), event.hasStarter(), event.hasMain(), event.hasDessert()};
        updateSelectedCourses();
        budgetEt.setText(event.getBudget());
    }

    private void displayDate(long date, @NonNull EditText editText) {
        String dateString = date > 0 ? DateUtils.formatDateTime(this, date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME) : "";
        editText.setText(dateString);
    }

    @OnClick(R.id.event_creation_start_time_et)
    public void onStartTimeEtClicked() {
        showDateTimePicker(startDate, dateTime -> {
            startDate = dateTime;
            displayDate(startDate, startTimeEt);
        });
    }

    @OnClick(R.id.event_creation_end_time_et)
    public void onEndTimeEtClicked() {
        showDateTimePicker(endDate, dateTime -> {
            endDate = dateTime;
            displayDate(endDate, endTimeEt);
        });
    }

    public void showDateTimePicker(long dateTime, DateTimePickerListener listener) {
        final Calendar date = Calendar.getInstance();
        if (dateTime > 0)
            date.setTimeInMillis(dateTime);

        new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);

            new TimePickerDialog(EventCreationActivity.this, (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                listener.onDateTimePicked(date.getTimeInMillis());
            }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), false).show();

        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE)).show();
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
        if (checkValidity()) {
            if (endDate > 0 && endDate <= startDate) {
                showWarning(R.string.enddate_cannot_be_before_startdate);
                return;
            }

            if (!TextUtils.isEmpty(servingsEt.getText().toString())) {
                int servings;
                try {
                    servings = Integer.parseInt(servingsEt.getText().toString());
                    event.setServings(servings);
                } catch (NumberFormatException e) {
                    showWarning(R.string.please_enter_valid_servings);
                    return;
                }
            }

            event.setName(nameEt.getText().toString());
            event.setDescription(descriptionEt.getText().toString());
            event.setTime(startDate);
            if (endDate > 0)
                event.setEndTime(endDate);
            event.setLocation(locationEt.getText().toString());
            event.setAppetizer(courses[0]);
            event.setStarter(courses[1]);
            event.setMain(courses[2]);
            event.setDessert(courses[3]);
            event.setBudget(budgetEt.getText().toString());

            if (eventCreation) {
                Logger.d(getLogTag(), String.format("Creating event %s", event));
                FirestoreManager.addEvent(event, new FirestoreManager.AddListener() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logger.d(getLogTag(), "Event created");
                        Toast.makeText(EventCreationActivity.this, R.string.event_created, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        showWarning(R.string.an_error_occurred);
                    }
                });
            }
        }
    }

    private boolean checkValidity() {
        if (nameEt.getText().length() == 0
                || startTimeEt.getText().length() == 0
                || locationEt.getText().length() == 0
                || coursesEt.getText().length() == 0) {
            showWarning(R.string.please_fill_non_optional);
            return false;
        }
        return true;
    }

    private void showWarning(@StringRes int message) {
        Snackbar.make(saveButton, message, Snackbar.LENGTH_LONG).show();
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

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public boolean isEventCreation() {
        return eventCreation;
    }

    public void setEventCreation(boolean eventCreation) {
        this.eventCreation = eventCreation;
    }

    private interface DateTimePickerListener {
        void onDateTimePicked(long dateTime);
    }
}
