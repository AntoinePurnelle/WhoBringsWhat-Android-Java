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
import android.view.View;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.commons.CollectionUtils;
import net.ouftech.whobringswhat.model.Event;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class EventsSection extends StatelessSection {

    private List<Event> events;
    private String title;

    public EventsSection(List<Event> events, String title) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.event_list_item)
                .headerResourceId(R.layout.event_list_item_header)
                .build());
        this.events = events;
        this.title = title;
    }

    @Override
    public int getContentItemsTotal() {
        return CollectionUtils.getSize(events);
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new EventListItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < CollectionUtils.getSize(events))
            ((EventListItemViewHolder) holder).bind(events.get(position));
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new EventListItemHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((EventListItemHeaderViewHolder)holder).bind(title);
    }
}
