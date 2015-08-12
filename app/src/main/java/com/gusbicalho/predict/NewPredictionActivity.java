package com.gusbicalho.predict;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NewPredictionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_prediction);
    }

    @Override
    public void onBackPressed() {
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setMessage(R.string.new_prediction_discard_dialog_message)
                .setNegativeButton(R.string.new_prediction_discard_dialog_discard_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NewPredictionActivity.super.onBackPressed();
                            }
                        })
                .setPositiveButton(R.string.new_prediction_discard_dialog_keep_editing_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                .create();
        dlg.show();
    }
}
