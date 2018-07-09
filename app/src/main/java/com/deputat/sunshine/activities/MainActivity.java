package com.deputat.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.deputat.sunshine.R;
import com.deputat.sunshine.events.OnForecastItemSelectedEvent;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.deputat.sunshine.fragments.DetailFragment;
import com.deputat.sunshine.fragments.ForecastFragment;
import com.deputat.sunshine.sync.SunshineSyncAdapter;
import com.deputat.sunshine.utils.LocationUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class MainActivity extends BaseActivity {

    public static final String KEY_TWO_PANE = "KEY_TWO_PANE";
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    public static final int PERMISSIONS_REQUEST_LOCATION = 101;

    private String mUnits;
    private boolean mTwoPane;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initUI() {

    }

    @Override
    protected void setUI(final Bundle savedInstanceState) {
        LocationUtil.updateLastLocation(this);

        mUnits = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_units_key),
                        getString(R.string.pref_units_label_metric));
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                final Fragment fragment = new DetailFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        final ForecastFragment forecastFragment =
                (ForecastFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_forecast);
        final Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_TWO_PANE, mTwoPane);

        Objects.requireNonNull(forecastFragment).setArguments(arguments);
        forecastFragment.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationUtil.updateLastLocation(this);
                }
                EventBus.getDefault().post(new OnLocationChangedEvent());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String units = preferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_label_metric));

        if (!units.equals(this.mUnits)) {
            EventBus.getDefault().post(new OnLocationChangedEvent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onItemSelected(OnForecastItemSelectedEvent event) {
        if (mTwoPane) {
            final DetailFragment detailFragment = new DetailFragment();
            final Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, event.getDateUri());
            detailFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            startActivity(new Intent(this,
                    DetailActivity.class).setData(event.getDateUri()));
        }
    }
}
