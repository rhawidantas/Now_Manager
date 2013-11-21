package com.collinguarino.nowmanager.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class NowManagerProvider extends ContentProvider {
    static final String AUTHORITY = "com.collinguarino.nowmanager.provider";
    private static final int TIME_CARDS = 1;
    private static final int TIME_CARD_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * Sets the integer value for multiple rows in time cards to 1. Notice that no wildcard is used in the path
         */
        sUriMatcher.addURI(AUTHORITY, Contracts.TimeCards.TABLE_NAME, TIME_CARDS);
        /*
         * Sets the code for a single row to 2. In this case, the "#" wildcard is used.
         */
        sUriMatcher.addURI(AUTHORITY, Contracts.TimeCards.TABLE_NAME + "/#", TIME_CARD_ID);
    }

    private MainDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.v("NowManagerProvider", "onCreate");
        mOpenHelper = new MainDatabaseHelper(getContext().getApplicationContext());
        return true;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case TIME_CARDS:
                qb.setTables(Contracts.TimeCards.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    // set a default sort order
                    sortOrder = "_ID ASC";
                }

                break;
            case TIME_CARD_ID:
                qb.setTables(Contracts.TimeCards.TABLE_NAME);
                /*
                 * Because this URI was for a single row, the _ID value part is
                 * present. Get the last path segment from the URI; this is the _ID value.
                 * Then, append the value to the WHERE clause for the query
                 */
                qb.appendWhere(Contracts.TimeCards._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Query the underlying database
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Notify the context's ContentResolver if the cursor result set changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor to the result set
        return c;
    }

    @Override
    public synchronized String getType(Uri uri) {
        return null;
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId;
        switch (sUriMatcher.match(uri)) {
            case TIME_CARDS:
                rowId = db.insert(Contracts.TimeCards.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri result = null;
        if (rowId > 0) {
            result = ContentUris.withAppendedId(Contracts.TimeCards.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(result, null);
        }
        return result;
    }

    @Override
    public synchronized int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case TIME_CARDS:
                count = db.delete(Contracts.TimeCards.TABLE_NAME, where, whereArgs);
                break;
            case TIME_CARD_ID:
                final String segment = uri.getLastPathSegment();
                final String whereClause = Contracts.TimeCards._ID + "=" + segment
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
                count = db.delete(Contracts.TimeCards.TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (count > 0) {
            // Notify the Context's ContentResolver of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case TIME_CARDS:
                count = db.update(Contracts.TimeCards.TABLE_NAME, values, where, whereArgs);
                break;
            case TIME_CARD_ID:
                final String segment = uri.getLastPathSegment();
                final String whereClause = Contracts.TimeCards._ID + "=" + segment
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
                count = db.update(Contracts.TimeCards.TABLE_NAME, values, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (count > 0) {
            // Notify the Context's ContentResolver of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
