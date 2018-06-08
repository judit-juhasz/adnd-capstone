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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements MessageAdapter.OnClickListener {

    public static final int RC_SIGN_IN = 1;
    public static int navigationItemIndex = 0;
    private static final String TAG_MESSAGES = "messages";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_ABOUT_US = "about_us";
    public static String CURRENT_TAG = TAG_MESSAGES;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar toolbar;
    private String[] fragmentTitles;
    private View navigationHeaderView;
    private TextView usernameTextView;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loadNavigationHeader(user);
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        navigationHeaderView = mNavigationView.getHeaderView(0);
        usernameTextView = navigationHeaderView.findViewById(R.id.tv_username);
        emailTextView = navigationHeaderView.findViewById(R.id.tv_email);

        setupDrawerContent(mNavigationView);
        loadActionBarDrawerToggle(mDrawerLayout);

        fragmentTitles = getResources().getStringArray(R.array.nav_item_fragment_titles);

        if (savedInstanceState == null) {
            navigationItemIndex = 0;
            CURRENT_TAG = TAG_MESSAGES;
            loadHomeFragment();
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
                mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

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

        switch(menuItem.getItemId()) {
            case R.id.nav_messages:
                navigationItemIndex = 0;
                CURRENT_TAG = TAG_MESSAGES;
                loadHomeFragment();
                break;
            case R.id.nav_profile:
                navigationItemIndex = 1;
                CURRENT_TAG = TAG_PROFILE;
                loadHomeFragment();
                break;
            case R.id.nav_about:
                navigationItemIndex = 2;
                CURRENT_TAG = TAG_ABOUT_US;
                loadHomeFragment();
                break;
            case R.id.nav_logout:
                mDrawerLayout.closeDrawers();
                AuthUI.getInstance().signOut(this);
                break;
            default:
                navigationItemIndex = 0;
        }
    }

    private Fragment getHomeFragment () {
        switch (navigationItemIndex) {
            case 0:
                MessagesFragment messagesFragment = new MessagesFragment();
                return messagesFragment;
            case 1:
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case 2:
                AboutFragment aboutFragment = new AboutFragment();
                return aboutFragment;
            default:
                return new MessagesFragment();

        }
    }

    private void selectNavigationMenu() {
        mNavigationView.getMenu().getItem(navigationItemIndex).setChecked(true);
    }

    private void setToolbarTitles() {
        getSupportActionBar().setTitle(fragmentTitles[navigationItemIndex]);
    }

    private void loadNavigationHeader(FirebaseUser user) {
        usernameTextView.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());
    }

    private void loadHomeFragment() {
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawerLayout.closeDrawers();
            return;
        } else {
            selectNavigationMenu();
            setToolbarTitles();
            Fragment fragment = getHomeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            mDrawerLayout.closeDrawers();
        }
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

        switch (itemId) {
            case R.id.messages_all:
                Toast.makeText(this, "All messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.messages_active:
                Toast.makeText(this, "Active messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.messages_done:
                Toast.makeText(this, "Done messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.messages_rejected:
                Toast.makeText(this, "Rejected messages", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.w(LOG_TAG, "Menu selection is not handled. ItemId: " + itemId);
        }
        return super.onOptionsItemSelected(item);
    }
}
