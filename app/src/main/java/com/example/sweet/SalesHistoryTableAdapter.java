package com.example.sweet;

import android.content.Context;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesHistoryTableAdapter extends ArrayAdapter<SaleItemRow> {

    public SalesHistoryTableAdapter(Context context, List<SaleItemRow> list) {
        super(context, 0, list);
    }

    static class ViewHolder {
        TextView tvDate, tvItem, tvQty, tvPrice, tvTotal, tvGst;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history_table, parent, false);

            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvItem = convertView.findViewById(R.id.tvItem);
            holder.tvQty = convertView.findViewById(R.id.tvQty);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvTotal = convertView.findViewById(R.id.tvTotal);

            // 👉 Optional GST TextView (add in XML if needed)
            holder.tvGst = convertView.findViewById(R.id.tvGst);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SaleItemRow row = getItem(position);

        if (row != null) {

            // ✅ Format date (clean)
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                Date d = input.parse(row.date);
                holder.tvDate.setText(output.format(d));
            } catch (Exception e) {
                holder.tvDate.setText(row.date); // fallback
            }

            holder.tvItem.setText(row.name);

            holder.tvQty.setText(String.format(Locale.getDefault(), "%.0f", row.qty));

            holder.tvPrice.setText("₹" + String.format(Locale.getDefault(), "%.2f", row.price));

            holder.tvTotal.setText("₹" + String.format(Locale.getDefault(), "%.2f", row.total));

            // ✅ GST (optional)
            if (holder.tvGst != null) {
                holder.tvGst.setText(row.gstRate + "% GST");
            }
        }

        return convertView;
    }
}