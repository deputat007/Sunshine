package com.deputat.sunshine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.deputat.sunshine.R;

public class SharedPreferenceUtil {

  /**
   * Location id.
   */
  public static String getLocationId(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getString(context.getString(R.string.pref_location_id),
        context.getString(R.string.pref_location_id_default));
  }

  /**
   * Longitude.
   */
  public static String getCoordLon(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getString(context.getString(R.string.pref_coord_lon),
        context.getString(R.string.pref_coord_lon_default));
  }

  /**
   * Latitude.
   */
  public static String getCoordLat(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getString(context.getString(R.string.pref_coord_lat),
        context.getString(R.string.pref_coord_lat_default));
  }

  static boolean isMetric(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getString(context.getString(R.string.pref_units_key),
        context.getString(R.string.pref_units_metric))
        .equals(context.getString(R.string.pref_units_metric));
  }

  /**
   * Is location detection enabled.
   */
  public static boolean isLocationDetectionEnabled(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getBoolean(context.getString(R.string.pref_enable_location_detection_key),
        true);
  }
}
