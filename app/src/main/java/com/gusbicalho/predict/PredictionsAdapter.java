package com.gusbicalho.predict;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gusbicalho.predict.data.PredictionsContract;
import com.gusbicalho.predict.data.PredictionsProvider;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.PredictionsAdapterViewHolder> {
    private static final String TAG = PredictionsAdapter.class.getSimpleName();

    public class PredictionsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final View mBackground;
        public final View mSwipeLeft;
        public final View mSwipeRight;
        public final View mContainer;
        public final TextView mQuestion;
        public final TextView mAnswer;
        @Nullable
        public final TextView mDetail;
        public final TextView mConfidence;
        public final TextView mCreationDate;
        private Long mPredictionId;
        public PredictionsAdapterViewHolder(View view) {
            super(view);
            mBackground = view.findViewById(R.id.list_item_background);
            mSwipeLeft = view.findViewById(R.id.list_item_bg_swipe_left);
            mSwipeRight = view.findViewById(R.id.list_item_bg_swipe_right);
            mContainer = view.findViewById(R.id.list_item_container);
            mQuestion = (TextView) view.findViewById(R.id.list_item_prediction_question);
            mDetail = (TextView) view.findViewById(R.id.list_item_prediction_detail);
            mAnswer = (TextView) view.findViewById(R.id.list_item_prediction_answer);
            mConfidence = (TextView) view.findViewById(R.id.list_item_prediction_confidence);
            mCreationDate = (TextView) view.findViewById(R.id.list_item_prediction_creation_date);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public Long getPredictionId() {
            return mPredictionId;
        }

        public void setPredictionId(Long predictionId) {
            this.mPredictionId = predictionId;
        }

        @Override
        public void onClick(View v) {
            if (mPredictionId != null) {
                if (expanded.contains(mPredictionId))
                    expanded.remove(mPredictionId);
                else
                    expanded.add(mPredictionId);
                PredictionsAdapter.this.notifyDataSetChanged();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(v.getContext(), "Long clicked!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private static final int VIEW_TYPE_SMALL = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    private Cursor mCursor;
    private Set<Long> expanded = new HashSet<>();

    public PredictionsAdapter() {}

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        long id = mCursor.getLong(PredictionsProvider.Util.INDEX_ID);
        return expanded.contains(id) ? VIEW_TYPE_EXPANDED : VIEW_TYPE_SMALL;
    }

    @Override
    public PredictionsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId =
                viewType == VIEW_TYPE_EXPANDED ? R.layout.list_item_prediction_expanded :
                        R.layout.list_item_prediction;
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);
        return new PredictionsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PredictionsAdapterViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        viewHolder.setPredictionId(mCursor.getLong(PredictionsProvider.Util.INDEX_ID));

        Context context = viewHolder.mAnswer.getContext();
        viewHolder.mContainer.setTranslationX(0.0f);
        viewHolder.mQuestion.setText(mCursor.getString(PredictionsProvider.Util.INDEX_QUESTION));
        viewHolder.mConfidence.setText(
                context.getString(R.string.prediction_confidence,
                        100.0 * mCursor.getDouble(PredictionsProvider.Util.INDEX_CONFIDENCE)));
        if (viewHolder.mDetail != null) {
            String detail = mCursor.getString(PredictionsProvider.Util.INDEX_DETAIL);
            viewHolder.mDetail.setText(
                    detail != null && !detail.isEmpty() ? context.getString(R.string.prediction_detail, detail) : null);
            viewHolder.mDetail.setVisibility(
                    detail != null && !detail.isEmpty() ? View.VISIBLE : View.GONE
            );
        }
        viewHolder.mCreationDate.setText(
                DateFormat
                        .getDateFormat(context)
                        .format(new Date(mCursor.getLong(PredictionsProvider.Util.INDEX_CREATION_DATE)*1000L))
        );
        bindAnswer(viewHolder, mCursor);
    }

    private void bindAnswer(PredictionsAdapterViewHolder viewHolder, Cursor cursor) {
        Context context = viewHolder.itemView.getContext();

        @PredictionsContract.PredictionEntry.AnswerType
        int answerType = cursor.getInt(PredictionsProvider.Util.INDEX_ANSWER_TYPE);

        switch (answerType) {
            case PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN: {
                boolean boolAnswer = cursor.getInt(PredictionsProvider.Util.INDEX_ANSWER_BOOLEAN) != 0;
                viewHolder.mAnswer.setText(boolAnswer ? R.string.prediction_answer_true : R.string.prediction_answer_false);
                break;
            }
            case PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE:
            case PredictionsContract.PredictionEntry.ANSWER_TYPE_INCLUSIVE_RANGE: {
                boolean isExclusive = answerType == PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE;
                double answerMin = cursor.getDouble(PredictionsProvider.Util.INDEX_ANSWER_MIN);
                double answerMax = cursor.getDouble(PredictionsProvider.Util.INDEX_ANSWER_MAX);
                viewHolder.mAnswer.setText(context.getString(
                        isExclusive ? R.string.prediction_answer_exclusive_range : R.string.prediction_answer_inclusive_range,
                        answerMin, answerMax
                ));
                break;
            }
            case PredictionsContract.PredictionEntry.ANSWER_TYPE_TEXT: {
                viewHolder.mAnswer.setText(cursor.getString(PredictionsProvider.Util.INDEX_ANSWER_TEXT));
                break;
            }
            default:
                viewHolder.mAnswer.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void dismiss(RecyclerView.ViewHolder viewHolder, int direction) {
        final Context context = viewHolder.itemView.getContext();

        final int pos = viewHolder.getAdapterPosition();
        mCursor.moveToPosition(pos);
        final long remId = mCursor.getLong(PredictionsProvider.Util.INDEX_ID);
        final String question = mCursor.getString(PredictionsProvider.Util.INDEX_QUESTION);

        PredictionsProvider.Util.setPredictionResult(context, remId, direction == ItemTouchHelper.RIGHT ? 1 : -1);
        expanded.remove(remId);

        String msg = context.getString(
                direction == ItemTouchHelper.RIGHT ?
                        R.string.prediction_dismiss_snackbar_right :
                        R.string.prediction_dismiss_snackbar_wrong,
                question
        );
        Snackbar.make(viewHolder.itemView, msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.prediction_dismiss_snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PredictionsProvider.Util.setPredictionResult(context, remId, 0);
                    }
                })
                .show();
    }
}
