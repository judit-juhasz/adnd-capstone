package name.juhasz.judit.udacity.tanits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";
    FloatingActionButton doneFloatingActionButton;
    FloatingActionButton rejectFloatingActionButton;

    public MessageDetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_message_details, container, false);

        final Bundle arguments = getArguments();
        final Message message = arguments.getParcelable(MESSAGE_DATA);

        final TextView subjectTextView = rootView.findViewById(R.id.tv_subject);
        subjectTextView.setText(message.getSubject());
        final TextView dateTextView = rootView.findViewById(R.id.tv_date);
        dateTextView.setText(message.getDate());
        final TextView contentTextView = rootView.findViewById(R.id.tv_content);
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("messageDetail/" + message.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        final String content = dataSnapshot.child("content").getValue(String.class);
                        contentTextView.setText(content);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Cannot retrieve message detail",
                                Toast.LENGTH_LONG).show();
                    }
                });

        doneFloatingActionButton = rootView.findViewById(R.id.fab_done);
        rejectFloatingActionButton = rootView.findViewById(R.id.fab_reject);

        doneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getContext(), "Task is done", Toast.LENGTH_SHORT).show();
            }
        });

        rejectFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getContext(), "Task is rejected", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
