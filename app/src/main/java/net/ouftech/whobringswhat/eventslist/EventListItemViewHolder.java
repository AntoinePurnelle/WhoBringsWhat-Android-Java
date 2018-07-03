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

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.model.Event;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventListItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.event_item_name_tv)
    TextView nameTv;
    @BindView(R.id.event_item_date_tv)
    TextView dateTv;
    @BindView(R.id.event_item_servings_tv)
    TextView servingsTv;

    public EventListItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Event event) {
        nameTv.setText(event.getName());
        servingsTv.setText(String.valueOf(event.getServings()));

        if (event.getTime() > 0)
            dateTv.setText(DateFormat.getDateFormat(itemView.getContext()).format(new Date(event.getTime())));
    }

}
