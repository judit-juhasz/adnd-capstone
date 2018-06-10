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
import java.util.Map;

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
        } catch(ClassCastException e) {
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
                        Uri.fromParts("mailto","judit@juhasz.name", null));
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Question");
                sendEmailIntent.putExtra(Intent.EXTRA_TEXT, "Write your message here.");
                startActivity(Intent.createChooser(sendEmailIntent, "Choose an Email client: "));
            }
        });

        feedbackFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto","judit@juhasz.name", null));
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
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (currentFirebaseUser != null) {
            firebaseDatabase.getReference("profiles/" + currentFirebaseUser.getUid() + "/childBirthdate")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String birthdate = dataSnapshot.getValue(String.class);
                            if (null != birthdate) {
                                try {
                                    final LocalDate childBirthdate = new LocalDate(birthdate);
                                    switch (filter) {
                                        case FILTER_ALL:
                                        case FILTER_ACTIVE: {
                                            firebaseDatabase.getReference("messageStatus/" + currentFirebaseUser.getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                                            final Map<String, String> messageIdToStatus = new HashMap<>();
                                                            for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                                                final String messageId = messageSnapshot.getKey();
                                                                final String status = messageSnapshot.child("status").getValue(String.class);
                                                                messageIdToStatus.put(messageId, status);
                                                            }
                                                            queryMessages(childBirthdate, filter, messageIdToStatus);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(getContext(), "Cannot retrieve message status data", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                            break;
                                        }
                                        case FILTER_DONE: {
                                            firebaseDatabase.getReference("messageStatus/" + currentFirebaseUser.getUid()).orderByChild("status").startAt("done").endAt("done")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                                            final Map<String, String> messageIdToStatus = new HashMap<>();
                                                            for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                                                final String messageId = messageSnapshot.getKey();
                                                                final String status = messageSnapshot.child("status").getValue(String.class);
                                                                messageIdToStatus.put(messageId, status);
                                                            }
                                                            queryMessages(childBirthdate, filter, messageIdToStatus);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(getContext(), "Cannot retrieve message status data", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                            break;
                                        }
                                        case FILTER_REJECTED: {
                                            firebaseDatabase.getReference("messageStatus/" + currentFirebaseUser.getUid()).orderByChild("status").startAt("rejected").endAt("rejected")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                                            final Map<String, String> messageIdToStatus = new HashMap<>();
                                                            for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                                                final String messageId = messageSnapshot.getKey();
                                                                final String status = messageSnapshot.child("status").getValue(String.class);
                                                                messageIdToStatus.put(messageId, status);
                                                            }
                                                            queryMessages(childBirthdate, filter, messageIdToStatus);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Toast.makeText(getContext(), "Cannot retrieve message status data", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                            break;
                                        }
                                        default:
                                            // Handle the error
                                    }
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
    }

    private void queryMessages(final LocalDate childBirthdate, final int filter,
                               @NonNull  final Map<String, String> messageIdToStatus) {
        final LocalDate currentDate = new LocalDate();
        final int days = Days.daysBetween(childBirthdate, currentDate).getDays();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("messageExtract").orderByChild("dayOffset").startAt(0).endAt(days)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        final ArrayList<Message> messages = new ArrayList<>();
                        for (final DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            final String messageId = messageSnapshot.getKey();
                            final int dayOffset = messageSnapshot.child("dayOffset").getValue(Integer.class);
                            final String subject = messageSnapshot.child("subject").getValue(String.class);
                            switch (filter) {
                                case FILTER_ALL: {
                                    messages.add(new Message(messageId, subject, childBirthdate.plusDays(dayOffset).toString()));
                                    break;
                                }
                                case FILTER_ACTIVE: {
                                    if (!messageIdToStatus.containsKey(messageId)) {
                                        messages.add(new Message(messageId, subject, childBirthdate.plusDays(dayOffset).toString()));
                                    }
                                    break;
                                }
                                case FILTER_DONE: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals("done")) {
                                        messages.add(new Message(messageId, subject, childBirthdate.plusDays(dayOffset).toString()));
                                    }
                                    break;
                                }
                                case FILTER_REJECTED: {
                                    if (messageIdToStatus.containsKey(messageId) && messageIdToStatus.get(messageId).equals("rejected")) {
                                        messages.add(new Message(messageId, subject, childBirthdate.plusDays(dayOffset).toString()));
                                    }
                                    break;
                                }
                                default:
                                    // Handle error
                            }
                        }
                        mMessageAdapter.setMessages(messages.toArray(new Message[messages.size()]));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Cannot retrieve messages", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
