package com.deputat.sunshine.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

import io.realm.annotations.PrimaryKey;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/10/18
 */
public class Weather {
    @PrimaryKey
    private long id;
    private Location location;
    private Date date;
    @SerializedName("weather_id")
    private String weatherId;
    @SerializedName("short_desc")
    private String shortDesc;
    private double min;
    private double max;
    private double humidity;
    private double pressure;
    private double wind;
    private double degrees;

    public Weather() {
    }

    public Weather(final long id, final Location location, final Date date, final String weatherId,
                   final String shortDesc, final double min, final double max, final double humidity,
                   final double pressure, final double wind, final double degrees) {
        this.id = id;
        this.location = location;
        this.date = date;
        this.weatherId = weatherId;
        this.shortDesc = shortDesc;
        this.min = min;
        this.max = max;
        this.humidity = humidity;
        this.pressure = pressure;
        this.wind = wind;
        this.degrees = degrees;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(final String weatherId) {
        this.weatherId = weatherId;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public double getMin() {
        return min;
    }

    public void setMin(final double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(final double max) {
        this.max = max;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(final double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(final double pressure) {
        this.pressure = pressure;
    }

    public double getWind() {
        return wind;
    }

    public void setWind(final double wind) {
        this.wind = wind;
    }

    public double getDegrees() {
        return degrees;
    }

    public void setDegrees(final double degrees) {
        this.degrees = degrees;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Weather weather = (Weather) o;
        return id == weather.id &&
                Double.compare(weather.min, min) == 0 &&
                Double.compare(weather.max, max) == 0 &&
                Double.compare(weather.humidity, humidity) == 0 &&
                Double.compare(weather.pressure, pressure) == 0 &&
                Double.compare(weather.wind, wind) == 0 &&
                Double.compare(weather.degrees, degrees) == 0 &&
                Objects.equals(location, weather.location) &&
                Objects.equals(date, weather.date) &&
                Objects.equals(weatherId, weather.weatherId) &&
                Objects.equals(shortDesc, weather.shortDesc);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, location, date, weatherId, shortDesc, min, max, humidity,
                pressure, wind, degrees);
    }
}
