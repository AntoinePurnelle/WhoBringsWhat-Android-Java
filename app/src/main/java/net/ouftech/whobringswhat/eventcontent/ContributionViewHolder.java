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

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.model.Contribution;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContributionViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.contribution_image_iv)
    ImageView imageIv;
    @BindView(R.id.contribution_name_tv)
    TextView nameTv;
    @BindView(R.id.contribution_contributor_tv)
    TextView contributorTv;
    @BindView(R.id.contribution_servings_iv)
    AppCompatImageView servingsIv;
    @BindView(R.id.contribution_servings_tv)
    TextView servingsTv;

    public ContributionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Contribution contribution) {
        nameTv.setText(contribution.getName());
        servingsTv.setText(String.valueOf(contribution.getServings()));

        if (contribution.getServings() > 0) {
            servingsTv.setText(String.valueOf(contribution.getServings()));
            servingsTv.setVisibility(View.VISIBLE);
            servingsIv.setVisibility(View.VISIBLE);
        } else {
            servingsTv.setVisibility(View.GONE);
            servingsIv.setVisibility(View.GONE);
        }

        contributorTv.setText(contribution.getContributor());

        if (contribution.isDrink())
            imageIv.setImageResource(R.drawable.ic_local_drink_black_24dp);
        else
            imageIv.setImageResource(R.drawable.ic_local_dining_black_24dp);
    }

}
