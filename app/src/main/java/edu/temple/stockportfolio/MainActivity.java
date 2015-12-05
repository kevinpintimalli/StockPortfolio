package edu.temple.stockportfolio;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.temple.stockportfolio.Fragments.DetailsFragment;
import edu.temple.stockportfolio.Fragments.SearchFragment;
import edu.temple.stockportfolio.Fragments.StockDetailsFragment;
import edu.temple.stockportfolio.Fragments.StockListFragment;
import edu.temple.stockportfolio.Manager.StockManager;
import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.Service.StockRetrievalService;

public class MainActivity extends Activity implements StockListFragment.OnFragmentInteractionListener,SearchFragment.OnFragmentInteractionListener,DetailsFragment.onFragmentInteraction{

    private StockManager stockManager;
    private StockRetrievalService updateService;
    private boolean mBound;
    private ArrayList<StockModel> stocks;
    private String currentSymbol;
    private boolean twoPanes;
    private boolean isSearch;
    private boolean stockListVisible;
    private SearchFragment searchFrag;
    private StockListFragment stockListFragment;
    private DetailsFragment detailsFragment;
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        twoPanes = (findViewById(R.id.detailsfragment) != null);

        stockManager = StockManager.getInstance();
        stockManager.setContext(this);

        if(isNetworkActive()) {
            Intent intent = new Intent(MainActivity.this, StockRetrievalService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            stocks = new ArrayList<>();

            Bundle args = new Bundle();
            args.putParcelableArrayList(getString(R.string.stock_list_key), stocks);

            stockListFragment = new StockListFragment();
            currentSymbol = "";
            isSearch = false;
            stockListVisible = true;

            loadFragment(R.id.stocklistfragment, stockListFragment, false, args);

            if (twoPanes) {
                loadDetails(null, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case(R.id.searchStockIcon):
                if(!isSearch) {
                    searchFrag = new SearchFragment();
                    loadFragment(R.id.stockSearchFragment, searchFrag, false, null);
                    isSearch=true;
                }
                else{
                    removeFragment(searchFrag);
                    isSearch=false;
                }
                return true;
            case(R.id.deleteStocks):
                if(stockListVisible){
                    removeStocks();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadFragment(int paneId,Fragment fragment,boolean placeOnBackStack,Bundle args){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(args != null)
            fragment.setArguments(args);

        fragmentTransaction.replace(paneId, fragment);

        if(placeOnBackStack){
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void removeFragment(Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.remove(fragment);

        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void onStockSelected(StockModel sm) {
        loadDetails(sm, true);
    }

    @Override
    public void onInitStock(String symbol){
        currentSymbol = symbol;
        for(StockModel sm:stocks){
            if(symbol.equalsIgnoreCase(sm.getShortName())){
                //Toast.makeText(getBaseContext(), R.string.toast_already_exists, Toast.LENGTH_SHORT).show();
                loadDetails(sm,true);
                return;
            }
        }
        updateService.initStock(symbol);
    }


    public void removeStocks(){
        ArrayList<String> stocksToRemove = stockListFragment.getStocksToRemove();

        for(String toRemove:stocksToRemove){
            updateService.removeStock(toRemove);
            for(int i=0;i<stocks.size();i++){
                StockModel stockModel = stocks.get(i);
                if(stockModel.getShortName().equalsIgnoreCase(toRemove)){
                    stocks.remove(i);
                    stockManager.removeStock(toRemove);
                }
            }
        }

        stockListFragment.updateStocks(stocks);
    }

    private void loadDetails(StockModel sm,boolean exists){

        if(!stockListVisible && detailsFragment!=null){
            detailsFragment.changeActiveDetails();
        }

        if(sm == null){
            Fragment details = new DetailsFragment();
            loadFragment(twoPanes ? R.id.detailsfragment : R.id.stocklistfragment, details, !twoPanes && stockListVisible, null);
        }
        else{
            Fragment details = new DetailsFragment();
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.stock_details_key), sm);
            loadFragment(twoPanes ? R.id.detailsfragment : R.id.stocklistfragment, details, !twoPanes && stockListVisible, args);
        }

        if(!twoPanes) {
            stockListVisible = false;
            mOptionsMenu.findItem(R.id.deleteStocks).setVisible(false);
        }

        if(!exists){
            searchFrag.setInit();
        }
        //isDetailsVisible = true;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StockRetrievalService.LocalBinder binder = (StockRetrievalService.LocalBinder) service;
            updateService = binder.getService();
            mBound = true;

            ArrayList<String> stockArray = stockManager.getStocks();
            for(String symbols:stockArray){
                updateService.addStock(symbols);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onDestroy(){
        if(mBound){
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(getString(R.string.service_stock_init_key))){
                StockModel sm = intent.getParcelableExtra(getString(R.string.service_stock_init_key));

                if(sm != null){
                    Log.d("Init Stock", sm.getName());
                    loadDetails(sm, false);
                }
                else{
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_stock, Toast.LENGTH_SHORT).show();
                }

            }
            else if(intent.hasExtra(getString(R.string.service_stock_added_key))){
                StockModel sm = intent.getParcelableExtra(getString(R.string.service_stock_added_key));
                if(sm != null) {
                    Log.d("Stock Added", sm.getName());
                    stocks.add(sm);
                    stockManager.addStock(sm.getShortName());
                    stockListFragment.updateStocks(stocks);
                }
            }
            else if(intent.hasExtra(getString(R.string.service_stock_update_key))){
                Log.d("Update received","YAY");
                HashMap<String,StockModel> stocksTemp = (HashMap<String,StockModel>) intent.getSerializableExtra(getString(R.string.service_stock_update_key));
                for(String key:stocksTemp.keySet()){
                    for(int i=0;i<stocks.size();i++){
                        if(stocks.get(i).getShortName().equalsIgnoreCase(key)){
                            stocks.set(i,stocksTemp.get(key));
                            break;
                        }
                    }
                }
                stockListFragment.updateStocks(stocks);
            }
            else{
                Toast.makeText(getBaseContext(), R.string.toast_invalid_stock, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(getString(R.string.stock_model_key));
        registerReceiver(receiver, filter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onStop(){
        stockManager.storePreferences();
        super.onStop();
    }


    @Override
    public void onAddStock(String symbol) {
        updateService.addStock(symbol);
    }

    @Override
    public void leavingFrag(boolean isActive) {
        if(!twoPanes) {
            stockListVisible = isActive;
            if (stockListVisible) {
                mOptionsMenu.findItem(R.id.deleteStocks).setVisible(true);
            }
        }
    }

    public boolean isNetworkActive(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Toast.makeText(this, "No Network Access", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
