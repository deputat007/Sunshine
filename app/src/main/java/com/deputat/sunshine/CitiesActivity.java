package com.deputat.sunshine;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.deputat.sunshine.data.CitiesAsyncTaskLoader;
import com.deputat.sunshine.data.City;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CitiesActivity extends AppCompatActivity {

    private static final String SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY";
    private final MenuItem.OnActionExpandListener expandListener =
            new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return true;
                }
            };
    private final SearchView.OnQueryTextListener queryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            };
    private CitiesAdapter citiesAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LoaderManager.LoaderCallbacks<List<City>> loaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<City>>() {
                @NonNull
                @Override
                public Loader<List<City>> onCreateLoader(int id, @Nullable Bundle args) {
                    return new CitiesAsyncTaskLoader(CitiesActivity.this,
                            Objects.requireNonNull(args).getString(SEARCH_TEXT_KEY));
                }

                @Override
                public void onLoadFinished(@NonNull Loader<List<City>> loader, List<City> data) {
                    citiesAdapter.replaceAll(data);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<City>> loader) {
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.cities);

        citiesAdapter = new CitiesAdapter(new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        recyclerView.setAdapter(citiesAdapter);

        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_TEXT_KEY, "Ivano");
        getSupportLoaderManager().initLoader(0, bundle, loaderCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);

        searchView = (SearchView) item.getActionView();
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(
                Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(queryTextListener);
        item.setOnActionExpandListener(expandListener);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(0);
    }
}
