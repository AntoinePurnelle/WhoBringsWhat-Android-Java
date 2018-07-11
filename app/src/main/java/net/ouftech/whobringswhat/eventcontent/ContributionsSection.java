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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.commons.CollectionUtils;
import net.ouftech.whobringswhat.model.Contribution;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ContributionsSection extends Section {

    private List<Contribution> contributions;
    private String type;
    private ContributionClickListener listener;

    public ContributionsSection(@NonNull List<Contribution> contributions, @NonNull String type, @NonNull ContributionClickListener listener) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.contribution_list_item)
                .headerResourceId(R.layout.contribution_list_header)
                .footerResourceId(R.layout.contribution_list_footer)
                .emptyResourceId(R.layout.empty_contributions_view)
                .build());

        this.contributions = contributions;
        this.type = type;
        this.listener = listener;

        if (CollectionUtils.isEmpty(contributions))
            setState(State.EMPTY);
    }

    @Override
    public int getContentItemsTotal() {
        return CollectionUtils.getSize(contributions);
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContributionViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < CollectionUtils.getSize(contributions)) {
            ((ContributionViewHolder) holder).bind(contributions.get(position));
            holder.itemView.setOnClickListener(v -> listener.onEditContributionClicked(position, type));
        }
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new ContributionHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((ContributionHeaderViewHolder) holder).bind(type);
    }

    @Override
    public RecyclerView.ViewHolder getFooterViewHolder(View view) {
        return new ContributionFooterViewHolder(view);
    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        ((ContributionFooterViewHolder)holder).getFooterTv().setOnClickListener(v -> listener.onAddContributionClicked(type));
    }

    @Override
    public void onBindEmptyViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindEmptyViewHolder(holder);
    }

    public interface ContributionClickListener {
        void onEditContributionClicked(int position, String type);

        void onAddContributionClicked(String type);
    }
}
