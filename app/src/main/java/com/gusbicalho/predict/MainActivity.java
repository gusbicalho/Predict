package com.gusbicalho.predict;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements PredictionsFragment.Callback {

    private static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        if (savedInstanceState == null) {
            PredictionsFragment fragment = new PredictionsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, MAIN_FRAGMENT_TAG)
                    .commit();
        }
        */

        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fabNewPrediction = (FloatingActionButton) findViewById(R.id.fab_new_prediction);
        fabNewPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewPredictionAction();
            }
        });

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final MainNavigationPagerAdapter pagerAdapter = new MainNavigationPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    /*
    private void showPredictionsFragment(int resultFilter) {
        PredictionsFragment fragment = PredictionsFragment.newInstance(resultFilter);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, MAIN_FRAGMENT_TAG)
                .commit();

    }
    */

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
