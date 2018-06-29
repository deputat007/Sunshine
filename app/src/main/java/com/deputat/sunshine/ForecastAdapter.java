package com.deputat.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deputat.sunshine.data.WeatherContract;

public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean useTodayLayout;

    ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }
        final View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder viewHolder = ((ViewHolder) view.getTag());
        @SuppressWarnings("unused") final int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        final int weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        @DrawableRes int icon = -1;
        switch (getItemViewType(cursor.getPosition())) {
            case VIEW_TYPE_TODAY:
                icon = Utility.getArtResourceForWeatherCondition(weatherConditionId);
                updateCityName(viewHolder, context);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                icon = Utility.getIconResourceForWeatherCondition(weatherConditionId);
                break;
        }
        viewHolder.iconView.setImageResource(icon);

        final long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, date));

        final String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.forecastView.setText(forecast);

        final double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highView.setText(Utility.formatTemperature(context, high));

        final double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowView.setText(Utility.formatTemperature(context, low));
    }

    private void updateCityName(ViewHolder viewHolder, Context context) {
        final Cursor cursor = context.getContentResolver()
                .query(WeatherContract.LocationEntry.CONTENT_URI,
                        new String[]{WeatherContract.LocationEntry.COLUMN_CITY_NAME},
                        WeatherContract.LocationEntry.COLUMN_CITY_ID + " == ? ",
                        new String[]{Utility.getLocationId(context)}, null);

        if (cursor != null && cursor.moveToPosition(0)) {
            viewHolder.cityName.setVisibility(View.VISIBLE);
            viewHolder.cityName.setText(cursor.getString(0));

            cursor.close();
        } else {
            viewHolder.cityName.setVisibility(View.GONE);
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public static class ViewHolder {
        final ImageView iconView;
        final TextView dateView;
        final TextView cityName;
        final TextView forecastView;
        final TextView highView;
        final TextView lowView;

        ViewHolder(View view) {
            iconView = view.findViewById(R.id.list_item_icon);
            dateView = view.findViewById(R.id.list_item_date_textview);
            cityName = view.findViewById(R.id.list_item_city_name);
            forecastView = view.findViewById(R.id.list_item_forecast_textview);
            highView = view.findViewById(R.id.list_item_high_textview);
            lowView = view.findViewById(R.id.list_item_low_textview);
        }
    }
}