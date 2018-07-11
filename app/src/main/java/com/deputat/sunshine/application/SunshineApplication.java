package com.deputat.sunshine.application;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/10/18
 */
public class SunshineApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}