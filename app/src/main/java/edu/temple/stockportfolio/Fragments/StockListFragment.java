package edu.temple.stockportfolio.Fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.stockportfolio.Adapters.StockListAdapter;
import edu.temple.stockportfolio.Manager.StockManager;
import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;


public class StockListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ListView listView;
    private ArrayList<StockModel> stocks;
    private StockListAdapter adapter;
    private ArrayList<Integer> positionsToRemove;
    private ArrayList<String> stocksToRemove;


    public static StockListFragment newInstance() {
        StockListFragment fragment = new StockListFragment();
        return fragment;
    }

    public StockListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_list, container, false);
        listView = (ListView) view.findViewById(R.id.stocklist);

        positionsToRemove = new ArrayList<>();

        stocks = getArguments().getParcelableArrayList(getString(R.string.stock_list_key));

        if(stocks == null){
            stocks = new ArrayList<>();
        }

        adapter = new StockListAdapter(view.getContext(),stocks);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (positionsToRemove.contains(position)) {
                    stocksToRemove.remove(stocks.get(position).getShortName());
                    positionsToRemove.remove((Integer)position);
                    view.setAlpha((float)1.0);
                } else {
                    mListener.onStockSelected((StockModel) parent.getItemAtPosition(position));
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                positionsToRemove.add(position);
                stocksToRemove.add(stocks.get(position).getShortName());
                //parent.setBackgroundColor(getContext().getColor(R.color.grey));
                view.setAlpha((float)0.7);
                return true;
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onStockSelected(StockModel sm);
    }

    public ArrayList<String> getStocksToRemove(){
        return stocksToRemove;
    }

    public void updateStocks(ArrayList<StockModel> stock){
        positionsToRemove = new ArrayList<>();
        stocksToRemove = new ArrayList<>();
        stocks = stock;
        adapter.setStocks(stocks);
        adapter.notifyDataSetChanged();
    }


}
