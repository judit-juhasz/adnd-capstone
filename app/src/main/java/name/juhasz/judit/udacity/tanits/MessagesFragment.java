package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MessagesFragment extends Fragment {
    private static final String TAG = MessagesFragment.class.getSimpleName();

    public static final String PARAMETER_FILTER = "PARAMETER_FILTER";
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ACTIVE = 1;
    public static final int FILTER_DONE = 2;
    public static final int FILTER_REJECTED = 3;

    private MessageAdapter mMessageAdapter;
    FloatingActionButton questionFloatingActionButton;
    FloatingActionButton feedbackFloatingActionButton;

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

        questionFloatingActionButton = rootView.findViewById(R.id.fab_question);
        feedbackFloatingActionButton = rootView.findViewById(R.id.fab_feedback);

        questionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "judit@juhasz.name", null));
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Question");
                sendEmailIntent.putExtra(Intent.EXTRA_TEXT, "Write your message here.");
                startActivity(Intent.createChooser(sendEmailIntent, "Choose an Email client: "));
            }
        });

        feedbackFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "judit@juhasz.name", null));
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                sendEmailIntent.putExtra(Intent.EXTRA_TEXT, "Write your message here.");
                startActivity(Intent.createChooser(sendEmailIntent, "Choose an Email client: "));
            }
        });

        final RecyclerView messagesRecycleView = rootView.findViewById(R.id.rv_messages);
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            final int filter = bundle.getInt(PARAMETER_FILTER, FILTER_ALL);
            queryMessages(filter);
        }
        messagesRecycleView.setAdapter(mMessageAdapter);
        messagesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    private void queryMessages(final int filter) {
        FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
            @Override
            public void onReceive(UserProfile userProfile) {
                if (null == userProfile) {
                    Log.w(TAG, "User profile is not available");
                    return;
                }
                final String birthdate = userProfile.getChildBirthdate();
                if (null != birthdate) {
                    try {
                        final LocalDate childBirthdate = new LocalDate(birthdate);
                        int messageQueryType;
                        switch (filter) {
                            case FILTER_ALL:
                            case FILTER_ACTIVE:
                                messageQueryType = Message.STATUS_ACTIVE;
                                break;
                            case FILTER_DONE:
                                messageQueryType = Message.STATUS_DONE;
                                break;
                            case FILTER_REJECTED:
                                messageQueryType = Message.STATUS_REJECTED;
                                break;
                            default:
                                // Handle the error
                                return;
                        }
                        FirebaseUtils.queryMessageStatus(messageQueryType, new FirebaseUtils.MessageStatusListener() {
                            @Override
                            public void onReceive(Map<String, Integer> messageIdToStatus) {
                                queryMessages(childBirthdate, filter, messageIdToStatus);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Cannot retrieve message status data", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        // Instruct the user to set the birthdate on the profile
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "User birthdate is not available", databaseError.toException());
            }
        });
    }

    private void queryMessages(final LocalDate childBirthdate, final int filter,
                               @NonNull final Map<String, Integer> messageIdToStatus) {
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
                Log.w(TAG, "Error: Unknown filter: " + filter);
        }
        FirebaseUtils.queryMessages(childBirthdate, firebaseMessageStatusFilter, messageIdToStatus,
                new FirebaseUtils.MessageListListener() {
                    @Override
                    public void onReceive(List<Message> messageList) {
                        mMessageAdapter.setMessages(messageList.toArray(new Message[messageList.size()]));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Cannot retrieve messages", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
