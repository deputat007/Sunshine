package com.deputat.sunshine.activities;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.deputat.sunshine.R;
import com.deputat.sunshine.adapters.CitiesAdapter;
import com.deputat.sunshine.data.CitiesAsyncTaskLoader;
import com.deputat.sunshine.data.City;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CitiesActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<City>>,
        SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private static final String SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY";

    private RecyclerView mRecyclerView;
    private SearchView mSearchView;

    private CitiesAdapter mCitiesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCitiesAdapter = new CitiesAdapter(new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        final Bundle bundle = new Bundle();
        bundle.putString(SEARCH_TEXT_KEY, "");

        getSupportLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_cities;
    }

    @Override
    protected void initUI() {
        mRecyclerView = findViewById(R.id.cities);
    }

    @Override
    protected void setUI(final Bundle savedInstanceState) {
        mRecyclerView.setAdapter(mCitiesAdapter);
    }

    @Override
    protected void configureToolbar(@NonNull final Toolbar toolbar) {
        super.configureToolbar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    @NonNull
    @Override
    public Loader<List<City>> onCreateLoader(int id, @Nullable Bundle args) {
        return new CitiesAsyncTaskLoader(CitiesActivity.this,
                Objects.requireNonNull(args).getString(SEARCH_TEXT_KEY));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<City>> loader, List<City> data) {
        mCitiesAdapter.replaceAll(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<City>> loader) {
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) item.getActionView();
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(
                Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));

        mSearchView.setOnQueryTextListener(this);
        item.setOnActionExpandListener(this);

        return true;
    }
}
