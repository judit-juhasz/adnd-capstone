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
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    @BindView(R.id.tv_subject) TextView mSubjectTextView;
    @BindView(R.id.tv_date) TextView mDateTextView;
    @BindView(R.id.tv_content) TextView mContentTextView;

    public MessageDetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View rootView =
                inflater.inflate(R.layout.fragment_message_details, container, false);

        final Bundle arguments = getArguments();
        final Message message = arguments.getParcelable(MESSAGE_DATA);

        ButterKnife.bind(this, rootView);

        mSubjectTextView.setText(message.getSubject());
        mDateTextView.setText(message.getDate());
        FirebaseUtils.queryMessageContent(message.getId(), new FirebaseUtils.StringListener() {
            @Override
            public void onReceive(String string) {
                mContentTextView.setText(string);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Cannot retrieve message detail",
                    Toast.LENGTH_LONG).show();
            }
        });

        final FloatingActionMenu statusFloatingActionMenu = rootView.findViewById(R.id.fab_menu_status);
        final FloatingActionButton doneFloatingActionButton = rootView.findViewById(R.id.fab_done);
        final FloatingActionButton rejectFloatingActionButton = rootView.findViewById(R.id.fab_reject);

        doneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_DONE);
                statusFloatingActionMenu.close(false);
                statusFloatingActionMenu.setIconAnimated(false);
                statusFloatingActionMenu.getMenuIconView().setImageResource(R.drawable.ic_done);
            }
        });

        rejectFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_REJECTED);
                statusFloatingActionMenu.close(false);
                statusFloatingActionMenu.setIconAnimated(false);
                statusFloatingActionMenu.getMenuIconView().setImageResource(R.drawable.ic_reject);
            }
        });

        return rootView;
    }
}
