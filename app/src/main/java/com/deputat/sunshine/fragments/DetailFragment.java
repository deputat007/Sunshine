package com.deputat.sunshine.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.deputat.sunshine.R;
import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.deputat.sunshine.utils.SharedPreferenceUtil;
import com.deputat.sunshine.utils.WeatherUtil;
import java.util.Objects;
import org.greenrobot.eventbus.Subscribe;


public class DetailFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  @SuppressWarnings("unused")
  public static final int COL_WEATHER_ID = 0;
  private static final int COL_WEATHER_DATE = 1;
  private static final int COL_WEATHER_DESC = 2;
  private static final int COL_WEATHER_MAX_TEMP = 3;
  private static final int COL_WEATHER_MIN_TEMP = 4;
  private static final int COL_WEATHER_HUMIDITY = 5;
  private static final int COL_WEATHER_PRESSURE = 6;
  private static final int COL_WEATHER_WIND_SPEED = 7;
  private static final int COL_WEATHER_DEGREES = 8;
  private static final int COL_WEATHER_CONDITION_ID = 9;

  private static final int DETAIL_LOADER = 202;
  public static final String DETAIL_URI = "DETAIL_URI";

  private static final String[] DETAIL_COLUMNS = {
      WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
      WeatherContract.WeatherEntry.COLUMN_DATE,
      WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
      WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
      WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
      WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
      WeatherContract.WeatherEntry.COLUMN_PRESSURE,
      WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
      WeatherContract.WeatherEntry.COLUMN_DEGREES,
      WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
      WeatherContract.LocationEntry.COLUMN_CITY_ID
  };
  private static final String TAG = DetailFragment.class.getSimpleName();
  private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

  private ImageView iconView;
  private TextView dateView;
  private TextView friendlyDateView;
  private TextView descriptionView;
  private TextView highTempView;
  private TextView lowTempView;
  private TextView humidityView;
  private TextView windView;
  private TextView pressureView;

  private ShareActionProvider shareActionProvider;
  private Uri uri;
  private String forecastStr;

  public DetailFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  protected int getContentView() {
    return R.layout.fragment_detail;
  }

  @Override
  protected void initUi() {
    iconView = findViewById(R.id.detail_icon);
    dateView = findViewById(R.id.detail_date_textview);
    friendlyDateView = findViewById(R.id.detail_day_textview);
    descriptionView = findViewById(R.id.detail_forecast_textview);
    highTempView = findViewById(R.id.detail_high_textview);
    lowTempView = findViewById(R.id.detail_low_textview);
    humidityView = findViewById(R.id.detail_humidity_textview);
    windView = findViewById(R.id.detail_wind_textview);
    pressureView = findViewById(R.id.detail_pressure_textview);

  }

  @Override
  protected void setUi(@Nullable final Bundle savedInstanceState) {
    final Bundle arguments = getArguments();

    if (arguments != null) {
      uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
    }
  }

  @Override
  protected boolean useEventBus() {
    return true;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.detail, menu);

    final MenuItem item = menu.findItem(R.id.action_share);

    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    if (shareActionProvider != null) {
      shareActionProvider.setShareIntent(createShareForecastIntent());
    } else {
      Log.d(TAG, "Share Action Provider is null?");
    }
  }

  private Intent createShareForecastIntent() {
    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, forecastStr + FORECAST_SHARE_HASHTAG);
    return shareIntent;
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(Objects.requireNonNull(getContext()), uri, DETAIL_COLUMNS,
        null, null, null);
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
    Log.v(TAG, "In onLoadFinished");
    if (!data.moveToFirst()) {
      return;
    }

    final long date = data.getLong(COL_WEATHER_DATE);
    final String dateString = WeatherUtil.getFormattedMonthDay(getContext(), date);
    final String friendlyDateText = WeatherUtil.getDayName(getContext(), date);
    dateView.setText(dateString);
    friendlyDateView.setText(friendlyDateText);

    final String weatherDescription = data.getString(COL_WEATHER_DESC);
    descriptionView.setText(weatherDescription);

    final String high =
        WeatherUtil.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP)
        );
    highTempView.setText(high);
    final String low =
        WeatherUtil.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP)
        );
    lowTempView.setText(low);

    final String humidity =
        Objects.requireNonNull(getActivity()).getString(
            R.string.format_humidity, data.getDouble(COL_WEATHER_HUMIDITY));
    humidityView.setText(humidity);
    final double windSpeed = data.getDouble(COL_WEATHER_WIND_SPEED);
    final double windDirection = data.getDouble(COL_WEATHER_DEGREES);
    windView.setText(WeatherUtil.getFormattedWind(getContext(), windSpeed, windDirection));

    final double pressure = data.getDouble(COL_WEATHER_PRESSURE);
    pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
    forecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
    iconView.setImageResource(
        WeatherUtil.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

    // If onCreateOptionsMenu has already happened, we need to update the share intent now.
    if (shareActionProvider != null) {
      shareActionProvider.setShareIntent(createShareForecastIntent());
    }
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
  }

  /**
   * Eventbus event when location has been changed.
   */
  @SuppressWarnings({"deprecation", "unused"})
  @Subscribe
  public void onLocationChanged(OnLocationChangedEvent event) {
    if (this.uri != null) {
      final long date = WeatherContract.WeatherEntry.getDateFromUri(this.uri);
      final String location = SharedPreferenceUtil.getLocationId(getContext());
      this.uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
      getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }
  }
}

