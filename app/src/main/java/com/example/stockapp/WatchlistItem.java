package com.example.stockapp;

/**
 * A model class representing an item in a stock watchlist.
 */
public class WatchlistItem {
    private String symbol;
    private String name;
    private double currentPrice;
    private double change;
    private double changePercent;

    /**
     * Constructs a new WatchlistItem.
     *
     * @param symbol        the stock symbol
     * @param name          the name of the company
     * @param currentPrice  the current stock price
     * @param change        the change in stock price
     * @param changePercent the percentage change in stock price
     */
    public WatchlistItem(String symbol, String name, double currentPrice, double change, double changePercent) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public String toString() {
        return "WatchlistItem{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                ", change=" + change +
                ", changePercent=" + changePercent +
                '}';
    }
}