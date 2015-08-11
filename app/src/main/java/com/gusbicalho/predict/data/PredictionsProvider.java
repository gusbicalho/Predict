package com.gusbicalho.predict.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

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
