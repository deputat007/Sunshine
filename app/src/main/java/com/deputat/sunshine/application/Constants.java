package com.deputat.sunshine.application;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/11/18
 */
public interface Constants {
    long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    String PACKAGE_NAME = "com.deputat.sunshine";
    String ACTION_LOCATION_BROADCAST = PACKAGE_NAME + ".broadcast";
    String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

    String CHANNEL_ID = "channel_01";
    int LOCATION_NOTIFICATION_ID = 12345678;

    String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
}
