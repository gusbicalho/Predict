package com.gusbicalho.predict;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.gusbicalho.predict.data.PredictionsContract;
import com.gusbicalho.predict.data.PredictionsProvider;

import java.util.Arrays;


public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SCORE_LOADER = 1;

    private StatsFragmentCallback mListener;
    private TextView mMainScore;
    private BarChart mCalibrationChart;
    private BarDataSet mDataSetPerfect;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SCORE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Params
        }

        BarEntry[] entries = new BarEntry[NewPredictionFragment.CREDENCE_SCORES.length];
        for (int i = 0; i < NewPredictionFragment.CREDENCE_SCORES.length; i++) {
            entries[i] = new BarEntry((float)NewPredictionFragment.CREDENCE_SCORES[i]*100.0f, i);
        }
        mDataSetPerfect = new BarDataSet(
                Arrays.asList(entries),
                getString(R.string.calibration_graph_label_reference)
        );
        mDataSetPerfect.setColor(getResources().getColor(R.color.accent));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        mMainScore = (TextView) rootView.findViewById(R.id.stats_main_score);
        mCalibrationChart = (BarChart) rootView.findViewById(R.id.chart_calibration);

        mCalibrationChart.setTouchEnabled(false);
        mCalibrationChart.setVisibleYRangeMaximum(100.f, YAxis.AxisDependency.LEFT);
        mCalibrationChart.setVisibleYRangeMaximum(100.f, YAxis.AxisDependency.RIGHT);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (StatsFragmentCallback) activity;
        } catch (ClassCastException e) {
            mListener = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                PredictionsContract.PredictionEntry.CONTENT_URI,
                PredictionsProvider.Util.PREDICTION_PROJECTION,
                PredictionsContract.PredictionEntry.COLUMN_RESULT + " <> 0",
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int score = 0;
        int[] questionsPerCredenceInterval = new int[NewPredictionFragment.CREDENCE_SCORES.length];
        int[] rightAnswersPerCredenceInterval = new int[NewPredictionFragment.CREDENCE_SCORES.length];
        if (data.moveToFirst())
            do {
                int result = data.getInt(PredictionsProvider.Util.INDEX_RESULT);
                double credence = data.getDouble(PredictionsProvider.Util.INDEX_CREDENCE);
                score += Math.round(PredictionsProvider.Util.calcScore(credence, result));
                int i = 0;
                for (; i < NewPredictionFragment.CREDENCE_SCORES.length && credence >= NewPredictionFragment.CREDENCE_SCORES[i]; i++);
                if (i >= NewPredictionFragment.CREDENCE_SCORES.length)
                    i = NewPredictionFragment.CREDENCE_SCORES.length - 1;
                else if (i >= 0) i--;
                questionsPerCredenceInterval[i]++;
                if (result == PredictionsFragment.RESULT_FILTER_RIGHT)
                    rightAnswersPerCredenceInterval[i]++;
            } while(data.moveToNext());
        mMainScore.setText(""+score);

        String[] labels = new String[NewPredictionFragment.CREDENCE_SCORES.length];
        BarEntry[] entries = new BarEntry[NewPredictionFragment.CREDENCE_SCORES.length];
        for (int i = 0; i < NewPredictionFragment.CREDENCE_SCORES.length; i++) {
            labels[i] = getString(R.string.edit_prediction_credence_value_label, (int)(NewPredictionFragment.CREDENCE_SCORES[i]*100));
            if (questionsPerCredenceInterval[i] == 0)
                entries[i] = new BarEntry(0, i);
            else
                entries[i] = new BarEntry(100.0f*rightAnswersPerCredenceInterval[i]/questionsPerCredenceInterval[i], i);
        }

        final BarDataSet dataSet = new BarDataSet(
                Arrays.asList(entries),
                getString(R.string.calibration_graph_label_score)
        );
        dataSet.setColor(getResources().getColor(R.color.primary_dark));

        BarData barData = new BarData(
                Arrays.asList(labels),
                Arrays.asList(dataSet,mDataSetPerfect)
        );
        mCalibrationChart.setData(barData);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMainScore.setText(null);
    }

    public interface StatsFragmentCallback {
        // TODO: Update argument type and name
        public void onStatsFragmentAction();
    }

}
