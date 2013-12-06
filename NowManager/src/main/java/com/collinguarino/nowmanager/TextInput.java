package com.collinguarino.nowmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Collin on 12/5/13.
 */
public class TextInput extends Activity {

    String eventNameMain;
    TextView dateText, timeText;
    EditText textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.text_input);

        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        textInput = (EditText) findViewById(R.id.textInput);

        // opens the keyboard to start entering log name first
        textInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Intent textEditIntent = getIntent();

        textInput.setText(textEditIntent.getExtras().getString("eventName"));
        dateText.setText(textEditIntent.getExtras().getString("dateText"));
        timeText.setText(textEditIntent.getExtras().getString("timeText"));
    }

    // Attempted to transfer new event name to main activity to set text. Work in progress.
    /*@Override
    public void onBackPressed() {
        // super.onBackPressed();

        Intent intent = new Intent();
        eventNameMain = textInput.getText().toString();
        intent.putExtra("eventNameMain", eventNameMain);
        setResult(RESULT_OK, intent);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
