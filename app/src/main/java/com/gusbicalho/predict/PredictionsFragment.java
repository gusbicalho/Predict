package com.gusbicalho.predict;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PredictionsAdapter mPredictionsAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private FloatingActionButton mFabNewPrediction;
    private PredictionsItemTouchHelper mItemTouchHelper;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PredictionsFragment newInstance(int sectionNumber) {
        PredictionsFragment fragment = new PredictionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mEmptyView = rootView.findViewById(R.id.recyclerview_prediction_empty);

        mFabNewPrediction = (FloatingActionButton) rootView.findViewById(R.id.fab_new_prediction);
        mFabNewPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof Callback)
                    ((Callback) getActivity()).onNewPredictionAction();
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_prediction);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), null));

        mPredictionsAdapter = new PredictionsAdapter();
        mRecyclerView.setAdapter(mPredictionsAdapter);
        mItemTouchHelper = new PredictionsItemTouchHelper(mPredictionsAdapter);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                PredictionsContract.PredictionEntry.CONTENT_URI,
                PredictionsProvider.Util.PREDICTION_PROJECTION,
                PredictionsContract.PredictionEntry.COLUMN_RESULT + " = 0",
                null, PredictionsContract.PredictionEntry.COLUMN_CREATION_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst())
            mEmptyView.setVisibility(View.GONE);
        else
            mEmptyView.setVisibility(View.VISIBLE);
        mPredictionsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPredictionsAdapter.swapCursor(null);
    }
}
