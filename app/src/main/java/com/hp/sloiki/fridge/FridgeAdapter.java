package com.hp.sloiki.fridge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hp.sloiki.R;
import com.hp.sloiki.model.FridgeElement;

import java.util.ArrayList;

public class FridgeAdapter extends ArrayAdapter<FridgeElement> {

    private ArrayList<FridgeElement> mData;

    public FridgeAdapter(Context context, int textViewResourceId, ArrayList<FridgeElement> objects) {
        super(context, textViewResourceId, objects);
        this.mData = objects;
    }

    public View getView(int position, View view, ViewGroup parent){
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fragment_fridge_item, null);

            holder = new ViewHolder();
            holder.nameView = (TextView) view.findViewById(R.id.name);
            holder.expiryView = (TextView) view.findViewById(R.id.expiry);
            holder.volumeView = (TextView) view.findViewById(R.id.volume);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        FridgeElement product = mData.get(position);

        holder.nameView.setText(product.getProduct().getName());
        holder.expiryView.setText(product.getExpiryDate());
        holder.volumeView.setText(product.getVolume().toString());

        view.setBackgroundColor(position % 2 == 0 ?
                view.getResources().getColor(R.color.lightgray) :
                view.getResources().getColor(android.R.color.white));

        return view;

    }

    private static class ViewHolder {
        TextView nameView;
        TextView expiryView;
        TextView volumeView;
    }
}
