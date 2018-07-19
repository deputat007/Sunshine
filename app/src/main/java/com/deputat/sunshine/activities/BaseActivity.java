package com.deputat.sunshine.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.deputat.sunshine.LocationUpdatesService;
import com.deputat.sunshine.R;
import com.deputat.sunshine.utils.SharedPreferenceUtil;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/3/18
 */
public abstract class BaseActivity extends AppCompatActivity {

  private LocationUpdatesService service = null;
  private boolean bound = false;
  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
      BaseActivity.this.service = binder.getService();
      bound = true;
      if (SharedPreferenceUtil.isLocationDetectionEnabled(BaseActivity.this)) {
        BaseActivity.this.service.requestLocationUpdates();
      } else {
        if (BaseActivity.this.service.serviceIsRunningInForeground(BaseActivity.this)) {
          BaseActivity.this.service.stopForeground(true);
        }
        BaseActivity.this.service.removeLocationUpdates();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      service = null;
      bound = false;
    }
  };

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getContentView());
    if (useEventBus()) {
      EventBus.getDefault().register(this);
    }

    final View toolbar = findViewById(R.id.toolbar);

    initUi();
    if (toolbar != null) {
      configureToolbar((Toolbar) toolbar);
    }
    setUi(savedInstanceState);
  }

  @LayoutRes
  protected abstract int getContentView();

  protected abstract void initUi();

  protected abstract void setUi(final Bundle savedInstanceState);

  void configureToolbar(@NonNull final Toolbar toolbar) {
    setSupportActionBar(toolbar);
  }

  boolean useEventBus() {
    return false;
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Bind to the service. If the service is in foreground mode, this signals to the service
    // that since this activity is in the foreground, the service can exit foreground mode.
    bindService(new Intent(this, LocationUpdatesService.class), serviceConnection,
        Context.BIND_AUTO_CREATE);

  }

  @Override
  protected void onStop() {
    if (bound) {
      // Unbind from the service. This signals to the service that this activity is no longer
      // in the foreground, and the service can respond by promoting itself to a foreground
      // service.
      unbindService(serviceConnection);
      bound = false;
    }

    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (useEventBus()) {
      EventBus.getDefault().unregister(this);
    }
  }

  LocationUpdatesService getLocationService() {
    return service;
  }
}
