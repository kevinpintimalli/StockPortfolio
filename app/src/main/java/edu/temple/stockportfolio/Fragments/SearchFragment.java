package edu.temple.stockportfolio.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import edu.temple.stockportfolio.R;

public class SearchFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    private AutoCompleteTextView stockText;
    private Button addButton;
    private boolean isInit;
    private String currentSymbol;
    private String[] sug;
    private String[] sugNames;


    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();

        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        stockText = (AutoCompleteTextView) view.findViewById(R.id.stockText);
        addButton = (Button) view.findViewById(R.id.goButton);

        isInit = false;
        currentSymbol = "";

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInit){
                    isInit = false;
                    addButton.setText(R.string.get_details);
                    stockText.setText("");
                    stockText.dismissDropDown();

                    mListener.onAddStock(currentSymbol);
                }
                else {
                    String symbol = stockText.getText().toString();

                    if(sugNames != null) {
                        for (int i = 0; i < sugNames.length; i++) {
                            if (sugNames[i].equalsIgnoreCase(symbol)) {
                                symbol = sug[i];
                                break;
                            }
                        }
                    }

                    currentSymbol = symbol;

                    mListener.onInitStock(currentSymbol.toUpperCase());
                }
            }
        });

        stockText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER) && (!isInit)) {
                    // Perform action on key press
                    String symbol = stockText.getText().toString();

                    if(sugNames != null) {
                        for (int i = 0; i < sugNames.length; i++) {
                            if (sugNames[i].equalsIgnoreCase(symbol)) {
                                symbol = sug[i];
                                break;
                            }
                        }
                    }

                    mListener.onInitStock(symbol.toUpperCase());
                    return true;
                }
                return false;
            }
        });

        stockText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if(isInit){
                    isInit = false;
                    addButton.setText(R.string.get_details);
                }


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d("Auto","before:"+before+" count:"+count+" start:"+start);

                stockText.dismissDropDown();
                if(count < 2 && s.length() != 0){
                    GetSuggestions suggest = new GetSuggestions(stockText);
                    suggest.execute(stockText.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    public void setInit(){
        isInit = true;
        addButton.setText(R.string.add);
    }


    public interface OnFragmentInteractionListener {
        public void onInitStock(String symbol);
        public void onAddStock(String symbol);
    }

    private class GetSuggestions extends AsyncTask<String,Void,String[]> {

        AutoCompleteTextView textView;

        public GetSuggestions(AutoCompleteTextView textView){
            this.textView = textView;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String newUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";
            try {
                if(params[0].contains(" ")){
                    newUrl = newUrl + "\""+URLEncoder.encode(params[0],"UTF-8")+"\"";
                }
                else
                    newUrl = newUrl + params[0];

                URL url = new URL(newUrl);

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String response = "",tmpResponse;

                tmpResponse = reader.readLine();
                while(tmpResponse != null){
                    response = response + tmpResponse;
                    tmpResponse = reader.readLine();
                }


                JSONArray suggestionsJSON = new JSONArray(response);
                sug = new String[suggestionsJSON.length()];
                sugNames = new String[sug.length];
                for(int i=0;i<suggestionsJSON.length();i++){
                    sug[i] = suggestionsJSON.getJSONObject(i).getString("Symbol");
                    sugNames[i] = sug[i]+": "+suggestionsJSON.getJSONObject(i).getString("Name");
                }

                return sugNames;

            }catch (MalformedURLException e){
                Log.d("MalformedURLException", e.toString());
            }catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String[] result){
            if(result != null) {
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, result);
                textView.setAdapter(adapter);
                textView.showDropDown();
            }
        }
    }

}
