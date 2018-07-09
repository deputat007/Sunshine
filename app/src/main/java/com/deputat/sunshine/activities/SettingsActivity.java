package com.deputat.sunshine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.deputat.sunshine.R;
import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.deputat.sunshine.events.OnWeatherForecastUpdatedEvent;
import com.deputat.sunshine.sync.SunshineSyncAdapter;
import com.deputat.sunshine.utils.LocationUtil;
import com.deputat.sunshine.utils.SharedPreferenceUtil;
import com.deputat.sunshine.views.SettingsItem;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private SettingsItem mSettingsItemUnits;
    private SettingsItem mSettingsItemEnableNotification;
    private SettingsItem mSettingsItemCity;
    private SettingsItem mSettingsItemEnableLocationDetection;

    private SharedPreferences mSharedPreferences;

    @Override
    protected int getContentView() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initUI() {
        mSettingsItemUnits = findViewById(R.id.si_units);
        mSettingsItemEnableNotification = findViewById(R.id.si_enable_notification);
        mSettingsItemCity = findViewById(R.id.si_city);
        mSettingsItemEnableLocationDetection = findViewById(R.id.si_enable_location_detection);
    }

    @Override
    protected void setUI(final Bundle savedInstanceState) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsItemUnits.setOnClickListener(this);
        mSettingsItemEnableNotification.setOnClickListener(this);
        mSettingsItemCity.setOnClickListener(this);
        mSettingsItemEnableLocationDetection.setOnClickListener(this);

        mSettingsItemEnableNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mSharedPreferences.edit()
                        .putBoolean(mSettingsItemEnableNotification.getKey(), b)
                        .apply();
                mSettingsItemEnableNotification.setSubtitleText(b ?
                        R.string.pref_enable_notifications_true :
                        R.string.pref_enable_notifications_false);
            }
        });
        mSettingsItemEnableLocationDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mSharedPreferences.edit()
                        .putBoolean(mSettingsItemEnableLocationDetection.getKey(),
                                b)
                        .apply();
                mSettingsItemEnableLocationDetection.setSubtitleText(b ?
                        R.string.pref_enable_location_detection_true :
                        R.string.pref_enable_location_detection_false);
            }
        });

        mSettingsItemUnits.setSubtitleText(mSharedPreferences.getString(mSettingsItemUnits.getKey(),
                mSettingsItemUnits.getDefaultValue()).equals(getString(R.string.pref_units_metric)) ?
                getString(R.string.pref_units_label_metric) :
                getString(R.string.pref_units_label_imperial));

        boolean enableNotification =
                mSharedPreferences.getBoolean(mSettingsItemEnableNotification.getKey(), true);
        mSettingsItemEnableNotification.setSwitchChecked(enableNotification);
        mSettingsItemEnableNotification.setSubtitleText(enableNotification ?
                R.string.pref_enable_notifications_true :
                R.string.pref_enable_notifications_false);

        boolean enableLocationDetection =
                mSharedPreferences.getBoolean(mSettingsItemEnableLocationDetection.getKey(), true);
        mSettingsItemEnableLocationDetection.setSwitchChecked(enableLocationDetection);
        mSettingsItemEnableLocationDetection.setSubtitleText(enableLocationDetection ?
                R.string.pref_enable_location_detection_true :
                R.string.pref_enable_location_detection_false);

        updateCity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Place place = PlaceAutocomplete.getPlace(this, data);
                final SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit()
                        .putString(getString(R.string.pref_coord_lon),
                                String.valueOf(place.getLatLng().longitude))
                        .putString(getString(R.string.pref_coord_lat),
                                String.valueOf(place.getLatLng().latitude))
                        .apply();

                SunshineSyncAdapter.syncImmediately(this);
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                final Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.si_city:
                try {
                    final Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                break;
            case R.id.si_enable_location_detection:
                boolean enableLocationDetection =
                        !mSettingsItemEnableLocationDetection.isSwitchChecked();
                mSettingsItemEnableLocationDetection.setSwitchChecked(enableLocationDetection);
                mSharedPreferences.edit()
                        .putBoolean(mSettingsItemEnableLocationDetection.getKey(),
                                enableLocationDetection)
                        .apply();
                mSettingsItemEnableLocationDetection.setSubtitleText(enableLocationDetection ?
                        R.string.pref_enable_location_detection_true :
                        R.string.pref_enable_location_detection_false);
                LocationUtil.updateLastLocation(this);
                break;
            case R.id.si_enable_notification:
                boolean enableNotification = !mSettingsItemEnableNotification.isSwitchChecked();
                mSettingsItemEnableNotification.setSwitchChecked(enableNotification);
                mSharedPreferences.edit()
                        .putBoolean(mSettingsItemEnableNotification.getKey(), enableNotification)
                        .apply();
                mSettingsItemEnableNotification.setSubtitleText(enableNotification ?
                        R.string.pref_enable_notifications_true :
                        R.string.pref_enable_notifications_false);
                break;
            case R.id.si_units:
                showUnitsDialog();
                break;
        }
    }

    private void showUnitsDialog() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice);
        final String[] units = getResources().getStringArray(R.array.pref_units_options);
        final String[] values = getResources().getStringArray(R.array.pref_units_values);

        adapter.addAll(units);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_dialog_choose_unit_title)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.dismiss();
                    }
                })
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String selectedUnit = adapter.getItem(i);

                        mSettingsItemUnits.setSubtitleText(selectedUnit);
                        mSharedPreferences.edit()
                                .putString(mSettingsItemUnits.getKey(), values[i])
                                .apply();

                        EventBus.getDefault().post(new OnLocationChangedEvent());
                    }
                });

        builder.show();
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onWeatherUpdated(OnWeatherForecastUpdatedEvent event) {
        updateCity();
    }

    private void updateCity() {
        final Cursor cursor = getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocation(
                        SharedPreferenceUtil.getLocationId(this)),
                new String[]{WeatherContract.LocationEntry.COLUMN_CITY_NAME},
                WeatherContract.LocationEntry.COLUMN_CITY_ID + " == ? ",
                new String[]{SharedPreferenceUtil.getLocationId(this)}, null);
        if (cursor != null && cursor.moveToPosition(0)) {
            final String cityName = cursor.getString(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSettingsItemCity.setSubtitleText(cityName);
                }
            });
            cursor.close();
        }
    }
}
