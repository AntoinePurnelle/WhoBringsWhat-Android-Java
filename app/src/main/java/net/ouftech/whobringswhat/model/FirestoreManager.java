package net.ouftech.whobringswhat.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import net.ouftech.whobringswhat.commons.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    public static final String USERS_COLLECTIONS_NAME = "users";
    public static final String EVENTS_COLLECTIONS_NAME = "events";
    public static final String CONTRIBUTIONS_COLLECTIONS_NAME = "contributions";

    private static FirebaseFirestore db;
    private static User currentUser;

    public static void init() {
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();

        db.setFirestoreSettings(settings);
    }


    // region Users

    /**
     * Fetches a {@link User} item from Firestore using its Firebase Auth id<br/>
     *
     * @param id       Firebase Auth id of the user ({@link FirebaseUser#getUid()})
     * @param listener Query listener used to return the fetched user
     */
    public static void fetchUserById(@NonNull String id, @NonNull UserQueryListener listener) {
        db.collection(USERS_COLLECTIONS_NAME)
                .document(id)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocumentSnapshot = userTask.getResult();
                        User user = User.fromDocument(userDocumentSnapshot);

                        listener.onSuccess(user);

                    } else {
                        Logger.w(getLogTag(), "Error getting user document: ", userTask.getException());
                        listener.onFailure(userTask.getException());
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Creates or fetches a {@link User} item using a {@link FirebaseUser} object.<br/>
     * If a {@link User} with the same id exists ({@link FirebaseUser#getUid()}), it will be fetched.<br/>
     * If the user is new, it will be created.<br/>
     *
     * @param firebaseUser {@link FirebaseUser} object retrieved from authentication used to create or fetch the {@link User} document
     */
    public static void initWithFirebaseUser(@NonNull FirebaseUser firebaseUser, UserQueryListener userQueryListener) {
        Logger.d(getLogTag(), String.format("Initializing with FirebaseUser %s", firebaseUser.getUid()));

        // Try to fetch the user
        db.collection(USERS_COLLECTIONS_NAME)
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocumentSnapshot = userTask.getResult();

                        if (userDocumentSnapshot.exists()) {
                            // If user from Firebase exists, save it
                            currentUser = User.fromDocument(userDocumentSnapshot);
                            Logger.d(getLogTag(), String.format("User fetched: %s", currentUser));
                            userQueryListener.onSuccess(currentUser);
                        } else {
                            // If user doesn't exist, create it
                            User user = User.fromFirebaseUser(firebaseUser);
                            saveUser(user, new AddListener() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentUser = user;
                                    Logger.d(getLogTag(), String.format("User created: %s", user));
                                    userQueryListener.onSuccess(currentUser);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Logger.w(getLogTag(), "Error creating the User on Firestore", e, false);
                                    userQueryListener.onFailure(e);
                                }
                            });
                        }

                    } else {
                        Logger.w(getLogTag(), "Error getting user document: ", userTask.getException(), false);
                        userQueryListener.onFailure(userTask.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.w(getLogTag(), "Error getting user document: ", e, false);
                    userQueryListener.onFailure(e);
                });
    }

    /**
     * Saves {@link User} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link User} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param user     {@link User} to save as a Firestore document
     * @param listener Query Listener for success and failure callbacks
     */
    public static void saveUser(@NonNull User user, @NonNull AddListener listener) {
        db.collection(USERS_COLLECTIONS_NAME).document(user.getFirebaseId())
                .set(user)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
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
    public static void fetchEventById(@NonNull String id, @NonNull EventQueryListener listener) {
        db.collection(EVENTS_COLLECTIONS_NAME)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        Event event = Event.fromDocument(documentSnapshot);
                        listener.onSuccess(event);

                    } else {
                        Logger.w(getLogTag(), "Error getting event document: ", task.getException());
                        listener.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Fetches the list of events for the given {@link User} object.<br/>
     * Events are ordered by start date ({@link Event#time})
     *
     * @param userId     id {@link User} object from which to get the events
     * @param listener Query Listener for success and failure callbacks
     */
    public static void fetchEventsForUser(@NonNull String userId, @NonNull EventsQueryListener listener) {
        /*
        The link between a user and an event is in the event.
        The event has a map for which the keys are the user ids and the values are the event start date.
        See https://firebase.google.com/docs/firestore/solutions/arrays
         */
        db.collection(EVENTS_COLLECTIONS_NAME)
                .whereGreaterThan(Event.USERS_FIELD + "." + userId, 0)
                .orderBy(Event.USERS_FIELD + "." + userId)
                .get()
                .addOnCompleteListener(eventsTask -> {
                    if (eventsTask.isSuccessful()) {
                        QuerySnapshot eventsQuerySnapshot = eventsTask.getResult();
                        List<Event> eventList = new ArrayList<>();

                        for (DocumentSnapshot eventsDocumentSnapshot : eventsQuerySnapshot) {
                            Event event = Event.fromDocument(eventsDocumentSnapshot);
                            eventList.add(event);
                        }

                        listener.onSuccess(eventList);
                    } else {
                        Logger.w(getLogTag(), "Error getting events documents: ", eventsTask.getException());
                        listener.onFailure(eventsTask.getException());
                    }

                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Fetches the list of events for the currently logged in user {@link User} object.<br/>
     * Events are ordered by start date ({@link Event#time})
     *
     * @param listener Query Listener for success and failure callbacks
     */
    public static void fetchEventsForCurrentUser(@NonNull EventsQueryListener listener) {
        if (currentUser != null)
            fetchEventsForUser(currentUser.getFirebaseId(), listener);
        else
            listener.onFailure(new IllegalStateException("No logged in user"));
    }

    /**
     * Saves {@link Event} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link Event} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param event    {@link Event} to save as a Firestore document
     * @param listener Query Listener for success and failure callbacks
     */
    public static void addEvent(@NonNull Event event, @NonNull AddListener listener) {
        DocumentReference documentReference = db.collection(EVENTS_COLLECTIONS_NAME).document();
        event.setId(documentReference.getId());

        documentReference
                .set(event)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

    // endregion Events


    // region Contributions

    /**
     * Fetches the {@link Contribution} objects of the "contributions" collection in the given {@link Event}<br/>
     *
     * @param event
     * @param listener
     */
    public static void fetchContributionsForEvent(@NonNull Event event, @NonNull ContributionsQueryListener listener) {
        // fetch the /events/[EVENT_ID]/contributions collection
        db.collection(EVENTS_COLLECTIONS_NAME + "/" + event.getId() + "/" + CONTRIBUTIONS_COLLECTIONS_NAME).get()
                .addOnCompleteListener(contributionsTask -> {
                    if (contributionsTask.isSuccessful()) {
                        QuerySnapshot contributionsQuerySnapshot = contributionsTask.getResult();
                        List<Contribution> contributionsList = new ArrayList<>();

                        for (DocumentSnapshot contributionsDocumentSnapshot : contributionsQuerySnapshot) {
                            Contribution contribution = Contribution.fromDocument(contributionsDocumentSnapshot);
                            contributionsList.add(contribution);
                        }
                        listener.onSuccess(contributionsList);
                    } else {
                        Logger.w(getLogTag(), "Error getting events documents: ", contributionsTask.getException());
                        listener.onFailure(contributionsTask.getException());
                    }

                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Saves {@link Contribution} document to Firestore.<br/>
     * CAUTION: This method will save every field of the {@link Contribution} object to save them in Firestore, even the NULL ones!<br/>
     * Calling this method will replace anything stored on Firestore for that document.
     *
     * @param event        {@link Event} in which to save the Contribution
     * @param contribution {@link Contribution} to save as a Firestore document
     * @param listener     Query Listener for success and failure callbacks
     */
    public static void addContribution(@NonNull Event event, @NonNull Contribution contribution, @NonNull AddListener listener) {
        DocumentReference documentReference = db
                .collection(EVENTS_COLLECTIONS_NAME).document(event.getId())
                .collection(CONTRIBUTIONS_COLLECTIONS_NAME).document();
        contribution.setId(documentReference.getId());

        documentReference
                .set(contribution)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

    // endregion Contributions


    // region Listener Interfaces

    public interface QueryListener {
        void onFailure(Exception e);
    }

    public interface UserQueryListener extends QueryListener {
        void onSuccess(@NonNull User user);
    }

    public interface EventQueryListener extends QueryListener {
        void onSuccess(@NonNull Event event);
    }

    public interface EventsQueryListener extends QueryListener {
        void onSuccess(@NonNull List<Event> events);
    }

    public interface ContributionsQueryListener extends QueryListener {
        void onSuccess(@NonNull List<Contribution> contributions);
    }

    public interface AddListener extends QueryListener {
        void onSuccess(Void aVoid);
    }

    // endregion Listener Interfaces


    // region test methods

    public static void test() {
        testFetch();
        //testFetchEventById();
        //testAddUser();
        //testAddEvent();
        //testAddContribution();
    }

    public static void testAddUser() {
        User user = new User("User2", "apu+user2@ouftech.net", "2222");
        saveUser(user, new AddListener() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.d(getLogTag(), "User created with success!");
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), "Error while creating user", e);
            }
        });
    }

    public static void testAddEvent() {
        long date = new Date().getTime();
        Map<String, Long> users = new HashMap<>();
        users.put("2222", date);
        Event event = new Event(
                "Event 3",
                "Description Event 3",
                date, date + 1000,
                "Event 3 location",
                100,
                true, true, true, true,
                "Barbecue",
                15, "$",
                users,
                db.collection(USERS_COLLECTIONS_NAME).document("apu+user2@ouftech.net")
        );

        addEvent(event, new AddListener() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.d(getLogTag(), "Event created with success!");
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), "Error while creating event", e);
            }
        });
    }

    public static void testFetchEventById() {
        fetchEventById("IMQbaBnwLbCLt9tEKwYG", new EventQueryListener() {
            @Override
            public void onSuccess(@Nullable Event event) {
                Logger.d(getLogTag(), event.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), "Error while adding event", e);
            }
        });
    }


    public static void testAddContribution() {
        fetchEventById("YiOGGEI2xY2PElnZZstg", new EventQueryListener() {
            @Override
            public void onSuccess(@Nullable Event event) {
                Logger.d(getLogTag(), event.toString());

                Contribution contribution = new Contribution(
                        "Contrib 1 E3",
                        11, 11, "plates",
                        "main",
                        "Comment C1E3",
                        db.collection(USERS_COLLECTIONS_NAME).document("apu+user2@ouftech.net")
                );

                addContribution(event, contribution, new AddListener() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logger.d(getLogTag(), contribution.toString());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.e(getLogTag(), "Error while adding contribution", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), "Error while adding event", e);
            }
        });
    }

    public static void testFetch() {
        fetchUserById("yfXkVS4B7mezZVMqqkruWHAf9Js2", new UserQueryListener() {
            @Override
            public void onSuccess(@Nullable User user) {
                if (user != null) {
                    Logger.d(getLogTag(), user.toString());

                    fetchEventsForUser(user.getFirebaseId(), new EventsQueryListener() {
                        @Override
                        public void onSuccess(@Nullable List<Event> events) {
                            if (events != null) {
                                Logger.d(getLogTag(), Arrays.toString(events.toArray()));

                                fetchContributionsForEvent(events.get(1), new ContributionsQueryListener() {
                                    @Override
                                    public void onSuccess(@Nullable List<Contribution> contributions) {
                                        Logger.d(getLogTag(), Arrays.toString(contributions.toArray()));
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Logger.e(getLogTag(), "Error while fetching contributions", e);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.e(getLogTag(), "Error while fetching events", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Logger.e(getLogTag(), "Error while fetching user", e);
            }
        });
    }

    // endregion Test methods


    public static String getLogTag() {
        return "FirestoreManager";
    }
}
