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

public class User {

    public static final String EMAIL_ADDRESS_FIELD = "emailAddress";

    private String emailAddress;
    private String name;
    private String firebaseId;

    public User(){}

    public User(String name, String emailAddress, String firebaseId) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.firebaseId = firebaseId;
    }

    public static User fromDocument(@NonNull DocumentSnapshot documentSnapshot) {
        return documentSnapshot.toObject(User.class);
    }

    public static User fromFirebaseUser(@NonNull FirebaseUser firebaseUser) {
       return new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getUid());
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

    @Override
    public String toString() {
        return "User{" +
                "\n emailAddress='" + emailAddress + '\'' +
                ",\n name='" + name + '\'' +
                ",\n firebaseId='" + firebaseId + '\'' +
                "\n}";
    }
}
