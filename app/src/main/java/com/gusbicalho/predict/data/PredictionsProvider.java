package com.gusbicalho.predict.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Gustavo on 10/08/2015.
 */
public class PredictionsProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int PREDICTION_DIR = 100;
    private static final int PREDICTION_ITEM = 101;

    private static final String PREDICTION_BY_ID_SELECTION =
            PredictionsContract.PredictionEntry.TABLE_NAME +
                    "." + PredictionsContract.PredictionEntry._ID + " = ?";

    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PredictionsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
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
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
