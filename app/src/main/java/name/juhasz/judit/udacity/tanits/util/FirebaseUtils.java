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

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.juhasz.judit.udacity.tanits.Message;
import name.juhasz.judit.udacity.tanits.UserProfile;

public class FirebaseUtils {
    private static String TAG = FirebaseUtils.class.getSimpleName();

    public static final int MESSAGE_STATUS_FILTER_ALL = 0;
    public static final int MESSAGE_STATUS_FILTER_ACTIVE = 1;
    public static final int MESSAGE_STATUS_FILTER_DONE = 2;
    public static final int MESSAGE_STATUS_FILTER_REJECTED = 3;

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

    public interface MessageListListener {
        void onReceive(final List<Message> messageList);

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
        database.getReference("messageStatus/" + currentFirebaseUser.getUid() +
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

    public static void queryMessages(@NonNull final LocalDate childBirthdate,
                                     @NonNull final int messageStatusFilter,
                                     @NonNull final MessageListListener messageListListener) {
        int messageQueryType;
        switch (messageStatusFilter) {
            case MESSAGE_STATUS_FILTER_ALL:
            case MESSAGE_STATUS_FILTER_ACTIVE:
                messageQueryType = Message.STATUS_ACTIVE;
                break;
            case MESSAGE_STATUS_FILTER_DONE:
                messageQueryType = Message.STATUS_DONE;
                break;
            case MESSAGE_STATUS_FILTER_REJECTED:
                messageQueryType = Message.STATUS_REJECTED;
                break;
            default:
                Log.e(TAG, "Internal error: unknown message status filter: " + messageStatusFilter);
                return;
        }
        FirebaseUtils.queryMessageStatus(messageQueryType, new FirebaseUtils.MessageStatusListener() {
            @Override
            public void onReceive(Map<String, Integer> messageIdToStatus) {
                FirebaseUtils.queryMessages(childBirthdate, messageStatusFilter, messageIdToStatus,
                        messageListListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                messageListListener.onCancelled(databaseError);
            }
        });
    }

    private static void queryMessages(@NonNull final LocalDate childBirthdate,
                                      @NonNull final int messageStatusFilter,
                                      @NonNull final Map<String, Integer> messageIdToStatus,
                                      @NonNull final MessageListListener messageListListener) {
        final LocalDate currentDate = new LocalDate();
        final int childAgeInDays = Days.daysBetween(childBirthdate, currentDate).getDays();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("messageExtract").orderByChild("dayOffset").startAt(0).endAt(childAgeInDays)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final ArrayList<Message> messages = new ArrayList<>();
                        for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            final String messageId = messageSnapshot.getKey();
                            final int dayOffset = messageSnapshot.child("dayOffset").getValue(Integer.class);
                            final String messageDate = childBirthdate.plusDays(dayOffset).toString();
                            final String subject = messageSnapshot.child("subject").getValue(String.class);
                            switch (messageStatusFilter) {
                                case MESSAGE_STATUS_FILTER_ALL: {
                                    final int messageStatus = messageIdToStatus.containsKey(messageId) ? messageIdToStatus.get(messageId) : Message.STATUS_ACTIVE;
                                    messages.add(new Message(messageId, subject, messageDate, messageStatus));
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_ACTIVE: {
                                    if (!messageIdToStatus.containsKey(messageId)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_ACTIVE));
                                    }
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_DONE: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals(Message.STATUS_DONE)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_DONE));
                                    }
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_REJECTED: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals(Message.STATUS_REJECTED)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_REJECTED));
                                    }
                                    break;
                                }
                                default:
                                    Log.w(TAG, "Error: Unknown message status filter: " + messageStatusFilter);
                                    continue;
                            }
                        }
                        messageListListener.onReceive(messages);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        messageListListener.onCancelled(databaseError);
                    }
                });
    }
}
