package com.deputat.sunshine.events;

public class OnForecastItemClickEvent {

  private final long date;
  private final int position;

  public OnForecastItemClickEvent(long date, int position) {
    this.date = date;
    this.position = position;
  }


  public long getDate() {
    return date;
  }

  public int getPosition() {
    return position;
  }
}
