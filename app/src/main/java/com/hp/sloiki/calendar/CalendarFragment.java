package com.hp.sloiki.calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.sloiki.R;
import com.hp.sloiki.model.ProductCalendar;
import com.hp.sloiki.network.NetworkService;
import com.hp.sloiki.network.NetworkServiceException;

import java.io.IOException;
import java.util.ArrayList;

public class CalendarFragment extends ListFragment {

    private FetchListTask mListTask;
    private ListView mListView;
    private ArrayList<ProductCalendar> mProductList;
    private ProgressBar mProgressView;

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mProgressView = (ProgressBar) view.findViewById(android.R.id.progress);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgress(true);
        mListTask = new FetchListTask();
        mListTask.execute();
    }

    /**
     * Shows the progress UI and hides the list.
     */
    public void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.getEmptyView().setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class FetchListTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final String url = getString(R.string.url_base) + getString(R.string.url_calendar_products);
            mProductList = new ArrayList<ProductCalendar>();

            String data = null;
            try {
                data = NetworkService.getInstance().sendGET(url);
            } catch (NetworkServiceException e) {
                e.printStackTrace();
            }

            if (data != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    mProductList = objectMapper.readValue(data, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, ProductCalendar.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return data != null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mListTask = null;

            showProgress(false);
            ListAdapter adapter = new CalendarAdapter(getActivity(), android.R.layout.simple_list_item_1, mProductList);
            mListView.setAdapter(adapter);
        }
    }

}
