package edu.temple.stockportfolio.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import edu.temple.stockportfolio.MainActivity;
import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;

/**
 * Created by Kevin on 11/17/15.
 */
public class StockRetrievalService extends Service {

    //private StockManager stockManager;
    private int NOTIFICATION = 10342456;
    private final IBinder mBinder = new LocalBinder();
    private HashMap<String,StockModel> stocks;
    private NotificationManager mNM;

    @Override
    public IBinder onBind(Intent intent) {
        //stockManager = StockManager.getInstance();

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid){

        stocks = new HashMap<>();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Thread thread = new Thread(){
            @Override
            public void run(){
                startChecker();
            }
        };

        thread.start();

        return START_REDELIVER_INTENT;
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
                        showNotification();
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

    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_more)  // the status icon
                .setTicker("Stocks Updated")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Stock Portfolio")  // the label of the entry
                .setContentText("Stocks Updated")  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.cancel(NOTIFICATION);
        mNM.notify(NOTIFICATION, notification);
    }

}
