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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class Event {

    public static final String USERS_FIELD = "users";

    private String id;
    private String name;
    @Nullable
    private String description;
    private long time;
    @Nullable
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
    private float budget;
    @Nullable
    private String budgetCurrency;
    private CollectionReference contributions;
    private Map<String, Long> users;
    private DocumentReference owner; // User

    public Event() {

    }

    public Event(String id, String name, @Nullable String description, long time, long endTime, @Nullable String location, int servings, boolean appetizer, boolean starter, boolean main, boolean dessert, @Nullable String type, float budget, @Nullable String budgetCurrency, CollectionReference contributions, Map<String, Long> users, DocumentReference owner) {
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
        this.budgetCurrency = budgetCurrency;
        this.contributions = contributions;
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

    public boolean isAppetizer() {
        return appetizer;
    }

    public void setAppetizer(boolean appetizer) {
        this.appetizer = appetizer;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public boolean isDessert() {
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

    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    @Nullable
    public String getBudgetCurrency() {
        return budgetCurrency;
    }

    public void setBudgetCurrency(@Nullable String budgetCurrency) {
        this.budgetCurrency = budgetCurrency;
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
                ",\n budgetCurrency='" + budgetCurrency + '\'' +
                ",\n contributions=" + contributions +
                ",\n users=" + users +
                ",\n owner=" + (owner != null ? owner.getId() : "null") +
                "\n}";
    }
}
