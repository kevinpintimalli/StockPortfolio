package edu.temple.stockportfolio.Fragments;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;
import info.hoang8f.android.segmented.SegmentedGroup;


public class StockDetailsFragment extends Fragment {

    private TextView stockHeader;
    private TextView currentPrice;
    private TextView openingPrice;
    private TextView percentChange;
    private TextView volume;
    private ImageView chart;
    private SegmentedGroup dayChoice;

   // private onAddStock mListener;
    private String symbol;
    private View view;

    public StockDetailsFragment() {
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
        view = inflater.inflate(R.layout.fragment_stock_details, container, false);

        stockHeader = (TextView) view.findViewById(R.id.stocknametext);
        currentPrice = (TextView) view.findViewById(R.id.currentpricetext);
        openingPrice = (TextView) view.findViewById(R.id.openingpricetext);
        percentChange = (TextView) view.findViewById(R.id.percentchangetext);
        volume = (TextView) view.findViewById(R.id.volumetext);
        //addStock = (Button) view.findViewById(R.id.addstockbutton);
        chart = (ImageView) view.findViewById(R.id.stockchart);
        dayChoice = (SegmentedGroup) view.findViewById(R.id.chartdategroup);

        Bundle args = getArguments();
        final StockModel sm = args.getParcelable(getString(R.string.stock_details_key));
        //boolean bool = args.getBoolean(getString(R.string.stock_details_bool_key));

        symbol = sm.getShortName();

        stockHeader.setText(sm.getShortName() + ": "+sm.getName());
        currentPrice.setText("Current: $"+String.format("%.2f", sm.getCurrentPrice()));
        openingPrice.setText("Opening: $"+String.format("%.2f", sm.getOpeningPrice()));
        percentChange.setText("Change:"+String.format("%.2f", sm.getChangePercent())+"%");
        volume.setText("Volume:"+sm.getVolume());

        dayChoice.setTintColor(R.color.radio_button_selected_color, R.color.black);

        dayChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) view.findViewById(checkedId);
                String text = (String) rb.getText();
                loadImage(text.toLowerCase());
            }
        });

        loadImage("1d");
/*
        if(bool){
            addStock.setVisibility(View.INVISIBLE);
        }
        else{
            addStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addStock.setVisibility(View.INVISIBLE);
                    mListener.addStock(sm);
                }
            });
        }
        */

        return view;
    }

    private void loadImage(String day){
        Picasso.with(getContext()).load("https://chart.yahoo.com/z?t="+day+"&s="+symbol)
                .resizeDimen(R.dimen.image_size_width, R.dimen.image_size_height).into(chart);
    }


}
