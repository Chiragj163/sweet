package com.example.sweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SalesByItemAdapter extends ArrayAdapter<String> {

    public SalesByItemAdapter(Context context, List<String> data) {
        super(context, 0, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.row_sales_item, parent, false);
        }

        String line = getItem(position);

        TextView tvItem = convertView.findViewById(R.id.tvItem);
        TextView tvQty = convertView.findViewById(R.id.tvQty);
        TextView tvAmount = convertView.findViewById(R.id.tvAmount);

        // Parse string
        String[] parts = line.split("\n");

        tvItem.setText(parts[0]);
        tvQty.setText(parts[1].replace("Qty Sold: ", ""));
        tvAmount.setText(parts[2].replace("Revenue: ", ""));

        return convertView;
    }
}
