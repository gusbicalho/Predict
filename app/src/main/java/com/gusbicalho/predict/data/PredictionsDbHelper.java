package com.gusbicalho.predict.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gusbicalho.predict.data.PredictionsContract.*;

/**
 * Created by Gustavo on 11/08/2015.
 */
public class PredictionsDbHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "weather.db";

    public PredictionsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_DETAIL = "detail";
        public static final String COLUMN_ANSWER_TYPE = "answerType";
        public static final String COLUMN_ANSWER_TEXT = "answerText";
        public static final String COLUMN_ANSWER_BOOLEAN = "answerBoolean";
        public static final String COLUMN_ANSWER_MIN = "answerMin";
        public static final String COLUMN_ANSWER_MAX = "answerMax";
        public static final String COLUMN_CONFIDENCE = "confidence";
         */
        final String SQL_CREATE_PREDICTION_TABLE = "CREATE TABLE " + PredictionEntry.TABLE_NAME + " (" +
                PredictionEntry._ID + " INTEGER PRIMARY KEY," +
                PredictionEntry.COLUMN_QUESTION + " TEXT NOT NULL, " +
                PredictionEntry.COLUMN_DETAIL + " TEXT NOT NULL, " +
                PredictionEntry.COLUMN_CONFIDENCE + " REAL NOT NULL, " +
                PredictionEntry.COLUMN_ANSWER_TYPE + " INTEGER NOT NULL, " +
                PredictionEntry.COLUMN_ANSWER_TEXT + " TEXT, " +
                PredictionEntry.COLUMN_ANSWER_BOOLEAN + " INTEGER, " +
                PredictionEntry.COLUMN_ANSWER_MIN + " REAL, " +
                PredictionEntry.COLUMN_ANSWER_MAX + " REAL " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_PREDICTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // TODO
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PredictionEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
