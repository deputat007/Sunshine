package com.deputat.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SunshineAuthenticatorService extends Service {

  private SunshineAuthenticator authenticator;

  @Override
  public void onCreate() {
    authenticator = new SunshineAuthenticator(this);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return authenticator.getIBinder();
  }
}
