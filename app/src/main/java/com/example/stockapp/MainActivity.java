package com.example.stockapp;

import android.content.Intent;
import androidx.appcompat.widget.SearchView;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {
    private Balance balanceManager;
    private Watchlist watchlistManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        TextView balanceTextView = findViewById(R.id.textView_balance);
        RecyclerView watchlistRecyclerView = findViewById(R.id.recycler_watchlist);

        balanceManager = new Balance(this, balanceTextView);
        watchlistManager = new Watchlist(this, watchlistRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        balanceManager.fetchBalanceData();  // Fetch and display the balance data
        watchlistManager.fetchWatchListData(); // Fetch and display the watch list data
    }
}
