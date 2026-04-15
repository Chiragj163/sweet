package com.example.sweet;

import android.content.Context;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesHistoryGroupedAdapter extends ArrayAdapter<Sale> {

    public SalesHistoryGroupedAdapter(Context context, List<Sale> list) {
        super(context, 0, list);
    }

    static class ViewHolder {
        TextView tvDate, tvTotal, tvBillNo;
        LinearLayout container;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_history_group, parent, false);

            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvTotal = convertView.findViewById(R.id.tvTotal);
            holder.container = convertView.findViewById(R.id.itemsContainer);
            holder.tvBillNo = convertView.findViewById(R.id.tvBillNo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Sale sale = getItem(position);

        if (sale != null) {

            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

                Date d = input.parse(sale.getDate());

                // ✅ FIXED BILL ID
                String billId = new SimpleDateFormat("ddMMyy", Locale.getDefault())
                        .format(d) + "-" + String.format("%03d", sale.getBillNo());

                holder.tvBillNo.setText("🧾 " + billId);
                holder.tvDate.setText(output.format(d));

            } catch (Exception e) {
                holder.tvDate.setText(sale.getDate());
            }

            holder.tvTotal.setText("₹" + String.format(Locale.getDefault(), "%.2f", sale.getAmount()));

            holder.container.removeAllViews();

            for (SaleItemRow item : sale.items) {

                View row = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_sales_item, holder.container, false);

                TextView tvItem = row.findViewById(R.id.tvItem);
                TextView tvQty = row.findViewById(R.id.tvQty);
                TextView tvAmount = row.findViewById(R.id.tvAmount);
                TextView tvGst = row.findViewById(R.id.tvGst);

                tvItem.setText(item.name);
                tvQty.setText(String.format(Locale.getDefault(), "%.0f", item.qty));
                tvAmount.setText("₹" + String.format(Locale.getDefault(), "%.2f", item.total));

                if (tvGst != null) {
                    tvGst.setText(item.gstRate + "%");
                }

                holder.container.addView(row);
            }
        }

        return convertView;
    }
}