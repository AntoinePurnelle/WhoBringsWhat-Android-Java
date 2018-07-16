package net.ouftech.whobringswhat.model;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.ouftech.whobringswhat.commons.Logger;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class RealTimeDBManager {

    public static final String USERS_COLLECTIONS_NAME = "users";
    public static final String EVENTS_COLLECTIONS_NAME = "events";
    public static final String CONTRIBUTIONS_COLLECTIONS_NAME = "contributions";

    private static FirebaseDatabase db;
    private static DatabaseReference usersRef;
    private static DatabaseReference eventsRef;
    private static DatabaseReference contributionsRef;
    private static User currentUser;
    private static String userId;

    public static void init() {
        db = FirebaseDatabase.getInstance();
        usersRef = db.getReference(USERS_COLLECTIONS_NAME);
        eventsRef = db.getReference(EVENTS_COLLECTIONS_NAME);
        contributionsRef = db.getReference(CONTRIBUTIONS_COLLECTIONS_NAME);
    }


    // region Users

    /**
     * Fetches a {@link User} item from Firestore using its Firebase Auth id<br/>
     *
     * @param id       Firebase Auth id of the user ({@link FirebaseUser#getUid()})
     * @param listener Query listener used to return the fetched user
     */
    public static void fetchUserById(@NonNull String id, @NonNull FirestoreManager.UserQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                usersRef.orderByChild(User.FIREBASE_ID_FIELD).equalTo(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User exists and update not necessary
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                listener.onSuccess(currentUser);
                                return;
                            }
                        } else {
                            Logger.w(getLogTag(), "Error while fetching user", new NullPointerException(String.format("User %s could not be found", id)), false);
                            listener.onFailure(new NullPointerException(String.format("User %s could not be found", id)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Logger.w(getLogTag(), "Error getting user document: ", databaseError.toException(), false);
                        listener.onFailure(databaseError.toException());
                    }
                });
                return null;
            }
        }.execute();
    }

    /**
     * Creates, fetches or updates a {@link User} item using a {@link FirebaseUser} object.<br/>
     * If a {@link User} with the same id exists ({@link FirebaseUser#getUid()}), it will be fetched.<br/>
     * If the user is new, it will be created.<br/>
     *
     * @param firebaseUser {@link FirebaseUser} object retrieved from authentication used to create or fetch the {@link User} document
     */
    public static void initWithFirebaseUser(@NonNull FirebaseUser firebaseUser, @NonNull FirestoreManager.UserQueryListener userQueryListener, boolean update) {
        Logger.d(getLogTag(), String.format("Initializing with FirebaseUser %s", firebaseUser.getUid()));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {


                usersRef.orderByChild(User.FIREBASE_ID_FIELD).equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User exists and update not necessary
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // do something with the individual "issues"
                                currentUser = snapshot.getValue(User.class);

                                if (!update) {
                                    // Simply query. No update needed --> success
                                    userQueryListener.onSuccess(currentUser);
                                } else {
                                    // User exists but needs update
                                    userId = snapshot.getKey();
                                    saveUser(currentUser, userQueryListener);
                                }
                                return;
                            }
                        } else {
                            // User does not exist --> create
                            User user = User.fromFirebaseUser(firebaseUser);
                            userId = usersRef.push().getKey();
                            saveUser(user, userQueryListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Logger.w(getLogTag(), "Error getting user document: ", databaseError.toException(), false);
                        userQueryListener.onFailure(databaseError.toException());
                    }
                });
                return null;
            }
        }.execute();
    }

    /**
     * Saves {@link User} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link User} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param user              {@link User} to save as a Firestore document
     * @param userQueryListener Query Listener for success and failure callbacks
     */
    public static void saveUser(@NonNull User user, @NonNull FirestoreManager.UserQueryListener userQueryListener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                usersRef.child(userId).setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                currentUser = user;
                                Logger.d(getLogTag(), String.format("User created: %s", user));
                                userQueryListener.onSuccess(currentUser);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Logger.w(getLogTag(), "Error creating the User on Firestore", e, false);
                                userQueryListener.onFailure(e);
                            }
                        });
                return null;
            }
        }.execute();
    }

    public static void logout() {
        Logger.d(getLogTag(), "Setting currentUser to null");
        currentUser = null;
    }

    // endregion Users


    @NonNull
    private static String getLogTag() {
        return "RealTimeDBManager";
    }
}
