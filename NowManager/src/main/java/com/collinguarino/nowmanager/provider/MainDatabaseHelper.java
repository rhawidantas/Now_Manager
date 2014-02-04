package com.collinguarino.nowmanager.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class that actually creates and manages the provider's underlying data repository.
 */
final class MainDatabaseHelper extends SQLiteOpenHelper {

    // Defines the database name
    static final String DBNAME = "nowManagerDb";

    /*
     * Instantiates an open helper for the provider's SQLite data repository
     * Do not do database creation and upgrade here.
     */
    MainDatabaseHelper(Context context) {
        super(context, DBNAME, null, 2); // increment database version (last param)
    }

    /*
     * Creates the data repository. This is called when the provider attempts to open the
     * repository and SQLite reports that it doesn't exist.
     */
    public void onCreate(SQLiteDatabase db) {
        // Creates the main table
        db.execSQL(Contracts.TimeCards.SQL_CREATE_MAIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE " + "timeCards" + " ADD COLUMN " + "isThirdParty" + " int");
            db.execSQL("UPDATE " + "timeCards"  + " SET " +
                    "isThirdParty" + "= 0");
        }
    }
}