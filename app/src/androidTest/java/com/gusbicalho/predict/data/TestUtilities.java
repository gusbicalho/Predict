package com.gusbicalho.predict.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.gusbicalho.predict.utils.PollingCheck;

/**
 * Created by Gustavo on 11/08/2015.
 */
public class TestUtilities extends AndroidTestCase {
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    static ContentValues createBooleanPredictionValues() {
        ContentValues predValues = new ContentValues();
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_QUESTION, "Will Dilma step out until the end of year?");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_DETAIL, (String) null);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE, PredictionsContract.PredictionEntry.ANSWER_TYPE_BOOLEAN);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_BOOLEAN, true);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE, 0.3d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ORDER, 0);
        return predValues;
    }
    static ContentValues createTextPredictionValues() {
        ContentValues predValues = new ContentValues();
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_QUESTION, "What is The Silence?");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_DETAIL, "From Doctor Who series");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE, PredictionsContract.PredictionEntry.ANSWER_TYPE_TEXT);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TEXT, "Mindwash of the universe");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE, 0.1d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ORDER, 0);
        return predValues;
    }
    static ContentValues createExclusiveRangePredictionValues() {
        ContentValues predValues = new ContentValues();
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_QUESTION, "How many kilometers walked?");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_DETAIL, "A round on EcoPark is 7.5km");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE, PredictionsContract.PredictionEntry.ANSWER_TYPE_EXCLUSIVE_RANGE);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MIN, 6.0d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MAX, 23.0d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE, 0.8d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ORDER, 0);
        return predValues;
    }
    static ContentValues createInclusiveRangePredictionValues() {
        ContentValues predValues = new ContentValues();
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_QUESTION, "How many roads should a man walk down?");
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_DETAIL, (String) null);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE, PredictionsContract.PredictionEntry.ANSWER_TYPE_INCLUSIVE_RANGE);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MIN, 42.0d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MAX, 42.0d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE, 0.9d);
        predValues.put(PredictionsContract.PredictionEntry.COLUMN_ORDER, 0);
        return predValues;
    }

    public static void checkCursorFirst(String testName, Cursor cursor, ContentValues expectedValues) {
        assertTrue("Error: empty cursor (" + testName + ")", cursor.moveToFirst());
        checkCursorCurrent(testName, cursor, expectedValues);
    }
    public static void checkCursorCurrent(String testName, Cursor cursor, ContentValues expectedValues) {
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            int type = cursor.getType(i);
            String fieldName = cursor.getColumnName(i);
            if (PredictionsContract.PredictionEntry._ID.equals(fieldName))
                continue;
            Object value = expectedValues.get(fieldName);
            Object dbValue;
            switch (type) {
                case Cursor.FIELD_TYPE_NULL:
                    dbValue = null;
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    dbValue = cursor.getInt(i);
                    if (value instanceof Boolean)
                        dbValue = dbValue.equals(0) ? Boolean.FALSE : Boolean.TRUE;
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    dbValue = cursor.getDouble(i);
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    dbValue = cursor.getString(i);
                    break;
                default:
                    dbValue = "Invalid Type";
            }
            assertEquals("Error: incorrect value for field "+fieldName+" (" + testName + ")", value, dbValue);
        }
    }

}
