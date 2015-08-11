package com.gusbicalho.predict;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
    private PredictionsItemTouchHelper mItemTouchHelper;
    private OnStartDragListener mItemDragListener = new OnStartDragListener() {
        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };

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

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_prediction);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        mPredictionsAdapter = new PredictionsAdapter(mItemDragListener);
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
                null, null);
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
