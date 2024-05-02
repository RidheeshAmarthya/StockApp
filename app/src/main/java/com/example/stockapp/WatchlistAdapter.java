package com.example.stockapp;

import android.content.Context;
import android.content.Intent;
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

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {
    private Context context; // Context to use for intents
    private List<WatchlistItem> items; // This should not be static
    private LayoutInflater inflater;

    private static OnItemClickListener listener;

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
        return new WatchlistViewHolder(view, context, items); // Pass items to ViewHolder
    }

    @Override
    public void onBindViewHolder(WatchlistViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class WatchlistViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private Context context; // Context to use for intents
        private List<WatchlistItem> items; // Now it's an instance variable

        public WatchlistViewHolder(View itemView, Context context, List<WatchlistItem> items) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.text_view_watchlist);
            this.context = context;
            this.items = items;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        private void navigateToSearchActivity(int position) {
            if (position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, SearchActivity.class);
                intent.putExtra("query", items.get(position).getSymbol());
                context.startActivity(intent);
            }
        }

        public void bind(WatchlistItem item) {
            String text = String.format(Locale.getDefault(), "%s $%.2f\n%s $%.2f (%.2f%%)",
                    item.getSymbol(), item.getCurrentPrice(), item.getName(),
                    item.getChange(), item.getChangePercent());
            SpannableString spannableString = new SpannableString(text);

            // Calculate positions for coloring
            int startChange = text.indexOf(String.format("$%.2f", item.getChange()));
            int endChange = startChange + String.format("$%.2f", item.getChange()).length();
            int startChangePercent = text.indexOf("(", endChange);
            int endChangePercent = text.indexOf(")", startChangePercent) + 1;

            // Apply color spans based on whether the change is positive or negative
            if (item.getChange() < 0) {
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), startChange, endChange, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), startChangePercent, endChangePercent, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), startChange, endChange, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), startChangePercent, endChangePercent, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
        }
    }
}
