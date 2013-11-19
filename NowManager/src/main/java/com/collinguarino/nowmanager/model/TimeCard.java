package com.collinguarino.nowmanager.model;

import android.database.Cursor;
import android.util.Log;

import com.collinguarino.nowmanager.provider.Contracts;

public class TimeCard {
    private long id;
    private String eventNameInput;
    private long timestamp;
    private boolean isTally;

    public TimeCard() {
    }

    public TimeCard(final Cursor cursor) {
        id = cursor.getLong(Contracts.TimeCards.I_ID);
        eventNameInput = cursor.getString(Contracts.TimeCards.I_EVENT_NAME_INPUT);
        timestamp = cursor.getLong(Contracts.TimeCards.I_TIMESTAMP);
        isTally = cursor.getInt(Contracts.TimeCards.I_IS_TALLY) == 1;
        Log.v("TimeCard", toString());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEventNameInput() {
        return eventNameInput;
    }

    public void setEventNameInput(String eventNameInput) {
        this.eventNameInput = eventNameInput;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTally() {
        return isTally;
    }

    public void setTally(boolean isTally) {
        this.isTally = isTally;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeCard timeCard = (TimeCard) o;

        if (id != timeCard.id) return false;
        if (isTally != timeCard.isTally) return false;
        if (timestamp != timeCard.timestamp) return false;
        if (eventNameInput != null ? !eventNameInput.equals(timeCard.eventNameInput) : timeCard.eventNameInput != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (eventNameInput != null ? eventNameInput.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (isTally ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeCard{" +
                "id=" + id +
                ", eventNameInput='" + eventNameInput + '\'' +
                ", timestamp=" + timestamp +
                ", isTally=" + isTally +
                '}';
    }
}
