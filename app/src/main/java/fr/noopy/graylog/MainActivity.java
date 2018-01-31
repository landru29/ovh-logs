package fr.noopy.graylog;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gotoStream();

        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigation_fragment_container);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return true;
            }
        });

        // setup menu icon
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        onMenuItemSelected();

    }

    public void gotoStream () {
        StreamFragment fragment = new StreamFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    public void gotoLogs (Bundle bundle) {
        LogFragment fragment = new LogFragment();
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onMenuItemSelected() {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    if (item.isCheckable()) {
                        item.setChecked(!item.isChecked());
                    }

                    drawerLayout.closeDrawers();

                    switch (item.getItemId()) {
                        case R.id.stream:
                            Log.i("HOME", "triggered");
                            gotoStream();
                            break;
                    }

                    return false;
                }
            }
        );
    }


}
