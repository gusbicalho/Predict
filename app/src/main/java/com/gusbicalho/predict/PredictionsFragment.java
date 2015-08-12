package com.gusbicalho.predict;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gusbicalho.predict.data.PredictionsContract;
import com.gusbicalho.predict.data.PredictionsProvider;
import com.gusbicalho.predict.util.DividerItemDecoration;

public class PredictionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PREDICTIONS_LOADER = 0;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String RESULT_FILTER = "RESULT_FILTER";
    public static final int RESULT_FILTER_OPEN = 0;
    public static final int RESULT_FILTER_RIGHT = 1;
    public static final int RESULT_FILTER_WRONG = -1;

    private int mResultFilter = 0;
    private PredictionsAdapter mPredictionsAdapter;
    private View mEmptyView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PredictionsFragment newInstance(int resultFilter) {
        PredictionsFragment fragment = new PredictionsFragment();
        Bundle args = new Bundle();
        args.putInt(RESULT_FILTER, (int) Math.signum(resultFilter));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of events.
     */
    public interface Callback {
        void onNewPredictionAction();
    }

    public PredictionsFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PREDICTIONS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mResultFilter = arguments.getInt(RESULT_FILTER, RESULT_FILTER_OPEN);
        }

        mEmptyView = rootView.findViewById(R.id.recyclerview_prediction_empty);

        final FloatingActionButton fabNewPrediction = (FloatingActionButton) rootView.findViewById(R.id.fab_new_prediction);
        fabNewPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof Callback)
                    ((Callback) getActivity()).onNewPredictionAction();
            }
        });

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_prediction);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), null, true, true));

        mPredictionsAdapter = new PredictionsAdapter(mResultFilter);
        recyclerView.setAdapter(mPredictionsAdapter);

        if (mResultFilter == RESULT_FILTER_OPEN) {
            PredictionsItemTouchHelper itemTouchHelper = new PredictionsItemTouchHelper(mPredictionsAdapter);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        } else {
            fabNewPrediction.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                PredictionsContract.PredictionEntry.CONTENT_URI,
                PredictionsProvider.Util.PREDICTION_PROJECTION,
                PredictionsContract.PredictionEntry.COLUMN_RESULT + " = " + mResultFilter,
                null, PredictionsContract.PredictionEntry.COLUMN_CREATION_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst())
            mEmptyView.setVisibility(View.GONE);
        else if (mResultFilter == RESULT_FILTER_OPEN)
            mEmptyView.setVisibility(View.VISIBLE);
        mPredictionsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPredictionsAdapter.swapCursor(null);
    }
}
