package com.example.sweet;

import android.net.Uri;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    List<Item> list;
    OnCartUpdateListener listener;

    public MenuItemAdapter(List<Item> list, OnCartUpdateListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item_card, parent, false);

        return new ViewHolder(v); // ✅ CORRECT
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Item item = list.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("₹" + item.getPrice() + " / " + item.getUnit());

        // ================= IMAGE =================
        String image = item.getImage();

        holder.imgItem.setImageResource(android.R.drawable.ic_menu_gallery);

        if (image != null && !image.trim().isEmpty()) {
            Glide.with(holder.imgItem.getContext())
                    .load(Uri.parse(image))
                    .into(holder.imgItem);
        }

        // ================= GET QTY =================
        int qty = 0;
        for (CartItem c : CartManager.getInstance().getCart()) {
            if (c.getId() == item.getId()) {
                qty = (int) c.getQty();
                break;
            }
        }

        // ================= UI CONTROL =================
        if (qty > 0) {
            holder.btnAdd.setVisibility(View.GONE);
            holder.layoutQty.setVisibility(View.VISIBLE);
            holder.tvQty.setText(String.valueOf(qty));
        } else {
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.layoutQty.setVisibility(View.GONE);
        }

        // ================= ADD BUTTON =================
        holder.btnAdd.setOnClickListener(v -> {

            CartManager.getInstance().addToCart(new CartItem(
                    item.getId(),
                    item.getName(),
                    1,
                    item.getPrice(),
                    item.getGstRate()
            ));

            holder.btnAdd.setVisibility(View.GONE);
            holder.layoutQty.setVisibility(View.VISIBLE);
            holder.tvQty.setText("1");

            if (listener != null) listener.onCartUpdated();
        });

        // ================= PLUS =================
        holder.btnPlus.setOnClickListener(v -> {

            CartManager.getInstance().addToCart(new CartItem(
                    item.getId(),
                    item.getName(),
                    1,
                    item.getPrice(),
                    item.getGstRate()
            ));

            int current = Integer.parseInt(holder.tvQty.getText().toString());
            holder.tvQty.setText(String.valueOf(current + 1));

            if (listener != null) listener.onCartUpdated();
        });

        // ================= MINUS =================
        holder.btnMinus.setOnClickListener(v -> {

            List<CartItem> cart = CartManager.getInstance().getCart();

            for (int i = 0; i < cart.size(); i++) {
                CartItem c = cart.get(i);

                if (c.getId() == item.getId()) {

                    if (c.getQty() > 1) {
                        c.setQty(c.getQty() - 1);
                    } else {
                        cart.remove(i);
                    }
                    break;
                }
            }

            int current = Integer.parseInt(holder.tvQty.getText().toString()) - 1;

            if (current <= 0) {
                holder.btnAdd.setVisibility(View.VISIBLE);
                holder.layoutQty.setVisibility(View.GONE);
            } else {
                holder.tvQty.setText(String.valueOf(current));
            }

            if (listener != null) listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void updateList(List<Item> newList) {
        this.list = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // ================= VIEW HOLDER =================
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQty;
        ImageView imgItem;

        MaterialButton btnAdd;
        LinearLayout layoutQty;
        Button btnPlus, btnMinus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
            imgItem = itemView.findViewById(R.id.imgItem);

            btnAdd = itemView.findViewById(R.id.btnAdd);
            layoutQty = itemView.findViewById(R.id.layoutQty);

            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}