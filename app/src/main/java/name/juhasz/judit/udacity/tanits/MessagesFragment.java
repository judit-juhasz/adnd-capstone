package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;

public class MessagesFragment extends Fragment {

    FloatingActionButton questionFloatingActionButton;
    FloatingActionButton feedbackFloatingActionButton;

    public MessagesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        questionFloatingActionButton = rootView.findViewById(R.id.fab_question);
        feedbackFloatingActionButton = rootView.findViewById(R.id.fab_question);

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
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "Question");
                sendEmailIntent.putExtra(Intent.EXTRA_TEXT, "Write your message here.");
                startActivity(Intent.createChooser(sendEmailIntent, "Choose an Email client: "));
            }
        });
        return rootView;
    }
}
