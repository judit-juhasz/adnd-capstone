package name.juhasz.judit.udacity.tanits.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
}
