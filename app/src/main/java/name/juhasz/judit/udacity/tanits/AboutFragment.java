package name.juhasz.judit.udacity.tanits;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

    private TextView mDisplayTextView;

    public AboutFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        mDisplayTextView = rootView.findViewById(R.id.tv_display);
        String displaytext = getResources().getString(R.string.text_one_about)
                + getResources().getString(R.string.text_two_about)
                + getResources().getString(R.string.text_one_about)
                + getResources().getString(R.string.text_one_about);
        mDisplayTextView.setText(displaytext);
        return rootView;
    }

}
