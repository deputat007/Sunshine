package com.deputat.sunshine.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.deputat.sunshine.activities.BaseActivity;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by a on 3/2/17.
 */

public abstract class BaseFragment extends Fragment {

  private View rootView;

  private static void callOnActivityResultOnChildFragments(Fragment parent, int requestCode,
      int resultCode, Intent data) {
    final FragmentManager childFragmentManager = parent.getChildFragmentManager();
    final List<Fragment> childFragments = childFragmentManager.getFragments();
    if (childFragments == null) {
      return;
    }
    for (Fragment child : childFragments) {
      if (child != null && !child.isDetached() && !child.isRemoving()) {
        child.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  @CallSuper
  @Nullable
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
      @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    return inflater.inflate(getContentView(), container, false);
  }

  @CallSuper
  @Override
  public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    rootView = view;
    initUi();
    setUi(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (useEventBus()) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (useEventBus()) {
      EventBus.getDefault().unregister(this);
    }
  }

  @LayoutRes
  protected abstract int getContentView();

  protected abstract void initUi();

  protected abstract void setUi(@Nullable final Bundle savedInstanceState);

  boolean useEventBus() {
    return false;
  }

  @SuppressWarnings("unused")
  public View getRootView() {
    return rootView;
  }

  @SuppressWarnings("unchecked")
  <E extends View> E findViewById(@IdRes final int id) {
    return (E) rootView.findViewById(id);
  }

  @SuppressWarnings("unused")
  public BaseActivity getParentActivity() {
    return (BaseActivity) getActivity();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callOnActivityResultOnChildFragments(this, requestCode, resultCode, data);
  }
}
