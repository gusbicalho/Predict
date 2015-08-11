package com.gusbicalho.predict.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Gustavo on 11/08/2015.
 */
public class TestPredictionsContract extends AndroidTestCase {
    private static final long TEST_PREDICTION_ID = 123L;

    public void testBuildWeatherLocation() {
        Uri locationUri = PredictionsContract.PredictionEntry.buildPredictionUri(TEST_PREDICTION_ID);
        assertNotNull("Error: Null Uri returned", locationUri);
        assertEquals("Error: ID not properly appended",
                ""+TEST_PREDICTION_ID, locationUri.getLastPathSegment());
        assertEquals("Error: Prediction Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.gusbicalho.predict/predictions/123");
    }
}
