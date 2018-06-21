package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG =DetailsActivity.class.getSimpleName();
    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_status_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = getIntent();
        Message message = intent.getParcelableExtra(MESSAGE_DATA);
        final int itemId = item.getItemId();

        switch (itemId) {
            case R.id.status_rejected:
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_REJECTED);
                break;
            case R.id.status_done:
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_DONE);
                break;
            default:
                Log.w(LOG_TAG, getString(R.string.log_messages_status_settings) + itemId);
        }
        return super.onOptionsItemSelected(item);
    }
}
