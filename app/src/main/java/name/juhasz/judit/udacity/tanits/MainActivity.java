package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.juhasz.judit.udacity.tanits.util.ConfigurationUtils;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MainActivity extends AppCompatActivity implements MessagesFragment.OnSelectMessageListener {

    private static final String SAVE_SELECTED_NAVIGATION_ITEM = "SAVE_SELECTED_NAVIGATION_ITEM";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mLastUserId = null;
    private int mSelectedNavigationItem = R.id.nav_messages;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    private Toolbar mToolbar;
    private View mNavigationHeaderView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;
    @BindView(R.id.content_message_details)
    FrameLayout mMessageDetailsFrameLayout;
    @BindView(R.id.fab_menu)
    FloatingActionMenu mGroupFloatingActionButton;
    @BindView(R.id.fab_question)
    FloatingActionButton mQuestionFloatingActionButton;
    @BindView(R.id.fab_feedback)
    FloatingActionButton mFeedbackFloatingActionButton;

    private boolean mTwoPaneMode;
    private Message mLoadedMessage;

    private FirebaseUtils.ValueEventListenerDetacher mUserProfileListenerDetacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mTwoPaneMode = ConfigurationUtils.isTwoPaneMode(this);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        JodaTimeAndroid.init(this);
        FirebaseUtils.initialize(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (null == firebaseAuth.getCurrentUser()) {
                    mUsernameTextView.setText("");
                    mEmailTextView.setText("");
                    if (null != mUserProfileListenerDetacher) {
                        mUserProfileListenerDetacher.detach();
                        mUserProfileListenerDetacher = null;
                    }
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(), RC_SIGN_IN);
                } else {
                    mUserProfileListenerDetacher =
                            FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
                        @Override
                        public void onReceive(UserProfile userProfile) {
                            loadNavigationHeader(userProfile);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(LOG_TAG, getString(R.string.log_error_login) + databaseError);
                        }
                    }, true);
                    final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                    if (!currentUserId.equals(mLastUserId)) {
                        mSelectedNavigationItem = R.id.nav_messages;
                        loadFragment();
                    }
                    mLastUserId = firebaseAuth.getCurrentUser().getUid();
                }
                LastActiveMessageWidgetProvider.updateAllWidgets(MainActivity.this);
            }
        };

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setupDrawerContent(mNavigationView);
        loadActionBarDrawerToggle(mDrawerLayout);

        mNavigationHeaderView = mNavigationView.getHeaderView(0);
        mUsernameTextView = mNavigationHeaderView.findViewById(R.id.tv_username);
        mEmailTextView = mNavigationHeaderView.findViewById(R.id.tv_email);

        mQuestionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(getString(R.string.email_scheme), getString(R.string.email_address), null));
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.subject_question);
                startActivity(Intent.createChooser(sendEmailIntent, getResources().getString(R.string.no_email_client_selected)));
            }
        });

        mFeedbackFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(getString(R.string.email_scheme), getString(R.string.email_address), null));
                sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.subject_feedback);
                startActivity(Intent.createChooser(sendEmailIntent, getResources().getString(R.string.no_email_client_selected)));
            }
        });

        loadFragment();
        setMessageDetailFragmentVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mUserProfileListenerDetacher) {
            mUserProfileListenerDetacher.detach();
            mUserProfileListenerDetacher = null;
        }
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_SELECTED_NAVIGATION_ITEM, mSelectedNavigationItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedNavigationItem = savedInstanceState.getInt(SAVE_SELECTED_NAVIGATION_ITEM);
    }

    private void loadActionBarDrawerToggle(DrawerLayout layout) {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(final MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_messages:
                mSelectedNavigationItem = R.id.nav_messages;
                setMessageDetailFragmentVisibility();
                loadFragment();
                break;
            case R.id.nav_profile:
                mSelectedNavigationItem = R.id.nav_profile;
                setMessageDetailFragmentVisibility();
                loadFragment();
                break;
            case R.id.nav_about:
                mSelectedNavigationItem = R.id.nav_about;
                setMessageDetailFragmentVisibility();
                loadFragment();
                break;
            case R.id.nav_logout:
                mDrawerLayout.closeDrawers();
                if (null != mUserProfileListenerDetacher) {
                    mUserProfileListenerDetacher.detach();
                    mUserProfileListenerDetacher = null;
                }
                AuthUI.getInstance().signOut(this);
                break;
            default:
                mSelectedNavigationItem = R.id.nav_messages;
        }
    }

    private Fragment getFragment() {
        switch (mSelectedNavigationItem) {
            case R.id.nav_messages: {
                final MessagesFragment fragment = new MessagesFragment();
                final Bundle arguments = new Bundle();
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ACTIVE);
                fragment.setArguments(arguments);
                return fragment;
            }
            case R.id.nav_profile:
                return new ProfileFragment();
            case R.id.nav_about:
                return new AboutFragment();
            default: {
                final MessagesFragment fragment = new MessagesFragment();
                final Bundle arguments = new Bundle();
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ACTIVE);
                fragment.setArguments(arguments);
                return fragment;
            }
        }
    }

    private void selectNavigationMenu() {
        mNavigationView.getMenu().findItem(mSelectedNavigationItem).setChecked(true);
    }

    private void setToolbarTitles() {
        getSupportActionBar().setTitle(mNavigationView.getMenu().findItem(mSelectedNavigationItem).getTitle());
    }

    private void loadNavigationHeader(final UserProfile user) {
        if (null != user) {
            if (null != user.getName()) {
                mUsernameTextView.setText(user.getName());
            } else {
                mUsernameTextView.setText("");
            }
            if (null != user.getEmail()) {
                mEmailTextView.setText(user.getEmail());
            } else {
                mEmailTextView.setText("");
            }
        } else {
            mUsernameTextView.setText("");
            mEmailTextView.setText("");
        }
    }

    private void loadFragment() {
        selectNavigationMenu();
        setToolbarTitles();
        Fragment fragment = getFragment();
        if (R.id.nav_messages == mSelectedNavigationItem) {
            mGroupFloatingActionButton.setVisibility(View.VISIBLE);
        } else {
            mGroupFloatingActionButton.setVisibility(View.GONE);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerLayout.closeDrawers();
        invalidateOptionsMenu();
    }

    @Override
    public void onSelectMessage(final Message message, final boolean userSelected) {
        if (mTwoPaneMode) {
            mLoadedMessage = message;
            setMessageDetailFragment(message);
            invalidateOptionsMenu();
        } else if (userSelected) {
            final Intent intentToStartDetailsActivity = new Intent(this, DetailsActivity.class);
            intentToStartDetailsActivity.putExtra(DetailsActivity.MESSAGE_DATA, message);
            startActivity(intentToStartDetailsActivity);
        }
    }

    private void setMessageDetailFragment(final Message message) {
        final MessageDetailsFragment fragment = new MessageDetailsFragment();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(MessageDetailsFragment.MESSAGE_DATA, message);
        fragment.setArguments(arguments);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.content_message_details, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTwoPaneMode && R.id.nav_messages == mSelectedNavigationItem && null != mLoadedMessage) {
            getMenuInflater().inflate(R.menu.menu_activity_main_two_panel, menu);
        } else if (R.id.nav_messages == mSelectedNavigationItem) {
            getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        final MessagesFragment fragment = new MessagesFragment();
        final Bundle arguments = new Bundle();
        switch (itemId) {
            case R.id.messages_all:
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ALL);
                break;
            case R.id.messages_active:
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ACTIVE);
                break;
            case R.id.messages_done:
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_DONE);
                break;
            case R.id.messages_rejected:
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_REJECTED);
                break;
            case R.id.status_rejected:
                if (null != mLoadedMessage) {
                    FirebaseUtils.saveMessageStatus(mLoadedMessage.getId(), Message.STATUS_REJECTED);
                    LastActiveMessageWidgetProvider.updateAllWidgets(this);
                }
                break;
            case R.id.status_done:
                if (null != mLoadedMessage) {
                    FirebaseUtils.saveMessageStatus(mLoadedMessage.getId(), Message.STATUS_DONE);
                    LastActiveMessageWidgetProvider.updateAllWidgets(this);
                }
                break;
            default:
                Log.w(LOG_TAG, getString(R.string.log_messages_menu_selection, itemId));
        }
        if (!arguments.isEmpty()) {
            fragment.setArguments(arguments);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMessageDetailFragmentVisibility() {
        if (mTwoPaneMode && R.id.nav_messages == mSelectedNavigationItem) {
            mMessageDetailsFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMessageDetailsFrameLayout.setVisibility(View.GONE);
        }
    }
}
