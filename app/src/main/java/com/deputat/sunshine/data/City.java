package com.deputat.sunshine.data;

import java.util.Objects;

public class City {
    private final long mId;
    private final String mName;
    private final String mCountry;
    private final Coord mCoord;

    public City(long id, String name, String country, Coord coord) {
        this.mId = id;
        this.mName = name;
        this.mCountry = country;
        this.mCoord = coord;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getCountry() {
        return mCountry;
    }

    public Coord getCoord() {
        return mCoord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return mId == city.mId &&
                Objects.equals(mName, city.mName) &&
                Objects.equals(mCountry, city.mCountry) &&
                Objects.equals(mCoord, city.mCoord);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mId, mName, mCountry, mCoord);
    }

    private static class Coord {
        private final double mLon;
        private final double mLat;

        private Coord(double lon, double lat) {
            this.mLon = lon;
            this.mLat = lat;
        }

        public double getLon() {
            return mLon;
        }

        public double getLat() {
            return mLat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coord coord = (Coord) o;
            return Double.compare(coord.mLon, mLon) == 0 &&
                    Double.compare(coord.mLat, mLat) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mLon, mLat);
        }
    }
}
