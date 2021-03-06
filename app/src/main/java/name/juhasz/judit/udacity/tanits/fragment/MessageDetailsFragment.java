package name.juhasz.judit.udacity.tanits.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.data.Message;
import name.juhasz.judit.udacity.tanits.R;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;
import name.juhasz.judit.udacity.tanits.util.NetworkUtils;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    private static final String SAVE_MESSAGE_CONTENT_KEY = "SAVE_MESSAGE_CONTENT_KEY";

    @BindView(R.id.tv_date)
    TextView mDateTextView;
    @BindView(R.id.tv_content)
    TextView mContentTextView;
    @BindView(R.id.tv_summary)
    TextView mSummaryTextView;
    @BindView(R.id.sw_notification_message_details)
    ScrollView mMessageDetailsScrollView;
    @BindView(R.id.tv_notification_message_details)
    TextView mNotificationTextView;
    @BindView(R.id.pb_message_details_loading_indicator)
    ProgressBar mLoadProgressBar;

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
            final boolean hasValidSavedData =
                    (null != savedInstanceState && savedInstanceState.containsKey(SAVE_MESSAGE_CONTENT_KEY));
            if (!NetworkUtils.isNetworkAvailable(getContext()) && !hasValidSavedData) {
                showNotification(getString(R.string.internet_required));
            } else if (hasValidSavedData) {
                mDateTextView.setText(mMessage.getDate());
                mSummaryTextView.setText(mMessage.getSummary());
                mContentTextView.setText(savedInstanceState.getString(SAVE_MESSAGE_CONTENT_KEY));
                showMessageDetails();
            } else {
                showProgressBar();
            }
            FirebaseUtils.queryMessageContent(mMessage.getId(), new FirebaseUtils.StringListener() {
                @Override
                public void onReceive(String string) {
                    mDateTextView.setText(mMessage.getDate());
                    mSummaryTextView.setText(mMessage.getSummary());
                    mContentTextView.setText(string);
                    showMessageDetails();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showNotification(getString(R.string.no_message_detail));
                }
            });
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(SAVE_MESSAGE_CONTENT_KEY, mContentTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void showNotification(@NonNull final String notificationText) {
        mLoadProgressBar.setVisibility(View.INVISIBLE);
        mMessageDetailsScrollView.setVisibility(View.GONE);
        mNotificationTextView.setVisibility(View.VISIBLE);
        mNotificationTextView.setText(notificationText);
    }

    private void showMessageDetails() {
        mLoadProgressBar.setVisibility(View.INVISIBLE);
        mNotificationTextView.setVisibility(View.GONE);
        mMessageDetailsScrollView.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        mMessageDetailsScrollView.setVisibility(View.INVISIBLE);
        mNotificationTextView.setVisibility(View.INVISIBLE);
        mLoadProgressBar.setVisibility(View.VISIBLE);
    }
}
