package edu.temple.stockportfolio.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;

/**
 * Created by Kevin on 11/17/15.
 */
public class StockListAdapter extends BaseAdapter {

    private ArrayList<StockModel> stocks;
    private LayoutInflater inflater;
    private Context context;

    public StockListAdapter(Context context, ArrayList<StockModel> stocks){
        this.context = context;
        this.stocks = stocks;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public Object getItem(int position) {
        return stocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = inflater.inflate(R.layout.stock_list_item,null);

        TextView nameShort = (TextView) v.findViewById(R.id.liststockshortname);
        TextView name = (TextView) v.findViewById(R.id.liststockname);
        TextView value = (TextView) v.findViewById(R.id.liststockvalue);

        nameShort.setText(stocks.get(position).getShortName());
        name.setText(stocks.get(position).getName().substring(0,Math.min(15,stocks.get(position).getName().length())));

        double current = stocks.get(position).getCurrentPrice();
        double opening = stocks.get(position).getOpeningPrice();

        value.setText(String.format("%.2f", current));

        if(current > opening){
            v.setBackgroundColor(context.getColor(android.R.color.holo_green_light));
        }
        else if(current < opening){
            v.setBackgroundColor(context.getColor(android.R.color.holo_red_light));
        }

        return v;
    }

    public void setStocks(ArrayList<StockModel> stocks) {
        this.stocks = stocks;
    }
}
