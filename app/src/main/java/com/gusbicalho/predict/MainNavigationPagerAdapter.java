package com.gusbicalho.predict;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainNavigationPagerAdapter extends FragmentPagerAdapter {

    private static final int[] PAGES_TITLES_RES_IDS = new int[] {
            R.string.tab_title_predictions_open,
            R.string.tab_title_predictions_right,
            R.string.tab_title_predictions_wrong,
            R.string.tab_title_stats
    };
    private static final int INDEX_PREDICTIONS_OPEN = 0;
    private static final int INDEX_PREDICTIONS_RIGHT = 1;
    private static final int INDEX_PREDICTIONS_WRONG = 2;
    private static final int INDEX_STATS = 3;

    private final Context mContext;

    public MainNavigationPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < 0 || position >= PAGES_TITLES_RES_IDS.length)
            return null;
        return mContext.getString(PAGES_TITLES_RES_IDS[position]);
    }

    @Override
    public int getCount() {
        return PAGES_TITLES_RES_IDS.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_PREDICTIONS_OPEN:
                return PredictionsFragment.newInstance(PredictionsFragment.RESULT_FILTER_OPEN);
            case INDEX_PREDICTIONS_RIGHT:
                return PredictionsFragment.newInstance(PredictionsFragment.RESULT_FILTER_RIGHT);
            case INDEX_PREDICTIONS_WRONG:
                return PredictionsFragment.newInstance(PredictionsFragment.RESULT_FILTER_WRONG);
            case INDEX_STATS:
                return StatsFragment.newInstance();
        }
        return null;
    }
}
