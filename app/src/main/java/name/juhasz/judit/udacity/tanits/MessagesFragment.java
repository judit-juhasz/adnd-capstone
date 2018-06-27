package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;

import org.joda.time.LocalDate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MessagesFragment extends Fragment {
    private static final String TAG = MessagesFragment.class.getSimpleName();

    public static final String PARAMETER_FILTER = "PARAMETER_FILTER";
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ACTIVE = 1;
    public static final int FILTER_DONE = 2;
    public static final int FILTER_REJECTED = 3;

    private MessageAdapter mMessageAdapter;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseUtils.ValueEventListenerDetacher mUserProfileListenerDetacher;
    private FirebaseUtils.ValueEventListenerDetacher mMessageStatusListenerDetacher;

    public MessagesFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mMessageAdapter = new MessageAdapter(context, (MessageAdapter.OnClickListener) context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + getString(R.string.exception_text) +
                    MessageAdapter.OnClickListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View rootView =
                inflater.inflate(R.layout.fragment_messages, container, false);

        ButterKnife.bind(this, rootView);

        final RecyclerView messagesRecycleView = rootView.findViewById(R.id.rv_messages);
        messagesRecycleView.setAdapter(mMessageAdapter);
        messagesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (null != firebaseAuth.getCurrentUser()) {
                    final Bundle bundle = MessagesFragment.this.getArguments();
                    if (bundle != null) {
                        final int filter = bundle.getInt(PARAMETER_FILTER, FILTER_ALL);
                        queryMessages(filter);
                    }
                } else {
                    if (null != mMessageStatusListenerDetacher) {
                        mMessageStatusListenerDetacher.detach();
                        mMessageStatusListenerDetacher = null;
                    }
                    if (null != mUserProfileListenerDetacher) {
                        mUserProfileListenerDetacher.detach();
                        mUserProfileListenerDetacher = null;
                    }
                }
            }
        };

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mMessageStatusListenerDetacher) {
            mMessageStatusListenerDetacher.detach();
            mMessageStatusListenerDetacher = null;
        }
        if (null != mUserProfileListenerDetacher) {
            mUserProfileListenerDetacher.detach();
            mUserProfileListenerDetacher = null;
        }
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void queryMessages(final int filter) {
        mUserProfileListenerDetacher = FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
            @Override
            public void onReceive(UserProfile userProfile) {
                if (null == userProfile) {
                    Log.w(TAG, getString(R.string.log_user_profile));
                    return;
                }
                final String birthdate = userProfile.getChildBirthdate();
                try {
                    final LocalDate childBirthdate = new LocalDate(birthdate);
                    queryMessages(childBirthdate, filter);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.set_birthdate), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, getString(R.string.log_user_birthday), databaseError.toException());
            }
        }, true);
    }

    private void queryMessages(final LocalDate childBirthdate, final int filter) {
        int firebaseMessageStatusFilter = FirebaseUtils.MESSAGE_STATUS_FILTER_ALL;
        switch (filter) {
            case FILTER_ALL:
                firebaseMessageStatusFilter = FirebaseUtils.MESSAGE_STATUS_FILTER_ALL;
                break;
            case FILTER_ACTIVE: {
                firebaseMessageStatusFilter = FirebaseUtils.MESSAGE_STATUS_FILTER_ACTIVE;
                break;
            }
            case FILTER_DONE: {
                firebaseMessageStatusFilter = FirebaseUtils.MESSAGE_STATUS_FILTER_DONE;
                break;
            }
            case FILTER_REJECTED: {
                firebaseMessageStatusFilter = FirebaseUtils.MESSAGE_STATUS_FILTER_REJECTED;
                break;
            }
            default:
                Log.w(TAG, getString(R.string.log_error_unknown_message_status_filter, filter));
        }
        mMessageStatusListenerDetacher = FirebaseUtils.queryMessages(childBirthdate, firebaseMessageStatusFilter,
                new FirebaseUtils.MessageListListener() {
                    @Override
                    public void onReceive(List<Message> messageList) {
                        mMessageAdapter.setMessages(messageList.toArray(new Message[messageList.size()]));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), getString(R.string.no_messages), Toast.LENGTH_LONG).show();
                    }
                }, true);
    }
}
