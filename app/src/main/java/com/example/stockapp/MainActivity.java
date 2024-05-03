package com.example.stockapp;

import android.content.Intent;
import androidx.appcompat.widget.SearchView;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import androidx.appcompat.widget.Toolbar.LayoutParams; // Ensure to import the correct LayoutParams

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private Balance balanceManager;
    private Watchlist watchlistManager;
    private Portfolio portfolioManager; // Added for the portfolio management

    private TextView netWorthTextView;

    String[] stocks = {"AAP",
            "AAPB",
            "AAPT",
            "AAPD",
            "AAPJ",
            "AAPI",
            "AAPL",
            "AAPR",
            "AAPX",
            "AAPY",
            "AAPU",
            "AEMMF",
            "NVD",
            "NVDL",
            "NVDX",
            "NVDA",
            "NVDS",
            "NVDQ",
            "NVDU",
            "NVDY",
            "NVDD",
            "NVMI"
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Setup top toolbar
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);


        // Search Stuff
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stocks);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapter);

        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            String query = autoCompleteTextView.getText().toString();
            if (!query.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
            }
        });

        netWorthTextView = findViewById(R.id.Net_Worth_val);

        fetchFinancialData();

        // Padding on the main page

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Date time section of main page
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        TextView dateTextView = findViewById(R.id.textView_date);
        dateTextView.setText(currentDate);

        //Portfolio and Watchlist
        TextView balanceTextView = findViewById(R.id.textView_balance);
        RecyclerView watchlistRecyclerView = findViewById(R.id.recycler_watchlist);
        RecyclerView portfolioRecyclerView = findViewById(R.id.recycler_portfolio); // Assuming you have this RecyclerView in your layout

        balanceManager = new Balance(this, balanceTextView);
        watchlistManager = new Watchlist(this, watchlistRecyclerView);
        portfolioManager = new Portfolio(this, portfolioRecyclerView); // Initialize the portfolio manager
    }

    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchFinancialData(); // Fetches and updates net worth

            // Additionally, fetch portfolio and watchlist data
            if (portfolioManager != null) {
                portfolioManager.fetchPortfolioData(); // Fetch and update the portfolio data
            }
            if (watchlistManager != null) {
                watchlistManager.fetchWatchListData(); // Fetch and update the watch list data
            }

            refreshHandler.postDelayed(this, 15000); // Schedule this Runnable again to run after 15 seconds
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        balanceManager.fetchBalanceData();  // Fetch and display the balance data
        watchlistManager.fetchWatchListData(); // Fetch and display the watch list data
        portfolioManager.fetchPortfolioData(); // Fetch and display the portfolio data

        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the periodic refresh when the activity is not in the foreground
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    public void openFinnhubWebsite(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://finnhub.io/"));  // Use the correct URL for Finnhub
        startActivity(intent);
    }

    private void fetchFinancialData() {
        new FetchFinancialDataTask().execute();
    }

    private class FetchFinancialDataTask extends AsyncTask<Void, Void, Double> {
        protected Double doInBackground(Void... params) {
            try {
                double balance = fetchBalance();
                double portfolioValue = fetchPortfolioValue();
                return balance + portfolioValue;
            } catch (Exception e) {
                Log.e("Error", "Failed to fetch financial data", e);
                return 0.0;
            }
        }

        protected void onPostExecute(Double netWorth) {
            netWorthTextView.setText(String.format(Locale.getDefault(), "$%.2f", netWorth));
        }

        private double fetchBalance() throws Exception {
            URL url = new URL("https://assignment3-backend.azurewebsites.net/Balance");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArr = new JSONArray(response.toString());
                return jsonArr.getDouble(0);
            }
            return 0.0;
        }

        private double fetchPortfolioValue() throws Exception {
            URL url = new URL("https://assignment3-backend.azurewebsites.net/portfolio");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray portfolio = new JSONArray(response.toString());
                double totalValue = 0.0;

                for (int i = 0; i < portfolio.length(); i++) {
                    JSONObject stock = portfolio.getJSONObject(i);
                    double currentPrice = fetchCurrentStockPrice(stock.getString("ticker"));
                    totalValue += currentPrice * stock.getInt("quantity");
                }
                return totalValue;
            }
            return 0.0;
        }

        private double fetchCurrentStockPrice(String ticker) throws Exception {
            String apiKey = "co326d9r01qp2simicagco326d9r01qp2simicb0";
            URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());
                double currentPrice = json.getDouble("c");  // Current price of the stock

                Random random = new Random();
                double randomAdjustment = random.nextDouble() * 2 - 1; // Generates a random double between -1 and 1
                return currentPrice + randomAdjustment;
            }
            return 0.0;
        }

    }

}
