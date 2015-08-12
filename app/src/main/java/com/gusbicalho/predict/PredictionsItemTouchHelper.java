package com.gusbicalho.predict;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by Gustavo on 10/08/2015.
 */
public class PredictionsItemTouchHelper extends ItemTouchHelper {
    private final PredictionsAdapter mAdapter;

    public PredictionsItemTouchHelper(PredictionsAdapter adapter) {
        super(new Callback(adapter));
        mAdapter = adapter;
    }

    @Override
    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() != mAdapter)
            throw new IllegalStateException("Adapter on RecyclerView must be the same given on constructor");
        super.attachToRecyclerView(recyclerView);
    }

    private static class Callback extends ItemTouchHelper.SimpleCallback {
        private final PredictionsAdapter mPredictionsAdapter;

        public Callback(@NonNull PredictionsAdapter adapter) {
            super(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mPredictionsAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mPredictionsAdapter.dismiss(viewHolder, direction);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                        actionState, isCurrentlyActive);
            }
            PredictionsAdapter.PredictionsAdapterViewHolder vh = (PredictionsAdapter.PredictionsAdapterViewHolder) viewHolder;
            if (dX > 0.0) {
                vh.mSwipeRight.setVisibility(View.VISIBLE);
                vh.mSwipeLeft.setVisibility(View.INVISIBLE);
            } else {
                vh.mSwipeRight.setVisibility(View.INVISIBLE);
                vh.mSwipeLeft.setVisibility(View.VISIBLE);
            }
            vh.mContainer.setTranslationX(dX);
        }
    }
}