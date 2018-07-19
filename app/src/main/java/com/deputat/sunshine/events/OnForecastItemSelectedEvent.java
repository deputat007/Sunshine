package com.deputat.sunshine.events;

import android.net.Uri;

public class OnForecastItemSelectedEvent {

  private final Uri dateUri;

  public OnForecastItemSelectedEvent(Uri dateUri) {
    this.dateUri = dateUri;
  }

  public Uri getDateUri() {
    return dateUri;
  }
}
