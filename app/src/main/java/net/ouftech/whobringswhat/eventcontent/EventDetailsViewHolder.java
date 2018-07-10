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

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.model.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.event_details_description_tv)
    TextView descriptionTv;
    @BindView(R.id.event_details_date_tv)
    TextView dateTv;
    @BindView(R.id.event_details_location_tv)
    TextView locationTv;
    @BindView(R.id.event_details_location_ll)
    LinearLayout locationLl;
    @BindView(R.id.event_details_end_date_tv)
    TextView endDateTv;
    @BindView(R.id.event_details_servings_tv)
    TextView servingsTv;
    @BindView(R.id.event_details_servings_ll)
    LinearLayout servingsLl;
    @BindView(R.id.event_details_budget_tv)
    TextView budgetTv;
    @BindView(R.id.event_details_budget_ll)
    LinearLayout budgetLl;

    public EventDetailsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(@NonNull Event event) {
        descriptionTv.setText(event.getDescription());

        Resources res = itemView.getResources();
        String dateString = event.getTime() > 0 ? res.getString(R.string.from) + " " + DateUtils.formatDateTime(itemView.getContext(), event.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME) : "";
        dateTv.setText(dateString);
        dateString = event.getEndTime() > 0 ? res.getString(R.string.to) + " " + DateUtils.formatDateTime(itemView.getContext(), event.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME) : null;
        endDateTv.setText(dateString);

        if (TextUtils.isEmpty(event.getLocation())) {
            locationLl.setVisibility(View.GONE);
        } else {
            locationLl.setVisibility(View.VISIBLE);
            locationTv.setText(event.getLocation());
        }

        if (event.getServings() > 0) {
            servingsLl.setVisibility(View.VISIBLE);
            servingsTv.setText(String.valueOf(event.getServings()));
        } else {
            servingsLl.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(event.getBudget())) {
            budgetLl.setVisibility(View.GONE);
        } else {
            budgetLl.setVisibility(View.VISIBLE);
            budgetTv.setText(event.getBudget());
        }
    }

}
