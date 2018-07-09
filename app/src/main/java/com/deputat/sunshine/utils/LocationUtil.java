package com.deputat.sunshine.utils;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.deputat.sunshine.R;
import com.deputat.sunshine.activities.MainActivity;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/9/18
 */
public class LocationUtil {
    public static void updateLastLocation(final AppCompatActivity context) {
        if (!SharedPreferenceUtil.isLocationDetectionEnabled(context)) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MainActivity.PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            final FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(context, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                final SharedPreferences preferences = PreferenceManager
                                        .getDefaultSharedPreferences(context);
                                preferences.edit()
                                        .putString(context.getString(R.string.pref_coord_lat),
                                                String.valueOf(location.getLatitude()))
                                        .putString(context.getString(R.string.pref_coord_lon),
                                                String.valueOf(location.getLongitude()))
                                        .apply();

                                EventBus.getDefault().post(new OnLocationChangedEvent());
                            }
                        }
                    });
        }
    }
}
