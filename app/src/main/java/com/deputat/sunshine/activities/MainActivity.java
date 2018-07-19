package com.deputat.sunshine.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.deputat.sunshine.BuildConfig;
import com.deputat.sunshine.R;
import com.deputat.sunshine.events.OnForecastItemSelectedEvent;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.deputat.sunshine.fragments.DetailFragment;
import com.deputat.sunshine.fragments.ForecastFragment;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends BaseActivity {

  public static final String KEY_TWO_PANE = "KEY_TWO_PANE";
  private static final String TAG = BaseActivity.class.getSimpleName();
  private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
  private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";

  private String units;
  private boolean twoPane;

  @Override
  protected int getContentView() {
    return R.layout.activity_main;
  }

  @Override
  protected void initUi() {

  }

  @Override
  protected void setUi(final Bundle savedInstanceState) {
    if (!checkPermissions()) {
      requestPermissions();
    }
    units = PreferenceManager.getDefaultSharedPreferences(this)
        .getString(getString(R.string.pref_units_key),
            getString(R.string.pref_units_label_metric));
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

  @Override
  protected boolean useEventBus() {
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    final SharedPreferences preferences =
        PreferenceManager.getDefaultSharedPreferences(this);
    final String units = preferences
        .getString(getString(R.string.pref_units_key), getString(R.string.pref_units_label_metric));

    if (!units.equals(this.units)) {
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


  /**
   * EventBus event when user has selected forecast item.
   */
  @Subscribe
  @SuppressWarnings("unused")
  public void onItemSelected(OnForecastItemSelectedEvent event) {
    if (twoPane) {
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

  /**
   * Callback received when a permissions request has been completed.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    Log.i(TAG, "onRequestPermissionResult");
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
      if (grantResults.length <= 0) {
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
        Log.i(TAG, "User interaction was cancelled.");
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission was granted.
        getLocationService().requestLocationUpdates();
      } else {
        Snackbar.make(
            findViewById(R.id.activity_main),
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.settings, new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                // Build intent that displays the App settings screen.
                final Intent intent = new Intent();
                intent.setAction(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                final Uri uri = Uri.fromParts("package",
                    BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
              }
            })
            .show();
      }
    }
  }

  /**
   * Returns the current state of the permissions needed.
   */
  private boolean checkPermissions() {
    return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION);
  }

  private void requestPermissions() {
    boolean shouldProvideRationale =
        ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION);

    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale) {
      Log.i(TAG, "Displaying permission rationale to provide additional context.");
      Snackbar.make(
          findViewById(R.id.activity_main), R.string.permission_rationale,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              // Request permission
              ActivityCompat.requestPermissions(MainActivity.this,
                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                  REQUEST_PERMISSIONS_REQUEST_CODE);
            }
          })
          .show();
    } else {
      Log.i(TAG, "Requesting permission");
      // Request permission. It's possible this can be auto answered if device policy
      // sets the permission in a given state or the user denied the permission
      // previously and checked "Never ask again".
      ActivityCompat.requestPermissions(MainActivity.this,
          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          REQUEST_PERMISSIONS_REQUEST_CODE);
    }
  }
}
