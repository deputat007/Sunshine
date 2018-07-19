package com.deputat.sunshine.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Recycler view with empty view. If adapter does not have items then empty view is visible, else
 * recycler view is visible.
 *
 * @author Deputat;
 */
@SuppressWarnings("unused")
public class RecyclerViewEmptySupport extends RecyclerView {

  @Nullable
  private View emptyView;

  private final AdapterDataObserver observer = new AdapterDataObserver() {
    @Override
    public void onChanged() {
      super.onChanged();

      checkIfEmpty();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      super.onItemRangeChanged(positionStart, itemCount);

      checkIfEmpty();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
      super.onItemRangeChanged(positionStart, itemCount, payload);

      checkIfEmpty();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      super.onItemRangeInserted(positionStart, itemCount);

      checkIfEmpty();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      super.onItemRangeRemoved(positionStart, itemCount);

      checkIfEmpty();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      super.onItemRangeMoved(fromPosition, toPosition, itemCount);

      checkIfEmpty();
    }
  };

  public RecyclerViewEmptySupport(Context context) {
    super(context);
  }

  public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void setAdapter(@Nullable Adapter adapter) {
    final Adapter oldAdapter = getAdapter();

    if (oldAdapter != null) {
      oldAdapter.unregisterAdapterDataObserver(observer);
    }

    super.setAdapter(adapter);

    if (adapter != null) {
      adapter.registerAdapterDataObserver(observer);
    }
  }

  private void checkIfEmpty() {
    if (emptyView != null && getAdapter() != null) {
      emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
    }
  }

  @SuppressWarnings("unused")
  public void setEmptyView(@Nullable View emptyView) {
    this.emptyView = emptyView;
    checkIfEmpty();
  }
}
