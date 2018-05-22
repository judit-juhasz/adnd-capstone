package name.juhasz.judit.udacity.tanits;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;

public class MessagesFragment extends Fragment {
    private MessageAdapter mMessageAdapter;
    FloatingActionButton questionFloatingActionButton;
    FloatingActionButton feedbackFloatingActionButton;

    private static final Message[] sDummyData = {
            new Message("Welcoming a new baby into your family", "2018-05-14T22:02:54+00:00"),
            new Message("Why art and creativity are important", "2018-05-15T22:02:54+00:00"),
            new Message("The role of parents in infant/toddler development", "2018-05-16T20:02:54+00:00"),
            new Message("Why parents sing to babies", "2018-05-17T13:02:54+00:00"),
            new Message("What role do parents play in a baby's brain development?", "2018-05-18T13:02:54+00:00"),
            new Message("How to support your child’s communication skills", "2018-05-19T18:02:54+00:00"),
            new Message("Baby sleep basics: birth to three months", "2018-05-20T18:02:54+00:00"),
            new Message("Why parents sing to babies 2", "2018-05-21T13:02:54+00:00"),
            new Message("How to support your child’s communication skills 2", "2018-05-22T18:02:54+00:00")
    };

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
        mMessageAdapter.setMessages(sDummyData);
        messagesRecycleView.setAdapter(mMessageAdapter);
        messagesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }
}
