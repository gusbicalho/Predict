package com.gusbicalho.predict.data;

import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final Uri TEST_PREDICTION_DIR = PredictionsContract.PredictionEntry.CONTENT_URI;
    private static final long[] TEST_PREDICTION_IDS = new long[]{1L, 32L, 0L};
    private static final String[] TEST_PREDICTION_IDS_FAIL = new String[]{"-1","-32","asd"};
    private static final Uri[] TEST_PREDICTION_ITEMS;
    private static final Uri[] TEST_PREDICTION_ITEMS_FAIL;

    static {
        {
            Uri[] itemUris = new Uri[TEST_PREDICTION_IDS.length];
            for (int i = 0; i < TEST_PREDICTION_IDS.length; i++)
                itemUris[i] = PredictionsContract.PredictionEntry.buildPredictionUri(TEST_PREDICTION_IDS[i]);
            TEST_PREDICTION_ITEMS = itemUris;
        }
        {
            Uri[] itemUris = new Uri[TEST_PREDICTION_IDS_FAIL.length];
            for (int i = 0; i < TEST_PREDICTION_IDS_FAIL.length; i++)
                itemUris[i] = PredictionsContract.PredictionEntry.CONTENT_URI.buildUpon().appendEncodedPath(TEST_PREDICTION_IDS_FAIL[i]).build();
            TEST_PREDICTION_ITEMS_FAIL = itemUris;
        }
    }

    public void testUriMatcher() {
        UriMatcher testMatcher = PredictionsProvider.buildUriMatcher();

        assertEquals("Error: The PREDICTION_DIR URI was matched incorrectly.",
                PredictionsProvider.PREDICTION_DIR, testMatcher.match(TEST_PREDICTION_DIR));
        for (Uri uri : TEST_PREDICTION_ITEMS)
            assertEquals("Error: The PREDICTION_ITEM URI was matched incorrectly for URI "+uri,
                    PredictionsProvider.PREDICTION_ITEM, testMatcher.match(uri));
        for (Uri uri : TEST_PREDICTION_ITEMS_FAIL)
            assertEquals("Error: PREDICTION_ITEM URI incorrectly accepted for URI "+uri,
                    UriMatcher.NO_MATCH, testMatcher.match(uri));
    }
}
