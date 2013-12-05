package com.collinguarino.nowmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Collin on 12/5/13.
 */
public class TextInput extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.text_input);

        EditText textInput = (EditText) findViewById(R.id.textInput);
        textInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // `JSONfunctions` is undefined, unable to transfer text
        /*JSONObject json = JSONfunctions.getJSONfromURL(getIntent().getStringExtra("URL"));

        if (json != null) {
            textInput.setText(String.valueOf(json));
        }*/
    }
}
