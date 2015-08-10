package com.gusbicalho.predict;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by Gustavo on 05/08/2015.
 */
public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.PredictionsAdapterViewHolder> {
    private static final String TAG = PredictionsAdapter.class.getSimpleName();

    public class PredictionsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView mShortDesc;
        public final TextView mAnswer;
        @Nullable
        public final TextView mDetail;
        public final TextView mConfidence;
        public final View mReorderHandle;
        private Prediction mPrediction;
        public PredictionsAdapterViewHolder(View view) {
            super(view);
            mShortDesc = (TextView) view.findViewById(R.id.list_item_prediction_shortDescription);
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

    private static final int ANSWER_TYPE_BOOLEAN = 0;
    private static final int ANSWER_TYPE_EXCLUSIVE_RANGE = 1;
    private static final int ANSWER_TYPE_INCLUSIVE_RANGE = 2;
    private static final int ANSWER_TYPE_TEXT = 3;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ANSWER_TYPE_BOOLEAN, ANSWER_TYPE_EXCLUSIVE_RANGE, ANSWER_TYPE_INCLUSIVE_RANGE, ANSWER_TYPE_TEXT})
    private @interface AnswerType {}

    private static final int VIEW_TYPE_SMALL = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    private static class Prediction {
        private String mShortDesc;
        private String mDetail;
        @AnswerType private int mAnswerType;
        private Object mAnswer;
        private double mConfidence;

        private Prediction(String shortDesc, String longDesc, int answerType, Object answer, double confidence) {
            switch (answerType) {
                case ANSWER_TYPE_EXCLUSIVE_RANGE:
                case ANSWER_TYPE_INCLUSIVE_RANGE: {
                    Pair<Double, Double> range = (Pair<Double, Double>) answer;
                    if (range.second < range.first) {
                        answer = new Pair<>(range.second, range.first);
                    }
                    break;
                }
            }
            this.mShortDesc = shortDesc;
            this.mDetail = longDesc;
            this.mAnswerType = answerType;
            this.mAnswer = answer;
            this.mConfidence = confidence;
        }

        public Prediction(String shortDesc, String longDesc, boolean answer, double confidence) {
            this(shortDesc, longDesc, ANSWER_TYPE_BOOLEAN, answer, confidence);
        }
        public Prediction(String shortDesc, String longDesc, Pair<Double, Double> answer, boolean exclusive, double confidence) {
            this(shortDesc, longDesc, exclusive ? ANSWER_TYPE_EXCLUSIVE_RANGE : ANSWER_TYPE_INCLUSIVE_RANGE, answer, confidence);
        }
        public Prediction(String shortDesc, String longDesc, double answerMin, double answerMax, boolean exclusive, double confidence) {
            this(shortDesc, longDesc, new Pair<>(answerMin, answerMax), exclusive, confidence);
        }
        public Prediction(String shortDesc, String longDesc, String answer, double confidence) {
            this(shortDesc, longDesc, ANSWER_TYPE_TEXT, answer, confidence);
        }

        public String getShortDesc() {
            return mShortDesc;
        }

        public String getDetail() {
            return mDetail;
        }

        @AnswerType
        public int getAnswerType() {
            return mAnswerType;
        }

        public Object getAnswer() {
            return mAnswer;
        }

        public double getConfidence() {
            return mConfidence;
        }
    }

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
        viewHolder.mShortDesc.setText(pred.getShortDesc());
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
            case ANSWER_TYPE_BOOLEAN: {
                boolean boolAnswer = (Boolean) pred.getAnswer();
                viewHolder.mAnswer.setText(boolAnswer ? R.string.prediction_answer_true : R.string.prediction_answer_false);
                break;
            }
            case ANSWER_TYPE_EXCLUSIVE_RANGE:
            case ANSWER_TYPE_INCLUSIVE_RANGE: {
                Pair<Double, Double> rangeAnswer = (Pair<Double, Double>) pred.getAnswer();
                boolean isExclusive = pred.getAnswerType() == ANSWER_TYPE_EXCLUSIVE_RANGE;
                viewHolder.mAnswer.setText(context.getString(
                        isExclusive ? R.string.prediction_answer_exclusive_range : R.string.prediction_answer_inclusive_range,
                        rangeAnswer.first, rangeAnswer.second
                ));
                break;
            }
            case ANSWER_TYPE_TEXT: {
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

    public boolean move(int fromPos, int toPos) {
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
        Snackbar.make(viewHolder.itemView, "Dismissed \""+pred.getShortDesc()+"\"", Snackbar.LENGTH_INDEFINITE)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preds.add(pos, pred);
                        notifyItemInserted(pos);
                    }
                })
                .show();
    }
}
