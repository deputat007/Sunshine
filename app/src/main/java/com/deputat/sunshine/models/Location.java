package com.deputat.sunshine.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/10/18
 */
public class Location extends RealmObject {

    @PrimaryKey
    @Required
    @SerializedName("city_id")
    private String cityId;
    @SerializedName("city_name")
    private String cityName;
    @SerializedName("coord_lat")
    private double coordLat;
    @SerializedName("coord_long")
    private double coordLong;

    public Location() {
    }

    public Location(final String cityId, final String cityName, final double coordLat,
                    final double coordLong) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.coordLat = coordLat;
        this.coordLong = coordLong;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(final String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(final String cityName) {
        this.cityName = cityName;
    }

    public double getCoordLat() {
        return coordLat;
    }

    public void setCoordLat(final double coordLat) {
        this.coordLat = coordLat;
    }

    public double getCoordLong() {
        return coordLong;
    }

    public void setCoordLong(final double coordLong) {
        this.coordLong = coordLong;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Location location = (Location) o;
        return Double.compare(location.coordLat, coordLat) == 0 &&
                Double.compare(location.coordLong, coordLong) == 0 &&
                Objects.equals(cityId, location.cityId) &&
                Objects.equals(cityName, location.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, cityName, coordLat, coordLong);
    }
}
