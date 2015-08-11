/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gusbicalho.predict.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(PredictionsDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(PredictionsDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new PredictionsDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        {
            final HashSet<String> tableNameHashSet = new HashSet<>();
            tableNameHashSet.add(PredictionsContract.PredictionEntry.TABLE_NAME);

            // have we created the tables we want?
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            assertTrue("Error: database has not been created correctly",
                    c.moveToFirst());

            // verify that the tables have been created
            do {
                tableNameHashSet.remove(c.getString(0));
            } while (c.moveToNext());

            // if this fails, it means that your database doesn't contain both the location entry
            // and weather entry tables
            StringBuilder tablesLeft = new StringBuilder();
            for (String s : tableNameHashSet)
                tablesLeft.append(s).append(" ");
            assertTrue("Error: tables missing: " + tablesLeft,
                    tableNameHashSet.isEmpty());
        }

        {
            // now, do our tables contain the correct columns?
            Cursor c = db.rawQuery("PRAGMA table_info(" + PredictionsContract.PredictionEntry.TABLE_NAME + ")",
                    null);

            assertTrue("Error: unable to query the database for table information.",
                    c.moveToFirst());

            // Build a HashSet of all of the column names we want to look for
            final HashSet<String> predictionColumnHashSet = new HashSet<String>();
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry._ID);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_QUESTION);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_DETAIL);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_CONFIDENCE);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TYPE);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_ANSWER_TEXT);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_ANSWER_BOOLEAN);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MIN);
            predictionColumnHashSet.add(PredictionsContract.PredictionEntry.COLUMN_ANSWER_MAX);

            int columnNameIndex = c.getColumnIndex("name");
            do {
                String columnName = c.getString(columnNameIndex);
                predictionColumnHashSet.remove(columnName);
            } while (c.moveToNext());

            StringBuilder columnsLeft = new StringBuilder();
            for (String s : predictionColumnHashSet)
                columnsLeft.append(s).append(" ");
            assertTrue("Error: prediction columns missing: "+columnsLeft,
                    predictionColumnHashSet.isEmpty());
        }
        db.close();
    }

    private void testInsertPredictionValues(String testName, SQLiteDatabase db, ContentValues predValues) {
        long rowId = db.insert(PredictionsContract.PredictionEntry.TABLE_NAME, null, predValues);
        assertTrue("Error: failed to insert Prediction ("+testName+")", rowId != -1);

        Cursor predCursor = db.query(
                PredictionsContract.PredictionEntry.TABLE_NAME,  // Table to Query
                null, null, null, null, null, null
        );
        TestUtilities.checkCursorFirst(testName, predCursor, predValues);
        assertFalse("Error: More than one record returned from prediction query (" + testName + ")",
                predCursor.moveToNext());
        predCursor.close();

        long rowsDeleted = db.delete(PredictionsContract.PredictionEntry.TABLE_NAME, null, null);
        assertEquals("Error: More than one row deleted (" + testName + ")", 1, rowsDeleted);
    }

    public void testPredictionTable() {
        PredictionsDbHelper dbHelper = new PredictionsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        testInsertPredictionValues("Boolean", db, TestUtilities.createBooleanPredictionValues());
        testInsertPredictionValues("Text", db, TestUtilities.createTextPredictionValues());
        testInsertPredictionValues("IncRange", db, TestUtilities.createInclusiveRangePredictionValues());
        testInsertPredictionValues("ExcRange", db, TestUtilities.createExclusiveRangePredictionValues());

        dbHelper.close();
    }
}
