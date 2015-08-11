package com.gusbicalho.predict.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestProviderUtil extends AndroidTestCase {

    private void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                PredictionsContract.PredictionEntry.CONTENT_URI,
                null, null
        );

        Cursor cursor = mContext.getContentResolver().query(
                PredictionsContract.PredictionEntry.CONTENT_URI,
                null, null, null, null
        );
        assertEquals("Error: Records not deleted from Prediction table during delete", 0, cursor.getCount());
        cursor.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    private long insertAndCheck(String testName, ContentValues testValues) {
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(PredictionsContract.PredictionEntry.CONTENT_URI, true, tco);
        Uri predictionUri = mContext.getContentResolver().insert(PredictionsContract.PredictionEntry.CONTENT_URI, testValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long rowId = ContentUris.parseId(predictionUri);
        assertTrue("Error: No Row ID returned on insert (" + testName + ")", rowId != -1);

        Cursor cursor = PredictionsProvider.Util.getPrediction(mContext, rowId);
        assertTrue("Error: no rows returned in query (" + testName + ")", cursor.moveToFirst());

        TestUtilities.checkCursorFirst("testInsertReadProvider", cursor, testValues);

        assertFalse("Error: More than one row returned in query (" + testName + ")", cursor.moveToNext());
        cursor.close();

        return rowId;
    }

    private void deleteAndCheck(String testName, long rowId) {
        assertTrue("Error: Nothing deleted ("+testName+")",
                PredictionsProvider.Util.removePrediction(mContext, rowId));
        Cursor cursor = mContext.getContentResolver().query(
                PredictionsContract.PredictionEntry.CONTENT_URI,
                null,
                PredictionsContract.PredictionEntry._ID + " = ?",
                new String[]{"" + rowId}, null
        );
        assertFalse("Error: row found ("+testName+")", cursor.moveToFirst());
    }

    public void testGetAndRemovePrediction() {
        ContentValues booleanPredictionValues = TestUtilities.createBooleanPredictionValues();
        ContentValues textPredictionValues = TestUtilities.createTextPredictionValues();
        ContentValues inclusiveRangePredictionValues = TestUtilities.createInclusiveRangePredictionValues();
        ContentValues exclusiveRangePredictionValues = TestUtilities.createExclusiveRangePredictionValues();
        long booleanId = insertAndCheck("Boolean", booleanPredictionValues);
        long textId = insertAndCheck("Text", textPredictionValues);
        long incRangeId = insertAndCheck("IncRange", inclusiveRangePredictionValues);
        long excRangeId = insertAndCheck("ExcRange", exclusiveRangePredictionValues);

        deleteAndCheck("IncRange", incRangeId);
        deleteAndCheck("Text", textId);
        deleteAndCheck("Boolean", booleanId);
        deleteAndCheck("ExcRange", excRangeId);
    }
}
