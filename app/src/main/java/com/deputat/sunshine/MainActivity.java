package com.deputat.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.deputat.sunshine.sync.SunshineSyncAdapter;

import java.util.Objects;

/**
 * @author Andriy Deputat on 05.01.18.
 */
public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_TWO_PANE = "KEY_TWO_PANE";
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private String location;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SunshineSyncAdapter.initializeSyncAdapter(this);
        location = Utility.getPreferredLocation(this);

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
        Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_TWO_PANE, twoPane);

        Objects.requireNonNull(forecastFragment).setArguments(arguments);
        forecastFragment.setUseTodayLayout(!twoPane);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.equals(this.location)) {
            ForecastFragment ff =
                    (ForecastFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_forecast);
            if (ff != null) {
                ff.onLocationChanged();
            }
            DetailFragment detailFragment =
                    (DetailFragment) getSupportFragmentManager()
                            .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onLocationChanged(location);
            }
            this.location = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_map) {
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri uri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (twoPane) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle arguments = new Bundle();
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
