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

import com.google.firebase.firestore.DocumentReference;
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
    private DocumentReference user;

    public Contribution() {
    }

    public Contribution(String id, String name, int servings, int quantity, @Nullable String unit, String type, @Nullable String comment, DocumentReference user) {
        this.id = id;
        this.name = name;
        this.servings = servings;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.comment = comment;
        this.user = user;
    }

    public Contribution(String name, int servings, int quantity, @Nullable String unit, String type, @Nullable String comment, DocumentReference user) {
        this.name = name;
        this.servings = servings;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.comment = comment;
        this.user = user;
    }

    public static Contribution fromDocument(DocumentSnapshot documentSnapshot) {
        Contribution contribution = documentSnapshot.toObject(Contribution.class);
        contribution.id = documentSnapshot.getId();
        return contribution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Nullable
    public String getUnit() {
        return unit;
    }

    public void setUnit(@Nullable String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Contribution{" +
                "\n id='" + id + '\'' +
                ",\n name='" + name + '\'' +
                ",\n servings=" + servings +
                ",\n quantity=" + quantity +
                ",\n unit='" + unit + '\'' +
                ",\n type='" + type + '\'' +
                ",\n comment='" + comment + '\'' +
                ",\n user=" + user.getId() +
                "\n}";
    }
}
