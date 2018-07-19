package com.deputat.sunshine.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public abstract class CursorRecyclerViewAdapter<V extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<V> {

  private Cursor cursor;
  private final DataSetObserver dataSetObserver;

  private boolean dataValid;
  private int rowIdColumn;

  CursorRecyclerViewAdapter(Cursor cursor) {
    this.cursor = cursor;
    dataValid = cursor != null;
    rowIdColumn = dataValid ? this.cursor.getColumnIndex("_id") : -1;
    dataSetObserver = new NotifyingDataSetObserver();

    if (this.cursor != null) {
      this.cursor.registerDataSetObserver(dataSetObserver);
    }
  }

  public Cursor getCursor() {
    return cursor;
  }

  @Override
  public int getItemCount() {
    if (dataValid && cursor != null) {
      return cursor.getCount();
    }
    return 0;
  }

  @Override
  public long getItemId(int position) {
    if (dataValid && cursor != null && cursor.moveToPosition(position)) {
      return cursor.getLong(rowIdColumn);
    }
    return 0;
  }

  @Override
  public void setHasStableIds(boolean hasStableIds) {
    super.setHasStableIds(true);
  }

  protected abstract void onBindViewHolder(@NonNull V viewHolder, Cursor cursor, Context context);

  @Override
  public void onBindViewHolder(@NonNull V viewHolder, int position) {
    if (!dataValid) {
      throw new IllegalStateException("this should only be called when the cursor is valid");
    }
    if (!cursor.moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    onBindViewHolder(viewHolder, cursor, viewHolder.itemView.getContext());
  }

  /**
   * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
   * closed.
   */
  @SuppressWarnings("unused")
  private void changeCursor(Cursor cursor) {
    Cursor old = swapCursor(cursor);
    if (old != null) {
      old.close();
    }
  }

  /**
   * Swap in a new Cursor, returning the old Cursor.  Unlike {@link #changeCursor(Cursor)}, the
   * returned old Cursor is <em>not</em> closed.
   */
  public Cursor swapCursor(Cursor newCursor) {
    if (newCursor == cursor) {
      return null;
    }
    final Cursor oldCursor = cursor;
    if (oldCursor != null && dataSetObserver != null) {
      oldCursor.unregisterDataSetObserver(dataSetObserver);
    }
    cursor = newCursor;
    if (cursor != null) {
      if (dataSetObserver != null) {
        cursor.registerDataSetObserver(dataSetObserver);
      }
      rowIdColumn = newCursor.getColumnIndexOrThrow("_id");
      dataValid = true;
      notifyDataSetChanged();
    } else {
      rowIdColumn = -1;
      dataValid = false;
      notifyDataSetChanged();
      //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
    }
    return oldCursor;
  }

  private class NotifyingDataSetObserver extends DataSetObserver {

    @Override
    public void onChanged() {
      super.onChanged();
      dataValid = true;
      notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      super.onInvalidated();
      dataValid = false;
      notifyDataSetChanged();
      //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
    }
  }
}