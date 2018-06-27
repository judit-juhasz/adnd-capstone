package name.juhasz.judit.udacity.tanits;

import android.graphics.Color;
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
import com.google.firebase.database.DatabaseError;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.ConfigurationUtils;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";
    public static final String COLOR_ACTIVE = "#81D4FA";
    public static final String COLOR_DONE = "#A5D6A7";
    public static final String COLOR_REJECTED = "#B0BEC5";

    @BindView(R.id.tv_date) TextView mDateTextView;
    @BindView(R.id.tv_content) TextView mContentTextView;
    @BindView(R.id.tv_summary) TextView mSummaryTextView;

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

        mDateTextView.setText(message.getDate());
        mSummaryTextView.setText(message.getSummary());
        FirebaseUtils.queryMessageContent(message.getId(), new FirebaseUtils.StringListener() {
            @Override
            public void onReceive(String string) {
                mContentTextView.setText(string);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), getString(R.string.no_message_detail),
                        Toast.LENGTH_LONG).show();
            }
        });

        final FloatingActionMenu statusFloatingActionMenu = rootView.findViewById(R.id.fab_menu_status);
        final FloatingActionButton doneFloatingActionButton = rootView.findViewById(R.id.fab_done);
        final FloatingActionButton rejectFloatingActionButton = rootView.findViewById(R.id.fab_reject);

        if (message.getStatus() == Message.STATUS_DONE) {
            statusFloatingActionMenu.setMenuButtonColorNormal((Color.parseColor(COLOR_DONE)));
        } else if (message.getStatus() == Message.STATUS_REJECTED) {
            statusFloatingActionMenu.getMenuIconView().setImageResource(R.drawable.ic_reject);
            statusFloatingActionMenu.setMenuButtonColorNormal((Color.parseColor(COLOR_REJECTED)));
        } else {
            statusFloatingActionMenu.setMenuButtonColorNormal((Color.parseColor(COLOR_ACTIVE)));
        }

        doneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_DONE);
                statusFloatingActionMenu.close(false);
                statusFloatingActionMenu.setMenuButtonColorNormal((Color.parseColor(COLOR_DONE)));
                if (!ConfigurationUtils.isTwoPaneMode(getActivity())) {
                    getActivity().onBackPressed();
                }
            }
        });

        rejectFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_REJECTED);
                statusFloatingActionMenu.close(false);
                statusFloatingActionMenu.getMenuIconView().setImageResource(R.drawable.ic_reject);
                statusFloatingActionMenu.setMenuButtonColorNormal((Color.parseColor(COLOR_REJECTED)));
                if (!ConfigurationUtils.isTwoPaneMode(getActivity())) {
                    getActivity().onBackPressed();
                }
            }
        });

        return rootView;
    }
}
