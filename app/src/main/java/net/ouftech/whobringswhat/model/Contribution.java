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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.PropertyName;

import net.ouftech.whobringswhat.R;

public class Contribution implements Parcelable {

    public static final String CONTRIBUTION_TYPE_APPETIZER = "appetizer";
    public static final String CONTRIBUTION_TYPE_STARTER = "starter";
    public static final String CONTRIBUTION_TYPE_MAIN = "main";
    public static final String CONTRIBUTION_TYPE_DESSERT = "dessert";

    private String id;
    private String name;
    private int servings;
    private int quantity;
    @Nullable
    private String unit;
    private String type;
    @Nullable
    private String comment;
    private DocumentReference user;
    private String contributor;
    private boolean isDrink;

    public Contribution() {
    }

    public Contribution(String name, int servings, int quantity, @Nullable String unit, String type, @Nullable String comment, DocumentReference user, String contributor, boolean isDrink) {
        this.name = name;
        this.servings = servings;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.comment = comment;
        this.user = user;
        this.contributor = contributor;
        this.isDrink = isDrink;
    }

    public static Contribution fromDocument(DocumentSnapshot documentSnapshot) {
        Contribution contribution = documentSnapshot.toObject(Contribution.class);
        if (contribution != null) {
            contribution.id = documentSnapshot.getId();
        }
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

    @NonNull
    public String getType() {
        return type != null ? type : CONTRIBUTION_TYPE_MAIN;
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

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    @PropertyName(value = "isDrink")
    public boolean isDrink() {
        return isDrink;
    }

    @PropertyName(value = "isDrink")
    public void setIsDrink(boolean isDrink) {
        this.isDrink = isDrink;
    }

    @NonNull
    public String getTypePrint(@NonNull Context context) {
        if (type == null)
            return context.getString(R.string.main);

        switch (type) {
            case Contribution.CONTRIBUTION_TYPE_APPETIZER:
                return context.getString(R.string.appetizer);
            case Contribution.CONTRIBUTION_TYPE_STARTER:
                return context.getString(R.string.starter);
            case Contribution.CONTRIBUTION_TYPE_DESSERT:
                return context.getString(R.string.dessert);
            case Contribution.CONTRIBUTION_TYPE_MAIN:
            default:
                return context.getString(R.string.main);
        }
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
                ",\n contributor=" + contributor +
                ",\n isDrink=" + isDrink +
                "\n}";
    }

    protected Contribution(Parcel in) {
        id = in.readString();
        name = in.readString();
        servings = in.readInt();
        quantity = in.readInt();
        unit = in.readString();
        type = in.readString();
        comment = in.readString();
        user = FirestoreManager.getUserReferenceForId(in.readString());
        contributor = in.readString();
        isDrink = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(servings);
        dest.writeInt(quantity);
        dest.writeString(unit);
        dest.writeString(type);
        dest.writeString(comment);
        dest.writeString(user != null ? user.getId() : null);
        dest.writeString(contributor);
        dest.writeByte((byte) (isDrink ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contribution> CREATOR = new Parcelable.Creator<Contribution>() {
        @Override
        public Contribution createFromParcel(Parcel in) {
            return new Contribution(in);
        }

        @Override
        public Contribution[] newArray(int size) {
            return new Contribution[size];
        }
    };
}