package com.deputat.sunshine;

import android.annotation.TargetApi;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.service.SunshineService;

import java.util.Objects;


/**
 * A placeholder fragment containing a simple view.
 *
 * @author Andriy Deputat on 05.01.18.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int FORECAST_LOADER = 101;
    static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
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

    private ForecastAdapter adapter;
    private ListView listView;

    private int position = ListView.INVALID_POSITION;

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
            case R.id.action_refresh:
                updateWeather();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new ForecastAdapter(getContext(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((MainActivity) Objects.requireNonNull(getActivity())).onItemSelected(
                            WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                                    cursor.getLong(COL_WEATHER_DATE)));
                }
                ForecastFragment.this.position = position;
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
        if (position != ListView.INVALID_POSITION) {
            outState.putInt(KEY_POSITION, position);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri =
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

        if (position != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(position);
        } else {
            if (Objects.requireNonNull(getArguments()).getBoolean(MainActivity.KEY_TWO_PANE,
                    false)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        listView.performItemClick(listView, 0,
                                listView.getItemIdAtPosition(0));
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @SuppressWarnings("deprecation")
    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeather() {
        final String location = Utility.getPreferredLocation(getActivity());
        SunshineService.startActionWeather(getContext(), location);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        adapter.setUseTodayLayout(useTodayLayout);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        @SuppressWarnings("unused")
        void onItemSelected(Uri dateUri);
    }
}
