package com.gusbicalho.predict;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class NewPredictionFragment extends DialogFragment {

    private static final int NO_SELECTION = -1;
    private static final int[] ANSWER_SELECTOR_OPTIONS_RESOURCES = new int[]{
            NO_SELECTION,
            R.string.prediction_answer_selector_true,
            R.string.prediction_answer_selector_false,
            R.string.prediction_answer_selector_text,
            R.string.prediction_answer_selector_inclusive_range,
            R.string.prediction_answer_selector_exclusive_range
    };
    private static final int ANSWER_SELECTOR_INDEX_NO_SELECTION = 0;
    private static final int ANSWER_SELECTOR_INDEX_TRUE = 1;
    private static final int ANSWER_SELECTOR_INDEX_FALSE = 2;
    private static final int ANSWER_SELECTOR_INDEX_TEXT = 3;
    private static final int ANSWER_SELECTOR_INDEX_INCLUSIVE_RANGE = 4;
    private static final int ANSWER_SELECTOR_INDEX_EXCLUSIVE_RANGE = 5;

    private String[] mAnswerSelectorOptions;
    private Spinner mAnswerSelector;
    private TextView mAnswerSelectorHint;
    private EditText mAnswerText;

    public interface Callback {
        void onSaveAction();
    }

    public NewPredictionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAnswerSelectorOptions = new String[ANSWER_SELECTOR_OPTIONS_RESOURCES.length];
        mAnswerSelectorOptions[0] = "";
        // Loop starts at 1 to skip the NO_SELECTION item
        for (int i = 1; i < ANSWER_SELECTOR_OPTIONS_RESOURCES.length; i++)
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
            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_LONG).show();
            if (getActivity() instanceof Callback)
                ((Callback) getActivity()).onSaveAction();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_prediction, container, false);

        mAnswerSelector = (Spinner) rootView.findViewById(R.id.new_prediction_answer_selector);
        mAnswerSelectorHint = (TextView) rootView.findViewById(R.id.new_prediction_answer_selector_hint);
        mAnswerText = (EditText) rootView.findViewById(R.id.new_prediction_answer_text);

        mAnswerSelector.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mAnswerSelectorOptions));
        mAnswerSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAnswerSelectorHint.setVisibility(
                        position == ANSWER_SELECTOR_INDEX_NO_SELECTION ? View.VISIBLE : View.INVISIBLE);
                mAnswerText.setVisibility(
                        position == ANSWER_SELECTOR_INDEX_TEXT ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAnswerSelectorHint.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
    }
}
