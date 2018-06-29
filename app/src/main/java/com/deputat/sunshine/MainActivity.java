package com.deputat.sunshine;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.deputat.sunshine.events.LocationChangedEvent;
import com.deputat.sunshine.sync.SunshineSyncAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    public static final String KEY_TWO_PANE = "KEY_TWO_PANE";
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private static final int PERMISSIONS_REQUEST_LOCATION = 101;

    private String units;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SunshineSyncAdapter.initializeSyncAdapter(this);
        updateLastLocation();
        units = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_units_key),
                        getString(R.string.pref_units_label_metric));

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            twoPane = true;

            if (savedInstanceState == null) {
                final Fragment fragment = new DetailFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }

        final ForecastFragment forecastFragment =
                (ForecastFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_forecast);
        final Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_TWO_PANE, twoPane);

        Objects.requireNonNull(forecastFragment).setArguments(arguments);
        forecastFragment.setUseTodayLayout(!twoPane);
    }

    private void updateLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            final FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                final SharedPreferences preferences = PreferenceManager
                                        .getDefaultSharedPreferences(MainActivity.this);
                                preferences.edit()
                                        .putString(getString(R.string.pref_coord_lat),
                                                String.valueOf(location.getLatitude()))
                                        .putString(getString(R.string.pref_coord_lon),
                                                String.valueOf(location.getLongitude()))
                                        .apply();
                                final String locationId = preferences.getString(
                                        getString(R.string.pref_location_id),
                                        getString(R.string.pref_location_id_default));

                                EventBus.getDefault().post(new LocationChangedEvent(locationId));

                            }
                        }
                    });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLastLocation();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String units = preferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_label_metric));

        if (!units.equals(this.units)) {
            final String locationId = preferences.getString(
                    getString(R.string.pref_location_id),
                    getString(R.string.pref_location_id_default));

            EventBus.getDefault().post(new LocationChangedEvent(locationId));
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

    @Override
    public void onItemSelected(Uri dateUri) {
        if (twoPane) {
            final DetailFragment detailFragment = new DetailFragment();
            final Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, dateUri);
            detailFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            startActivity(new Intent(this, DetailActivity.class).setData(dateUri));
        }
    }
}
