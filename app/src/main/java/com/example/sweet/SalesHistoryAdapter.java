package com.example.sweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SalesHistoryAdapter extends ArrayAdapter<Sale> {

    public SalesHistoryAdapter(Context context, List<Sale> data) {
        super(context, 0, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history, parent, false);
        }

        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvTotal = convertView.findViewById(R.id.tvTotal);
        TextView tvItems = convertView.findViewById(R.id.tvItems);

        Sale sale = getItem(position); // ✅ FIX

        if (sale != null) {
            tvDate.setText(sale.getDate());
            tvTotal.setText("₹" + sale.getAmount());
            tvItems.setText(sale.getSummary());
        }

        return convertView;
    }
}