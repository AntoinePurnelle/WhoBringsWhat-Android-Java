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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;

public class Event implements Parcelable, Comparable<Event> {

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
    private HashMap<String, Boolean> courses;
    @Nullable
    private String type;
    @Nullable
    private String budget;
    private CollectionReference contributions;
    private HashMap<String, Long> users;
    private DocumentReference owner; // User

    public Event() {
        users = new HashMap<>();
        courses = new HashMap<>();
    }

    public Event(String id, String name, @Nullable String description, long time, long endTime, @Nullable String location, int servings, boolean appetizer, boolean starter, boolean main, boolean dessert, @Nullable String type, @Nullable String budget, CollectionReference contributions, HashMap<String, Long> users, DocumentReference owner) {
        this(name, description, time, endTime, location, servings, appetizer, starter, main, dessert, type, budget, users, owner);
        this.id = id;
        this.contributions = contributions;
    }

    public Event(String name, @Nullable String description, long time, long endTime, @Nullable String location, int servings, boolean appetizer, boolean starter, boolean main, boolean dessert, @Nullable String type, @Nullable String budget, HashMap<String, Long> users, DocumentReference owner) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.endTime = endTime;
        this.location = location;
        this.servings = servings;
        this.type = type;
        this.budget = budget;
        this.users = users;
        this.owner = owner;
        this.courses = new HashMap<>();
        this.courses.put(Contribution.CONTRIBUTION_TYPE_APPETIZER, appetizer);
        this.courses.put(Contribution.CONTRIBUTION_TYPE_STARTER, starter);
        this.courses.put(Contribution.CONTRIBUTION_TYPE_MAIN, main);
        this.courses.put(Contribution.CONTRIBUTION_TYPE_DESSERT, dessert);
    }

    public static Event fromDocument(@NonNull DocumentSnapshot documentSnapshot) {
        Event event = documentSnapshot.toObject(Event.class);
        event.id = documentSnapshot.getId();
        return event;
    }

    public void addUser(String userId) {
        if (!users.containsKey(userId))
            users.put(userId, time);
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

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
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
        return courses.get(Contribution.CONTRIBUTION_TYPE_APPETIZER) != null && courses.get(Contribution.CONTRIBUTION_TYPE_APPETIZER) ;
    }

    public void setAppetizer(boolean appetizer) {
        this.courses.put(Contribution.CONTRIBUTION_TYPE_APPETIZER, appetizer);
    }

    public boolean hasStarter() {
        return courses.get(Contribution.CONTRIBUTION_TYPE_STARTER) != null && courses.get(Contribution.CONTRIBUTION_TYPE_STARTER) ;
    }

    public void setStarter(boolean starter) {
        this.courses.put(Contribution.CONTRIBUTION_TYPE_STARTER, starter);
    }

    public boolean hasMain() {
        return courses.get(Contribution.CONTRIBUTION_TYPE_MAIN) != null && courses.get(Contribution.CONTRIBUTION_TYPE_MAIN) ;
    }

    public void setMain(boolean main) {
        this.courses.put(Contribution.CONTRIBUTION_TYPE_MAIN, main);
    }

    public boolean hasDessert() {
        return courses.get(Contribution.CONTRIBUTION_TYPE_DESSERT) != null && courses.get(Contribution.CONTRIBUTION_TYPE_DESSERT) ;
    }

    public void setDessert(boolean dessert) {
        this.courses.put(Contribution.CONTRIBUTION_TYPE_DESSERT, dessert);
    }

    @PropertyName(value = "courses")
    public HashMap<String, Boolean> getCourses() {
        return courses;
    }

    @PropertyName(value = "courses")
    public void setCourses(HashMap<String, Boolean> courses) {
        this.courses = courses;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getBudget() {
        return budget;
    }

    public void setBudget(@Nullable String budget) {
        this.budget = budget;
    }

    public CollectionReference getContributions() {
        return contributions;
    }

    public void setContributions(CollectionReference contributions) {
        this.contributions = contributions;
    }

    public HashMap<String, Long> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Long> users) {
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
                ",\n courses=" + courses +
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
        courses = (HashMap) in.readValue(HashMap.class.getClassLoader());
        type = in.readString();
        budget = in.readString();
        users = (HashMap) in.readValue(HashMap.class.getClassLoader());
        owner = FirestoreManager.getUserReferenceForId(in.readString());
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
        dest.writeValue(courses);
        dest.writeString(type);
        dest.writeString(budget);
        dest.writeValue(users);
        dest.writeString(owner != null ? owner.getId() : null);
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

    @Override
    public int compareTo(@NonNull Event o) {

        long compareDate = o.getTime();

        //ascending order
        return (int) (this.time - compareDate);
    }
}