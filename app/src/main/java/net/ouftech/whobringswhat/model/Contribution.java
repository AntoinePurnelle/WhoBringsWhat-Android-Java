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

package net.ouftech.whobringswhat.model;

import android.support.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

public class Contribution {

    private String id;
    private String name;
    private int servings;
    private int quantity;
    @Nullable
    private String unit;
    private String type;
    @Nullable
    private String comment ;
    private DocumentSnapshot user;

    public Contribution() {
    }

    public Contribution(String id, String name, int servings, int quantity, @Nullable String unit, String type, @Nullable String comment, DocumentSnapshot user) {
        this.id = id;
        this.name = name;
        this.servings = servings;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.comment = comment;
        this.user = user;
    }

    @Override
    public String toString() {
        return "Contribution{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", servings=" + servings +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", user=" + user +
                '}';
    }
}
