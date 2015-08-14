package com.gusbicalho.predict;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gusbicalho.predict.data.PredictionsContract;
import com.gusbicalho.predict.data.PredictionsProvider;

public class NewPredictionFragment extends DialogFragment {

    private static final int[] ANSWER_SELECTOR_OPTIONS_RESOURCES = new int[]{
            R.string.prediction_answer_selector_true,
            R.string.prediction_answer_selector_false,
            R.string.prediction_answer_selector_text,
            R.string.prediction_answer_selector_inclusive_range,
            R.string.prediction_answer_selector_exclusive_range
    };
    private static final int ANSWER_SELECTOR_INDEX_NO_SELECTION = -1;
    private static final int ANSWER_SELECTOR_INDEX_TRUE = 0;
    private static final int ANSWER_SELECTOR_INDEX_FALSE = 1;
    private static final int ANSWER_SELECTOR_INDEX_TEXT = 2;
    private static final int ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE = 3;
    private static final int ANSWER_SELECTOR_INDEX_EXCLUSIVE_RANGE = 4;

    public static final double[] CREDENCE_SCORES = new double[]{
            0.5, 0.6, 0.7, 0.8, 0.9, 0.99
    };
    private static final int CREDENCE_INDEX_DEFAULT = 1;

    private String[] mAnswerSelectorOptions;
    private Spinner mAnswerSelector;
    private TextView mAnswerSelectorHint;
    private EditText mAnswerText;
    private View mAnswerRangeContainer;
    private TextView mAnswerRangeLabel;
    private EditText mAnswerRangeMin;
    private EditText mAnswerRangeMax;
    private SeekBar mCredenceSelector;
    private TextView mCredenceValueLabel;
    private EditText mQuestion;
    private EditText mDetail;

    public interface Callback {
        void onSaveAction();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAnswerSelectorOptions = new String[ANSWER_SELECTOR_OPTIONS_RESOURCES.length];
        for (int i = 0; i < ANSWER_SELECTOR_OPTIONS_RESOURCES.length; i++)
            mAnswerSelectorOptions[i] = getString(ANSWER_SELECTOR_OPTIONS_RESOURCES[i]);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new_prediction, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_prediction_save) {
            saveAction();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_prediction, container, false);

        mQuestion = (EditText) rootView.findViewById(R.id.edit_prediction_question);
        mDetail = (EditText) rootView.findViewById(R.id.edit_prediction_detail);
        mAnswerSelector = (Spinner) rootView.findViewById(R.id.edit_prediction_answer_selector);
        mAnswerSelectorHint = (TextView) rootView.findViewById(R.id.edit_prediction_answer_selector_hint);
        mAnswerText = (EditText) rootView.findViewById(R.id.edit_prediction_answer_text);
        mAnswerRangeContainer = rootView.findViewById(R.id.edit_prediction_answer_range_container);
        mAnswerRangeLabel = (TextView) rootView.findViewById(R.id.edit_prediction_answer_range_label);
        mAnswerRangeMin = (EditText) rootView.findViewById(R.id.edit_prediction_answer_range_min);
        mAnswerRangeMax = (EditText) rootView.findViewById(R.id.edit_prediction_answer_range_max);
        mCredenceSelector = (SeekBar) rootView.findViewById(R.id.edit_prediction_credence);
        mCredenceValueLabel = (TextView) rootView.findViewById(R.id.edit_prediction_credence_value_label);

