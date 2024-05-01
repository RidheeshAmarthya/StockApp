package com.example.stockapp;

import android.app.Activity;
import android.widget.TextView;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class Balance {
    private Activity activity;
    private TextView balanceTextView;

    public Balance(Activity activity, TextView balanceTextView) {
        this.activity = activity;
        this.balanceTextView = balanceTextView;
    }

    public void fetchBalanceData() {
        new Thread(() -> {
            try {
                URL url = new URL("https://stock-app3-backend-obu6dw52ya-wm.a.run.app/Balance");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = reader.readLine();
                    JSONArray jsonArray = new JSONArray(response);
                    float balance = (float) jsonArray.getDouble(0);

                    updateBalanceOnUI(balance);
                } else {
                    System.err.println("HTTP_ERROR: Response Code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateBalanceOnUI(float balance) {
        activity.runOnUiThread(() -> balanceTextView.setText(String.format(Locale.getDefault(), "%.2f", balance)));
    }
}
