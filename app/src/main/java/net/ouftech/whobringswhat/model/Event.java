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


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class Event implements Parcelable {

    public static final String USERS_FIELD = "users";

    private String id;
    private String name;
    @Nullable
    private String description;
    private long time;
    private long endTime;
    @Nullable
    private String location;
    private int servings;
    private boolean appetizer;
    private boolean starter;
    private boolean main;
    private boolean dessert;
    @Nullable
    private String type;
    @Nullable
    private String budget;
    private CollectionReference contributions;
    private Map<String, Long> users;
    private DocumentReference owner; // User

    public Event() {
        main = true;
    }

    public Event(String id, String name, @Nullable String description, long time, long endTime, @Nullable String location, int servings, boolean appetizer, boolean starter, boolean main, boolean dessert, @Nullable String type, @Nullable String budget, CollectionReference contributions, Map<String, Long> users, DocumentReference owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.time = time;
        this.endTime = endTime;
        this.location = location;
        this.servings = servings;
        this.appetizer = appetizer;
        this.starter = starter;
        this.main = main;
        this.dessert = dessert;
        this.type = type;
        this.budget = budget;
        this.contributions = contributions;
        this.users = users;
        this.owner = owner;
    }

    public Event(String name, @Nullable String description, long time, long endTime, @Nullable String location, int servings, boolean appetizer, boolean starter, boolean main, boolean dessert, @Nullable String type, @Nullable String budget, Map<String, Long> users, DocumentReference owner) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.endTime = endTime;
        this.location = location;
        this.servings = servings;
        this.appetizer = appetizer;
        this.starter = starter;
        this.main = main;
        this.dessert = dessert;
        this.type = type;
        this.budget = budget;
        this.users = users;
        this.owner = owner;
    }

    public static Event fromDocument(DocumentSnapshot documentSnapshot) {
        Event event = documentSnapshot.toObject(Event.class);
        event.id = documentSnapshot.getId();
        return event;
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

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Nullable
    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable long endTime) {
        this.endTime = endTime;
    }

    @Nullable
    public String getLocation() {
        return location;
    }

    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public boolean hasAppetizer() {
        return appetizer;
    }

    public void setAppetizer(boolean appetizer) {
        this.appetizer = appetizer;
    }

    public boolean hasStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public boolean hasMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public boolean hasDessert() {
        return dessert;
    }

    public void setDessert(boolean dessert) {
        this.dessert = dessert;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public CollectionReference getContributions() {
        return contributions;
    }

    public void setContributions(CollectionReference contributions) {
        this.contributions = contributions;
    }

    public Map<String, Long> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Long> users) {
        this.users = users;
    }

    public DocumentReference getOwner() {
        return owner;
    }

    public void setOwner(DocumentReference owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Event{\n" +
                " id='" + id + '\'' +
                ",\n name='" + name + '\'' +
                ",\n description='" + description + '\'' +
                ",\n time=" + time +
                ",\n endTime=" + endTime +
                ",\n location='" + location + '\'' +
                ",\n servings=" + servings +
                ",\n appetizer=" + appetizer +
                ",\n starter=" + starter +
                ",\n main=" + main +
                ",\n dessert=" + dessert +
                ",\n type='" + type + '\'' +
                ",\n budget=" + budget +
                ",\n contributions=" + contributions +
                ",\n users=" + users +
                ",\n owner=" + (owner != null ? owner.getId() : "null") +
                "\n}";
    }

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        time = in.readLong();
        endTime = in.readLong();
        location = in.readString();
        servings = in.readInt();
        appetizer = in.readByte() != 0x00;
        starter = in.readByte() != 0x00;
        main = in.readByte() != 0x00;
        dessert = in.readByte() != 0x00;
        type = in.readString();
        budget = in.readString();
        contributions = (CollectionReference) in.readValue(CollectionReference.class.getClassLoader());
        owner = (DocumentReference) in.readValue(DocumentReference.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(time);
        dest.writeLong(endTime);
        dest.writeString(location);
        dest.writeInt(servings);
        dest.writeByte((byte) (appetizer ? 0x01 : 0x00));
        dest.writeByte((byte) (starter ? 0x01 : 0x00));
        dest.writeByte((byte) (main ? 0x01 : 0x00));
        dest.writeByte((byte) (dessert ? 0x01 : 0x00));
        dest.writeString(type);
        dest.writeString(budget);
        dest.writeValue(contributions);
        dest.writeValue(owner);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}