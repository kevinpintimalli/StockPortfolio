package edu.temple.stockportfolio.Fragments;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.temple.stockportfolio.Manager.StockManager;
import edu.temple.stockportfolio.Models.StockModel;
import edu.temple.stockportfolio.R;


public class DetailsFragment extends Fragment{


    private Fragment stockDetailsFragment;
    private Fragment newsFragment;
    private StockModel currentModel;
    private boolean isActiveDetails;
    private onFragmentInteraction mListener;

    public DetailsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        return view;
    }

    /**
     * Initialize view
     * @param view current item view
     * @param savedInstanceState saved state on close
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isActiveDetails = true;

        Bundle args = getArguments();
        if(args != null)
            currentModel = args.getParcelable(getString(R.string.stock_details_key));

        Bundle newsArgs = new Bundle();
        ArrayList<String> symbols = new ArrayList<>();

        newsFragment = new NewsFragment();

        if(currentModel != null) {
            stockDetailsFragment = new StockDetailsFragment();
            loadFragment(R.id.stockDetailsFrame, stockDetailsFragment, false, args);
            symbols.add(currentModel.getShortName());
        }
        else{
            symbols = StockManager.getInstance().getStocks();
        }

        newsArgs.putStringArrayList(getString(R.string.news_key),symbols);
        loadFragment(R.id.newsFrame, newsFragment, false, newsArgs);

    }

    public void loadFragment(int paneId,Fragment fragment,boolean placeOnBackStack,Bundle args){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(args != null)
            fragment.setArguments(args);

        fragmentTransaction.replace(paneId,fragment);

        if(placeOnBackStack){
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void changeActiveDetails(){
        isActiveDetails = false;
    }

    public interface onFragmentInteraction{
        void leavingFrag(boolean isActive);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onFragmentInteraction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener.leavingFrag(isActiveDetails);
        super.onDetach();
        mListener = null;
    }

}
