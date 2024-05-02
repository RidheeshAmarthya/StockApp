package com.example.stockapp;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(""); // Optionally set the title based on the type
        }

        // Handle navigation click on the toolbar (go back)
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Apply system window insets as padding for the main view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the search query and type passed from MainActivity or PortfolioActivity
        String query = getIntent().getStringExtra("query");
        String type = getIntent().getStringExtra("type"); // Could be "watchlist" or "portfolio"

        TextView queryTextView = findViewById(R.id.textView_query);
        queryTextView.setText(query);

        // Optionally adjust UI based on the type of item
        TextView queryBarView = findViewById(R.id.toolbar_text);
        if ("portfolio".equals(type)) {
            queryBarView.setText("Portfolio Item: " + query);
        } else {
            queryBarView.setText("Watchlist Item: " + query);
        }

        // Configure the WebView to display a local HTML chart
        WebView webView = findViewById(R.id.chart1);
        setupWebView(webView);
    }

    private void setupWebView(WebView webView) {
        webView.loadUrl("file:///android_asset/chart1.html");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }
}