        mAnswerSelector.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mAnswerSelectorOptions));
        mAnswerSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAnswerSelectorHint.setVisibility(
                        position == ANSWER_SELECTOR_INDEX_NO_SELECTION ? View.VISIBLE : View.INVISIBLE);
                mAnswerText.setVisibility(
                        position == ANSWER_SELECTOR_INDEX_TEXT ? View.VISIBLE : View.GONE);
                if (position == ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE || position == ANSWER_SELECTOR_INDEX_EXCLUSIVE_RANGE) {
                    mAnswerRangeContainer.setVisibility(View.VISIBLE);
                    mAnswerRangeLabel.setText(
                            position == ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE ?
                                    R.string.edit_prediction_answer_range_label_inclusive :
                                    R.string.edit_prediction_answer_range_label_exclusive
                    );
                } else {
                    mAnswerRangeContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAnswerSelectorHint.setVisibility(View.VISIBLE);
                mAnswerText.setVisibility(View.GONE);
                mAnswerRangeContainer.setVisibility(View.GONE);
            }
        });

        mCredenceSelector.setMax(CREDENCE_SCORES.length - 1);
        mCredenceSelector.setProgress(CREDENCE_INDEX_DEFAULT);
        mCredenceValueLabel.setText(
                getString(R.string.edit_prediction_credence_value_label,
                        (int) (CREDENCE_SCORES[CREDENCE_INDEX_DEFAULT] * 100.0)));
        mCredenceSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCredenceValueLabel.setText(getString(R.string.edit_prediction_credence_value_label, (int) (CREDENCE_SCORES[progress] * 100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        return rootView;
    }

    private void saveFailed(int messageResId) {
        Toast.makeText(getActivity(), messageResId, Toast.LENGTH_LONG).show();
    }

    private void saveAction() {
        final String question = mQuestion.getText().toString();
        final String detail = mDetail.getText().toString().trim();
        final double credence = CREDENCE_SCORES[mCredenceSelector.getProgress()];

        if (question.isEmpty()) {
            saveFailed(R.string.edit_prediction_notice_save_failed_no_question);
            return;
        }

        int answerSelectorIndex = mAnswerSelector.getSelectedItemPosition();
        if (answerSelectorIndex == ANSWER_SELECTOR_INDEX_NO_SELECTION) {
            saveFailed(R.string.edit_prediction_notice_save_failed_no_answer);
            return;
        }

        @PredictionsContract.PredictionEntry.AnswerType
        final int answerType;
        final Object answer;
        switch (answerSelectorIndex) {
            case ANSWER_SELECTOR_INDEX_TRUE: {
                answerType = PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN;
                answer = true;
                break;
            }
            case ANSWER_SELECTOR_INDEX_FALSE: {
                answerType = PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN;
                answer = false;
                break;
            }
            case ANSWER_SELECTOR_INDEX_TEXT: {
                answerType = PredictionsContract.PredictionEntry.ANSWER_TYPE_TEXT;
                answer = mAnswerText.getText().toString();
                if (answer.toString().isEmpty()) {
                    saveFailed(R.string.edit_prediction_notice_save_failed_no_answer);
                    return;
                }
                break;
            }
            case ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE:
            case ANSWER_SELECTOR_INDEX_EXCLUSIVE_RANGE: {
                answerType = answerSelectorIndex == ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE ?
                        PredictionsContract.PredictionEntry.ANSWER_TYPE_INCLUSIVE_RANGE :
                        PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE;
                double min, max;
                try {
                    min = Double.parseDouble(mAnswerRangeMin.getText().toString());
                    max = Double.parseDouble(mAnswerRangeMax.getText().toString());
                } catch (NumberFormatException e) {
                    saveFailed(R.string.edit_prediction_notice_save_failed_no_answer);
                    return;
                }
                answer = new Pair<>(min, max);
                break;
            }
            default: {
                saveFailed(R.string.edit_prediction_notice_save_failed_no_answer);
                return;
            }
        }

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                return PredictionsProvider.Util.insertPrediction(getActivity(),
                        question, detail.isEmpty() ? null : detail, answerType, answer, credence);
            }

            @Override
            protected void onPostExecute(Long newPredictionId) {
                Toast.makeText(getActivity(), R.string.edit_prediction_notice_saved, Toast.LENGTH_LONG).show();
                if (getActivity() instanceof Callback)
                    ((Callback) getActivity()).onSaveAction();
            }
        }.execute();
    }



}
