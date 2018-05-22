package name.juhasz.judit.udacity.tanits;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MessageAdapter.OnClickListener {

    public static int navigationItemIndex = 0;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        loadHomeFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(final MenuItem menuItem) {

        Fragment fragment = null;

        switch(menuItem.getItemId()) {
            case R.id.nav_messages:
                navigationItemIndex = 0;
                break;
            case R.id.nav_profile:
                navigationItemIndex = 1;
                break;
            case R.id.nav_about:
                navigationItemIndex = 2;
                break;
            default:
                navigationItemIndex = 0;
        }

        getHomeFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    private void loadHomeFragment() {
        Fragment homeFragment = new MessagesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, homeFragment).commit();
        setTitle(R.string.navigation_menu_item_title_messages);
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

    @Override
    public void onItemClick(final Message message) {
        final Intent intentToStartDetailsActivity = new Intent(this, DetailsActivity.class);
        intentToStartDetailsActivity.putExtra(DetailsActivity.MESSAGE_DATA, message);
        startActivity(intentToStartDetailsActivity);
    }
}
