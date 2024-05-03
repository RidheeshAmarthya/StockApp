package com.example.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {
    private Context context;
    private static List<PortfolioItem> items;
    private LayoutInflater inflater;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PortfolioItem item);
    }

    public PortfolioAdapter(Context context, List<PortfolioItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public PortfolioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_item, parent, false);
        return new PortfolioViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(PortfolioViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSymbol;
        private TextView textViewPrice;
        private TextView textViewName;
        private TextView textViewChange;
        private TextView textViewChangePercent;
        private ImageView imageView;
        private OnItemClickListener listener;

        public PortfolioViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            textViewSymbol = itemView.findViewById(R.id.text_view_symbol);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewChange = itemView.findViewById(R.id.text_view_change);
            textViewChangePercent = itemView.findViewById(R.id.text_view_change_percent);
            imageView = itemView.findViewById(R.id.imageView);
            this.listener = listener;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(PortfolioItem item) {
            textViewSymbol.setText(item.getSymbol());
            textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getCurrentPrice()));
            textViewName.setText(item.getQuantity() + " shares");
            double change = item.getChange();
            textViewChange.setText(String.format(Locale.getDefault(), "$%.2f shares", change));
            textViewChangePercent.setText(String.format(Locale.getDefault(), " (%.2f%%)", item.getChangePercent()));

            // Set color based on positive or negative change
            int color = change >= 0 ? Color.parseColor("#319c5e") : Color.RED;
            textViewChange.setTextColor(color);
            textViewChangePercent.setTextColor(color);

            imageView.setImageResource(change >= 0 ? R.drawable.trending_up : R.drawable.trending_down);
        }
    }
}
