package name.juhasz.judit.udacity.tanits.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import name.juhasz.judit.udacity.tanits.Message;
import name.juhasz.judit.udacity.tanits.UserProfile;

public class FirebaseUtils {
    private static String TAG = FirebaseUtils.class.getSimpleName();

    public interface StringListener {
        void onReceive(final String string);
        void onCancelled(@NonNull DatabaseError databaseError);
    }

    public interface UserProfileListener {
        void onReceive(final UserProfile userProfile);
        void onCancelled(@NonNull DatabaseError databaseError);
    }

    public interface MessageStatusListener {
        void onReceive(final Map<String, Integer> messageIdToStatus);
        void onCancelled(@NonNull DatabaseError databaseError);
    }

    public static void saveUserProfile(@NonNull UserProfile profile) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("profiles/" + currentFirebaseUser.getUid()).setValue(profile);
    }

    public static void saveMessageStatus(@NonNull String messageId, final int messageStatus) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String status;
        switch (messageStatus) {
            case Message.STATUS_ACTIVE:
                // No status means active
                return;
            case Message.STATUS_DONE:
                status = "done";
                break;
            case Message.STATUS_REJECTED:
                status = "rejected";
                break;
            default:
                Log.e(TAG, "Internal error: unknown message status: " + messageStatus);
                return;
        }
        database.getReference("messageStatus/" +  currentFirebaseUser.getUid() +
                "/" + messageId + "/status").setValue(status);
    }

    public static void queryUserProfile(@NonNull final UserProfileListener userProfileListener) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("profiles/" + currentFirebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final UserProfile user = dataSnapshot.getValue(UserProfile.class);
                        userProfileListener.onReceive(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        userProfileListener.onCancelled(databaseError);
                    }
                });
    }

    public static void queryMessageContent(@NonNull final String messageId,
                                           @NonNull final StringListener stringListener) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("messageDetail/" + messageId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String content = dataSnapshot.child("content").getValue(String.class);
                        stringListener.onReceive(content);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        stringListener.onCancelled(databaseError);
                    }
                });
    }

    public static void queryMessageStatus(@NonNull final int messageStatus,
                                          @NonNull final MessageStatusListener messageStatusListener) {
        final Query query = getMessageStatusQuery(messageStatus);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Map<String, Integer> messageIdToStatus = new HashMap<>();
                for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    final String messageId = messageSnapshot.getKey();
                    final String status = messageSnapshot.child("status").getValue(String.class);
                    messageIdToStatus.put(messageId, firebaseMessageStatusToClientMessageStatus(status));
                }
                messageStatusListener.onReceive(messageIdToStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                messageStatusListener.onCancelled(databaseError);
            }
        });
    }

    private static Query getMessageStatusQuery(@NonNull final int messageStatus) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference("messageStatus/" + currentFirebaseUser.getUid());
        switch (messageStatus) {
            case Message.STATUS_ACTIVE:
                // Active messages are the not-done, not-rejected messages, e.g. the user
                // need to query all the message status data in order to evaluate that
                // a messages is not in this list
                break;
            case Message.STATUS_DONE:
            case Message.STATUS_REJECTED: {
                final String status = clientMessageStatusToFirebaseMessageStatus(messageStatus);
                query = query.orderByChild("status").startAt(status).endAt(status);
                break;
            }
            default:
                Log.e(TAG, "Internal error: unknown message status: " + messageStatus);
        }
        return query;
    }

    private static String clientMessageStatusToFirebaseMessageStatus(@NonNull final int messageStatus) {
        switch (messageStatus) {
            case Message.STATUS_ACTIVE:
                // Active messages are the not-done, not-rejected messages, e.g. the user
                // need to query all the message status data in order to evaluate that
                // a messages is not in this list
                // No status means active, there is no label attached to this state
                return null;
            case Message.STATUS_DONE:
                return "done";
            case Message.STATUS_REJECTED:
                return "rejected";
            default:
                Log.e(TAG, "Internal error: unknown message status: " + messageStatus);
                return null;
        }
    }

    private static int firebaseMessageStatusToClientMessageStatus(final String status) {
        if (status.equals("active")) {
            return Message.STATUS_ACTIVE;
        } else if (status.equals("done")) {
            return Message.STATUS_DONE;
        } else if (status.equals("rejected")) {
            return Message.STATUS_REJECTED;
        } else {
            Log.e(TAG, "Internal error: unknown message status: " + status);
            return Message.STATUS_ACTIVE; // Fallback
        }
    }
}
