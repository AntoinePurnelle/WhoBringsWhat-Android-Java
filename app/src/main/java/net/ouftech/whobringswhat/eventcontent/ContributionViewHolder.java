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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.model.Contribution;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.contribution_more_button)
    AppCompatImageView moreButton;
    @BindView(R.id.contribution_servings_layout)
    LinearLayout servingsLayout;
    @BindView(R.id.contribution_quantity_tv)
    TextView quantityTv;
    @BindView(R.id.contribution_quantity_layout)
    LinearLayout quantityLayout;
    @BindView(R.id.contribution_comment_tv)
    TextView commentTv;
    @BindView(R.id.contribution_comment_layout)
    LinearLayout commentLayout;
    @BindView(R.id.contribution_more_layout)
    LinearLayout moreLayout;

    boolean expanded = false;

    public ContributionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Contribution contribution) {
        nameTv.setText(contribution.getName());
        servingsTv.setText(String.valueOf(contribution.getServings()));

        contributorTv.setText(contribution.getContributor());

        if (contribution.isDrink()) {
            imageIv.setImageResource(R.drawable.ic_local_drink_black_24dp);
            imageIv.setContentDescription(itemView.getContext().getString(R.string.contribution_type_drink));
        } else {
            imageIv.setImageResource(R.drawable.ic_local_dining_black_24dp);
            imageIv.setContentDescription(itemView.getContext().getString(R.string.contribution_type_food));
        }

        if (contribution.getQuantity() <= 0 && contribution.getServings() <= 0 && TextUtils.isEmpty(contribution.getComment())) {
            moreLayout.setVisibility(View.GONE);
            moreButton.setVisibility(View.GONE);
        } else {
            if (expanded)
                expand();
            else
                collapse();
        }

        if (contribution.getServings() > 0) {
            servingsLayout.setVisibility(View.VISIBLE);
            servingsTv.setText(String.valueOf(contribution.getServings()));
        } else {
            servingsLayout.setVisibility(View.GONE);
            servingsTv.setText(null);
        }

        if (contribution.getQuantity() > 0) {
            quantityLayout.setVisibility(View.VISIBLE);
            String quantity = String.valueOf(contribution.getQuantity());
            if (!TextUtils.isEmpty(contribution.getUnit()))
                quantity = String.format("%s %s", quantity, contribution.getUnit());
            quantityTv.setText(quantity);
        } else {
            quantityLayout.setVisibility(View.GONE);
            quantityTv.setText(null);
        }

        if (!TextUtils.isEmpty(contribution.getComment())) {
            commentLayout.setVisibility(View.VISIBLE);
            commentTv.setText(contribution.getComment());
        } else {
            commentLayout.setVisibility(View.GONE);
            commentTv.setText(null);
        }
    }

    @OnClick(R.id.contribution_more_button)
    public void onMoreClicked() {
        if (expanded) {
            collapse();
            expanded = false;
        } else {
            expand();
            expanded = true;
        }
    }

    private void expand() {
        moreLayout.setVisibility(View.VISIBLE);
        moreButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
    }

    private void collapse() {
        moreLayout.setVisibility(View.GONE);
        moreButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
    }
}
