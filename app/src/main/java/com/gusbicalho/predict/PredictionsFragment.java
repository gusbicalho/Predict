package com.gusbicalho.predict;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PredictionsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PredictionsAdapter mPredictionsAdapter;
    private RecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                public boolean isLongPressDragEnabled() {
                    return false;
                }
                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    final int fromPos = viewHolder.getAdapterPosition();
                    final int toPos = target.getAdapterPosition();
                    return mPredictionsAdapter.move(fromPos, toPos);
                }
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    mPredictionsAdapter.dismiss(viewHolder, direction);
                }
            });
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_prediction);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        mPredictionsAdapter = new PredictionsAdapter(mItemDragListener);
        mRecyclerView.setAdapter(mPredictionsAdapter);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
