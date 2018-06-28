package net.ouftech.whobringswhat.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    public static void init() {
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();

        db.setFirestoreSettings(settings);
    }

    public static void test() {
        //testFetch();
        //testFetchEventById();
        //testAddUser();
        //testAddEvent();
        testAddContribution();
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
        fetchUserByEmail("apu+user1@ouftech.net", new UserQueryListener() {
            @Override
            public void onSuccess(@Nullable User user) {
                if (user != null) {
                    Logger.d(getLogTag(), user.toString());

                    fetchEventsForUser(user, new EventsQueryListener() {
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

    public static void fetchUserByEmail(String emailAddress, @NonNull UserQueryListener listener) {
        db.collection(USERS_COLLECTIONS_NAME)
                .document(emailAddress)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocumentSnapshot = userTask.getResult();

                        User user = User.fromDocument(userDocumentSnapshot);
                        listener.onSuccess(user);

                    } else {
                        Logger.w(getLogTag(), "Error getting user document: ", userTask.getException());
                        listener.onSuccess(null);
                    }

                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void fetchEventById(String id, @NonNull EventQueryListener listener) {
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
                        listener.onSuccess(null);
                    }

                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void fetchEventsForUser(@NonNull User user, @NonNull EventsQueryListener listener) {
        db.collection(EVENTS_COLLECTIONS_NAME)
                .whereGreaterThan(Event.USERS_FIELD + "." + user.getFirebaseAuthIdToken(), 0)
                .orderBy(Event.USERS_FIELD + "." + user.getFirebaseAuthIdToken())
                .get()
                .addOnCompleteListener(eventsTask -> {
                    List<Event> eventList = null;
                    if (eventsTask.isSuccessful()) {

                        QuerySnapshot eventsQuerySnapshot = eventsTask.getResult();

                        eventList = new ArrayList<>();

                        for (DocumentSnapshot eventsDocumentSnapshot : eventsQuerySnapshot) {
                            Event event = Event.fromDocument(eventsDocumentSnapshot);
                            eventList.add(event);
                        }

                    } else {
                        Logger.w(getLogTag(), "Error getting events documents: ", eventsTask.getException());
                    }

                    listener.onSuccess(eventList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void fetchContributionsForEvent(@NonNull Event event, @NonNull ContributionsQueryListener listener) {
        db.collection(EVENTS_COLLECTIONS_NAME + "/" + event.getId() + "/" + CONTRIBUTIONS_COLLECTIONS_NAME).get()
                .addOnCompleteListener(contributionsTask -> {
                    List<Contribution> contributionsList = null;

                    if (contributionsTask.isSuccessful()) {
                        QuerySnapshot contributionsQuerySnapshot = contributionsTask.getResult();

                        contributionsList = new ArrayList<>();

                        for (DocumentSnapshot contributionsDocumentSnapshot : contributionsQuerySnapshot) {
                            Contribution contribution = Contribution.fromDocument(contributionsDocumentSnapshot);
                            contributionsList.add(contribution);
                        }

                    } else {
                        Logger.w(getLogTag(), "Error getting events documents: ", contributionsTask.getException());
                    }

                    listener.onSuccess(contributionsList);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void saveUser(@NonNull User user, @NonNull AddListener listener) {
        db.collection(USERS_COLLECTIONS_NAME).document(user.getEmailAddress())
                .set(user)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

    public static void addEvent(@NonNull Event event, @NonNull AddListener listener) {
        DocumentReference documentReference = db.collection(EVENTS_COLLECTIONS_NAME).document();
        event.setId(documentReference.getId());

        documentReference
                .set(event)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

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

    public interface QueryListener {
        void onFailure(Exception e);
    }

    public interface UserQueryListener extends QueryListener {
        void onSuccess(@Nullable User user);
    }

    public interface EventQueryListener extends QueryListener {
        void onSuccess(@Nullable Event event);
    }

    public interface EventsQueryListener extends QueryListener {
        void onSuccess(@Nullable List<Event> events);
    }

    public interface ContributionsQueryListener extends QueryListener {
        void onSuccess(@Nullable List<Contribution> contributions);
    }

    public interface AddListener extends QueryListener {
        void onSuccess(Void aVoid);
    }

    public static String getLogTag() {
        return "FirestoreManager";
    }
}
