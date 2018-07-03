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
import com.deputat.sunshine.utils.WeatherUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;


public class DetailFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
            WeatherContract.LocationEntry.COLUMN_CITY_ID
    };
    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ImageView mIconView;
    private TextView mDateView;
    private TextView mFriendlyDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private String mForecastStr;

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
    protected void initUI() {
        mIconView = findViewById(R.id.detail_icon);
        mDateView = findViewById(R.id.detail_date_textview);
        mFriendlyDateView = findViewById(R.id.detail_day_textview);
        mDescriptionView = findViewById(R.id.detail_forecast_textview);
        mHighTempView = findViewById(R.id.detail_high_textview);
        mLowTempView = findViewById(R.id.detail_low_textview);
        mHumidityView = findViewById(R.id.detail_humidity_textview);
        mWindView = findViewById(R.id.detail_wind_textview);
        mPressureView = findViewById(R.id.detail_pressure_textview);

    }

    @Override
    protected void setUI(@Nullable final Bundle savedInstanceState) {
        final Bundle arguments = getArguments();

        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
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

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(Objects.requireNonNull(getContext()), mUri, DETAIL_COLUMNS,
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
        mDateView.setText(dateString);
        mFriendlyDateView.setText(friendlyDateText);

        final String weatherDescription = data.getString(COL_WEATHER_DESC);
        mDescriptionView.setText(weatherDescription);

        final String high =
                WeatherUtil.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP)
                );
        mHighTempView.setText(high);
        final String low =
                WeatherUtil.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP)
                );
        mLowTempView.setText(low);

        final String humidity =
                Objects.requireNonNull(getActivity()).getString(
                        R.string.format_humidity, data.getDouble(COL_WEATHER_HUMIDITY));
        mHumidityView.setText(humidity);
        final double windSpeed = data.getDouble(COL_WEATHER_WIND_SPEED);
        final double windDirection = data.getDouble(COL_WEATHER_DEGREES);
        mWindView.setText(WeatherUtil.getFormattedWind(getContext(), windSpeed, windDirection));

        final double pressure = data.getDouble(COL_WEATHER_PRESSURE);
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
        mForecastStr = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
        mIconView.setImageResource(
                WeatherUtil.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @SuppressWarnings({"deprecation", "unused"})
    @Subscribe
    public void onLocationChanged(String location) {
        if (this.mUri != null) {
            final long date = WeatherContract.WeatherEntry.getDateFromUri(this.mUri);
            this.mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}

