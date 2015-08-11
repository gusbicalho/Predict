package com.gusbicalho.predict.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestProvider extends AndroidTestCase {

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

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                PredictionsProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: PredictionsProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + PredictionsContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PredictionsContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: PredictionsProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
       This test doesn't touch the database.  It verifies that the ContentProvider returns
       the correct type for each type of URI that it can handle.
    */
    public void testGetType() {
        String type = mContext.getContentResolver().getType(PredictionsContract.PredictionEntry.CONTENT_URI);
        assertEquals("Error: the PredictionEntry CONTENT_URI should return PredictionEntry.CONTENT_TYPE",
                PredictionsContract.PredictionEntry.CONTENT_TYPE, type);

        long testId = 123L;
        type = mContext.getContentResolver().getType(
                PredictionsContract.PredictionEntry.buildPredictionUri(testId));
        assertEquals("Error: the PredictionEntry CONTENT_URI with id should return PredictionEntry.CONTENT_ITEM_TYPE",
                PredictionsContract.PredictionEntry.CONTENT_ITEM_TYPE, type);
    }

    private void testInsertReadDeletePredictionValues(String testName, ContentValues testValues) {
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(PredictionsContract.PredictionEntry.CONTENT_URI, true, tco);
        Uri predictionUri = mContext.getContentResolver().insert(PredictionsContract.PredictionEntry.CONTENT_URI, testValues);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long predictionRowId = ContentUris.parseId(predictionUri);

        // Verify we got a row back.
        assertTrue("Error: No Row ID returned on insert (" + testName + ")", predictionRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                PredictionsContract.PredictionEntry.CONTENT_URI,
                null, null, null, null
        );

        assertTrue("Error: no rows returned in query (" + testName + ")",cursor.moveToFirst());

        TestUtilities.checkCursorFirst("testInsertReadProvider", cursor, testValues);

        assertFalse("Error: More than one row returned in query (" + testName + ")", cursor.moveToNext());
        cursor.close();

        long rowsDeleted = mContext.getContentResolver().delete(PredictionsContract.PredictionEntry.CONTENT_URI, null, null);
        assertEquals("Error: More than one row deleted (" + testName + ")", 1, rowsDeleted);
    }

    public void testInsertReadDeleteProvider() {
        testInsertReadDeletePredictionValues("Boolean", TestUtilities.createBooleanPredictionValues());
        testInsertReadDeletePredictionValues("Text", TestUtilities.createTextPredictionValues());
        testInsertReadDeletePredictionValues("IncRange", TestUtilities.createInclusiveRangePredictionValues());
        testInsertReadDeletePredictionValues("ExcRange", TestUtilities.createExclusiveRangePredictionValues());
    }

    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = new ContentValues[] {
                TestUtilities.createBooleanPredictionValues(),
                TestUtilities.createTextPredictionValues(),
                TestUtilities.createExclusiveRangePredictionValues(),
                TestUtilities.createInclusiveRangePredictionValues()
        };

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(PredictionsContract.PredictionEntry.CONTENT_URI, true, tco);

        int insertCount = mContext.getContentResolver().bulkInsert(PredictionsContract.PredictionEntry.CONTENT_URI, bulkInsertContentValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        assertEquals(insertCount, bulkInsertContentValues.length);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                PredictionsContract.PredictionEntry.CONTENT_URI,
                null, null, null, null
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), bulkInsertContentValues.length);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < bulkInsertContentValues.length; i++, cursor.moveToNext() ) {
            TestUtilities.checkCursorCurrent("testBulkInsert, entry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
