package com.deputat.sunshine.events;

public class LocationChangedEvent {
    private String locationId;

    public LocationChangedEvent(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationId() {
        return locationId;
    }
}
