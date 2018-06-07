package name.juhasz.judit.udacity.tanits;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageDetailsFragment extends Fragment {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";

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
        contentTextView.setText(message.getContent());

        return rootView;
    }
}
