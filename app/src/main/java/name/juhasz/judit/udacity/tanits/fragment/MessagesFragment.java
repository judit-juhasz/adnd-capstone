package name.juhasz.judit.udacity.tanits.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;

import org.joda.time.LocalDate;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.data.Message;
import name.juhasz.judit.udacity.tanits.data.MessageAdapter;
import name.juhasz.judit.udacity.tanits.R;
import name.juhasz.judit.udacity.tanits.data.UserProfile;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;
import name.juhasz.judit.udacity.tanits.util.NetworkUtils;

public class MessagesFragment extends Fragment implements MessageAdapter.OnClickListener {
    private static final String TAG = MessagesFragment.class.getSimpleName();
    private static final String SAVE_LAST_MESSAGE_KEY = "SAVE_LAST_MESSAGE_KEY";
    private static final String SAVE_LAYOUT_MANAGER_STATE_KEY = "SAVE_LAYOUT_MANAGER_STATE_KEY";

    public static final String PARAMETER_FILTER = "PARAMETER_FILTER";
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ACTIVE = 1;
    public static final int FILTER_DONE = 2;
    public static final int FILTER_REJECTED = 3;

    public interface OnSelectMessageListener {
        void onSelectMessage(final Message message, final boolean userSelected);
    }

    private MessageAdapter mMessageAdapter;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mLayoutManagerSavedState = null;
    @BindView(R.id.rv_messages)
    RecyclerView mMessagesRecycleView;
    @BindView(R.id.tv_notification)
    TextView mNotificationTextView;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadProgressBar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseUtils.ValueEventListenerDetacher mUserProfileListenerDetacher;
    private FirebaseUtils.ValueEventListenerDetacher mMessageStatusListenerDetacher;

    private OnSelectMessageListener mOnSelectMessageListener = null;
    private Message mLastSelectedMessage = null;

    public MessagesFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnSelectMessageListener = (OnSelectMessageListener) context;
            mMessageAdapter = new MessageAdapter(context, this);
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

        showProgressBar();
        mOnSelectMessageListener.onSelectMessage(null, false);

