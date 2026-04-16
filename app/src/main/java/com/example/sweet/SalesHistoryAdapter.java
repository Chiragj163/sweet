package com.example.sweet;

import android.content.Context;
import android.view.*;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SalesHistoryAdapter extends RecyclerView.Adapter<SalesHistoryAdapter.ViewHolder> {

    public interface OnBillClick {
        void onClick(Sale sale);
    }

    private Context context;
    private List<Sale> list;
    private OnBillClick listener;

    public SalesHistoryAdapter(Context context, List<Sale> list, OnBillClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Sale> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Sale sale = list.get(position);

        holder.title.setText("Bill #" + sale.getBillNo());
        holder.sub.setText("₹" + sale.getAmount() + " • " + sale.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(sale);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, sub;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            sub = itemView.findViewById(android.R.id.text2);
        }
    }
}