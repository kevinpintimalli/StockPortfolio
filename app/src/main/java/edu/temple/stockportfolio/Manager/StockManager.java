package edu.temple.stockportfolio.Manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.temple.stockportfolio.MainActivity;
import edu.temple.stockportfolio.R;

/**
 * Created by Kevin on 11/18/15.
 */
public class StockManager {
    private ArrayList<String> stocks;
    private static StockManager stockManager;
    private SharedPreferences sharedPreferences;
    private Context context;

    public StockManager(){
        stocks = new ArrayList<>();
    }

    public static StockManager getInstance(){
        if(stockManager == null)
            stockManager = new StockManager();
        return stockManager;
    }

    public void setContext(MainActivity context){
        this.context = context;
        sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        stocks = loadPreferences();
    }

    public boolean addStock(String symbol){
        if (!stocks.contains(symbol)) {
            stocks.add(symbol);
            return true;
        }
        return false;
    }

    public boolean removeStock(String symbol){
        if(stocks.contains(symbol)){
            stocks.remove(symbol);
            return true;
        }
        return false;
    }

    public void storePreferences(){
        if(sharedPreferences == null)
            return;

        Set<String> symbols = new HashSet<>();


        for(String symbol:stocks){
            symbols.add(symbol);
        }

        sharedPreferences.edit().putStringSet(context.getString(R.string.shared_preferences_key), symbols).apply();
    }

    private ArrayList<String> loadPreferences(){
        ArrayList<String> symbols = new ArrayList<>();

        Set<String> symbolsSet = sharedPreferences.getStringSet(context.getString(R.string.shared_preferences_key),new HashSet<String>());

        for(String symbol:symbolsSet){
            symbols.add(symbol);
        }

        return symbols;
    }

    public void setStocks(ArrayList<String> stocks) {
        this.stocks = stocks;
    }

    public ArrayList<String> getStocks() {
        return stocks;
    }
}
