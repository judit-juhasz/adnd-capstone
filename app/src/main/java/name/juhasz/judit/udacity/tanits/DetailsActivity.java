package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        final Intent intent = getIntent();
        final boolean hasMessageExtra = (null != intent && intent.hasExtra(MESSAGE_DATA));
        if (hasMessageExtra) {
            final Bundle arguments = new Bundle();
            final Message message = intent.getParcelableExtra(MESSAGE_DATA);
            arguments.putParcelable(MessageDetailsFragment.MESSAGE_DATA, message);
            final MessageDetailsFragment fragment = new MessageDetailsFragment();
            fragment.setArguments(arguments);
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.content_frame_detail, fragment)
                    .commit();
        }
    }
}
