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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.ouftech.whobringswhat.R;
import net.ouftech.whobringswhat.model.Contribution;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContributionHeaderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.event_item_header_tv)
    TextView headerTv;

    public ContributionHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(String type) {
        headerTv.setText(Contribution.getTypePrint(itemView.getContext(), type));
    }
}
