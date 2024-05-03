package com.example.stockapp;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.function.Consumer;

public class TradeDialog {
    private Context context;
    private Dialog dialog;
    private LinearLayout tradeLayout, successLayout;
    private TextView totalCostTextView, balanceTextView, successMessage, tradeTitleTextView;
    private EditText shareAmountEditText;
    private Button buyButton, sellButton, doneButton;
    private double currentPrice, currentBalance, currentTotalCost;
    private String stockSymbol;

    public interface TradeCompleteListener {
        void onTradeComplete();
    }

    public TradeDialog(Context context, String stockSymbol) {
        this.context = context;
        this.stockSymbol = stockSymbol;
        initializeUI();
        fetchCurrentPrice();
        fetchBalance();
        fetchCurrentTotalCost();
        fetchStockName();     // Now explicitly calling fetchStockName to set the stock name in the dialog

    }

    private void initializeUI() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_trade);

        tradeLayout = dialog.findViewById(R.id.layout_trade);
        successLayout = dialog.findViewById(R.id.layout_success);
        totalCostTextView = dialog.findViewById(R.id.textView_totalCost);
        balanceTextView = dialog.findViewById(R.id.textView_balance);
        successMessage = dialog.findViewById(R.id.success_message);
        shareAmountEditText = dialog.findViewById(R.id.editText_shareAmount);
        buyButton = dialog.findViewById(R.id.button_buy);
        sellButton = dialog.findViewById(R.id.button_sell);
        doneButton = dialog.findViewById(R.id.button_done);
        tradeTitleTextView = dialog.findViewById(R.id.textView_tradeTitle); // Initialize the TextView


        doneButton.setOnClickListener(v -> {
            dialog.dismiss();  // This should close the dialog
        });

        shareAmountEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateTotalCost();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        buyButton.setOnClickListener(v -> handleBuyOperation());
        sellButton.setOnClickListener(v -> handleSellOperation());

        dialog.show();
    }

    private void fetchStockName() {
        new Thread(() -> {
            try {
                URL url = new URL("https://finnhub.io/api/v1/stock/profile2?symbol=" + stockSymbol + "&token=co2aripr01qvggedvg6gco2aripr01qvggedvg70");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                String stockName = jsonResponse.getString("name");
                ((AppCompatActivity) context).runOnUiThread(() -> tradeTitleTextView.setText("Trade " + stockName + " stocks"));
            } catch (Exception e) {
                e.printStackTrace();
                ((AppCompatActivity) context).runOnUiThread(() -> tradeTitleTextView.setText("Trade stocks")); // Fallback title
            }
        }).start();
    }



    private void fetchCurrentPrice() {
        new Thread(() -> {
            try {
                URL url = new URL("https://finnhub.io/api/v1/quote?symbol=" + stockSymbol + "&token=co326d9r01qp2simicagco326d9r01qp2simicb0");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                currentPrice = jsonResponse.getDouble("c");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fetchBalance() {
        new Thread(() -> {
            try {
                currentBalance = getCurrentBalance();
                ((AppCompatActivity) context).runOnUiThread(() -> balanceTextView.setText(String.format("$%.2f to buy %s", currentBalance, stockSymbol)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private double getCurrentBalance() throws IOException, JSONException {
        URL url = new URL("https://assignment3-backend.azurewebsites.net/balance");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        JSONArray jsonArray = new JSONArray(response.toString());
        return jsonArray.getDouble(0);
    }

    private void fetchCurrentTotalCost() {
        new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/portfolio");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("ticker").equals(stockSymbol)) {
                        currentTotalCost = jsonObject.getDouble("totalcost");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTotalCost() {
        try {
            double amount = Double.parseDouble(shareAmountEditText.getText().toString());
            double totalCost = amount * currentPrice;
            totalCostTextView.setText(String.format(Locale.US, "%d*$%.2f/share = %.2f", (int)amount, currentPrice, totalCost));
        } catch (NumberFormatException e) {
            totalCostTextView.setText(String.format("0*$%.2f/share = 0", currentPrice));
        }
    }

    private void handleBuyOperation() {
        try {
            int quantityToBuy = Integer.parseInt(shareAmountEditText.getText().toString());

            if (quantityToBuy == 0) {
                Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            double costOfPurchase = quantityToBuy * currentPrice;

            if (costOfPurchase > currentBalance) {
                Toast.makeText(context, "Insufficient funds to complete transaction", Toast.LENGTH_SHORT).show();
                return;
            }

            double newTotalCost = currentTotalCost + (quantityToBuy * currentPrice);
            double newBalance = currentBalance - (quantityToBuy * currentPrice);

            // Fetch current quantity from the portfolio before updating it
            getCurrentPortfolioQuantity(stockSymbol, currentQuantity -> {
                int newQuantity = currentQuantity + quantityToBuy;
                updatePortfolio(stockSymbol, newTotalCost, newQuantity, newBalance);
                showSuccessMessage("bought", quantityToBuy);
            });
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentPortfolioQuantity(String ticker, Consumer<Integer> onQuantityFetched) {
        new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/portfolio");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONArray jsonArray = new JSONArray(response.toString());
                int currentQuantity = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("ticker").equals(ticker)) {
                        currentQuantity = jsonObject.getInt("quantity");
                        break;
                    }
                }
                int finalCurrentQuantity = currentQuantity;
                ((AppCompatActivity) context).runOnUiThread(() -> onQuantityFetched.accept(finalCurrentQuantity));
            } catch (Exception e) {
                e.printStackTrace();
                ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, "Error fetching portfolio data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleSellOperation() {
        try {
            int quantityToSell = Integer.parseInt(shareAmountEditText.getText().toString());

            if (quantityToSell == 0) {
                Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            getCurrentPortfolioQuantity(stockSymbol, currentQuantity -> {
                if (currentQuantity < quantityToSell) {
                    ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, "Not enough shares to sell", Toast.LENGTH_SHORT).show());
                } else {
                    double newTotalCost = currentTotalCost - (quantityToSell * currentPrice);
                    double newBalance = currentBalance + (quantityToSell * currentPrice);
                    int newQuantity = currentQuantity - quantityToSell;
                    updatePortfolio(stockSymbol, newTotalCost, newQuantity, newBalance);
                    showSuccessMessage("sold", quantityToSell);
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePortfolio(String ticker, double newTotalCost, int newQuantity, double newBalance) {
        new Thread(() -> {
            try {
                URL url = new URL("https://stock-app3-backend-obu6dw52ya-wm.a.run.app/portfolio/" + ticker);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("quantity", newQuantity);
                jsonParam.put("totalcost", newTotalCost);
                connection.getOutputStream().write(jsonParam.toString().getBytes());
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    updateBalance(newBalance);
                } else {
                    ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, "Failed to update portfolio!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void updateBalance(double newBalance) {
        new Thread(() -> {
            try {
                URL url = new URL("https://assignment3-backend.azurewebsites.net/balance/66066e225c4da4de1832cc40");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("balance", newBalance);
                connection.getOutputStream().write(jsonParam.toString().getBytes());
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ((AppCompatActivity) context).runOnUiThread(() -> {
                        balanceTextView.setText(String.format("Balance: $%.2f", newBalance));
                    });
                } else {
                    ((AppCompatActivity) context).runOnUiThread(() -> Toast.makeText(context, "Failed to update balance!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccessMessage(String action, int quantity) {
        tradeLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.VISIBLE);
        successMessage.setText(String.format("You have successfully %s %d shares of %s", action, quantity, stockSymbol));
    }

    private void submitTrade(String type) {
        Toast.makeText(context, type.toUpperCase() + " submitted for: " + shareAmountEditText.getText(), Toast.LENGTH_SHORT).show();
    }
}
