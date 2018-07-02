package com.deputat.sunshine;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deputat.sunshine.data.City;

import java.util.Comparator;
import java.util.List;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.ViewHolder> {

    private final Comparator<City> comparator;
    private final SortedList<City> cities = new SortedList<>(City.class,
            new SortedList.Callback<City>() {
                @Override
                public int compare(City o1, City o2) {
                    return comparator.compare(o1, o2);
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(City oldItem, City newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areItemsTheSame(City item1, City item2) {
                    return item1.getId() == item2.getId();
                }
            });

    CitiesAdapter(Comparator<City> comparator) {
        this.comparator = comparator;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final City city = cities.get(position);
        holder.cityName.setText(String.format(holder.cityName.getContext().getString(R.string.format_city), city.getName(),
                city.getCountry()));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public void add(@NonNull final City model) {
        cities.add(model);
    }

    @SuppressWarnings("unused")
    public void remove(@NonNull final City model) {
        cities.remove(model);
    }

    public void add(@NonNull final List<City> models) {
        cities.addAll(models);
    }

    @SuppressWarnings("unused")
    public void remove(@NonNull final List<City> models) {
        cities.beginBatchedUpdates();

        for (City model : models) {
            cities.remove(model);
        }
        cities.endBatchedUpdates();
    }

    public void replaceAll(@NonNull final List<City> models) {
        cities.beginBatchedUpdates();

        for (int i = cities.size() - 1; i >= 0; i--) {
            final City model = cities.get(i);

            if (!models.contains(model)) {
                cities.remove(model);
            }
        }

        cities.addAll(models);
        cities.endBatchedUpdates();
    }

    public City getItem(int position) {
        return cities.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView cityName;

        ViewHolder(View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
        }
    }
}