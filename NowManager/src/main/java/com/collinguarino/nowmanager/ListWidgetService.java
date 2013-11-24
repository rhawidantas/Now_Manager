package com.collinguarino.nowmanager;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.collinguarino.nowmanager.model.TimeCard;
import com.collinguarino.nowmanager.provider.Contracts;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by kiran on 11/22/13.
 */
public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    private Cursor mCursor;
    private Context mContext;
    private static final Uri BASE_URI = Contracts.TimeCards.CONTENT_URI;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    public void onCreate() {
        mCursor = queryContentProvider();
    }

    public void onDestroy() {
        mCursor.close();
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        mCursor.moveToPosition(position);

        final TimeCard timeCard = new TimeCard(mCursor);

        /*
         * Currently, using the same layout for the widget as the main screen,
         * replacing the EditText with a TextView since a widget does not support EditText
         *
         * TODO: Use a separate layout for the widget. It should take widget resizing into account.
         */
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_item);


        rv.setTextViewText(R.id.eventName, timeCard.getEventNameInput() != null ? timeCard.getEventNameInput() : "");
        // setting date and time
        final Calendar datetimeCalendar = Calendar.getInstance();
        datetimeCalendar.setTimeInMillis(timeCard.getTimestamp());
        final Date dateTime = datetimeCalendar.getTime();
        rv.setTextViewText(R.id.timeText, !DateFormat.is24HourFormat(mContext) ?
                TimeCardAdapter.TIME_FORMAT_STANDARD.format(dateTime) :
                TimeCardAdapter.TIME_FORMAT_MILITARY.format(dateTime));

       rv.setTextViewText(R.id.dateText, TimeCardAdapter.DATE_FORMAT.format(dateTime));

        /*
         * TODO: Use PendingIntentTemplate and fillInIntents to ensure that clicking on a row in the widget takes the user to the corresponding row in the app.
         */
//        Bundle extras = new Bundle();
//        extras.putInt(WidgetProvider.EXTRA_ITEM, position);
//        Intent fillInIntent = new Intent();
//        fillInIntent.putExtras(extras);
//        rv.setOnClickFillInIntent(R.id.timeCardFragmentLayout, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        if(mCursor != null){
            mCursor.close();
        }

        mCursor = queryContentProvider();

    }

    private Cursor queryContentProvider(){
        return mContext.getContentResolver().query(
                BASE_URI,
                Contracts.TimeCards.SELECT_ALL_PROJECTION,
                null,
                null,
                Contracts.TimeCards.C_TIMESTAMP + " DESC");
    }
}
