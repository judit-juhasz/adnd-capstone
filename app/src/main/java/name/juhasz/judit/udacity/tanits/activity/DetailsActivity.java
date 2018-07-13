package name.juhasz.judit.udacity.tanits.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.widget.ActiveMessagesWidgetProvider;
import name.juhasz.judit.udacity.tanits.data.Message;
import name.juhasz.judit.udacity.tanits.R;
import name.juhasz.judit.udacity.tanits.fragment.MessageDetailsFragment;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mLastUserId = null;

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
            if (null == savedInstanceState) {
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
        } else {
            Log.w(LOG_TAG, getString(R.string.log_error_missing_message_data));
            onBackPressed();
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (null != firebaseAuth.getCurrentUser()) {
                    final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                    if (!currentUserId.equals(mLastUserId)) {
                        onBackPressed();
                    }
                    mLastUserId = firebaseAuth.getCurrentUser().getUid();
                }
                ActiveMessagesWidgetProvider.updateAllWidgets(DetailsActivity.this);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
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
                ActiveMessagesWidgetProvider.updateAllWidgets(this);
                onBackPressed();
                break;
            case R.id.status_done:
                FirebaseUtils.saveMessageStatus(message.getId(), Message.STATUS_DONE);
                ActiveMessagesWidgetProvider.updateAllWidgets(this);
                onBackPressed();
                break;
            default:
                Log.w(LOG_TAG, getString(R.string.log_messages_status_settings, itemId));
        }
        return super.onOptionsItemSelected(item);
    }
}
