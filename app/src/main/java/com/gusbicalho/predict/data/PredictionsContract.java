package com.gusbicalho.predict.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PredictionsContract {
    public static final String CONTENT_AUTHORITY = "com.gusbicalho.predict";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PREDICTIONS = "predictions";

    public static final class PredictionEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PREDICTIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PREDICTIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PREDICTIONS;

        public static final String TABLE_NAME = "prediction";

        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_DETAIL = "detail";
        public static final String COLUMN_ANSWER_TYPE = "answerType";
        public static final String COLUMN_ANSWER_TEXT = "answerText";
        public static final String COLUMN_ANSWER_BOOLEAN = "answerBoolean";
        public static final String COLUMN_ANSWER_MIN = "answerMin";
        public static final String COLUMN_ANSWER_MAX = "answerMax";
        public static final String COLUMN_CONFIDENCE = "confidence";
        public static final String COLUMN_ORDER = "orderIndex";

        public static final int ANSWER_TYPE_BOOLEAN = 0;
        public static final int ANSWER_TYPE_EXCLUSIVE_RANGE = 1;
        public static final int ANSWER_TYPE_INCLUSIVE_RANGE = 2;
        public static final int ANSWER_TYPE_TEXT = 3;
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({ANSWER_TYPE_BOOLEAN, ANSWER_TYPE_EXCLUSIVE_RANGE, ANSWER_TYPE_INCLUSIVE_RANGE, ANSWER_TYPE_TEXT})
        public @interface AnswerType {}

        public static Uri buildPredictionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
