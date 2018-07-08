package name.juhasz.judit.udacity.tanits.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.juhasz.judit.udacity.tanits.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView =
                inflater.inflate(R.layout.fragment_about, container, false);

        TextView displayTextView = rootView.findViewById(R.id.tv_introduction);
        displayTextView.setText(getResources().getString(R.string.lorem_ipsum_long));
        return rootView;
    }
}