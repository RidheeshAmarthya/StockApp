package com.example.stockapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Watchlist {
    private Activity activity;
    private RecyclerView recyclerView;
    private WatchlistAdapter adapter;
    private List<WatchlistItem> watchlistItems = new ArrayList<>();

    public Watchlist(Activity activity, RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.adapter = new WatchlistAdapter(activity, watchlistItems, this::handleItemClick);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.setAdapter(adapter);
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(this.recyclerView);

        fetchWatchListData();
    }

    private void handleItemClick(WatchlistItem item) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra("query", item.getSymbol());
        activity.startActivity(intent);
    }

    public void fetchWatchListData() {
        new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/watchList");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(response.toString());
                    List<WatchlistItem> newItems = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject watchlistItemJson = jsonArray.getJSONObject(i);
                        String symbol = watchlistItemJson.getString("watchList");
                        newItems.add(new WatchlistItem(symbol, "", 0, 0, 0));  // Other fields are default for now
                    }
                    updateWatchlistUI(newItems);
                } else {
                    System.err.println("HTTP_ERROR: Response Code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateWatchlistUI(List<WatchlistItem> newItems) {
        new Handler(Looper.getMainLooper()).post(() -> {
            watchlistItems.clear();
            watchlistItems.addAll(newItems);
            adapter.notifyDataSetChanged();
            for (WatchlistItem item : watchlistItems) {
                fetchStockData(item);
            }
        });
    }

    private void fetchStockData(WatchlistItem item) {
        new Thread(() -> {
            try {
                URL profileUrl = new URL("https://finnhub.io/api/v1/stock/profile2?symbol=" + item.getSymbol() + "&token=co2aripr01qvggedvg6gco2aripr01qvggedvg70");
                HttpURLConnection profileConnection = (HttpURLConnection) profileUrl.openConnection();
                profileConnection.setRequestMethod("GET");
                profileConnection.connect();

                if (profileConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(profileConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject profileObject = new JSONObject(response.toString());
                    item.setName(profileObject.optString("name", "N/A"));

                    URL quoteUrl = new URL("https://finnhub.io/api/v1/quote?symbol=" + item.getSymbol() + "&token=co2aripr01qvggedvg6gco2aripr01qvggedvg70");
                    HttpURLConnection quoteConnection = (HttpURLConnection) quoteUrl.openConnection();
                    quoteConnection.setRequestMethod("GET");
                    quoteConnection.connect();

                    if (quoteConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader quoteReader = new BufferedReader(new InputStreamReader(quoteConnection.getInputStream()));
                        StringBuilder quoteResponse = new StringBuilder();
                        while ((line = quoteReader.readLine()) != null) {
                            quoteResponse.append(line);
                        }

                        JSONObject quoteObject = new JSONObject(quoteResponse.toString());
                        item.setCurrentPrice(quoteObject.optDouble("c", 0));
                        item.setChange(quoteObject.optDouble("d", 0));
                        item.setChangePercent(quoteObject.optDouble("dp", 0));

                        updateStockUI(item);
                    }
                }
            } catch (Exception e) {
                System.err.println("NETWORK_ERROR: Failed to fetch stock data for " + item.getSymbol());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateStockUI(WatchlistItem item) {
        activity.runOnUiThread(() -> {
            int index = watchlistItems.indexOf(item);
            if (index != -1) {
                watchlistItems.set(index, item);
                adapter.notifyItemChanged(index);
            }
        });
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(watchlistItems, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true; // Return true to indicate the item was moved
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            watchlistItems.remove(position);
            adapter.notifyItemRemoved(position);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(Color.RED);

                // Draw the red background
                RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom());
                c.drawRect(background, paint);

                // Set up and draw the trash icon
                Drawable icon = ContextCompat.getDrawable(activity, R.drawable.white_trash);
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + icon.getIntrinsicHeight();
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.draw(c);
            }
        }
    };
}
