package com.hp.sloiki.fridge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.sloiki.R;
import com.hp.sloiki.model.FridgeElement;
import com.hp.sloiki.network.NetworkService;
import com.hp.sloiki.network.NetworkServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FridgeFragment extends Fragment implements AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private FetchListTask mListTask;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private ProgressBar mProgressView;
    private ArrayList<FridgeElement> mProductList;
    private int itemToDeletePosition;

    public static FridgeFragment newInstance() {
        FridgeFragment fragment = new FridgeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FridgeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_list, container, false);

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        mProgressView = (ProgressBar) view.findViewById(android.R.id.progress);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgress(true);
        mListTask = new FetchListTask();
        mListTask.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fridge, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_new_product) {
            startActivity(new Intent(getActivity().getApplicationContext(), NewProductActivity.class));
            return true;
        } else if (item.getItemId() == R.id.clear_fridge) {
            showProgress(true);
            itemToDeletePosition = -1;
            DeleteItemTask deleteItemTask = new DeleteItemTask();
            deleteItemTask.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // handle single clicks
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        showProgress(true);
                        DeleteItemTask deleteItemTask = new DeleteItemTask();
                        deleteItemTask.execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //'No' button clicked, do nothing, dialog will close itself
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_product))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();

        itemToDeletePosition = i;

        return false;
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


    /**
     * Fetches list of products in the fridge
     */
    private class FetchListTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final String url = getString(R.string.url_base) + getString(R.string.url_fridge_products);
            mProductList = new ArrayList<FridgeElement>();

            String data = null;
            try {
                data = NetworkService.getInstance().sendGET(url);
            } catch (NetworkServiceException e) {
                e.printStackTrace();
            }

            if (data != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    mProductList = objectMapper.readValue(data, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, FridgeElement.class));
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

            ListAdapter adapter = new FridgeAdapter(getActivity(), android.R.layout.simple_list_item_1, mProductList);
            mListView.setAdapter(adapter);
        }
    }

    /**
     * Deletes given product from the fridge, then executes FetchListTask
     */
    private class DeleteItemTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String url;
            Integer elementId = null;
            List<FridgeElement> list = null;
            if (itemToDeletePosition >= 0 && itemToDeletePosition < mProductList.size()) {
                url = getString(R.string.url_base) + getString(R.string.url_fridge_products);
                elementId = mProductList.get(itemToDeletePosition).getElementId();
            } else {
                url = getString(R.string.url_base) + getString(R.string.url_fridge);
            }

            boolean success = false;
            try {
                success = NetworkService.getInstance().sendDELETE(url, elementId);
            } catch (NetworkServiceException e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            FetchListTask fetchListTask = new FetchListTask();
            fetchListTask.execute();
        }
    }
}
