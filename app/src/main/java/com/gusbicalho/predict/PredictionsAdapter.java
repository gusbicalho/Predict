package com.gusbicalho.predict;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gusbicalho.predict.data.PredictionsContract;
import com.gusbicalho.predict.data.PredictionsProvider;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.PredictionsAdapterViewHolder> {
    private static final String TAG = PredictionsAdapter.class.getSimpleName();

    private static final int SWIPE_DIRECTION_RIGHT = ItemTouchHelper.RIGHT;
    private static final int SWIPE_DIRECTION_WRONG = ItemTouchHelper.LEFT;
    private final int mResultFilter;

    public class PredictionsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final View mBackground;
        public final View mSwipeLeft;
        public final View mSwipeRight;
        public final View mContainer;
        public final TextView mQuestion;
        public final TextView mAnswer;
        @Nullable
        public final TextView mDetail;
        public final TextView mCredence;
        public final TextView mCreationDate;
        public final View mActionRight;
        public final View mActionWrong;
        public final View mActionReopen;
        public final View mActionDelete;
        private Long mPredictionId;
        private AlertDialog mLongClickDialog;
        public PredictionsAdapterViewHolder(View view) {
            super(view);
            mBackground = view.findViewById(R.id.list_item_background);
            mSwipeLeft = view.findViewById(R.id.list_item_bg_swipe_left);
            mSwipeRight = view.findViewById(R.id.list_item_bg_swipe_right);
            mContainer = view.findViewById(R.id.list_item_container);
            mQuestion = (TextView) view.findViewById(R.id.list_item_prediction_question);
            mDetail = (TextView) view.findViewById(R.id.list_item_prediction_detail);
            mAnswer = (TextView) view.findViewById(R.id.list_item_prediction_answer);
            mCredence = (TextView) view.findViewById(R.id.list_item_prediction_credence);
            mCreationDate = (TextView) view.findViewById(R.id.list_item_prediction_creation_date);
            mActionRight = view.findViewById(R.id.list_item_expanded_button_right);
            mActionWrong = view.findViewById(R.id.list_item_expanded_button_wrong);
            mActionReopen = view.findViewById(R.id.list_item_expanded_button_reopen);
            mActionDelete = view.findViewById(R.id.list_item_expanded_button_delete);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            if (mActionRight != null && mActionWrong != null &&
                    mActionReopen != null && mActionDelete != null) {
                (mResultFilter == PredictionsFragment.RESULT_FILTER_WRONG ?
                        mActionWrong :
                        mResultFilter == PredictionsFragment.RESULT_FILTER_RIGHT ?
                                mActionRight :
                                mActionReopen).setVisibility(View.GONE);
                mActionRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss(PredictionsAdapterViewHolder.this, SWIPE_DIRECTION_RIGHT);
                    }
                });
                mActionWrong.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss(PredictionsAdapterViewHolder.this, SWIPE_DIRECTION_WRONG);
                    }
                });
                mActionReopen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reopen(PredictionsAdapterViewHolder.this);
                    }
                });
                mActionDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete(PredictionsAdapterViewHolder.this);
                    }
                });
            }

            mLongClickDialog = makeLongClickDialog();
        }

        private AlertDialog makeLongClickDialog() {
            final LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            @SuppressLint("InflateParams")
            final View dialogRootView = inflater.inflate(
                    mResultFilter == PredictionsFragment.RESULT_FILTER_WRONG ?
                            R.layout.list_item_prediction_long_click_dialog_wrong :
                            mResultFilter == PredictionsFragment.RESULT_FILTER_RIGHT ?
                                    R.layout.list_item_prediction_long_click_dialog_right :
                                    R.layout.list_item_prediction_long_click_dialog_open,
                    null);

            final Button bRight = (Button) dialogRootView.findViewById(R.id.list_item_prediction_long_click_dialog_right);
            if (bRight != null)
                bRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss(PredictionsAdapterViewHolder.this, SWIPE_DIRECTION_RIGHT);
                        mLongClickDialog.dismiss();
                    }
                });

            final Button bWrong = (Button) dialogRootView.findViewById(R.id.list_item_prediction_long_click_dialog_wrong);
            if (bWrong != null)
                bWrong.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss(PredictionsAdapterViewHolder.this, SWIPE_DIRECTION_WRONG);
                        mLongClickDialog.dismiss();
                    }
                });

            final Button bReopen = (Button) dialogRootView.findViewById(R.id.list_item_prediction_long_click_dialog_reopen);
            if (bReopen != null)
                bReopen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reopen(PredictionsAdapterViewHolder.this);
                        mLongClickDialog.dismiss();
                    }
                });

            final Button bDelete = (Button) dialogRootView.findViewById(R.id.list_item_prediction_long_click_dialog_delete);
            bDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(PredictionsAdapterViewHolder.this);
                    mLongClickDialog.dismiss();
                }
            });

            return new AlertDialog.Builder(itemView.getContext())
                    .setView(dialogRootView)
                    .create();
        }

        public Long getPredictionId() {
            return mPredictionId;
        }

        public void setPredictionId(Long predictionId) {
            this.mPredictionId = predictionId;
        }

        @Override
        public void onClick(View v) {
            if (v == this.itemView) {
                if (mPredictionId != null) {
                    if (expanded.contains(mPredictionId))
                        expanded.remove(mPredictionId);
                    else
                        expanded.add(mPredictionId);
                    PredictionsAdapter.this.notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mLongClickDialog.show();
            return true;
        }
    }

    private static final int VIEW_TYPE_SMALL = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    private Cursor mCursor;
    private Set<Long> expanded = new HashSet<>();

    public PredictionsAdapter(int resultFilter) {
        mResultFilter = resultFilter;
    }

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
        viewHolder.mCredence.setText(
                context.getString(R.string.prediction_credence,
                        100.0 * mCursor.getDouble(PredictionsProvider.Util.INDEX_CREDENCE)));
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

    public void dismiss(final RecyclerView.ViewHolder viewHolder, final int direction) {
        final Context context = viewHolder.itemView.getContext();

        final int pos = viewHolder.getAdapterPosition();
        mCursor.moveToPosition(pos);
        final long remId = mCursor.getLong(PredictionsProvider.Util.INDEX_ID);
        final String question = mCursor.getString(PredictionsProvider.Util.INDEX_QUESTION);
        final int score = (int) PredictionsProvider.Util.calcScore(
                mCursor.getDouble(PredictionsProvider.Util.INDEX_CREDENCE),
                direction == SWIPE_DIRECTION_RIGHT ? 1 : -1
        );

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return PredictionsProvider.Util.setPredictionResult(context, remId, direction == SWIPE_DIRECTION_RIGHT ? 1 : -1);
            }

            @Override
            protected void onPostExecute(Boolean removed) {
                if (!removed)
                    return;
                expanded.remove(remId);
                notifyItemRemoved(pos);

                String msg = context.getString(
                        direction == SWIPE_DIRECTION_RIGHT ?
                                R.string.prediction_dismiss_snackbar_right :
                                R.string.prediction_dismiss_snackbar_wrong,
                        question, score
                );
                Snackbar.make(viewHolder.itemView, msg, Snackbar.LENGTH_LONG)
                        .setAction(R.string.prediction_dismiss_snackbar_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AsyncTask<Void, Void, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... params) {
                                        return PredictionsProvider.Util.setPredictionResult(context, remId, mResultFilter);
                                    }
                                }.execute();
                            }
                        })
                        .show();
            }
        }.execute();
    }

    public void reopen(final RecyclerView.ViewHolder viewHolder) {
        final Context context = viewHolder.itemView.getContext();
        final int pos = viewHolder.getAdapterPosition();
        mCursor.moveToPosition(pos);
        final long remId = mCursor.getLong(PredictionsProvider.Util.INDEX_ID);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return PredictionsProvider.Util.setPredictionResult(context, remId, PredictionsFragment.RESULT_FILTER_OPEN);
            }

            @Override
            protected void onPostExecute(Boolean removed) {
                if (!removed)
                    return;
                expanded.remove(remId);
                notifyItemRemoved(pos);
            }
        }.execute();
    }

    public void delete(final RecyclerView.ViewHolder viewHolder) {
        final Context context = viewHolder.itemView.getContext();

        final int pos = viewHolder.getAdapterPosition();
        mCursor.moveToPosition(pos);
        final long remId = mCursor.getLong(PredictionsProvider.Util.INDEX_ID);
        final String question = mCursor.getString(PredictionsProvider.Util.INDEX_QUESTION);

        final ContentValues predValues = new ContentValues();
        for (int i = 0; i < mCursor.getColumnCount(); i++) {
            switch (mCursor.getType(i)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    predValues.put(mCursor.getColumnName(i), mCursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    predValues.put(mCursor.getColumnName(i), mCursor.getDouble(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    predValues.put(mCursor.getColumnName(i), mCursor.getString(i));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    predValues.put(mCursor.getColumnName(i), mCursor.getBlob(i));
                    break;
                default: // null
            }
        }

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return PredictionsProvider.Util.removePrediction(context, remId);
            }

            @Override
            protected void onPostExecute(Boolean removed) {
                if (!removed)
                    return;
                expanded.remove(remId);
                notifyItemRemoved(pos);

                String msg = context.getString(R.string.prediction_dismiss_snackbar_delete, question);
                Snackbar.make(viewHolder.itemView, msg, Snackbar.LENGTH_LONG)
                        .setAction(R.string.prediction_dismiss_snackbar_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AsyncTask<Void, Void, Uri>() {
                                    @Override
                                    protected Uri doInBackground(Void... params) {
                                        return context.getContentResolver().insert(PredictionsContract.PredictionEntry.CONTENT_URI, predValues);
                                    }
                                }.execute();

                            }
                        })
                        .show();
            }
        }.execute();
    }
}
