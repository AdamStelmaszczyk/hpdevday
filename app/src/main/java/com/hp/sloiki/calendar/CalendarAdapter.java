package com.hp.sloiki.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hp.sloiki.R;
import com.hp.sloiki.model.ProductCalendar;

import java.util.List;

public class CalendarAdapter extends ArrayAdapter<ProductCalendar> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private LayoutInflater mInflater;
    private List<ProductCalendar> mData;


    public CalendarAdapter(Context context, int resource, List<ProductCalendar> objects) {
        super(context, resource, objects);
        mData = objects;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fragment_calendar_item, null);

            holder = new ViewHolder();
            holder.separator = (TextView) view.findViewById(android.R.id.title);
            holder.content = (TextView) view.findViewById(android.R.id.content);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ProductCalendar previousProduct = null;
        if (i > 0) {
            previousProduct = mData.get(i-1);
        }
        ProductCalendar product = mData.get(i);

        if (product != null) {
            holder.separator.setVisibility( previousProduct == null || !product.getConsumptionDate().equals(previousProduct.getConsumptionDate()) ? View.VISIBLE : View.GONE );
            holder.separator.setText(product.getConsumptionDate());
            holder.content.setText(product.getProduct().getName());
        }

        return view;
    }

    private static class ViewHolder {
        TextView separator;
        TextView content;
    };
}
