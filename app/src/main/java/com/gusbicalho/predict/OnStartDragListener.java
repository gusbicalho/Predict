package com.gusbicalho.predict;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Gustavo on 10/08/2015.
 */
public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}