package name.juhasz.judit.udacity.tanits.util;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.juhasz.judit.udacity.tanits.Message;
import name.juhasz.judit.udacity.tanits.R;
import name.juhasz.judit.udacity.tanits.UserProfile;

public class FirebaseUtils {
    private static String TAG = FirebaseUtils.class.getSimpleName();

    public static final int MESSAGE_STATUS_FILTER_ALL = 0;
    public static final int MESSAGE_STATUS_FILTER_ACTIVE = 1;
    public static final int MESSAGE_STATUS_FILTER_DONE = 2;
    public static final int MESSAGE_STATUS_FILTER_REJECTED = 3;
    public static final String MESSAGE_STATUS_DONE = "done";
    public static final String MESSAGE_STATUS_ACTIVE = "active";
    public static final String MESSAGE_STATUS_REJECTED = "rejected";
    public static final String DATE_TODAY = "Today";
    public static final String DATE_YESTERDAY = "Yesterday";

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

    public static class ValueEventListenerDetacher {
        private Query mDatabaseNode;
        private ValueEventListener mValueEventListener;

        public ValueEventListenerDetacher(@NonNull final Query databaseNode,
                                          @NonNull final ValueEventListener valueEventListener) {
            this.mDatabaseNode = databaseNode;
            this.mValueEventListener = valueEventListener;
        }

