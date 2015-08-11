package com.gusbicalho.predict.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;

public class PredictionsProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int PREDICTION_DIR = 100;
    static final int PREDICTION_ITEM = 101;

    private static final String PREDICTION_BY_ID_SELECTION =
            PredictionsContract.PredictionEntry.TABLE_NAME +
                    "." + PredictionsContract.PredictionEntry._ID + " = ?";

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PredictionsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PredictionsContract.PATH_PREDICTIONS, PREDICTION_DIR);
        matcher.addURI(authority, PredictionsContract.PATH_PREDICTIONS + "/#", PREDICTION_ITEM);

        return matcher;
    }

    public static final class Util {
        public static final String[] PREDICTION_PROJECTION = new String[] {
                PredictionsContract.PredictionEntry._ID,
                PredictionsContract.PredictionEntry.COLUMN_QUESTION,
                PredictionsContract.PredictionEntry. COLUMN_DETAIL,
                PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE,
                PredictionsContract.PredictionEntry.COLUMN_ANSWER_TEXT,
                PredictionsContract.PredictionEntry.COLUMN_ANSWER_BOOLEAN,
                PredictionsContract.PredictionEntry.COLUMN_ANSWER_MIN,
                PredictionsContract.PredictionEntry.COLUMN_ANSWER_MAX,
                PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE,
                PredictionsContract.PredictionEntry.COLUMN_RESULT,
        };
        public static final int INDEX_ID = 0;
        public static final int INDEX_QUESTION = 1;
        public static final int INDEX_DETAIL = 2;
        public static final int INDEX_ANSWER_TYPE = 3;
        public static final int INDEX_ANSWER_TEXT = 4;
        public static final int INDEX_ANSWER_BOOLEAN = 5;
        public static final int INDEX_ANSWER_MIN = 6;
        public static final int INDEX_ANSWER_MAX = 7;
        public static final int INDEX_CONFIDENCE = 8;
        public static final int INDEX_RESULT = 9;

        public static Cursor getPrediction(Context context, long id) {
            return context.getContentResolver().query(
                    PredictionsContract.PredictionEntry.CONTENT_URI,
                    PREDICTION_PROJECTION,
                    PredictionsContract.PredictionEntry._ID + " = ?",
                    new String[]{"" + id}, null
            );
        }
        public static boolean removePrediction(Context context, long id) {
            long rowsDeleted = context.getContentResolver().delete(
                    PredictionsContract.PredictionEntry.CONTENT_URI,
                    PredictionsContract.PredictionEntry._ID + " = ?",
                    new String[]{"" + id});
            return rowsDeleted > 0;
        }
        public static long insertPrediction(Context context, String question, String detail,
                                            @PredictionsContract.PredictionEntry.AnswerType int answerType,
                                            Object answer, double confidence) {
            String answerText = null;
            Boolean answerBoolean = null;
            Double answerMin = null, answerMax = null;
            switch (answerType) {
                case PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN: {
                    answerBoolean = (Boolean) answer;
                    break;
                }
                case PredictionsContract.PredictionEntry.ANSWER_TYPE_TEXT: {
                    answerText = answer.toString();
                    break;
                }
                case PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE:
                case PredictionsContract.PredictionEntry.ANSWER_TYPE_INCLUSIVE_RANGE: {
                    Pair<Double, Double> range = (Pair<Double, Double>) answer;
                    answerMin = Math.min(range.first, range.second);
                    answerMax = Math.max(range.first, range.second);
                    break;
                }
            }

            ContentValues predValues = new ContentValues();
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_QUESTION, question);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_DETAIL, detail);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE, answerType);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_BOOLEAN, answerBoolean);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TEXT, answerText);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MIN, answerMin);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MAX, answerMax);
            predValues.put(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE, confidence);

            Uri insertedUri = context.getContentResolver().insert(PredictionsContract.PredictionEntry.CONTENT_URI, predValues);
            return ContentUris.parseId(insertedUri);
        }
        public static long insertPrediction(Context context, String question, String detail, boolean answer, double confidence) {
            return insertPrediction(context, question, detail, PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN, answer, confidence);
        }
        public static long insertPrediction(Context context, String question, String detail, Pair<Double, Double> answer, boolean exclusive, double confidence) {
            return insertPrediction(context, question, detail,
                    exclusive ?
                            PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE :
                            PredictionsContract.PredictionEntry.ANSWER_TYPE_INCLUSIVE_RANGE,
                    answer, confidence);
        }
        public static long insertPrediction(Context context, String question, String detail, double answerMin, double answerMax, boolean exclusive, double confidence) {
            return insertPrediction(context, question, detail, new Pair<>(answerMin, answerMax), exclusive, confidence);
        }
        public static long insertPrediction(Context context, String question, String detail, String answer, double confidence) {
            return insertPrediction(context, question, detail, PredictionsContract.PredictionEntry.ANSWER_TYPE_TEXT, answer, confidence);
        }
    }


    private PredictionsDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new PredictionsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PREDICTION_DIR:
                return PredictionsContract.PredictionEntry.CONTENT_TYPE;
            case PREDICTION_ITEM:
                return PredictionsContract.PredictionEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "prediction/#"
            case PREDICTION_ITEM:
            {
                String predId = uri.getLastPathSegment();
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PredictionsContract.PredictionEntry.TABLE_NAME,
                        projection,
                        PREDICTION_BY_ID_SELECTION,
                        new String[]{predId},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "prediction"
            case PREDICTION_DIR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PredictionsContract.PredictionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PREDICTION_DIR: {
                long _id = db.insert(PredictionsContract.PredictionEntry.TABLE_NAME, null, values);
                if (_id != -1)
                    returnUri = PredictionsContract.PredictionEntry.buildPredictionUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PREDICTION_DIR:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PredictionsContract.PredictionEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PREDICTION_DIR: {
                rowsUpdated = db.update(PredictionsContract.PredictionEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case PREDICTION_DIR: {
                rowsDeleted = db.delete(PredictionsContract.PredictionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
