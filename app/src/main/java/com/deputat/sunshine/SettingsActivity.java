package com.deputat.sunshine;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.deputat.sunshine.data.WeatherContract;
import com.deputat.sunshine.utils.Utility;
import com.deputat.sunshine.views.SettingsItem;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SettingsItem settingsItemUnits;
    private SettingsItem settingsItemEnableNotification;
    private SettingsItem settingsItemCity;
    private SettingsItem settingsItemEnableLocationDetection;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        settingsItemUnits = findViewById(R.id.si_units);
        settingsItemEnableNotification = findViewById(R.id.si_enable_notification);
        settingsItemCity = findViewById(R.id.si_city);
        settingsItemEnableLocationDetection = findViewById(R.id.si_enable_location_detection);

        updateData();
    }

    private void updateData() {
        settingsItemUnits.setOnClickListener(this);
        settingsItemEnableNotification.setOnClickListener(this);
        settingsItemCity.setOnClickListener(this);
        settingsItemEnableLocationDetection.setOnClickListener(this);

        settingsItemEnableNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit()
                        .putBoolean(settingsItemEnableNotification.getKey(), b)
                        .apply();
                settingsItemEnableNotification.setSubtitleText(b ?
                        R.string.pref_enable_notifications_true :
                        R.string.pref_enable_notifications_false);
            }
        });
        settingsItemEnableLocationDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit()
                        .putBoolean(settingsItemEnableLocationDetection.getKey(),
                                b)
                        .apply();
                settingsItemEnableLocationDetection.setSubtitleText(b ?
                        R.string.pref_enable_location_detection_true :
                        R.string.pref_enable_location_detection_false);
            }
        });

        settingsItemUnits.setSubtitleText(sharedPreferences.getString(settingsItemUnits.getKey(),
                settingsItemUnits.getDefaultValue()));

        boolean enableNotification =
                sharedPreferences.getBoolean(settingsItemEnableNotification.getKey(), true);
        settingsItemEnableNotification.setSwitchChecked(enableNotification);
        settingsItemEnableNotification.setSubtitleText(enableNotification ?
                R.string.pref_enable_notifications_true :
                R.string.pref_enable_notifications_false);

        boolean enableLocationDetection =
                sharedPreferences.getBoolean(settingsItemEnableLocationDetection.getKey(), true);
        settingsItemEnableLocationDetection.setSwitchChecked(enableLocationDetection);
        settingsItemEnableLocationDetection.setSubtitleText(enableLocationDetection ?
                R.string.pref_enable_location_detection_true :
                R.string.pref_enable_location_detection_false);

        final Cursor cursor = getContentResolver()
                .query(WeatherContract.LocationEntry.CONTENT_URI,
                        new String[]{WeatherContract.LocationEntry.COLUMN_CITY_NAME},
                        WeatherContract.LocationEntry.COLUMN_CITY_ID + " == ? ",
                        new String[]{Utility.getLocationId(this)}, null);

        if (cursor != null && cursor.moveToPosition(0)) {
            settingsItemCity.setSubtitleText(cursor.getString(0));

            cursor.close();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.si_city:
                break;
            case R.id.si_enable_location_detection:
                boolean enableLocationDetection =
                        !settingsItemEnableLocationDetection.isSwitchChecked();
                settingsItemEnableLocationDetection.setSwitchChecked(enableLocationDetection);
                sharedPreferences.edit()
                        .putBoolean(settingsItemEnableLocationDetection.getKey(),
                                enableLocationDetection)
                        .apply();
                settingsItemEnableLocationDetection.setSubtitleText(enableLocationDetection ?
                        R.string.pref_enable_location_detection_true :
                        R.string.pref_enable_location_detection_false);
                break;
            case R.id.si_enable_notification:
                boolean enableNotification = !settingsItemEnableNotification.isSwitchChecked();
                settingsItemEnableNotification.setSwitchChecked(enableNotification);
                sharedPreferences.edit()
                        .putBoolean(settingsItemEnableNotification.getKey(), enableNotification)
                        .apply();
                settingsItemEnableNotification.setSubtitleText(enableNotification ?
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

                        settingsItemUnits.setSubtitleText(selectedUnit);
                        sharedPreferences.edit()
                                .putString(settingsItemUnits.getKey(), values[i])
                                .apply();
                    }
                });

        builder.show();
    }
}