        public void detach() {
            if (null != mDatabaseNode && null != mValueEventListener) {
                mDatabaseNode.removeEventListener(mValueEventListener);
                mDatabaseNode = null;
                mValueEventListener = null;
            }
        }
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
                status = MESSAGE_STATUS_DONE;
                break;
            case Message.STATUS_REJECTED:
                status = MESSAGE_STATUS_REJECTED;
                break;
            default:
                Log.e(TAG, Resources.getSystem().getString(R.string.log_unknown_message_status, messageStatus));
                return;
        }
        database.getReference("messageStatus/" + currentFirebaseUser.getUid() +
                "/" + messageId + "/status").setValue(status);
    }

    public static ValueEventListenerDetacher queryUserProfile(@NonNull final UserProfileListener userProfileListener,
                                                              @NonNull final boolean attachQueryToDatabase) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (null == currentFirebaseUser) {
            return null;
        }
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final UserProfile user = dataSnapshot.getValue(UserProfile.class);
                final String displayName = currentFirebaseUser.getDisplayName();
                if (null != user && null == user.getName() && null != displayName) {
                    user.setName(displayName);
                }
                userProfileListener.onReceive(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userProfileListener.onCancelled(databaseError);
            }
        };
        final DatabaseReference databaseNode = database.getReference("profiles/" + currentFirebaseUser.getUid());
        if (attachQueryToDatabase) {
            databaseNode.addValueEventListener(valueEventListener);
            return new ValueEventListenerDetacher(databaseNode, valueEventListener);
        } else {
            databaseNode.addListenerForSingleValueEvent(valueEventListener);
            return null;
        }
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

    public static ValueEventListenerDetacher queryMessageStatus(@NonNull final int messageStatus,
                                                                @NonNull final MessageStatusListener messageStatusListener,
                                                                @NonNull final boolean attachQueryToDatabase) {
        final Query query = getMessageStatusQuery(messageStatus);
        final ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        if (attachQueryToDatabase) {
            query.addValueEventListener(valueEventListener);
            return new ValueEventListenerDetacher(query, valueEventListener);
        } else {
            query.addListenerForSingleValueEvent(valueEventListener);
            return null;
        }
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
                Log.e(TAG, Resources.getSystem().getString(R.string.log_unknown_message_status, messageStatus));
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
                return MESSAGE_STATUS_DONE;
            case Message.STATUS_REJECTED:
                return MESSAGE_STATUS_REJECTED;
            default:
                Log.e(TAG, Resources.getSystem().getString(R.string.log_unknown_message_status, messageStatus));
                return null;
        }
    }

    private static int firebaseMessageStatusToClientMessageStatus(final String status) {
        if (status.equals(MESSAGE_STATUS_ACTIVE)) {
            return Message.STATUS_ACTIVE;
        } else if (status.equals(MESSAGE_STATUS_DONE)) {
            return Message.STATUS_DONE;
        } else if (status.equals(MESSAGE_STATUS_REJECTED)) {
            return Message.STATUS_REJECTED;
        } else {
            Log.e(TAG, Resources.getSystem().getString(R.string.log_unknown_message_status_string, status));
            return Message.STATUS_ACTIVE; // Fallback
        }
    }

    public static ValueEventListenerDetacher queryMessages(@NonNull final LocalDate childBirthdate,
                                                           @NonNull final int messageStatusFilter,
                                                           @NonNull final MessageListListener messageListListener,
                                                           @NonNull final boolean attachQueryToDatabase) {
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
                Log.e(TAG, Resources.getSystem().getString(R.string.log_internal_error_unknown_message_status_filter, messageStatusFilter));
                return null;
        }
        return FirebaseUtils.queryMessageStatus(messageQueryType, new FirebaseUtils.MessageStatusListener() {
            @Override
            public void onReceive(Map<String, Integer> messageIdToStatus) {
                FirebaseUtils.queryMessages(childBirthdate, messageStatusFilter, messageIdToStatus,
                        messageListListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                messageListListener.onCancelled(databaseError);
            }
        }, attachQueryToDatabase);
    }

    private static void queryMessages(@NonNull final LocalDate childBirthdate,
                                      @NonNull final int messageStatusFilter,
                                      @NonNull final Map<String, Integer> messageIdToStatus,
                                      @NonNull final MessageListListener messageListListener) {
        final LocalDate currentDate = LocalDate.now(DateTimeZone.UTC);
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
                            final String messageDate =
                                    getMessageDate(childBirthdate, currentDate, childAgeInDays, dayOffset);
                            final String subject = messageSnapshot.child("subject").getValue(String.class);
                            final String summary = messageSnapshot.child("summary").getValue(String.class);
                            switch (messageStatusFilter) {
                                case MESSAGE_STATUS_FILTER_ALL: {
                                    final int messageStatus = messageIdToStatus.containsKey(messageId) ? messageIdToStatus.get(messageId) : Message.STATUS_ACTIVE;
                                    messages.add(new Message(messageId, subject, messageDate, messageStatus, summary));
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_ACTIVE: {
                                    if (!messageIdToStatus.containsKey(messageId)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_ACTIVE, summary));
                                    }
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_DONE: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals(Message.STATUS_DONE)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_DONE, summary));
                                    }
                                    break;
                                }
                                case MESSAGE_STATUS_FILTER_REJECTED: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals(Message.STATUS_REJECTED)) {
                                        messages.add(new Message(messageId, subject, messageDate, Message.STATUS_REJECTED, summary));
                                    }
                                    break;
                                }
                                default:
                                    Log.w(TAG, Resources.getSystem().getString(R.string.log_error_unknown_message_status_filter, messageStatusFilter));
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

    private static String getMessageDate(@NonNull final LocalDate childBirthdate,
                                         @NonNull final LocalDate currentDate,
                                         @NonNull final int childAgeInDays,
                                         @NonNull final int messageDayOffset) {
        if (0 == (childAgeInDays - messageDayOffset)) {
            return DATE_TODAY;
        } else if (1 == (childAgeInDays - messageDayOffset)) {
            return DATE_YESTERDAY;
        } else if (currentDate.getDayOfYear() > (childAgeInDays - messageDayOffset)) {
            return childBirthdate.plusDays(messageDayOffset).toString("d MMMM");
        } else {
            return childBirthdate.plusDays(messageDayOffset).toString("d MMMM, Y");
        }
    }
}
