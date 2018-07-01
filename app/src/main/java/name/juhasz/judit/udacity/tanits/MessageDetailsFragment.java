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

import com.google.firebase.database.DatabaseError;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;
import name.juhasz.judit.udacity.tanits.util.NetworkUtils;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    @BindView(R.id.tv_date) TextView mDateTextView;
    @BindView(R.id.tv_content) TextView mContentTextView;
    @BindView(R.id.tv_summary) TextView mSummaryTextView;

    private Message mMessage = null;

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
        mMessage = arguments.getParcelable(MESSAGE_DATA);

        ButterKnife.bind(this, rootView);

        if (null != mMessage) {
            if (!NetworkUtils.isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), "Internet connection is required",
                        Toast.LENGTH_LONG).show();
            }
            FirebaseUtils.queryMessageContent(mMessage.getId(), new FirebaseUtils.StringListener() {
                @Override
                public void onReceive(String string) {
                    mDateTextView.setText(mMessage.getDate());
                    mSummaryTextView.setText(mMessage.getSummary());
                    mContentTextView.setText(string);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), getString(R.string.no_message_detail),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        return rootView;
    }
}
