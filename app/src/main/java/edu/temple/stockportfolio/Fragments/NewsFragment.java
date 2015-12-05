package edu.temple.stockportfolio.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.stockportfolio.Adapters.NewsListAdapter;
import edu.temple.stockportfolio.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {
    public static String NEWS_TAG = "NEWS FRAGMENT";
    private ArrayList<String> titles;
    private ListView newsList;
    private ArrayList<String> urls;

    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        return fragment;
    }

    public NewsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsList = (ListView) view.findViewById(R.id.newsList);

        Bundle args = getArguments();
        ArrayList<String> stocks = args.getStringArrayList(getString(R.string.news_key));

        if(stocks != null && !stocks.isEmpty()) {
            NewsAsyncTask news = new NewsAsyncTask();
            news.execute(stocks);
        }

        return view;
    }

    private class NewsAsyncTask extends AsyncTask<ArrayList<String>,Void,String>{

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            String newUrl = "http://finance.yahoo.com/rss/headline?s="+params[0].get(0);

            for(int i=1;i<params[0].size();i++){
                newUrl = newUrl + "+" + params[0].get(i);
            }

            try {

                URL url = new URL(newUrl);

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String response = "",tmpResponse;

                tmpResponse = reader.readLine();
                while(tmpResponse != null){
                    response = response + tmpResponse;
                    tmpResponse = reader.readLine();
                }

                JSONObject object = XML.toJSONObject(response).getJSONObject("rss").getJSONObject("channel");
                JSONArray array = object.getJSONArray("item");

                urls = new ArrayList<>();
                titles = new ArrayList<>();
                for(int i=0;i<array.length();i++){
                    JSONObject tempObject = array.getJSONObject(i);
                    String title = tempObject.getString("title");
                    String tempUrl = tempObject.getString("link");
                    urls.add(tempUrl);
                    titles.add(title);
                }

                return response;

            }catch (MalformedURLException e){
                Log.d("MalformedURLException", e.toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            newsList.setAdapter(new NewsListAdapter(getContext(),titles,urls));
        }
    }


}
