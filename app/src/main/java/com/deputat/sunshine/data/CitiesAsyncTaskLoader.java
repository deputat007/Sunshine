package com.deputat.sunshine.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CitiesAsyncTaskLoader extends AsyncTaskLoader<List<City>> {
    private final String searchText;
    private List<City> data;

    public CitiesAsyncTaskLoader(Context context, String searchText) {
        super(context);
        this.searchText = searchText;
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public List<City> loadInBackground() {
        return loadCityFromAsset();
    }

    @Override
    public void deliverResult(List<City> data) {
        this.data = data;

        super.deliverResult(data);
    }

    private List<City> loadCityFromAsset() {
        final List<City> cities = new ArrayList<>();

        try (final InputStream inputStream = getContext().getAssets().open("city-list.json");
             final JsonReader jsonReader = new JsonReader(
                     new InputStreamReader(inputStream, "UTF-8"))) {

            jsonReader.beginArray();

            final Gson gson = new GsonBuilder().create();

            while (jsonReader.hasNext()) {
                final City city = gson.fromJson(jsonReader, City.class);

                if (city.getName().contains(searchText)) {
                    cities.add(city);
                }
            }

            jsonReader.endArray();

        } catch (
                IOException e)

        {
            e.printStackTrace();
        }

        return cities;
    }
}
