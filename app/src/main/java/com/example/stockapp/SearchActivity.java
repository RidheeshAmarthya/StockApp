package com.example.stockapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.AlertDialog;
import android.widget.Toast;


public class SearchActivity extends AppCompatActivity {

    private WebView webView1, webView2, webView3, webView4, section1, section2, section3, section4, section5;
    private ImageView starIcon;
    private Button tradeButton;
    private PopupWindow tradePopup;

    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Find buttons
        View butt1 = findViewById(R.id.button1);
        View butt2 = findViewById(R.id.button2);
        View line1 = findViewById(R.id.line1);
        View line2 = findViewById(R.id.line2);

        // Find WebViews
        webView1 = findViewById(R.id.chart1);
        webView2 = findViewById(R.id.chart2);

        // Set initial transparency
        webView1.setVisibility(View.GONE);
        webView2.setVisibility(View.VISIBLE);
        line1.setAlpha(1f);
        line2.setAlpha(0f);

        // Set click listeners for buttons
        butt1.setOnClickListener(v -> {
            // Increase transparency of chart1, decrease transparency of chart2
            webView1.setVisibility(View.GONE);
            webView2.setVisibility(View.VISIBLE);
            line1.setAlpha(1f);
            line2.setAlpha(0f);
        });

        butt2.setOnClickListener(v -> {
            // Increase transparency of chart2, decrease transparency of chart1
            webView1.setVisibility(View.VISIBLE);
            webView2.setVisibility(View.GONE);
            line1.setAlpha(0f);
            line2.setAlpha(1f);
        });

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
        TextView queryTextView = findViewById(R.id.textView_query);
        queryTextView.setText(query);

        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(query); // Set the text for the toolbar



        // Setup WebViews
        webView1 = findViewById(R.id.chart1);
        webView2 = findViewById(R.id.chart2);
        webView3 = findViewById(R.id.chart3);
        webView4 = findViewById(R.id.chart4);
        section1 = findViewById(R.id.section1);
        section2 = findViewById(R.id.section2);
        section3 = findViewById(R.id.section3);
        section4 = findViewById(R.id.section4);
        section5 = findViewById(R.id.section5);

        setupWebView(webView1, "chart1.html");
        setupWebView(webView2, "chart2.html");
        setupWebView(webView3, "chart3.html");
        setupWebView(webView4, "chart4.html");
        setupWebView(section1, "section1.html");
        setupWebView(section2, "section2.html");
        setupWebView(section3, "section3.html");
        setupWebView(section4, "section4.html");
        setupWebView(section5, "section5.html");

        // Set up the star icon
        starIcon = findViewById(R.id.star_icon);
        setupStarIcon(query);

        // Check if the query is in the watchlist
        checkWatchListStatus(query);

        // Set up the trade button and popup
        tradeButton = findViewById(R.id.button4);
        tradeButton.setOnClickListener(v -> showTradePopup());

        autoRefreshHandler = new Handler();
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Here you can refresh your views, or restart the activity
                recreate(); // Simplest way to refresh the activity
                // Schedule the next refresh
                autoRefreshHandler.postDelayed(this, 30000); // 15000 milliseconds = 15 seconds
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto-refreshing when the activity is in the foreground
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refreshing when the activity goes into the background
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up to avoid memory leaks
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }


    private void setupWebView(WebView webView, String fileName) {
        String query = getIntent().getStringExtra("query");
        String url = "file:///android_asset/" + fileName + "?query=" + Uri.encode(query);
        webView.loadUrl(url);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new CustomWebViewClient()); // Set the custom WebViewClient here
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Check if the URL is the specific one we're interested in
            if (url.equals("http://text.com/QCOM")) {
                // If the URL is "http://text.com/AMD", open SearchActivity with query "AMD"
                Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
                intent.putExtra("query", "QCOM");
                startActivity(intent);
                return true; // Return true indicating we've handled the URL
            }

            if (url.startsWith("https://www.facebook.com/sharer/sharer.php")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true; // Return true indicating we've handled the URL
            }

            if (url.startsWith("https://twitter.com/intent/tweet")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true; // Return true indicating we've handled the URL
            }

            if (url.startsWith("https://finnhub.io/api/news?")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true; // Return true indicating we've handled the URL
            }

            // For other URLs, let the WebView handle them
            return false;
        }
    }
    private void setupStarIcon(String query) {
        starIcon.setOnClickListener(v -> {
            String currentTag = (String) starIcon.getTag();
            if ("gold".equals(currentTag)) {
                removeFromWatchList(query);
            } else {
                addToWatchList(query);
            }
        });
    }

    private void checkWatchListStatus(String query) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/watchList");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONArray watchList = new JSONArray(response.toString());
                boolean isInWatchList = false;
                for (int i = 0; i < watchList.length(); i++) {
                    JSONObject item = watchList.getJSONObject(i);
                    if (query.equals(item.getString("watchList"))) {
                        isInWatchList = true;
                        break;
                    }
                }
                final boolean finalIsInWatchList = isInWatchList;
                runOnUiThread(() -> {
                    if (finalIsInWatchList) {
                        starIcon.setImageResource(R.drawable.full_star);
                        starIcon.setTag("gold");
                    } else {
                        starIcon.setImageResource(R.drawable.star_border);
                        starIcon.setTag("grey");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void addToWatchList(String query) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/CreatewatchList");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonInputString = "{\"watchList\": \"" + query + "\"}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                runOnUiThread(() -> {
                    starIcon.setImageResource(R.drawable.full_star);
                    Toast.makeText(getApplicationContext(), query + " added to favorites", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    private void removeFromWatchList(String query) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/DeletewatchList/" + query);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.getResponseCode(); // You can use the response for more detailed error handling
                runOnUiThread(() -> {
                    starIcon.setImageResource(R.drawable.star_border);
                    Toast.makeText(getApplicationContext(), query + " removed from favorites", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void showTradePopup() {
        // Retrieve the query (stock symbol)
        String query = getIntent().getStringExtra("query");
        new TradeDialog(this, query);
    }


    private void submitTrade(String amount) {
        // Implement the trade submission logic here
        // For example, you might want to call a backend API to execute the trade
        Toast.makeText(this, "Trade submitted for: " + amount, Toast.LENGTH_SHORT).show();
    }
}
