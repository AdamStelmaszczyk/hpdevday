package com.hp.sloiki.fridge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.sloiki.R;
import com.hp.sloiki.model.FridgeElement;
import com.hp.sloiki.model.Product;
import com.hp.sloiki.network.NetworkService;
import com.hp.sloiki.network.NetworkServiceException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NewProductActivity extends Activity {

    private View mContentView;
    private ProgressBar mProgressView;

    private AutoCompleteTextView mNameField;
    private EditText mVolumeField;
    private EditText mExpiryDateField;

    private ArrayList<Product> mAvailableProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        mContentView = findViewById(R.id.content_layout);
        mProgressView = (ProgressBar) findViewById(android.R.id.progress);

        mNameField = (AutoCompleteTextView) findViewById(R.id.nameEditText);
        mVolumeField = (EditText) findViewById(R.id.volumeEditText);
        mExpiryDateField = (EditText) findViewById(R.id.expiryDateEditText);
        mExpiryDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentDate=Calendar.getInstance();
                int year = currentDate.get(Calendar.YEAR);
                int month = currentDate.get(Calendar.MONTH);
                int day = currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(NewProductActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar date = new GregorianCalendar(year, month, day);
                        mExpiryDateField.setText(dateFormat.format(date.getTime()));
                    }
                }, year, month, day);
                mDatePicker.setTitle("");
                mDatePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.save), (DialogInterface.OnClickListener)null);
                mDatePicker.show();
            }
        });

        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFieldsAndSendToServer();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgress(true);
        FetchAvailableTask fetchAvailableTask = new FetchAvailableTask();
        fetchAvailableTask.execute();
    }

    private void checkFieldsAndSendToServer() {
        //TODO: Validation is missing!
        showProgress(true);
        SendToServerTask task = new SendToServerTask();
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Shows the progress UI and hides the content.
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
        mContentView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
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


    private class SendToServerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final String url = getString(R.string.url_base) + getString(R.string.url_fridge_products);

            FridgeElement product = new FridgeElement();
            product.setVolume(Long.parseLong(mVolumeField.getText().toString()));
            product.setExpiryDate(mExpiryDateField.getText().toString());
            product.setProduct(new Product());
            product.getProduct().setProductId(getIdFromName(mAvailableProducts, mNameField.getText().toString()));


            List<FridgeElement> list = new ArrayList<FridgeElement>();
            list.add(product);
            Boolean success = false;
            try {
                success = NetworkService.getInstance().sendPUT(url, new ObjectMapper().writeValueAsString(list));
            } catch (NetworkServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(getApplicationContext(), aBoolean ? getString(R.string.save_ok) : getString(R.string.save_bad), Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }

    private class FetchAvailableTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            final String url = getString(R.string.url_base) + getString(R.string.url_fridge_products_available);
            String data = null;
            boolean success = true;

            try {
                data = NetworkService.getInstance().sendGET(url);

            } catch (NetworkServiceException e) {
                e.printStackTrace();
                success = false;
            }

            if (data != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    mAvailableProducts = objectMapper.readValue(data, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Product.class));
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            } else {
                success = false;
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            super.onPostExecute(aBoolean);
            Toast.makeText(getApplicationContext(), aBoolean ? getString(R.string.list_ok) : getString(R.string.list_bad), Toast.LENGTH_SHORT).show();

            List<String> names = extractNamesFromProducts(mAvailableProducts);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.spinner_item,
                    names);
            mNameField.setAdapter(adapter);

            showProgress(false);
        }
    }

    List<String> extractNamesFromProducts(List<Product> products) {
        List<String> names = new ArrayList<String>();

        for (Product product : products) {
            names.add(product.getName());
        }

        return names;
    }

    Integer getIdFromName(List<Product> products, String name) {
        Integer result = null;

        for (Product product : products) {
            if (product.getName() != null && product.getName().equals(name)) {
                result = product.getProductId();
                break;
            }
        }

        return result;
    }
}

