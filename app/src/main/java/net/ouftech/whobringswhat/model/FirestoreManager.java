package net.ouftech.whobringswhat.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import net.ouftech.whobringswhat.commons.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirestoreManager {

    public static final String USERS_COLLECTIONS_NAME = "users";
    public static final String EVENTS_COLLECTIONS_NAME = "events";

    private static FirebaseFirestore db;

    public static void init() {
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();

        db.setFirestoreSettings(settings);
    }

    // TODO remove
    public static void test() {
        fetchUserByEmail("apu+user1@ouftech.net", user -> {
            if (user != null) {
                Logger.d(getLogTag(), user.toString());

                fetchEventsForUser(user, events -> {
                    if (events != null)
                        Logger.d(getLogTag(), Arrays.toString(events.toArray()));
                });
            }
        });
    }

    public static void fetchUserByEmail(String emailAddress, @NonNull UserQueryListener listener) {
        db.collection(USERS_COLLECTIONS_NAME)
                .whereEqualTo(User.EMAIL_ADDRESS_FIELD, emailAddress)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        QuerySnapshot userQuerySnapshot = userTask.getResult();

                        if (userQuerySnapshot.size() != 1) {
                            Logger.e(getLogTag(), new Exception(String.format("Wrong size of list of retrieved user (%s). Should be 1", userQuerySnapshot.size())));
                            listener.onUserFetched(null);
                        }

                        for (DocumentSnapshot userDocumentSnapshot : userQuerySnapshot) {

                            User user = User.fromDocument(userDocumentSnapshot);
                            listener.onUserFetched(user);
                        }

                    } else {
                        Logger.w(getLogTag(), "Error getting user documents: ", userTask.getException());
                        listener.onUserFetched(null);
                    }

                });
    }

    public static void fetchEventsForUser(@NonNull User user, @NonNull EventsQueryListener listener) {
        db.collection(EVENTS_COLLECTIONS_NAME)
                .whereEqualTo(Event.USERS_FIELD + "." + user.getId(), true)
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

                    listener.onEventsFetched(eventList);
                });
    }

    public interface UserQueryListener {
        void onUserFetched(@Nullable User user);
    }

    public interface EventsQueryListener {
        void onEventsFetched(@Nullable List<Event> events);
    }

    public static String getLogTag() {
        return "FirestoreManager";
    }
}
