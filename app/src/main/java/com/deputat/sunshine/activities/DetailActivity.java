package com.deputat.sunshine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.deputat.sunshine.R;
import com.deputat.sunshine.fragments.DetailFragment;
import java.util.Objects;

public class DetailActivity extends BaseActivity {

  @Override
  protected int getContentView() {
    return R.layout.activity_detail;
  }

  @Override
  protected void initUi() {
  }

  @Override
  protected void setUi(final Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      final DetailFragment detailFragment = new DetailFragment();
      final Bundle arguments = new Bundle();
      arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
      detailFragment.setArguments(arguments);

      getSupportFragmentManager().beginTransaction()
          .replace(R.id.weather_detail_container, detailFragment)
          .commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.detail_fragment, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int id = item.getItemId();

    if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    } else if (id == android.R.id.home) {
      NavUtils.navigateUpFromSameTask(this);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void configureToolbar(@NonNull final Toolbar toolbar) {
    super.configureToolbar(toolbar);

    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
  }
}

