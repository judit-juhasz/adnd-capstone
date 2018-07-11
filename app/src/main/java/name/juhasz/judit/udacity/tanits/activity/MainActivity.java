package name.juhasz.judit.udacity.tanits.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import name.juhasz.judit.udacity.tanits.widget.ActiveMessagesWidgetProvider;
import name.juhasz.judit.udacity.tanits.data.Message;
import name.juhasz.judit.udacity.tanits.R;
import name.juhasz.judit.udacity.tanits.data.UserProfile;
import name.juhasz.judit.udacity.tanits.fragment.AboutFragment;
import name.juhasz.judit.udacity.tanits.fragment.MessageDetailsFragment;
import name.juhasz.judit.udacity.tanits.fragment.MessagesFragment;
import name.juhasz.judit.udacity.tanits.fragment.ProfileFragment;
import name.juhasz.judit.udacity.tanits.util.ConfigurationUtils;
import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MainActivity extends AppCompatActivity implements MessagesFragment.OnSelectMessageListener {

    private static final String SAVE_SELECTED_NAVIGATION_ITEM = "SAVE_SELECTED_NAVIGATION_ITEM";
    private static final String SAVE_SELECTED_MESSAGE_FILTER = "SAVE_SELECTED_MESSAGE_FILTER";
    private static final String SAVE_LAST_USER_ID = "SAVE_LAST_USER_ID";
    private static final String SAVE_FAB_OPENED = "SAVE_FAB_OPENED";
    private static final int COLOR_LABEL_BACKGROUND = 0Xffffffff;
    private static final String COLOR_FAB_LABEL_TEXT = "#212121";

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mLastUserId = null;
    private int mSelectedMessageFilter = MessagesFragment.FILTER_ACTIVE;
    private int mSelectedNavigationItem = R.id.nav_messages;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.content_message_details)
    FrameLayout mMessageDetailsFrameLayout;
    @BindView(R.id.fab_menu)
    FloatingActionMenu mGroupFloatingActionButton;
    @BindView(R.id.fab_question)
    FloatingActionButton mQuestionFloatingActionButton;
    @BindView(R.id.fab_feedback)
    FloatingActionButton mFeedbackFloatingActionButton;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;

    private boolean mTwoPaneMode;
    private Message mLoadedMessage;

    private FirebaseUtils.ValueEventListenerDetacher mUserProfileListenerDetacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);
        FirebaseUtils.initialize(this);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mTwoPaneMode = ConfigurationUtils.isTwoPaneMode(this);

        final View navigationHeaderView = mNavigationView.getHeaderView(0);
        mUsernameTextView = navigationHeaderView.findViewById(R.id.tv_username);
        mEmailTextView = navigationHeaderView.findViewById(R.id.tv_email);

        setSupportActionBar(mToolbar);
        setupDrawerContent(mNavigationView);
        loadActionBarDrawerToggle(mDrawerLayout);

        if (null != savedInstanceState) {
            if (savedInstanceState.containsKey(SAVE_LAST_USER_ID)) {
                mLastUserId = savedInstanceState.getString(SAVE_LAST_USER_ID);
            }
            if (savedInstanceState.containsKey(SAVE_SELECTED_MESSAGE_FILTER)) {
                mSelectedMessageFilter = savedInstanceState.getInt(SAVE_SELECTED_MESSAGE_FILTER);
            }
            if (savedInstanceState.containsKey(SAVE_SELECTED_NAVIGATION_ITEM)) {
                final int previouslySelectedNavigationItem =
                        savedInstanceState.getInt(SAVE_SELECTED_NAVIGATION_ITEM);
                loadActivityContentForNavigationItemId(previouslySelectedNavigationItem, false);
            }
            if (savedInstanceState.containsKey(SAVE_FAB_OPENED) && savedInstanceState.getBoolean(SAVE_FAB_OPENED)) {
                mGroupFloatingActionButton.open(false);
            }
        }

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
                        loadActivityContentForNavigationItemId(R.id.nav_messages, true);
                    }
                    mLastUserId = firebaseAuth.getCurrentUser().getUid();
                }
                ActiveMessagesWidgetProvider.updateAllWidgets(MainActivity.this);
            }
        };

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

        mFeedbackFloatingActionButton.setLabelColors(COLOR_LABEL_BACKGROUND, COLOR_LABEL_BACKGROUND, COLOR_LABEL_BACKGROUND);
        mFeedbackFloatingActionButton.setLabelTextColor(Color.parseColor(COLOR_FAB_LABEL_TEXT));

        mQuestionFloatingActionButton.setLabelColors(COLOR_LABEL_BACKGROUND, COLOR_LABEL_BACKGROUND, COLOR_LABEL_BACKGROUND);
        mQuestionFloatingActionButton.setLabelTextColor(Color.parseColor(COLOR_FAB_LABEL_TEXT));
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
        outState.putInt(SAVE_SELECTED_MESSAGE_FILTER, mSelectedMessageFilter);
        outState.putString(SAVE_LAST_USER_ID, mLastUserId);
        outState.putBoolean(SAVE_FAB_OPENED, mGroupFloatingActionButton.isOpened());
        super.onSaveInstanceState(outState);
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
        final int itemId = menuItem.getItemId();
        switch (itemId) {
            case R.id.nav_messages:
                if (itemId != mSelectedNavigationItem) {
                    loadActivityContentForNavigationItemId(itemId, true);
                }
                break;
            case R.id.nav_profile:
                if (itemId != mSelectedNavigationItem) {
                    loadActivityContentForNavigationItemId(itemId, true);
                }
                break;
            case R.id.nav_about:
                if (itemId != mSelectedNavigationItem) {
                    loadActivityContentForNavigationItemId(itemId, true);
                }
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
                Log.w(LOG_TAG, getString(R.string.log_messages_menu_selection, itemId));
        }
        mDrawerLayout.closeDrawers();
    }

    private Fragment getFragmentForNavigationItemId(@NonNull final int navigationItemId) {
        switch (navigationItemId) {
            case R.id.nav_profile:
                return new ProfileFragment();
            case R.id.nav_about:
                return new AboutFragment();
            case R.id.nav_messages:
            default: {
                final MessagesFragment fragment = new MessagesFragment();
                final Bundle arguments = new Bundle();
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, mSelectedMessageFilter);
                fragment.setArguments(arguments);
                return fragment;
            }
        }
    }

    private void selectNavigationMenu(@NonNull final int navigationItemId) {
        mNavigationView.getMenu().findItem(navigationItemId).setChecked(true);
    }

    private void setToolbarTitles(@NonNull final int navigationItemId) {
        getSupportActionBar().setTitle(mNavigationView.getMenu().findItem(navigationItemId).getTitle());
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

    private void loadActivityContentForNavigationItemId(@NonNull final int navigationItemId,
                                                        @NonNull final boolean reloadFragment) {
        setMessageDetailFragmentVisibility(navigationItemId);
        selectNavigationMenu(navigationItemId);
        setToolbarTitles(navigationItemId);
        setFabVisibilityForNavigationItemId(navigationItemId);
        if (reloadFragment) {
            setFragmentForNavigationItemId(navigationItemId);
        }
        mSelectedNavigationItem = navigationItemId;
        invalidateOptionsMenu();
    }

    private void setFragmentForNavigationItemId(@Nullable final int navigationItemId) {
        final Fragment fragment = getFragmentForNavigationItemId(navigationItemId);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    private void setFabVisibilityForNavigationItemId(@Nullable final int navigationItemId) {
        if (R.id.nav_messages == navigationItemId) {
            mGroupFloatingActionButton.setVisibility(View.VISIBLE);
        } else {
            mGroupFloatingActionButton.setVisibility(View.GONE);
        }
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
        switch (itemId) {
            case R.id.messages_all:
                if (itemId != mSelectedMessageFilter) {
                    mSelectedMessageFilter = MessagesFragment.FILTER_ALL;
                    loadActivityContentForNavigationItemId(R.id.nav_messages, true);
                }
                break;
            case R.id.messages_active:
                if (itemId != mSelectedMessageFilter) {
                    mSelectedMessageFilter = MessagesFragment.FILTER_ACTIVE;
                    loadActivityContentForNavigationItemId(R.id.nav_messages, true);
                }
                break;
            case R.id.messages_done:
                if (itemId != mSelectedMessageFilter) {
                    mSelectedMessageFilter = MessagesFragment.FILTER_DONE;
                    loadActivityContentForNavigationItemId(R.id.nav_messages, true);
                }
                break;
            case R.id.messages_rejected:
                if (itemId != mSelectedMessageFilter) {
                    mSelectedMessageFilter = MessagesFragment.FILTER_REJECTED;
                    loadActivityContentForNavigationItemId(R.id.nav_messages, true);
                }
                break;
            case R.id.status_rejected:
                if (null != mLoadedMessage) {
                    FirebaseUtils.saveMessageStatus(mLoadedMessage.getId(), Message.STATUS_REJECTED);
                    ActiveMessagesWidgetProvider.updateAllWidgets(this);
                }
                break;
            case R.id.status_done:
                if (null != mLoadedMessage) {
                    FirebaseUtils.saveMessageStatus(mLoadedMessage.getId(), Message.STATUS_DONE);
                    ActiveMessagesWidgetProvider.updateAllWidgets(this);
                }
                break;
            default:
                Log.w(LOG_TAG, getString(R.string.log_messages_menu_selection, itemId));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMessageDetailFragmentVisibility(@NonNull final int navigationItemId) {
        if (mTwoPaneMode && R.id.nav_messages == navigationItemId) {
            mMessageDetailsFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMessageDetailsFrameLayout.setVisibility(View.GONE);
        }
    }
}
