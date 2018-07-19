package com.deputat.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;
import com.deputat.sunshine.BuildConfig;
import com.deputat.sunshine.R;
import com.deputat.sunshine.activities.MainActivity;
import com.deputat.sunshine.application.Constants;
import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.events.OnWeatherForecastUpdatedEvent;
import com.deputat.sunshine.utils.SharedPreferenceUtil;
import com.deputat.sunshine.utils.WeatherUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
      WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
      WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
      WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
      WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
  };
  private static final int INDEX_WEATHER_ID = 0;
  private static final int INDEX_MAX_TEMP = 1;
  private static final int INDEX_MIN_TEMP = 2;
  private static final int INDEX_SHORT_DESC = 3;
  private static final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

  SunshineSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
  }

  /**
   * Helper method to have the sync adapter sync immediately.
   *
   * @param context The context used to access the account service
   */
  public static void syncImmediately(Context context) {
    final Bundle bundle = new Bundle();
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    ContentResolver.requestSync(getSyncAccount(context),
        context.getString(R.string.content_authority), bundle);
  }

  /**
   * Helper method to schedule the sync adapter periodic execution.
   */
  private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
    final Account account = getSyncAccount(context);
    final String authority = context.getString(R.string.content_authority);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      final SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime)
          .setSyncAdapter(account, authority).setExtras(new Bundle()).build();
      ContentResolver.requestSync(request);
    } else {
      ContentResolver.addPeriodicSync(account,
          authority, new Bundle(), syncInterval);
    }
  }

  private static void onAccountCreated(Account newAccount, Context context) {
    SunshineSyncAdapter.configurePeriodicSync(context, Constants.SYNC_INTERVAL,
        Constants.SYNC_FLEXTIME);
    ContentResolver
        .setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
  }

  public static void initializeSyncAdapter(Context context) {
    getSyncAccount(context);
  }

  private static Account getSyncAccount(Context context) {
    final AccountManager accountManager =
        (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

    final Account newAccount = new Account(
        context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

    if (null == Objects.requireNonNull(accountManager).getPassword(newAccount)) {

      if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
        return null;
      }

      onAccountCreated(newAccount, context);
    }
    return newAccount;
  }

  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    final String lon = SharedPreferenceUtil.getCoordLon(getContext());
    final String lat = SharedPreferenceUtil.getCoordLat(getContext());

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;

    String forecastJsonStr;

    final String format = "json";
    final String units = "metric";
    final int numDays = 14;

    try {
      final String forecastBaseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?";
      final String lonParam = "lon";
      final String latParam = "lat";
      final String formatParam = "mode";
      final String unitsParam = "units";
      final String daysParam = "cnt";
      final String appidParam = "APPID";
      final String langParam = "lang";
      final Uri builtUri = Uri.parse(forecastBaseUrl)
          .buildUpon()
          .appendQueryParameter(lonParam, lon)
          .appendQueryParameter(latParam, lat)
          .appendQueryParameter(formatParam, format)
          .appendQueryParameter(unitsParam, units)
          .appendQueryParameter(langParam, Locale.getDefault().getLanguage())
          .appendQueryParameter(daysParam, Integer.toString(numDays))
          .appendQueryParameter(appidParam, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
          .build();

      final URL url = new URL(builtUri.toString());
      Log.d(LOG_TAG, builtUri.toString());

      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      final InputStream inputStream = urlConnection.getInputStream();
      final StringBuilder buffer = new StringBuilder();
      if (inputStream == null) {
        return;
      }
      reader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line).append("\n");
      }

      if (buffer.length() == 0) {
        return;
      }

      forecastJsonStr = buffer.toString();
      getWeatherDataFromJson(forecastJsonStr);
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error ", e);
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "Error closing stream", e);
        }
      }
    }
  }

  /**
   * Helper method to handle insertion of a new location in the weather database.
   *
   * @param locationSetting The location string used to request updates from the server.
   * @param cityName A human-readable city name, e.g "Mountain View"
   * @param lat the latitude of the city
   * @param lon the longitude of the city
   * @return the row ID of the added location.
   */
  private long addLocation(String locationSetting, String cityName, double lat, double lon) {
    @SuppressLint("Recycle") Cursor cursor = getContext().getContentResolver()
        .query(WeatherContract.LocationEntry.CONTENT_URI,
            new String[]{WeatherContract.LocationEntry._ID},
            WeatherContract.LocationEntry.COLUMN_CITY_ID + " = ?",
            new String[]{locationSetting}, null);
    if (cursor != null && cursor.moveToFirst()) {
      final int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);

      return cursor.getLong(locationIdIndex);
    } else {
      final ContentValues contentValues = new ContentValues(1);
      contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_ID,
          locationSetting);
      contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
      contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
      contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

      final Uri insertedUri = getContext().getContentResolver()
          .insert(WeatherContract.LocationEntry.CONTENT_URI, contentValues);

      return ContentUris.parseId(insertedUri);
    }
  }

  private void getWeatherDataFromJson(String forecastJsonStr) {
    final String jsonCityIid = "id";
    final String jsonCity = "city";
    final String jsonCityName = "name";
    final String jsonCoord = "coord";

    final String jsonLat = "lat";
    final String jsonLon = "lon";

    final String jsonList = "list";

    final String jsonPressure = "pressure";
    final String jsonHumidity = "humidity";
    final String jsonWindSpeed = "speed";
    final String jsonWindDirection = "deg";

    final String jsonTemp = "temp";
    final String jsonMax = "max";
    final String jsonMin = "min";

    final String jsonWeather = "weather";
    final String jsonDescription = "main";
    final String jsonWeatherId = "id";

    try {
      final JSONObject forecastJson = new JSONObject(forecastJsonStr);
      final JSONArray weatherArray = forecastJson.getJSONArray(jsonList);

      final JSONObject cityJson = forecastJson.getJSONObject(jsonCity);

      final String cityId = cityJson.getString(jsonCityIid);
      final String cityName = cityJson.getString(jsonCityName);

      final JSONObject cityCoord = cityJson.getJSONObject(jsonCoord);
      final double cityLatitude = cityCoord.getDouble(jsonLat);
      final double cityLongitude = cityCoord.getDouble(jsonLon);
      final long locationId = addLocation(cityId, cityName, cityLatitude, cityLongitude);
      PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
          .putString(getContext().getString(R.string.pref_location_id),
              String.valueOf(cityId))
          .apply();

      final Vector<ContentValues> cVVector = new Vector<>(weatherArray.length());

      Time dayTime = new Time();
      dayTime.setToNow();

      final int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

      dayTime = new Time();

      for (int i = 0; i < weatherArray.length(); i++) {
        final JSONObject dayForecast = weatherArray.getJSONObject(i);

        final long dateTime = dayTime.setJulianDay(julianStartDay + i);
        final double pressure = dayForecast.getDouble(jsonPressure);
        final int humidity = dayForecast.getInt(jsonHumidity);
        final double windSpeed = dayForecast.getDouble(jsonWindSpeed);
        final double windDirection = dayForecast.getDouble(jsonWindDirection);

        final JSONObject temperatureObject = dayForecast.getJSONObject(jsonTemp);
        final double high = temperatureObject.getDouble(jsonMax);
        final double low = temperatureObject.getDouble(jsonMin);

        final JSONObject weatherObject = dayForecast.getJSONArray(jsonWeather)
            .getJSONObject(0);

        final String description = weatherObject.getString(jsonDescription);
        final int weatherId = weatherObject.getInt(jsonWeatherId);

        final ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

        cVVector.add(weatherValues);
      }

      int inserted = 0;

      if (cVVector.size() > 0) {
        final ContentValues[] contentValues = new ContentValues[cVVector.size()];
        cVVector.toArray(contentValues);
        inserted = getContext().getContentResolver()
            .bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);

        getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
            WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
            new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});

        notifyWeather();
      }

      Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");
    } catch (JSONException e) {
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }
  }

  @SuppressWarnings("deprecation")
  private void notifyWeather() {
    final Context context = getContext();
    final String enableNotificationKey = context.getString(R.string.pref_enable_notifications_key);
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    EventBus.getDefault().post(new OnWeatherForecastUpdatedEvent());
    final boolean enableNotification = prefs.getBoolean(enableNotificationKey, true);

    if (!enableNotification) {
      return;
    }

    final String lastNotificationKey = context.getString(R.string.pref_last_notification);
    final long lastSync = prefs.getLong(lastNotificationKey, 0);

    if (System.currentTimeMillis() - lastSync >= Constants.DAY_IN_MILLIS) {
      final String locationQuery = SharedPreferenceUtil.getLocationId(context);

      final Uri weatherUri = WeatherContract.WeatherEntry
          .buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

      final Cursor cursor = context.getContentResolver().query(weatherUri,
          NOTIFY_WEATHER_PROJECTION, null, null, null);

      if (Objects.requireNonNull(cursor).moveToFirst()) {
        final int weatherId = cursor.getInt(INDEX_WEATHER_ID);
        final double high = cursor.getDouble(INDEX_MAX_TEMP);
        final double low = cursor.getDouble(INDEX_MIN_TEMP);
        final String desc = cursor.getString(INDEX_SHORT_DESC);

        final int iconId = WeatherUtil.getIconResourceForWeatherCondition(weatherId);
        final String title = context.getString(R.string.app_name);

        final String contentText = String.format(context.getString(R.string.format_notification),
            desc, WeatherUtil.formatTemperature(context, high),
            WeatherUtil.formatTemperature(context, low));

        final Intent nextIntent = new Intent(getContext(), MainActivity.class);
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getContext());
        taskStackBuilder.addNextIntent(nextIntent);

        final PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,
            PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(getContext())
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        final NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(getContext());

        notificationManager.notify(Constants.WEATHER_NOTIFICATION_ID, mBuilder.build());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(lastNotificationKey, System.currentTimeMillis());
        editor.apply();
      }
      cursor.close();
    }
  }

}
