package com.example.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {
    private Context context;
    private List<WatchlistItem> items;
    private LayoutInflater inflater;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WatchlistItem item);
    }

    public WatchlistAdapter(Context context, List<WatchlistItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public WatchlistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_item, parent, false);
        return new WatchlistViewHolder(view, context, items);
    }

    @Override
    public void onBindViewHolder(WatchlistViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class WatchlistViewHolder extends RecyclerView.ViewHolder {
        private TextView symbolView, priceView, nameView, changeView, changePercentView;
        private ImageView imageView;
        private Context context;
        private List<WatchlistItem> items;

        public WatchlistViewHolder(View itemView, Context context, List<WatchlistItem> items) {
            super(itemView);
            symbolView = itemView.findViewById(R.id.text_view_symbol);
            priceView = itemView.findViewById(R.id.text_view_price);
            nameView = itemView.findViewById(R.id.text_view_name);
            changeView = itemView.findViewById(R.id.text_view_change);
            changePercentView = itemView.findViewById(R.id.text_view_change_percent);
            imageView = itemView.findViewById(R.id.imageView);
            this.context = context;
            this.items = items;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(WatchlistItem item) {
            symbolView.setText(item.getSymbol());
            priceView.setText(String.format(Locale.getDefault(), "$%.2f", item.getCurrentPrice()));
            nameView.setText(item.getName());
            changeView.setText(String.format(Locale.getDefault(), "$%.2f", item.getChange()));
            changePercentView.setText(String.format(Locale.getDefault(), " (%.2f%%)", item.getChangePercent()));

            // Set image and color based on the change
            int color;
            if (item.getChange() < 0) {
                color = Color.RED;
                imageView.setImageResource(R.drawable.trending_down);
            } else {
                color = Color.parseColor("#319c5e");
                imageView.setImageResource(R.drawable.trending_up);
            }
            changeView.setTextColor(color);
            changePercentView.setTextColor(color);
        }
    }
}
