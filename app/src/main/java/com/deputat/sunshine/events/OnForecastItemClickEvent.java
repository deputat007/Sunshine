package com.deputat.sunshine.events;

public class OnForecastItemClickEvent {
    private final long mDate;
    private final int mPosition;

    public OnForecastItemClickEvent(long date, int position) {
        this.mDate = date;
        this.mPosition = position;
    }


    public long getDate() {
        return mDate;
    }

    public int getPosition() {
        return mPosition;
    }
}
