package com.deputat.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deputat.sunshine.data.WeatherContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    @SuppressWarnings("unused")
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;
    public static final int DETAIL_LOADER = 202;
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
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_CITY_ID
    };
    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String forecastStr;
    private ShareActionProvider shareActionProvider;
    private Uri uri;

    private ImageView iconView;
    private TextView dateView;
    private TextView friendlyDateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        if (arguments != null) {
            uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        final View rootView = inflater.inflate(R.layout.fragment_detail, container,
                false);

        iconView = rootView.findViewById(R.id.detail_icon);
        dateView = rootView.findViewById(R.id.detail_date_textview);
        friendlyDateView = rootView.findViewById(R.id.detail_day_textview);
        descriptionView = rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = rootView.findViewById(R.id.detail_high_textview);
        lowTempView = rootView.findViewById(R.id.detail_low_textview);
        humidityView = rootView.findViewById(R.id.detail_humidity_textview);
        windView = rootView.findViewById(R.id.detail_wind_textview);
        pressureView = rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(Objects.requireNonNull(getContext()), uri, DETAIL_COLUMNS,
                null, null, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.v(TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        long date = data.getLong(COL_WEATHER_DATE);
        String dateString = Utility.getFormattedMonthDay(getContext(), date);
        String friendlyDateText = Utility.getDayName(getContext(), date);
        dateView.setText(dateString);
        friendlyDateView.setText(friendlyDateText);

        String weatherDescription = data.getString(COL_WEATHER_DESC);
        descriptionView.setText(weatherDescription);

        String high =
                Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP)
                );
        highTempView.setText(high);
        String low =
                Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP)
                );
        lowTempView.setText(low);

        String humidity =
                Objects.requireNonNull(getActivity()).getString(
                        R.string.format_humidity, data.getDouble(COL_WEATHER_HUMIDITY));
        humidityView.setText(humidity);
        double windSpeed = data.getDouble(COL_WEATHER_WIND_SPEED);
        double windDirection = data.getDouble(COL_WEATHER_DEGREES);
        windView.setText(Utility.getFormattedWind(getContext(), windSpeed, windDirection));

        double pressure = data.getDouble(COL_WEATHER_PRESSURE);
        pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
        forecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
        iconView.setImageResource(
                Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @SuppressWarnings({"deprecation", "unused"})
    @Subscribe
    public void onLocationChanged(String location) {
        if (this.uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(this.uri);
            this.uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}

