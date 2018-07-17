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

import net.ouftech.whobringswhat.commons.CollectionUtils;
import net.ouftech.whobringswhat.commons.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                                listener.onSuccess(snapshot.getValue(User.class));
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
                                userId = snapshot.getKey();

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

    /**
     * Remove the given event from the current user's events list
     *
     * @param event Event to remove
     * @param listener Query Listener for success and failure callbacks
     */
    public static void removeCurrentUserFromEvent(Event event, FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (event.getId() == null) {
                    listener.onFailure(new NullPointerException(String.format("Id of event %s is null. Cannot update", event)));
                } else {
                    currentUser.removeEvent(event.getId());
                    saveUser(currentUser, new FirestoreManager.UserQueryListener() {
                        @Override
                        public void onSuccess(@NonNull User user) {
                            Logger.d(getLogTag(), String.format("Event %s removed from user %s", event, userId));
                            currentUser = user;
                            listener.onSuccess(null);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.w(getLogTag(), String.format("Error while removing event %s from user %s", event, userId), e, false);
                            listener.onFailure(e);
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

    /**
     * Adds the given event to the current user's events list
     *
     * @param event Event to add
     * @param listener Query Listener for success and failure callbacks
     */
    public static void addCurrentUserToEvent(Event event, FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (event.getId() == null) {
                    listener.onFailure(new NullPointerException(String.format("Id of event %s is null. Cannot update", event)));
                } else {
                    currentUser.addEvent(event.getId());
                    saveUser(currentUser, new FirestoreManager.UserQueryListener() {
                        @Override
                        public void onSuccess(@NonNull User user) {
                            Logger.d(getLogTag(), String.format("Event %s added to user %s", event, userId));
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.w(getLogTag(), String.format("Error while adding event %s to user %s", event, userId), e);
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

    public static void logout() {
        Logger.d(getLogTag(), "Setting currentUser to null");
        currentUser = null;
    }

    // endregion Users


    // region Events

    /**
     * Fetches a {@link Event} item from Firestore using its id<br/>
     *
     * @param id       id of the {@link Event} document to fetch
     * @param listener Query Listener for success and failure callbacks
     */
    public static void fetchEventById(@NonNull String id, @NonNull FirestoreManager.EventQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                eventsRef.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            listener.onSuccess(dataSnapshot.getValue(Event.class));
                        else
                            listener.onFailure(new NullPointerException(String.format("Event %s could not be found", id)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Logger.w(getLogTag(), "Error getting Event document: ", databaseError.toException(), false);
                        listener.onFailure(databaseError.toException());
                    }
                });
                return null;
            }
        }.execute();
    }

    /**
     * Fetches the list of events for the given {@link User} object.<br/>
     * Events are ordered by start date ({@link Event#time})
     *
     * @param user     {@link User} object from which to get the events
     * @param listener Query Listener for success and failure callbacks
     */
    public static void fetchEventsForUser(@NonNull User user, @NonNull FirestoreManager.EventsQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {

            List<Event> events = new ArrayList<>();
            List<String> eventIds = user.getEvents();
            int eventsFetched = 0;
            int totalEvents;

            @Override
            protected Void doInBackground(Void... voids) {

                if (CollectionUtils.isEmpty(eventIds)) {
                    Logger.d(getLogTag(), "User doesn't have any event");
                    listener.onSuccess(events);
                } else {
                    totalEvents = eventIds.size();
                    for (String eventId : eventIds) {
                        eventsRef.child(eventId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                addEvent(dataSnapshot.getValue(Event.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Logger.w(getLogTag(), "Error getting Event document: ", databaseError.toException(), false);
                                addEvent(null);
                            }
                        });
                    }
                }
                return null;
            }

            private synchronized void addEvent(Event event) {
                if (event != null)
                    events.add(event);

                eventsFetched++;

                if (eventsFetched == totalEvents) {
                    Collections.sort(events);
                    listener.onSuccess(events);
                }
            }
        }.execute();
    }

    /**
     * Fetches the list of events for the currently logged in user {@link User} object.<br/>
     * Events are ordered by start date ({@link Event#time})
     *
     * @param listener Query Listener for success and failure callbacks
     */
    public static void fetchEventsForCurrentUser(@NonNull FirestoreManager.EventsQueryListener listener) {
        if (currentUser != null)
            fetchEventsForUser(currentUser, listener);
        else
            listener.onFailure(new IllegalStateException("No logged in user"));
    }

    /**
     * Creates a {@link Event} document on Firestore.<br/>
     *
     * @param event    {@link Event} to save as a Firestore document
     * @param listener Query Listener for success and failure callbacks
     */
    public static void addEvent(@NonNull Event event, @NonNull FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String eventId = eventsRef.push().getKey();
                event.setId(eventId);
                event.setOwner(userId);
                eventsRef.child(eventId).setValue(event)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Logger.d(getLogTag(), String.format("Event %s created", event));
                                listener.onSuccess(aVoid);
                                currentUser.addEvent(eventId);
                                saveUser(currentUser, new FirestoreManager.UserQueryListener() {
                                    @Override
                                    public void onSuccess(@NonNull User user) {
                                        Logger.d(getLogTag(), String.format("Event %s added to user %s", event, userId));
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Logger.w(getLogTag(), String.format("Error while adding event %s to user %s", event, userId), e);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onFailure(e);
                            }
                        });
                return null;
            }
        }.execute();
    }

    /**
     * Updates the  {@link Event} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link Event} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param event    {@link Event} to save as a Firestore document
     * @param listener Query Listener for success and failure callbacks
     */
    public static void updateEvent(@NonNull Event event, @NonNull FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (event.getId() == null) {
                    listener.onFailure(new NullPointerException(String.format("Id of event %s is null. Cannot update", event)));
                } else {
                    eventsRef.child(event.getId()).setValue(event)
                            .addOnSuccessListener(listener::onSuccess)
                            .addOnFailureListener(listener::onFailure);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Deletes the  {@link Event} document from Firestore.<br/>
     * Calling this method will remove anything stored on Firestore for that document.
     *
     * @param event    {@link Event} to delete from Firestore
     * @param listener Query Listener for success and failure callbacks
     */
    public static void deleteEvent(@NonNull Event event, @NonNull FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if (event.getId() == null) {
                    listener.onFailure(new NullPointerException(String.format("Id of event %s is null. Cannot delete", event)));
                } else {
                    eventsRef.child(event.getId()).removeValue()
                            .addOnSuccessListener(listener::onSuccess)
                            .addOnFailureListener(listener::onFailure);
                }
                return null;
            }
        }.execute();
    }

    // endregion Events


    // region Contribution


    /**
     * Saves {@link Contribution} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link Contribution} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param event        {@link Event} in which to save the Contribution
     * @param contribution {@link Contribution} to save as a Firestore document
     * @param listener     Query Listener for success and failure callbacks
     */
    public static void addContribution(@NonNull Event event, @NonNull Contribution contribution, @NonNull FirestoreManager.SimpleQueryListener listener) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                event.addContribution(contribution);
                updateEvent(event, listener);
                return null;
            }
        }.execute();
    }


    // endregion Contributions


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        RealTimeDBManager.currentUser = currentUser;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        RealTimeDBManager.userId = userId;
    }

    @NonNull
    private static String getLogTag() {
        return "RealTimeDBManager";
    }
}
