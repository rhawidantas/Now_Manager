package com.collinguarino.nowmanager;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.collinguarino.nowmanager.model.TimeCard;
import com.collinguarino.nowmanager.provider.Contracts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeCardAdapter extends CursorAdapter {

    private Context mContext;
    private final LayoutInflater mInflater;
    final Animation mSlideInAnimation;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd");
    public static final SimpleDateFormat TIME_FORMAT_MILITARY = new SimpleDateFormat("kk:mm:ss a");
    public static final SimpleDateFormat TIME_FORMAT_STANDARD = new SimpleDateFormat("hh:mm:ss a");

    public TimeCardAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSlideInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_top_to_bottom);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        final TimeCard timeCard = new TimeCard(cursor);

        //set event name input
        if (timeCard.getEventNameInput() != null) {
            viewHolder.eventNameInput.setText(timeCard.getEventNameInput());
        } else {
            viewHolder.eventNameInput.setText("");
        }

        // isThirdParty is passed into the `createNewTimeCard` method
        if (timeCard.isThirdParty()) {
            viewHolder.thirdPartyTag.setVisibility(View.VISIBLE);
        } else {
            viewHolder.thirdPartyTag.setVisibility(View.GONE);
        }

        // Saves event name input when focus is lost
        // TODO: not sure if this is needed anymore because you no longer can type directly into the edittext of a TimeCard on the main layout
        viewHolder.eventNameInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // focus is gone, save the text.
                    mContext.getContentResolver().update(Contracts.TimeCards.CONTENT_URI,
                            Contracts.TimeCards.getUpdateValues(viewHolder.eventNameInput.getText().toString()),
                            Contracts.TimeCards._ID + " = " + timeCard.getId(),
                            null);
                }
            }
        });

        // Hacked together method of expanding the touch radius of the edittext without making the TimeCard layout bigger
        viewHolder.expandTouchEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amendInput(viewHolder, timeCard);
            }
        });

        // Amending the event name
        viewHolder.eventNameInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amendInput(viewHolder, timeCard);
            }
        });

        // setting date and time
        final Calendar datetimeCalendar = Calendar.getInstance();
        datetimeCalendar.setTimeInMillis(timeCard.getTimestamp());
        final Date dateTime = datetimeCalendar.getTime();
        if (!DateFormat.is24HourFormat(mContext)) {
            viewHolder.timeText.setText(TimeCardAdapter.TIME_FORMAT_STANDARD.format(dateTime));
        } else if (DateFormat.is24HourFormat(mContext)) {
            viewHolder.timeText.setText(TimeCardAdapter.TIME_FORMAT_MILITARY.format(dateTime));
        }
        viewHolder.dateText.setText(TimeCardAdapter.DATE_FORMAT.format(dateTime));

    }

    // Edit the TimeCard object data
    public void amendInput(final ViewHolder viewHolder, final TimeCard timeCard) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.text_input);

        final EditText eventNameEdit = (EditText) dialog.findViewById(R.id.textInput);
        final TextView dateText = (TextView) dialog.findViewById(R.id.dateText);
        final TextView timeText = (TextView) dialog.findViewById(R.id.timeText);

        eventNameEdit.append(viewHolder.eventNameInput.getText().toString()); // append sets input to last char of string
        dateText.setText(viewHolder.dateText.getText().toString());
        timeText.setText(viewHolder.timeText.getText().toString());

        // open keyboard to focus on appending the log name
        eventNameEdit.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button b1 = (Button) dialog.findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewHolder.eventNameInput.setText(eventNameEdit.getText().toString());

                // save the new log data
                mContext.getContentResolver().update(Contracts.TimeCards.CONTENT_URI,
                        Contracts.TimeCards.getUpdateValues(viewHolder.eventNameInput.getText().toString()),
                        Contracts.TimeCards._ID + " = " + timeCard.getId(),
                        null);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public boolean getSystemTimeFormat() {

        if (!DateFormat.is24HourFormat(mContext)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        // This animation is called when scrolling to the top and on time card deletion.
        // Triggered too often, temporarily removed from release.
        /*if (position == 0) {
            view.startAnimation(mSlideInAnimation);
        }*/
        return view;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.time_card, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.eventNameInput = (EditText) view.findViewById(R.id.eventNameInput);
        viewHolder.expandTouchEvent = (RelativeLayout) view.findViewById(R.id.expandTouchEvent);
        viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
        viewHolder.timeText = (TextView) view.findViewById(R.id.timeText);
        viewHolder.thirdPartyTag = (ImageView) view.findViewById(R.id.thirdPartyTag);
        view.setTag(viewHolder);

        return view;
    }

    private class ViewHolder {
        EditText eventNameInput;
        RelativeLayout expandTouchEvent;
        TextView dateText;
        TextView timeText;
        ImageView thirdPartyTag;
    }

    /**
     * Helper method to get a TimeCard object from this adapter at a position.
     *
     * @param position Position to get timecard from
     * @return A TimeCard object or null if the position is invalid.
     */
    public TimeCard getTimeCard(final int position) {
        final Cursor cursor = getCursor();
        TimeCard timeCard = null;

        if (cursor.moveToPosition(position)) {
            timeCard = new TimeCard(cursor);
        }

        return timeCard;
    }
}