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
import com.deputat.sunshine.MainActivity;
import com.deputat.sunshine.R;
import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Vector;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final int SYNC_INTERVAL = 30;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

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

    SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
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
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        final Account account = getSyncAccount(context);
        final String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        syncImmediately(context);
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
        final String lon = Utility.getCoordLon(getContext());
        final String lat = Utility.getCoordLat(getContext());

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonStr;

        final String format = "json";
        final String units = "metric";
        final int numDays = 14;

        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String LON_PARAM = "lon";
            final String LAT_PARAM = "lat";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";
            final Uri builtUri = Uri.parse(FORECAST_BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(LON_PARAM, lon)
                    .appendQueryParameter(LAT_PARAM, lat)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
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
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
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
        final String OWM_CITY_ID = "id";
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            final JSONObject forecastJson = new JSONObject(forecastJsonStr);
            final JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            final JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

            final String cityId = cityJson.getString(OWM_CITY_ID);
            final String cityName = cityJson.getString(OWM_CITY_NAME);

            final JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            final double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            final double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
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
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                final JSONObject dayForecast = weatherArray.getJSONObject(i);

                dateTime = dayTime.setJulianDay(julianStartDay + i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                final JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER)
                        .getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                final JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

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
        final boolean enableNotification = prefs.getBoolean(enableNotificationKey, true);

        if (!enableNotification) {
            return;
        }

        final String lastNotificationKey = context.getString(R.string.pref_last_notification);
        final long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            final String locationQuery = Utility.getLocationId(context);

            final Uri weatherUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            final Cursor cursor = context.getContentResolver().query(weatherUri,
                    NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (Objects.requireNonNull(cursor).moveToFirst()) {
                final int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                final double high = cursor.getDouble(INDEX_MAX_TEMP);
                final double low = cursor.getDouble(INDEX_MIN_TEMP);
                final String desc = cursor.getString(INDEX_SHORT_DESC);

                final int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                final String title = context.getString(R.string.app_name);

                final String contentText = String.format(context.getString(R.string.format_notification),
                        desc, Utility.formatTemperature(context, high),
                        Utility.formatTemperature(context, low));

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

                notificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
            }
            cursor.close();
        }
    }

}
