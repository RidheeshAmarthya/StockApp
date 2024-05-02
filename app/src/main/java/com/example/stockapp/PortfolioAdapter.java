package com.example.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private final TextView textView;
        private OnItemClickListener listener;

        public PortfolioViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.text_view_watchlist);
            this.listener = listener;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(PortfolioItem item) {
            String text = String.format(Locale.getDefault(), "%s x%d @ $%.2f\nTotal Cost: $%.2f",
                    item.getSymbol(), item.getQuantity(), item.getCurrentPrice(), item.getTotalCost());
            SpannableString spannableString = new SpannableString(text);
            int startTotalCost = text.indexOf("Total Cost:");
            int endTotalCost = startTotalCost + String.format("Total Cost: $%.2f", item.getTotalCost()).length();
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), startTotalCost, endTotalCost, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannableString);
        }
    }
}
