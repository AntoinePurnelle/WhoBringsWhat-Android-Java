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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class User {

    public static final String EMAIL_ADDRESS_FIELD = "emailAddress";
    public static final String EVENTS_FIELD = "events";

    private String id;
    private String name;
    private String emailAddress;
    private String firebaseAuthIdToken;

    public User(){}

    public User(String id, String name, String emailAddress, String firebaseAuthIdToken) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.firebaseAuthIdToken = firebaseAuthIdToken;
    }

    public User(String id, String name, String emailAddress, String firebaseAuthIdToken, List<DocumentReference> events, List<Event> eventList) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.firebaseAuthIdToken = firebaseAuthIdToken;
    }

    public static User fromDocument(DocumentSnapshot documentSnapshot) {
        User user = documentSnapshot.toObject(User.class);
        user.id = documentSnapshot.getId();
        return user;
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirebaseAuthIdToken() {
        return firebaseAuthIdToken;
    }

    public void setFirebaseAuthIdToken(String firebaseAuthIdToken) {
        this.firebaseAuthIdToken = firebaseAuthIdToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "\n id='" + id + '\'' +
                ",\n name='" + name + '\'' +
                ",\n emailAddress='" + emailAddress + '\'' +
                ",\n firebaseAuthIdToken='" + firebaseAuthIdToken + '\'' +
                "\n}";
    }
}