        mMessagesRecycleView.setAdapter(mMessageAdapter);
        mLayoutManager = new LinearLayoutManager(getContext());
        mMessagesRecycleView.setLayoutManager(mLayoutManager);
        if (null != savedInstanceState) {
            mLastSelectedMessage = savedInstanceState.getParcelable(SAVE_LAST_MESSAGE_KEY);
            mLayoutManagerSavedState = savedInstanceState.getParcelable(SAVE_LAYOUT_MANAGER_STATE_KEY);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (null != firebaseAuth.getCurrentUser()) {
                    final Bundle bundle = MessagesFragment.this.getArguments();
                    if (bundle != null) {
                        final int filter = bundle.getInt(PARAMETER_FILTER, FILTER_ACTIVE);
                        queryMessages(filter);
                    }
                } else {
                    showProgressBar();
                    if (null != mMessageStatusListenerDetacher) {
                        mMessageStatusListenerDetacher.detach();
                        mMessageStatusListenerDetacher = null;
                    }
                    if (null != mUserProfileListenerDetacher) {
                        mUserProfileListenerDetacher.detach();
                        mUserProfileListenerDetacher = null;
                    }
                    mOnSelectMessageListener.onSelectMessage(null, false);
                    mLastSelectedMessage = null;
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(SAVE_LAST_MESSAGE_KEY, mLastSelectedMessage);
        outState.putParcelable(SAVE_LAYOUT_MANAGER_STATE_KEY, mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    private void queryMessages(final int filter) {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            showNotification(getString(R.string.internet_required));
        }
        if (null != mUserProfileListenerDetacher) {
            mUserProfileListenerDetacher.detach();
        }
        mUserProfileListenerDetacher = FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
            @Override
            public void onReceive(UserProfile userProfile) {
                if (null == userProfile) {
                    showProgressBar();
                    Log.w(TAG, getString(R.string.log_user_profile));
                    return;
                }
                try {
                    final String birthdate = userProfile.getChildBirthdate();
                    if (null == birthdate) {
                        showNotification(getString(R.string.set_birthdate));
                    } else {
                        final LocalDate childBirthdate = new LocalDate(birthdate);
                        // According to the Firebase Realtime Database documentation, the DB values
                        // are cached (even if there is no internet access), so it is save to just
                        // query the messages from Firebase, there is no need to save it in the
                        // saveInstanceState (e.g. no need to store them locally).
                        // This call also attaches the listener, so the list will be automatically
                        // updated if something changes in the database.
                        queryMessages(childBirthdate, filter);
                    }
                } catch (Exception e) {
                    showNotification(getString(R.string.error_internal));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgressBar();
                mOnSelectMessageListener.onSelectMessage(null, false);
                mLastSelectedMessage = null;
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
                Log.w(TAG, getActivity().getString(R.string.log_error_unknown_message_status_filter, filter));
        }
        if (null != mMessageStatusListenerDetacher) {
            mMessageStatusListenerDetacher.detach();
        }
        mMessageStatusListenerDetacher = FirebaseUtils.queryMessages(childBirthdate, firebaseMessageStatusFilter,
                new FirebaseUtils.MessageListListener() {
                    @Override
                    public void onReceive(List<Message> messageList) {
                        if (!messageList.isEmpty()) {
                            if (null == mLastSelectedMessage) {
                                final Message firstAvailableMessage = messageList.get(0);
                                mOnSelectMessageListener.onSelectMessage(firstAvailableMessage, false);
                                mLastSelectedMessage = firstAvailableMessage;
                            } else {
                                // TODO: It is a bit of hack. In the current implementation the ID
                                // is number only, and the IDs of the messages are sorted by date.
                                // The better solution would be to store LocalDate in the messages
                                // and make the comparision based on that.
                                final int lastCalledMessageId =
                                        Integer.parseInt(mLastSelectedMessage.getId());
                                for (Iterator<Message> i = messageList.iterator(); i.hasNext();) {
                                    final Message message = i.next();
                                    final int messageId = Integer.parseInt(message.getId());
                                    if (lastCalledMessageId == messageId) {
                                        mOnSelectMessageListener.onSelectMessage(message, false);
                                        mLastSelectedMessage = message;
                                        break;
                                    } else if (!i.hasNext() || lastCalledMessageId < messageId) {
                                        mOnSelectMessageListener.onSelectMessage(message, false);
                                        mLastSelectedMessage = message;
                                        break;
                                    }
                                }
                            }
                            showMessages(messageList);
                            return;
                        }
                        if (FILTER_ACTIVE == filter) {
                            showNotification(getString(R.string.all_message_done));
                        } else {
                            showNotification(getString(R.string.no_messages));
                        }
                        mOnSelectMessageListener.onSelectMessage(null, false);
                        mLastSelectedMessage = null;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showProgressBar();
                        mOnSelectMessageListener.onSelectMessage(null, false);
                        mLastSelectedMessage = null;
                        Log.w(TAG, getString(R.string.log_message_query_canceled),
                                databaseError.toException());
                    }
                }, true);
    }

    private void showNotification(@NonNull final String notificationText) {
        mLoadProgressBar.setVisibility(View.INVISIBLE);
        mMessagesRecycleView.setVisibility(View.GONE);
        mNotificationTextView.setVisibility(View.VISIBLE);
        mNotificationTextView.setText(notificationText);
    }

    private void showMessages(@NonNull final List<Message> messageList) {
        mLoadProgressBar.setVisibility(View.INVISIBLE);
        mNotificationTextView.setVisibility(View.GONE);
        mMessagesRecycleView.setVisibility(View.VISIBLE);
        mMessageAdapter.setMessages(messageList.toArray(new Message[messageList.size()]));
        if (null != mLayoutManagerSavedState) {
            mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
            mLayoutManagerSavedState = null;
        }
    }

    private void showProgressBar() {
        mMessagesRecycleView.setVisibility(View.INVISIBLE);
        mNotificationTextView.setVisibility(View.INVISIBLE);
        mLoadProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(final Message message) {
        if (null != mOnSelectMessageListener) {
            mOnSelectMessageListener.onSelectMessage(message, true);
        }
        mLastSelectedMessage = message;
    }
}
