package com.deputat.sunshine.data;

import java.util.Objects;

public class City {
    private final long id;
    private final String name;
    private final String country;
    private final Coord coord;

    public City(long id, String name, String country, Coord coord) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.coord = coord;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public Coord getCoord() {
        return coord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id &&
                Objects.equals(name, city.name) &&
                Objects.equals(country, city.country) &&
                Objects.equals(coord, city.coord);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, country, coord);
    }

    private static class Coord {
        private final double lon;
        private final double lat;

        private Coord(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coord coord = (Coord) o;
            return Double.compare(coord.lon, lon) == 0 &&
                    Double.compare(coord.lat, lat) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lon, lat);
        }
    }
}
