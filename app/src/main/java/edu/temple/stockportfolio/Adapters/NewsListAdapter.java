package edu.temple.stockportfolio.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.temple.stockportfolio.R;

/**
 * Created by Kevin on 12/3/15.
 */
public class NewsListAdapter extends BaseAdapter {

    private ArrayList<String> titles;
    private ArrayList<String> links;
    private LayoutInflater inflater;
    private Context context;

    public NewsListAdapter(Context context,ArrayList<String> titles,ArrayList<String> links){
        this.context = context;
        this.titles = titles;
        this.links = links;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int position) {
        return titles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.news_list_item,null);

        TextView textView = (TextView) view.findViewById(R.id.newsTitle);
        ImageView imageView = (ImageView) view.findViewById(R.id.goLink);

        textView.setText(titles.get(position));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = links.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(link));
                context.startActivity(intent);
            }
        });

        return view;
    }
}
