package com.deputat.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.Objects;

public class SettingsActivity extends AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            final int prefIndex = listPreference.findIndexOfValue(stringValue);

            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }

        return true;
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public Intent getParentActivityIntent() {
        return Objects.requireNonNull(super.getParentActivityIntent())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
