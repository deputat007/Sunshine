package com.deputat.sunshine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.deputat.sunshine.R;
import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.events.LocationChangedEvent;
import com.deputat.sunshine.utils.SharedPreferenceUtil;
import com.deputat.sunshine.views.SettingsItem;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private SettingsItem mSettingsItemUnits;
    private SettingsItem mSettingsItemEnableNotification;
    private SettingsItem mSettingsItemCity;
    private SettingsItem mSettingsItemEnableLocationDetection;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

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

        final Cursor cursor = getContentResolver()
                .query(WeatherContract.LocationEntry.CONTENT_URI,
                        new String[]{WeatherContract.LocationEntry.COLUMN_CITY_NAME},
                        WeatherContract.LocationEntry.COLUMN_CITY_ID + " == ? ",
                        new String[]{SharedPreferenceUtil.getLocationId(this)}, null);

        if (cursor != null && cursor.moveToPosition(0)) {
            mSettingsItemCity.setSubtitleText(cursor.getString(0));

            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.si_city:
                startActivity(new Intent(this, CitiesActivity.class));
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

                        EventBus.getDefault().post(new LocationChangedEvent());
                    }
                });

        builder.show();
    }
}
