package com.example.sweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class BillAdapter extends ArrayAdapter<CartItem> {

    List<CartItem> list;
    Context context;
    OnItemDeleteListener listener;

    public interface OnItemDeleteListener {
        void onItemDeleted();
    }

    public BillAdapter(Context context, List<CartItem> list, OnItemDeleteListener listener) {
        super(context, 0, list);
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_bill_item, parent, false);
        }

        TextView tvItem = convertView.findViewById(R.id.tvItem);
        TextView tvQty = convertView.findViewById(R.id.tvQty);
        TextView tvAmount = convertView.findViewById(R.id.tvAmount);
        ImageView btnDelete = convertView.findViewById(R.id.btnDelete);

        CartItem item = list.get(position);

        tvItem.setText(item.getName());
        tvQty.setText(String.valueOf(item.getQty()));
        tvAmount.setText("₹" + item.getTotal());

        btnDelete.setOnClickListener(v -> {
            list.remove(position);
            notifyDataSetChanged();

            if (listener != null) {
                listener.onItemDeleted();
            }
        });

        return convertView;
    }
}