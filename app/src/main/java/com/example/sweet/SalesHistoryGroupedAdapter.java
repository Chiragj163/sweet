package com.example.sweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class SalesHistoryGroupedAdapter extends ArrayAdapter<Sale> {

    public SalesHistoryGroupedAdapter(Context context, List<Sale> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history_group, parent, false);
        }

        Sale sale = getItem(position);

        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvTotal = convertView.findViewById(R.id.tvTotal);
        LinearLayout container = convertView.findViewById(R.id.itemsContainer);

        tvDate.setText("Date: " + sale.getDate());
        tvTotal.setText("TOTAL: ₹" + sale.getAmount());

        container.removeAllViews(); // 🔥 important

        // 🔥 Add each item row dynamically
        for (SaleItemRow item : sale.items) {

            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_sales_item, container, false);

            ((TextView) row.findViewById(R.id.tvItem)).setText(item.name);
            ((TextView) row.findViewById(R.id.tvQty)).setText(String.valueOf(item.qty));
            ((TextView) row.findViewById(R.id.tvAmount)).setText("₹" + item.total);

            container.addView(row);
        }

        return convertView;
    }
}
