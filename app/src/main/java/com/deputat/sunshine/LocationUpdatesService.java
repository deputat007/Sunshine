package com.deputat.sunshine;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.deputat.sunshine.activities.MainActivity;
import com.deputat.sunshine.application.Constants;
import com.deputat.sunshine.events.OnLocationChangedEvent;
import com.deputat.sunshine.utils.LocationUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/11/18
 */
public class LocationUpdatesService extends Service {

  private static final String TAG = LocationUpdatesService.class.getSimpleName();

  private final IBinder binder = new LocalBinder();

  private NotificationManager notificationManager;
  private LocationRequest locationRequest;
  private FusedLocationProviderClient fusedLocationClient;
  private LocationCallback locationCallback;
  private Handler serviceHandler;
  private Location location;

  private boolean changingConfiguration = false;

  public LocationUpdatesService() {
  }

  @Override
  public void onCreate() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        onNewLocation(locationResult.getLastLocation());
      }
    };

    createLocationRequest();
    getLastLocation();

    final HandlerThread handlerThread = new HandlerThread(TAG);
    handlerThread.start();
    serviceHandler = new Handler(handlerThread.getLooper());
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    // Android O requires a Notification Channel.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      final CharSequence name = getString(R.string.app_name);
      // Create the channel for the notification
      final NotificationChannel mChannel =
          new NotificationChannel(Constants.CHANNEL_ID, name,
              NotificationManager.IMPORTANCE_DEFAULT);

      // Set the Notification Channel for the Notification Manager.
      notificationManager.createNotificationChannel(mChannel);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "Service started");
    final boolean startedFromNotification = intent.getBooleanExtra(
        Constants.EXTRA_STARTED_FROM_NOTIFICATION,
        false);

    // We got here because the user decided to remove location updates from the notification.
    if (startedFromNotification) {
      removeLocationUpdates();
      stopSelf();
    }
    // Tells the system to not try to recreate the service after it has been killed.
    return START_STICKY;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    changingConfiguration = true;
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Called when a client (MainActivity in case of this sample) comes to the foreground
    // and binds with this service. The service should cease to be a foreground service
    // when that happens.
    Log.i(TAG, "in onBind()");
    stopForeground(true);
    changingConfiguration = false;
    return binder;
  }

  @Override
  public void onRebind(Intent intent) {
    // Called when a client (MainActivity in case of this sample) returns to the foreground
    // and binds once again with this service. The service should cease to be a foreground
    // service when that happens.
    Log.i(TAG, "in onRebind()");
    stopForeground(true);
    changingConfiguration = false;
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.i(TAG, "Last client unbound from service");

    // Called when the last client (MainActivity in case of this sample) unbinds from this
    // service. If this method is called due to a configuration change in MainActivity, we
    // do nothing. Otherwise, we make this service a foreground service.
    if (!changingConfiguration && LocationUtil.requestingLocationUpdates(this)) {
      Log.i(TAG, "Starting foreground service");

      startForeground(Constants.LOCATION_NOTIFICATION_ID, getNotification());
    }
    return true; // Ensures onRebind() is called when a client re-binds.
  }

  @Override
  public void onDestroy() {
    serviceHandler.removeCallbacksAndMessages(null);
  }

  /**
   * Makes a request for location updates. Note that in this sample we merely log the {@link
   * SecurityException}.
   */
  @SuppressWarnings("unused")
  public void requestLocationUpdates() {
    Log.i(TAG, "Requesting location updates");
    LocationUtil.setRequestingLocationUpdates(this, true);
    startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
    try {
      fusedLocationClient.requestLocationUpdates(locationRequest,
          locationCallback, Looper.myLooper());
    } catch (SecurityException unlikely) {
      LocationUtil.setRequestingLocationUpdates(this, false);
      Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
    }
  }

  /**
   * Removes location updates. Note that in this sample we merely log the {@link
   * SecurityException}.
   */
  public void removeLocationUpdates() {
    Log.i(TAG, "Removing location updates");
    try {
      fusedLocationClient.removeLocationUpdates(locationCallback);
      LocationUtil.setRequestingLocationUpdates(this, false);
      stopSelf();
    } catch (SecurityException unlikely) {
      LocationUtil.setRequestingLocationUpdates(this, true);
      Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
    }
  }

  /**
   * Returns the {@link NotificationCompat} used as part of the foreground service.
   */
  private Notification getNotification() {
    final Intent intent = new Intent(this, LocationUpdatesService.class);

    final CharSequence text = LocationUtil.getLocationText(location);

    // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
    intent.putExtra(Constants.EXTRA_STARTED_FROM_NOTIFICATION, true);

    // The PendingIntent that leads to a call to onStartCommand() in this service.
    final PendingIntent servicePendingIntent = PendingIntent.getService(this,
        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    // The PendingIntent to launch activity.
    final PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
        0, new Intent(this, MainActivity.class), 0);

    final NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
        Constants.CHANNEL_ID)
        .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
            activityPendingIntent)
        .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
            servicePendingIntent)
        .setContentText(text)
        .setContentTitle(LocationUtil.getLocationTitle(this))
        .setOngoing(true)
        .setPriority(Notification.PRIORITY_HIGH)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setTicker(text)
        .setWhen(System.currentTimeMillis());

    // Set the Channel ID for Android O.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      builder.setChannelId(Constants.CHANNEL_ID); // Channel ID
    }

    return builder.build();
  }

  private void getLastLocation() {
    try {
      fusedLocationClient.getLastLocation()
          .addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
              if (task.isSuccessful() && task.getResult() != null) {
                location = task.getResult();
              } else {
                Log.w(TAG, "Failed to get location.");
              }
            }
          });
    } catch (SecurityException unlikely) {
      Log.e(TAG, "Lost location permission." + unlikely);
    }
  }

  private void onNewLocation(Location location) {
    Log.i(TAG, "New location: " + location);

    this.location = location;

    if (location != null) {

      final SharedPreferences preferences = PreferenceManager
          .getDefaultSharedPreferences(this);
      preferences.edit()
          .putString(getString(R.string.pref_coord_lat),
              String.valueOf(location.getLatitude()))
          .putString(getString(R.string.pref_coord_lon),
              String.valueOf(location.getLongitude()))
          .apply();

      EventBus.getDefault().post(new OnLocationChangedEvent());

      // Update notification content if running as a foreground service.
      if (serviceIsRunningInForeground(this)) {
        notificationManager.notify(Constants.LOCATION_NOTIFICATION_ID, getNotification());
      }
    }
  }

  /**
   * Sets the location request parameters.
   */
  private void createLocationRequest() {
    locationRequest = new LocationRequest();
    locationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
    locationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  /**
   * Returns true if this is a foreground service.
   *
   * @param context The {@link Context}.
   */
  public boolean serviceIsRunningInForeground(Context context) {
    final ActivityManager manager = (ActivityManager) context.getSystemService(
        Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service :
        Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
      if (getClass().getName().equals(service.service.getClassName())) {
        if (service.foreground) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Class used for the client Binder.  Since this service runs in the same process as its clients,
   * we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {

    public LocationUpdatesService getService() {
      return LocationUpdatesService.this;
    }
  }
}