package com.deputat.sunshine.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Time;

import com.deputat.sunshine.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WeatherUtil {
    @SuppressLint("StringFormatInvalid")
    public static String formatTemperature(Context context, double temperature) {
        if (!SharedPreferenceUtil.isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    @SuppressLint({"StringFormatMatches", "StringFormatInvalid"})
    public static String getFriendlyDayString(Context context,
                                              long dateInMillis) {
        final Time time = new Time();
        time.setToNow();

        final long currentTime = System.currentTimeMillis();
        final int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        final int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        if (julianDay == currentJulianDay) {
            final String today = context.getString(R.string.today);
            final int formatId = R.string.format_full_friendly_date;

            return context.getString(formatId, today, getFormattedMonthDay(context, dateInMillis));
        } else if (julianDay < currentJulianDay + 7) {
            return getDayName(context, dateInMillis);
        } else {
            final SimpleDateFormat shortenedDateFormat =
                    new SimpleDateFormat("EEE MMM dd", Locale.getDefault());

            return shortenedDateFormat.format(dateInMillis);
        }
    }

    public static String getDayName(Context context, long dateInMillis) {
        final Time t = new Time();
        t.setToNow();

        final int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        final int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            final Time time = new Time();

            time.setToNow();

            final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

            return dayFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(@SuppressWarnings("unused") Context context,
                                              long dateInMillis) {
        final Time time = new Time();

        time.setToNow();
        final SimpleDateFormat monthDayFormat =
                new SimpleDateFormat("MMMM dd", Locale.getDefault());

        return monthDayFormat.format(dateInMillis);
    }

    public static String getFormattedWind(Context context, double windSpeed, double degrees) {
        int windFormat;

        if (SharedPreferenceUtil.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }

        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static int getIconResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }

        return -1;
    }

    public static int getArtResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }

        return -1;
    }
}
