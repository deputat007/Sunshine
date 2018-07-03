package com.deputat.sunshine.events;

import android.net.Uri;

public class OnForecastItemSelectedEvent {
    private final Uri mDateUri;

    public OnForecastItemSelectedEvent(Uri dateUri) {
        this.mDateUri = dateUri;
    }

    public Uri getDateUri() {
        return mDateUri;
    }
}
