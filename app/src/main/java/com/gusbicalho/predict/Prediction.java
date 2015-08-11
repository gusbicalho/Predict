package com.gusbicalho.predict;

import android.support.annotation.IntDef;
import android.util.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Prediction {
    public static final int ANSWER_TYPE_BOOLEAN = 0;
    public static final int ANSWER_TYPE_EXCLUSIVE_RANGE = 1;
    public static final int ANSWER_TYPE_INCLUSIVE_RANGE = 2;
    public static final int ANSWER_TYPE_TEXT = 3;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ANSWER_TYPE_BOOLEAN, ANSWER_TYPE_EXCLUSIVE_RANGE, ANSWER_TYPE_INCLUSIVE_RANGE, ANSWER_TYPE_TEXT})
    public @interface AnswerType {}

    private String mQuestion;
    private String mDetail;
    @AnswerType private int mAnswerType;
    private Object mAnswer;
    private double mConfidence;

    private Prediction(String question, String detail, int answerType, Object answer, double confidence) {
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
        this.mQuestion = question;
        this.mDetail = detail;
        this.mAnswerType = answerType;
        this.mAnswer = answer;
        this.mConfidence = confidence;
    }

    public Prediction(String question, String detail, boolean answer, double confidence) {
        this(question, detail, ANSWER_TYPE_BOOLEAN, answer, confidence);
    }
    public Prediction(String question, String detail, Pair<Double, Double> answer, boolean exclusive, double confidence) {
        this(question, detail, exclusive ? ANSWER_TYPE_EXCLUSIVE_RANGE : ANSWER_TYPE_INCLUSIVE_RANGE, answer, confidence);
    }
    public Prediction(String question, String detail, double answerMin, double answerMax, boolean exclusive, double confidence) {
        this(question, detail, new Pair<>(answerMin, answerMax), exclusive, confidence);
    }
    public Prediction(String question, String detail, String answer, double confidence) {
        this(question, detail, ANSWER_TYPE_TEXT, answer, confidence);
    }

    public String getQuestion() {
        return mQuestion;
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
