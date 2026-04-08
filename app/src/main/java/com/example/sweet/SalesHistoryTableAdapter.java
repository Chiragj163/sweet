package com.example.sweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SalesHistoryTableAdapter extends ArrayAdapter<SaleItemRow> {

    public SalesHistoryTableAdapter(Context context, List<SaleItemRow> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history_table, parent, false);
        }

        SaleItemRow row = getItem(position);

        if (row != null) {
            ((TextView) convertView.findViewById(R.id.tvDate)).setText(row.date);
            ((TextView) convertView.findViewById(R.id.tvItem)).setText(row.name);
            ((TextView) convertView.findViewById(R.id.tvQty)).setText(String.valueOf(row.qty));
            ((TextView) convertView.findViewById(R.id.tvPrice)).setText("₹" + row.price);
            ((TextView) convertView.findViewById(R.id.tvTotal)).setText("₹" + row.total);
        }

        return convertView;
    }
}