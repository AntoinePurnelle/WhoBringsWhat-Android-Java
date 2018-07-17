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

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User extends BaseModel {

    public static final String EMAIL_ADDRESS_FIELD = "emailAddress";
    public static final String FIREBASE_ID_FIELD = "firebaseId";

    private String emailAddress;
    private String name;
    private String firebaseId;
    private List<String> events;

    public User(){
        super();
    }

    public User(String name, String emailAddress, String firebaseId, long creationDate) {
        this(name, emailAddress, firebaseId, creationDate, new ArrayList<>());
    }

    public User(String name, String emailAddress, String firebaseId, long creationDate, List<String> events) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.firebaseId = firebaseId;
        this.creationDate = creationDate;
        if (events == null)
            events = new ArrayList<>();
        this.events = events;
    }

    public static User fromDocument(@NonNull DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(User.class);
    }

    public static User fromFirebaseUser(@NonNull FirebaseUser firebaseUser) {
       return new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getUid(), firebaseUser.getMetadata().getCreationTimestamp());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public void addEvent(@NonNull Event event) {
        addEvent(event.getId());
    }

    public void addEvent(@NonNull String eventId) {
        if (events == null)
            events = new ArrayList<>();

        if (!events.contains(eventId))
            events.add(eventId);
    }

    public void removeEvent(@NonNull String eventId) {
        if (events == null)
            return;

        if (events.contains(eventId))
            events.remove(eventId);
    }

    @Override
    public String toString() {
        return "User{" +
                "\n emailAddress='" + emailAddress + '\'' +
                ",\n name='" + name + '\'' +
                ",\n firebaseId='" + firebaseId + '\'' +
                ",\n creationDate='" + creationDate + '\'' +
                ",\n events='" + (events != null ? Arrays.toString(events.toArray()) : "null") + '\'' +
                "\n}";
    }
}
