package com.gusbicalho.predict.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

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

        public static Uri buildPredictionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
