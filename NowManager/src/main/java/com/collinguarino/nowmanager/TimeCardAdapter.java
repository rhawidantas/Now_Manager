package com.collinguarino.nowmanager;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.collinguarino.nowmanager.provider.Contracts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeCardAdapter extends CursorAdapter {
    private Context mContext;
    private final LayoutInflater mInflater;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM-dd a");
    public static final SimpleDateFormat TIME_FORMAT_MILITARY = new SimpleDateFormat("kk:mm:ss");
    public static final SimpleDateFormat TIME_FORMAT_STANDARD = new SimpleDateFormat("hh:mm:ss");


    public TimeCardAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set event name input
        final String eventNameInput = cursor.getString(Contracts.TimeCards.I_EVENT_NAME_INPUT);
        if(eventNameInput != null) {
        viewHolder.eventNameInput.setText(eventNameInput);
        }

        // setting date and time
        final Calendar datetimeCalendar = Calendar.getInstance();
        final long timestamp = cursor.getLong(Contracts.TimeCards.I_TIMESTAMP);
        datetimeCalendar.setTimeInMillis(timestamp);
        final Date dateTime = datetimeCalendar.getTime();
        if (!DateFormat.is24HourFormat(mContext)) {
            viewHolder.timeText.setText(TimeCardAdapter.TIME_FORMAT_STANDARD.format(dateTime));
        } else if (DateFormat.is24HourFormat(mContext)) {
            viewHolder.timeText.setText(TimeCardAdapter.TIME_FORMAT_MILITARY.format(dateTime));
        }
        viewHolder.dateText.setText(TimeCardAdapter.DATE_FORMAT.format(dateTime));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.time_card, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.eventNameInput = (EditText) view.findViewById(R.id.eventNameInput);
        viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
        viewHolder.timeText = (TextView) view.findViewById(R.id.timeText);
        view.setTag(viewHolder);

        return view;
    }


    private class ViewHolder {
        EditText eventNameInput;
        TextView dateText;
        TextView timeText;
    }
}