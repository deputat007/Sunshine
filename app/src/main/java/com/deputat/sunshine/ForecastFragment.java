package com.deputat.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.events.LocationChangedEvent;
import com.deputat.sunshine.events.OnForecastItemClickEvent;
import com.deputat.sunshine.events.OnForecastItemSelectedEvent;
import com.deputat.sunshine.sync.SunshineSyncAdapter;
import com.deputat.sunshine.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int FORECAST_LOADER = 101;
    static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_ID,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    @SuppressWarnings("unused")
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    @SuppressWarnings("unused")
    static final int COL_COORD_LAT = 7;
    @SuppressWarnings("unused")
    static final int COL_COORD_LONG = 8;

    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private int INVALID_POSITION = -1;

    private int position = INVALID_POSITION;

    public ForecastFragment() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                openPreferredLocationInMap();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new ForecastAdapter(getContext(), null);

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = rootView.findViewById(R.id.recycler_view_forecast);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.sunshine_light_blue,
                R.color.sunshine_blue, R.color.sunshine_dark_blue);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeather();
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_POSITION)) {
            position = savedInstanceState.getInt(KEY_POSITION);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (position != INVALID_POSITION) {
            outState.putInt(KEY_POSITION, position);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String locationSetting = Utility.getLocationId(getActivity());
        final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        final Uri weatherForLocationUri =
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,
                        System.currentTimeMillis());

        return new CursorLoader(Objects.requireNonNull(getContext()), weatherForLocationUri,
                FORECAST_COLUMNS, null, null, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader,
                               Cursor data) {
        adapter.swapCursor(data);
        swipeRefreshLayout.setRefreshing(false);

        if (position != INVALID_POSITION) {
            recyclerView.smoothScrollToPosition(position);
        } else {
            if (Objects.requireNonNull(getArguments()).getBoolean(MainActivity.KEY_TWO_PANE,
                    false)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.smoothScrollToPosition(0);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
        swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressWarnings({"deprecation", "unused"})
    @Subscribe
    public void onLocationChanged(LocationChangedEvent locationChangedEvent) {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onForecastItemClick(OnForecastItemClickEvent event) {
        final int position = event.getPosition();

        final String locationSetting = Utility.getLocationId(getActivity());
        EventBus.getDefault().post(new OnForecastItemSelectedEvent(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                        event.getDate())));

        ForecastFragment.this.position = position;
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocationInMap() {
        final Cursor cursor = adapter.getCursor();

        if (cursor != null && cursor.moveToPosition(0)) {

            final String posLat = Objects.requireNonNull(cursor).getString(COL_COORD_LAT);
            final String posLong = cursor.getString(COL_COORD_LONG);

            final Uri uri = Uri.parse("geo:" + posLat + "," + posLong);

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            if (intent.resolveActivity(Objects.requireNonNull(
                    getActivity()).getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Log.d(TAG, "Couldn't call " + uri.toString() + ", no receiving apps installed!");
            }
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        adapter.setUseTodayLayout(useTodayLayout);
    }
}
