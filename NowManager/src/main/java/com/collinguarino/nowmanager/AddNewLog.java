package com.collinguarino.nowmanager;

import android.os.Bundle;

/**
 * Created by Collin on 1/10/14.
 */
public class AddNewLog extends Main {

    /**
    * This activity is called by 3rd party applications, such as NFC task managers to call a new time card action.
    * NOTE: This is having problems if "Audio Responsive" mode is on. Otherwise, it works fine.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNewTimeCard();

    }
}
