package com.example.sweet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BillRecyclerAdapter extends RecyclerView.Adapter<BillRecyclerAdapter.ViewHolder> {

    List<CartItem> list;
    OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public BillRecyclerAdapter(List<CartItem> list, OnCartChangeListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvQty, tvAmount, btnPlus, btnMinus;

        public ViewHolder(View view) {
            super(view);
            tvItem = view.findViewById(R.id.tvItem);
            tvQty = view.findViewById(R.id.tvQty);
            tvAmount = view.findViewById(R.id.tvAmount);
            btnPlus = view.findViewById(R.id.btnPlus);
            btnMinus = view.findViewById(R.id.btnMinus);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_bill_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {

        CartItem item = list.get(position);

        h.tvItem.setText(item.getName());
        h.tvQty.setText(String.valueOf(item.getQty()));
        h.tvAmount.setText("₹" + item.getTotal());

        // ➕ Increase
        h.btnPlus.setOnClickListener(v -> {
            item.qty++;
            notifyItemChanged(position);
            listener.onCartChanged();
        });

        // ➖ Decrease
        h.btnMinus.setOnClickListener(v -> {
            if (item.qty > 1) {
                item.qty--;
                notifyItemChanged(position);
            } else {
                list.remove(position);
                notifyItemRemoved(position);
            }
            listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}