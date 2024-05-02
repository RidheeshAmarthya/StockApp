package com.example.stockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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

public class Portfolio {
    private Activity activity;
    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private List<PortfolioItem> portfolioItems = new ArrayList<>();

    public Portfolio(Activity activity, RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.adapter = new PortfolioAdapter(activity, portfolioItems, this::handleItemClick);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fetchPortfolioData();
    }


    void fetchPortfolioData() {
        new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/portfolio");
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
                    List<PortfolioItem> newItems = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject portfolioItemJson = jsonArray.getJSONObject(i);
                        String symbol = portfolioItemJson.getString("ticker");
                        int quantity = portfolioItemJson.getInt("quantity");
                        double totalCost = portfolioItemJson.getDouble("totalcost");
                        newItems.add(new PortfolioItem(symbol, 0, 0, 0, quantity, totalCost));  // Stock data is filled later
                    }
                    updatePortfolioUI(newItems);
                } else {
                    Toast.makeText(activity, "Failed to fetch portfolio data: " + connection.getResponseCode(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "Error fetching portfolio data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }).start();
    }

    private void updatePortfolioUI(List<PortfolioItem> newItems) {
        new Handler(Looper.getMainLooper()).post(() -> {
            portfolioItems.clear();
            portfolioItems.addAll(newItems);
            adapter.notifyDataSetChanged();
            for (PortfolioItem item : portfolioItems) {
                fetchStockData(item);
            }
        });
    }

    private void fetchStockData(PortfolioItem item) {
        new Thread(() -> {
            try {
                URL quoteUrl = new URL("https://finnhub.io/api/v1/quote?symbol=" + item.getSymbol() + "&token=co2aripr01qvggedvg6gco2aripr01qvggedvg70");
                HttpURLConnection quoteConnection = (HttpURLConnection) quoteUrl.openConnection();
                quoteConnection.setRequestMethod("GET");
                quoteConnection.connect();

                if (quoteConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(quoteConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject quoteObject = new JSONObject(response.toString());
                    item.setCurrentPrice(quoteObject.optDouble("c", 0));
                    item.setChange(quoteObject.optDouble("d", 0));
                    item.setChangePercent(quoteObject.optDouble("dp", 0));

                    activity.runOnUiThread(() -> adapter.notifyItemChanged(portfolioItems.indexOf(item)));
                } else {
                    Toast.makeText(activity, "Failed to fetch stock data for " + item.getSymbol(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "Error fetching stock data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }).start();
    }

    private ItemTouchHelper.SimpleCallback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,  // Enable drag in both directions
                0) {  // Disable swipe actions

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(portfolioItems, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // No operation for swiping
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder.itemView.setAlpha(0.5f); // Optional: change alpha during drag
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1.0f); // Restore alpha on drop
            }
        };
    }

    private void handleItemClick(PortfolioItem item) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra("query", item.getSymbol());
        intent.putExtra("type", "portfolio");
        activity.startActivity(intent);
    }
}
