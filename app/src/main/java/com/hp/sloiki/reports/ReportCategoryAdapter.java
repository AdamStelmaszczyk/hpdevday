package com.hp.sloiki.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hp.sloiki.R;
import com.hp.sloiki.model.CategoryReport;
import com.hp.sloiki.model.FridgeElement;

import java.util.ArrayList;

public class ReportCategoryAdapter extends ArrayAdapter<CategoryReport> {

    private ArrayList<CategoryReport> mData;

    public ReportCategoryAdapter(Context context, int textViewResourceId, ArrayList<CategoryReport> objects) {
        super(context, textViewResourceId, objects);
        this.mData = objects;
    }

    public View getView(int position, View view, ViewGroup parent){
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fragment_report_category_item, null);

            holder = new ViewHolder();
            holder.nameView = (TextView) view.findViewById(android.R.id.text1);
            holder.countView = (TextView) view.findViewById(android.R.id.text2);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        CategoryReport category = mData.get(position);

        holder.nameView.setText(category.getName());
        holder.countView.setText(category.getProductCount().toString());


        view.setBackgroundColor(position % 2 == 0 ?
                view.getResources().getColor(R.color.lightgray) :
                view.getResources().getColor(android.R.color.white));

        return view;

    }

    private static class ViewHolder {
        TextView nameView;
        TextView countView;
    }
}
