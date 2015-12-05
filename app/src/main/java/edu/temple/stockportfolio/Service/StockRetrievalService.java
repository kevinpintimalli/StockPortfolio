package edu.temple.stockportfolio.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;

/**
 * Created by Kevin on 11/17/15.
 */
public class StockRetrievalService extends Service {

    //private StockManager stockManager;
    private final IBinder mBinder = new LocalBinder();
    private HashMap<String,StockModel> stocks;

    @Override
    public IBinder onBind(Intent intent) {
        //stockManager = StockManager.getInstance();
        stocks = new HashMap<>();

        Thread thread = new Thread(){
            @Override
            public void run(){
                startChecker();
            }
        };

        thread.start();

        return mBinder;
    }

    public void initStock(final String symbol){
        Thread thread = new Thread(){
            @Override
            public void run(){
                StockModel sm = getQuote(symbol);

                sendBroadcastMessage(getString(R.string.stock_model_key), sm, getString(R.string.service_stock_init_key));
            }
        };

        thread.start();

    }

    public void addStock(final String symbol) {
        Thread thread = new Thread(){
            @Override
            public void run(){
                if (!containsStock(symbol)) {
                    StockModel sm = getQuote(symbol);
                    if(sm != null){
                        stocks.put(symbol, sm);
                    }
                    sendBroadcastMessage(getString(R.string.stock_model_key),sm,getString(R.string.service_stock_added_key));
                }
            }
        };

        thread.start();

    }

    public void removeStock(String symbol){
        if(containsStock(symbol)) {
            stocks.remove(symbol);
        }
    }

    public boolean containsStock(String symbol){
        return stocks.containsKey(symbol);
    }

    public void startChecker(){
        Thread thread = new Thread(){
            @Override
            public void run(){
                Integer waitTime = 20000;
                Long lastCheck = System.currentTimeMillis();
                while(true) {
                    if(lastCheck+waitTime < System.currentTimeMillis()){
                        getUpdate();
                        lastCheck = System.currentTimeMillis();
                    }
                    else{
                        try {
                            Thread.sleep(lastCheck + waitTime - System.currentTimeMillis());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        thread.start();
    }

    public void getUpdate(){

        for(String key:stocks.keySet()){
            StockModel sm = stocks.get(key);
            sm = getQuote(sm.getShortName());
            if(sm != null)
                stocks.put(key,sm);
        }

        //Setup broadcast
        sendBroadcastMessage(getString(R.string.stock_model_key), stocks, getString(R.string.service_stock_update_key));
    }

    private StockModel getQuote(final String symbol) {

        URL stockQuoteUrl;
        StockModel sm;
        try {

            stockQuoteUrl = new URL("http://finance.yahoo.com/webservice/v1/symbols/"+symbol+"/quote?format=json&view=basic");

            BufferedReader reader = new BufferedReader(new InputStreamReader(stockQuoteUrl.openStream()));

            String response = "", tmpResponse;

            tmpResponse = reader.readLine();
            while (tmpResponse != null) {
                response = response + tmpResponse;
                tmpResponse = reader.readLine();
            }

            JSONObject stockObject = new JSONObject(response);
            JSONObject toParse = stockObject.getJSONObject("list")
                    .getJSONArray("resources")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getJSONObject("fields");

            sm = new StockModel(symbol,toParse.getString("name"),toParse.getDouble("price"),
                    toParse.getDouble("price")-toParse.getDouble("change"),
                    toParse.getDouble("chg_percent"),toParse.getInt("volume"));
            return sm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public class LocalBinder extends Binder {
        public StockRetrievalService getService(){
            return StockRetrievalService.this;
        }
    }

    private void sendBroadcastMessage(String intentFilterName, StockModel arg1, String extraKey) {
        Intent intent = new Intent(intentFilterName);
        if (arg1 != null && extraKey != null) {
            intent.putExtra(extraKey, arg1);
        }
        sendBroadcast(intent);
    }

    private void sendBroadcastMessage(String intentFilterName, HashMap<String,StockModel> arg1, String extraKey) {
        Intent intent = new Intent(intentFilterName);
        if (arg1 != null && extraKey != null) {
            intent.putExtra(extraKey, arg1);
        }
        sendBroadcast(intent);
    }

}
