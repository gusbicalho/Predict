package com.gusbicalho.predict;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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
        public final View mReorderHandle;
        private Prediction mPrediction;
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
            mReorderHandle = view.findViewById(R.id.list_item_prediction_handle);
            view.setOnClickListener(this);
            mReorderHandle.setOnLongClickListener(this);
            view.setOnLongClickListener(this);
        }

        public Prediction getPrediction() {
            return mPrediction;
        }

        public void setPrediction(Prediction prediction) {
            this.mPrediction = prediction;
        }

        @Override
        public void onClick(View v) {
            if (mPrediction != null) {
                if (expanded.contains(mPrediction))
                    expanded.remove(mPrediction);
                else
                    expanded.add(mPrediction);
                PredictionsAdapter.this.notifyDataSetChanged();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (v == mReorderHandle && mStartDragListener != null) {
                Toast.makeText(v.getContext(), "Handle long clicked!", Toast.LENGTH_SHORT).show();
                mStartDragListener.onStartDrag(this);
                return true;
            }
            Toast.makeText(v.getContext(), "Long clicked!", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private static final int VIEW_TYPE_SMALL = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    @Nullable
    private OnStartDragListener mStartDragListener;
    private Vector<Prediction> preds = new Vector<>(Arrays.asList(new Prediction[]{
            new Prediction("Some sample question", "Some observation about the nature of this well pondered prediction",
                    "Answer! Bacon ipsum dolor amet pastrami ground round pork, ham hock bacon alcatra shoulder pork belly leberkas tail tenderloin flank salami boudin short loin.",
                    0.3),
            new Prediction("Will Dilma step out until end of year?", null, true, 0.4d),
            new Prediction("How many kilometers walked?", null, 6.0d, 23.0d, false, 0.8d),
            new Prediction("What will The Silence be?", null, "Mindwash of the universe", 0.2),
            new Prediction("Never gonna give you up?", null, "Never gonna let you down!\nNever gonna come around, and hurt you!", 0.2),
    }));
    private Set<Prediction> expanded = new HashSet<>();

    public PredictionsAdapter(@Nullable OnStartDragListener startDragListener) {
        this.mStartDragListener = startDragListener;
    }

    @Override
    public int getItemViewType(int position) {
        return expanded.contains(preds.get(position)) ? VIEW_TYPE_EXPANDED : VIEW_TYPE_SMALL;
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
        Prediction pred = preds.get(position);
        viewHolder.setPrediction(pred);

        Context context = viewHolder.mAnswer.getContext();
        viewHolder.mContainer.setTranslationX(0.0f);
        viewHolder.mQuestion.setText(pred.getQuestion());
        viewHolder.mConfidence.setText(context.getString(R.string.prediction_confidence, 100.0 * pred.getConfidence()));
        if (viewHolder.mDetail != null) {
            viewHolder.mDetail.setText(
                    pred.getDetail() != null ?
                        context.getString(R.string.prediction_detail, pred.getDetail()) :
                        null);
            viewHolder.mDetail.setVisibility(
                    pred.getDetail() != null ? View.VISIBLE : View.GONE
            );
        }
        bindAnswer(viewHolder, pred);
    }

    private void bindAnswer(PredictionsAdapterViewHolder viewHolder, Prediction pred) {
        Context context = viewHolder.mAnswer.getContext();
        switch (pred.getAnswerType()) {
            case Prediction.ANSWER_TYPE_BOOLEAN: {
                boolean boolAnswer = (Boolean) pred.getAnswer();
                viewHolder.mAnswer.setText(boolAnswer ? R.string.prediction_answer_true : R.string.prediction_answer_false);
                break;
            }
            case Prediction.ANSWER_TYPE_EXCLUSIVE_RANGE:
            case Prediction.ANSWER_TYPE_INCLUSIVE_RANGE: {
                Pair<Double, Double> rangeAnswer = (Pair<Double, Double>) pred.getAnswer();
                boolean isExclusive = pred.getAnswerType() == Prediction.ANSWER_TYPE_EXCLUSIVE_RANGE;
                viewHolder.mAnswer.setText(context.getString(
                        isExclusive ? R.string.prediction_answer_exclusive_range : R.string.prediction_answer_inclusive_range,
                        rangeAnswer.first, rangeAnswer.second
                ));
                break;
            }
            case Prediction.ANSWER_TYPE_TEXT: {
                viewHolder.mAnswer.setText(pred.getAnswer().toString());
                break;
            }
            default:
                viewHolder.mAnswer.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return preds.size();
    }

    public boolean move(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        final int fromPos = viewHolder.getAdapterPosition();
        final int toPos = target.getAdapterPosition();
        Prediction pred = preds.get(fromPos);
        preds.remove(fromPos);
        preds.add(toPos, pred);
        notifyItemMoved(fromPos, toPos);
        return true;
    }

    public void dismiss(RecyclerView.ViewHolder viewHolder, int direction) {
        final int pos = viewHolder.getAdapterPosition();
        final Context context = viewHolder.itemView.getContext();
        final Prediction pred = preds.remove(pos);
        notifyItemRemoved(pos);
        String msg = context.getString(
                direction == ItemTouchHelper.RIGHT ?
                        R.string.prediction_dismiss_snackbar_right :
                        R.string.prediction_dismiss_snackbar_wrong,
                pred.getQuestion()
        );
        Snackbar.make(viewHolder.itemView, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.prediction_dismiss_snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preds.add(pos, pred);
                        notifyItemInserted(pos);
                    }
                })
                .show();
    }
}
