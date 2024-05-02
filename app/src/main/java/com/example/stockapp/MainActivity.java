package com.example.stockapp;

import android.content.Intent;
import androidx.appcompat.widget.SearchView;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.widget.Toolbar.LayoutParams; // Ensure to import the correct LayoutParams


public class MainActivity extends AppCompatActivity {
    private Balance balanceManager;
    private Watchlist watchlistManager;
    private Portfolio portfolioManager; // Added for the portfolio management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Setup top toolbar
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Search Stuff
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Search", "Query submitted: " + query);
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes to the search text if necessary
                return false;
            }
        });

        //Search bar logic to hide the text when searching
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // When gaining focus, expand to match parent
                    Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.RIGHT; // Retain the original gravity
                    searchView.setLayoutParams(layoutParams);
                } else {
                    // When losing focus, revert to wrap content and reset to right gravity
                    Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.RIGHT; // Set gravity to right again
                    searchView.setLayoutParams(layoutParams);
                }
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        balanceManager.fetchBalanceData();  // Fetch and display the balance data
        watchlistManager.fetchWatchListData(); // Fetch and display the watch list data
        portfolioManager.fetchPortfolioData(); // Fetch and display the portfolio data
    }
}
