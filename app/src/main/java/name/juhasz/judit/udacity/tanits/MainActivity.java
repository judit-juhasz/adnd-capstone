package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
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
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Arrays;

import name.juhasz.judit.udacity.tanits.util.FirebaseUtils;

public class MainActivity extends AppCompatActivity implements MessageAdapter.OnClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int RC_SIGN_IN = 1;
    public static int navigationItemIndex = 0;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private String[] mFragmentTitles;
    private View mNavigationHeaderView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUtils.queryUserProfile(new FirebaseUtils.UserProfileListener() {
                    @Override
                    public void onReceive(UserProfile userProfile) {
                        loadNavigationHeader(userProfile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(LOG_TAG, "Internal error at login: " + databaseError);
                    }
                });
                if (null == firebaseAuth.getCurrentUser()) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(), RC_SIGN_IN);
                }
            }
        };

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationHeaderView = mNavigationView.getHeaderView(0);
        mUsernameTextView = mNavigationHeaderView.findViewById(R.id.tv_username);
        mEmailTextView = mNavigationHeaderView.findViewById(R.id.tv_email);

        setupDrawerContent(mNavigationView);
        loadActionBarDrawerToggle(mDrawerLayout);

        mFragmentTitles = getResources().getStringArray(R.array.nav_item_fragment_titles);

        if (savedInstanceState == null) {
            navigationItemIndex = 0;
            loadFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
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
                navigationItemIndex = 0;
                loadFragment();
                break;
            case R.id.nav_profile:
                navigationItemIndex = 1;
                loadFragment();
                break;
            case R.id.nav_about:
                navigationItemIndex = 2;
                loadFragment();
                break;
            case R.id.nav_logout:
                mDrawerLayout.closeDrawers();
                AuthUI.getInstance().signOut(this);
                break;
            default:
                navigationItemIndex = 0;
        }
    }

    private Fragment getFragment() {
        switch (navigationItemIndex) {
            case 0: {
                final MessagesFragment fragment = new MessagesFragment();
                final Bundle arguments = new Bundle();
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ALL);
                fragment.setArguments(arguments);
                return fragment;
            }
            case 1:
                return new ProfileFragment();
            case 2:
                return new AboutFragment();
            default: {
                final MessagesFragment fragment = new MessagesFragment();
                final Bundle arguments = new Bundle();
                arguments.putInt(MessagesFragment.PARAMETER_FILTER, MessagesFragment.FILTER_ALL);
                fragment.setArguments(arguments);
                return fragment;
            }
        }
    }

    private void selectNavigationMenu() {
        mNavigationView.getMenu().getItem(navigationItemIndex).setChecked(true);
    }

    private void setToolbarTitles() {
        getSupportActionBar().setTitle(mFragmentTitles[navigationItemIndex]);
    }

    private void loadNavigationHeader(final UserProfile user) {
        mUsernameTextView.setText(user.getName());
        mEmailTextView.setText(user.getEmail());
    }

    private void loadFragment() {
        selectNavigationMenu();
        setToolbarTitles();
        Fragment fragment = getFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerLayout.closeDrawers();
        invalidateOptionsMenu();
    }

    @Override
    public void onItemClick(final Message message) {
        final Intent intentToStartDetailsActivity = new Intent(this, DetailsActivity.class);
        intentToStartDetailsActivity.putExtra(DetailsActivity.MESSAGE_DATA, message);
        startActivity(intentToStartDetailsActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (navigationItemIndex == 0) {
            getMenuInflater().inflate(R.menu.menu_message_categorization, menu);
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
            default:
                Log.w(LOG_TAG, getString(R.string.log_messages_menu_selection) + itemId);
        }
        if (!arguments.isEmpty()) {
            fragment.setArguments(arguments);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
        return super.onOptionsItemSelected(item);
    }
}
