package com.deputat.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import java.util.Objects;

public class WeatherProvider extends ContentProvider {

  private static final int WEATHER = 100;
  private static final int WEATHER_WITH_LOCATION = 101;
  private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
  private static final int LOCATION = 300;

  private static final UriMatcher sUriMatcher = buildUriMatcher();
  private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

  //location.location_setting = ?
  private static final String sLocationSettingSelection = WeatherContract.LocationEntry.TABLE_NAME
      + "."
      + WeatherContract.LocationEntry.COLUMN_CITY_ID
      + " = ? ";
  //location.location_setting = ? AND date >= ?
  private static final String sLocationSettingWithStartDateSelection =
      WeatherContract.LocationEntry.TABLE_NAME
          + "."
          + WeatherContract.LocationEntry.COLUMN_CITY_ID
          + " = ? AND "
          + WeatherContract.WeatherEntry.COLUMN_DATE
          + " >= ? ";
  //location.location_setting = ? AND date = ?
  private static final String sLocationSettingAndDaySelection =
      WeatherContract.LocationEntry.TABLE_NAME
          + "."
          + WeatherContract.LocationEntry.COLUMN_CITY_ID
          + " = ? AND "
          + WeatherContract.WeatherEntry.COLUMN_DATE
          + " = ? ";

  static {
    sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

    //INNER JOIN location ON weather.location_id = location._id
    sWeatherByLocationSettingQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME
        + " INNER JOIN "
        + WeatherContract.LocationEntry.TABLE_NAME
        + " ON "
        + WeatherContract.WeatherEntry.TABLE_NAME
        + "."
        + WeatherContract.WeatherEntry.COLUMN_LOC_KEY
        + " = "
        + WeatherContract.LocationEntry.TABLE_NAME
        + "."
        + WeatherContract.LocationEntry._ID);
  }

  private WeatherDbHelper weatherDbHelper;

  private static UriMatcher buildUriMatcher() {
    final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = WeatherContract.CONTENT_AUTHORITY;

    uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
    uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*",
        WEATHER_WITH_LOCATION);
    uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/#",
        WEATHER_WITH_LOCATION_AND_DATE);
    uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);

    return uriMatcher;
  }

  private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
    final String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
    final long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

    String[] selectionArgs;
    String selection;

    if (startDate == 0) {
      selection = sLocationSettingSelection;
      selectionArgs = new String[]{locationSetting};
    } else {
      selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
      selection = sLocationSettingWithStartDateSelection;
    }

    return sWeatherByLocationSettingQueryBuilder.query(weatherDbHelper.getReadableDatabase(),
        projection, selection, selectionArgs, null, null, sortOrder);
  }

  private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection,
      String sortOrder) {
    final String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
    final long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

    return sWeatherByLocationSettingQueryBuilder.query(weatherDbHelper.getReadableDatabase(),
        projection, sLocationSettingAndDaySelection,
        new String[]{locationSetting, Long.toString(date)}, null, null,
        sortOrder);
  }

  @Override
  public boolean onCreate() {
    weatherDbHelper = new WeatherDbHelper(getContext());
    return true;
  }

  @Override
  public String getType(@NonNull Uri uri) {
    final int match = sUriMatcher.match(uri);

    switch (match) {
      case WEATHER_WITH_LOCATION_AND_DATE:
        return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
      case WEATHER_WITH_LOCATION:
        return WeatherContract.WeatherEntry.CONTENT_TYPE;
      case WEATHER:
        return WeatherContract.WeatherEntry.CONTENT_TYPE;
      case LOCATION:
        return WeatherContract.LocationEntry.CONTENT_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[]
      selectionArgs, String sortOrder) {
    Cursor retCursor;
    switch (sUriMatcher.match(uri)) {
      // "weather/*/*"
      case WEATHER_WITH_LOCATION_AND_DATE: {
        retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
        break;
      }
      // "weather/*"
      case WEATHER_WITH_LOCATION: {
        retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
        break;
      }
      // "weather"
      case WEATHER: {
        retCursor = weatherDbHelper.getReadableDatabase()
            .query(WeatherContract.WeatherEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        break;
      }
      // "location"
      case LOCATION: {
        retCursor = weatherDbHelper.getReadableDatabase()
            .query(WeatherContract.LocationEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        break;
      }

      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    retCursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
    return retCursor;
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    final SQLiteDatabase db = weatherDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri;

    switch (match) {
      case WEATHER: {
        normalizeDate(values);
        final long id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,
            null, values);
        if (id > 0) {
          returnUri = WeatherContract.WeatherEntry.buildWeatherUri(id);
        } else {
          throw new SQLException("Failed to insert row into " + uri);
        }
        break;
      }
      case LOCATION: {
        normalizeDate(values);
        final long id = db.insert(WeatherContract.LocationEntry.TABLE_NAME,
            null, values);
        if (id > 0) {
          returnUri = WeatherContract.LocationEntry.buildLocationUri(id);
        } else {
          throw new SQLException("Failed to insert row into " + uri);
        }

        break;
      }
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
    return returnUri;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase database = weatherDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int deletedRow;
    if (null == selection) {
      selection = "1";
    }

    switch (match) {
      case WEATHER: {
        deletedRow = database.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection,
            selectionArgs);
        break;
      }
      case LOCATION: {
        deletedRow = database.delete(WeatherContract.LocationEntry.TABLE_NAME, selection,
            selectionArgs);
        break;
      }
      default:
        throw new UnsupportedOperationException("Unknown uri " + uri);
    }

    if (deletedRow != 0) {
      Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,
          null);
    }

    return deletedRow;
  }

  private void normalizeDate(ContentValues values) {
    // normalize the date value
    if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
      final long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);

      values.put(WeatherContract.WeatherEntry.COLUMN_DATE,
          WeatherContract.normalizeDate(dateValue));
    }
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    final SQLiteDatabase database = weatherDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int updatedRows;

    if (null == selection) {
      selection = "1";
    }
    switch (match) {
      case WEATHER: {
        updatedRows = database.update(WeatherContract.WeatherEntry.TABLE_NAME, values,
            selection, selectionArgs);
        break;
      }

      case LOCATION: {
        updatedRows = database.update(WeatherContract.LocationEntry.TABLE_NAME, values,
            selection, selectionArgs);
        break;
      }

      default:
        throw new UnsupportedOperationException("Unknown uri " + uri);
    }

    if (updatedRows != 0) {
      Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,
          null);
    }

    return updatedRows;
  }

  @Override
  public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
    final SQLiteDatabase db = weatherDbHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);

    switch (match) {
      case WEATHER:
        db.beginTransaction();
        int returnCount = 0;
        try {
          for (ContentValues value : values) {
            normalizeDate(value);
            long id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,
                null, value);
            if (id != -1) {
              returnCount++;
            }
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,
            null);

        return returnCount;
      default:
        return super.bulkInsert(uri, values);
    }
  }

  @Override
  public void shutdown() {
    weatherDbHelper.close();
    super.shutdown();
  }
}