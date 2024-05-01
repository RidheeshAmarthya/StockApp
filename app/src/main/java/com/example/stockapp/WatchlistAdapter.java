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

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {
    private List<WatchlistItem> items;
    private LayoutInflater inflater;

    public WatchlistAdapter(Context context, List<WatchlistItem> items) {
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public WatchlistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_item, parent, false);
        return new WatchlistViewHolder(view);
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

        public WatchlistViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view_watchlist);
        }

        public void bind(WatchlistItem item) {
            String text = String.format(Locale.getDefault(), "%s $%.2f\n%s $%.2f (%.2f%%)",
                    item.getSymbol(), item.getCurrentPrice(), item.getName(),
                    item.getChange(), item.getChangePercent());
            SpannableString spannableString = new SpannableString(text);

            int startChange = text.indexOf(String.format("$%.2f", item.getChange()));
            int endChange = startChange + String.format("$%.2f", item.getChange()).length();
            int startChangePercent = text.indexOf("(", endChange);
            int endChangePercent = text.indexOf(")", startChangePercent) + 1;

            if (item.getChangePercent() < 0) {
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
