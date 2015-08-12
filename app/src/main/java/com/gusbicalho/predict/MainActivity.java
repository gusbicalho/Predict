package com.gusbicalho.predict;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity
        implements PredictionsFragment.Callback {

    private static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            PredictionsFragment fragment = new PredictionsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, MAIN_FRAGMENT_TAG)
                    .commit();
        }


        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navView = (NavigationView) findViewById(R.id.drawer_main);
        final ImageButton bShowNavDrawer = (ImageButton) findViewById(R.id.button_show_nav_drawer);

        bShowNavDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navView);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked())
                    return true;
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_predictions_open: {
                        showPredictionsFragment(0);
                        menuItem.setChecked(true);
                        return true;
                    }
                    case R.id.nav_item_predictions_right: {
                        showPredictionsFragment(1);
                        menuItem.setChecked(true);
                        return true;
                    }
                    case R.id.nav_item_predictions_wrong: {
                        showPredictionsFragment(-1);
                        menuItem.setChecked(true);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private void showPredictionsFragment(int resultFilter) {
        PredictionsFragment fragment = PredictionsFragment.newInstance(resultFilter);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, MAIN_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onNewPredictionAction() {
        startActivity(new Intent(this, NewPredictionActivity.class));
    }
}
