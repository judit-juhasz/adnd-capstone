package name.juhasz.judit.udacity.tanits.util;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import name.juhasz.judit.udacity.tanits.UserProfile;

public class FirebaseUtils {
    public interface UserProfileListener {
        void onReceive(final UserProfile userProfile);
        void onCancelled(@NonNull DatabaseError databaseError);
    }

    public static void saveUserProfile(@NonNull UserProfile profile) {
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("profiles/" + currentFirebaseUser.getUid()).setValue(profile);
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
}
