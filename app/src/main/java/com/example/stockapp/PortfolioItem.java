package com.example.stockapp;

public class PortfolioItem {
    private String symbol;
    private double currentPrice;
    private double change;
    private double changePercent;
    private int quantity;
    private double totalCost;

    public PortfolioItem(String symbol, double currentPrice, double change, double changePercent, int quantity, double totalCost) {
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.change = change;
        this.changePercent = changePercent;
        this.quantity = quantity;
        this.totalCost = totalCost;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        return "PortfolioItem{" +
                "symbol='" + symbol + '\'' +
                ", currentPrice=" + currentPrice +
                ", change=" + change +
                ", changePercent=" + changePercent +
                ", quantity=" + quantity +
                ", totalCost=" + totalCost +
                '}';
    }
}
