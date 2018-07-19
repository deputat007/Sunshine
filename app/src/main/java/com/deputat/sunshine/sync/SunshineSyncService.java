package com.deputat.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SunshineSyncService extends Service {

  private static final Object syncAdapterLock = new Object();
  private static SunshineSyncAdapter sunshineSyncAdapter = null;

  @Override
  public void onCreate() {
    synchronized (syncAdapterLock) {
      if (sunshineSyncAdapter == null) {
        sunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(),
            true);
      }
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return sunshineSyncAdapter.getSyncAdapterBinder();
  }
}