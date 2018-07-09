package com.deputat.sunshine.activities;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.deputat.sunshine.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 7/3/18
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        if (useEventBus()) EventBus.getDefault().register(this);

        final View toolbar = findViewById(R.id.toolbar);

        initUI();
        if (toolbar != null) configureToolbar((Toolbar) toolbar);
        setUI(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useEventBus()) EventBus.getDefault().unregister(this);
    }

    @LayoutRes
    protected abstract int getContentView();

    protected void configureToolbar(@NonNull final Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    protected boolean useEventBus() {
        return false;
    }

    protected abstract void initUI();

    protected abstract void setUI(final Bundle savedInstanceState);
}
