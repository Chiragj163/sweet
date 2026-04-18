package com.example.sweet;

import android.view.*;
import android.widget.*;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;

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
                    .load(image)
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
            vibrate(v);

            CartManager.getInstance().addToCart(new CartItem(
                    item.getId(),
                    item.getName(),
                    1,
                    item.getPrice(),
                    item.getGstRate()
            ));

            // ✅ FIRST update UI
            holder.btnAdd.setVisibility(View.GONE);
            holder.layoutQty.setVisibility(View.VISIBLE);
            holder.tvQty.setText("1");

            // ✅ THEN notify fragment
            if (listener != null) listener.onCartUpdated();

            // ✅ THEN update recycler item
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }
        });
        holder.btnAdd.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    scaleDown(v);
                    break;

                case MotionEvent.ACTION_UP:
                    scaleUp(v);
                    v.performClick(); // ✅ IMPORTANT
                    break;

                case MotionEvent.ACTION_CANCEL:
                    scaleUp(v);
                    break;
            }
            return true; // ✅ now we handled the event
        });

        // ================= PLUS =================


        holder.btnPlus.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    scaleDown(v);
                    vibrate(v);

                    v.setTag(System.currentTimeMillis()); // ⏱ store press time

                    int[] delay = {300};

                    holder.runnable = new Runnable() {
                        @Override
                        public void run() {

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

                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                notifyItemChanged(pos);
                            }

                            delay[0] = Math.max(50, delay[0] - 20);
                            holder.handler.postDelayed(this, delay[0]);
                        }
                    };

                    holder.handler.post(holder.runnable);
                    return true;

                case MotionEvent.ACTION_UP:

                    scaleUp(v);

                    if (holder.runnable != null) {
                        holder.handler.removeCallbacks(holder.runnable);
                    }

                    // ⏱ check if it's a quick tap (click)
                    long pressTime = (long) v.getTag();
                    long duration = System.currentTimeMillis() - pressTime;

                    if (duration < 200) { // 👈 treat as click
                        v.performClick(); // ✅ required
                    }

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    scaleUp(v);

                    if (holder.runnable != null) {
                        holder.handler.removeCallbacks(holder.runnable);
                    }

                    return true;
            }

            return false;
        });
        // ================= MINUS =================
        holder.btnMinus.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    scaleDown(v);
                    vibrate(v);

                    int[] delay = {300};

                    holder.runnable = new Runnable() {
                        @Override
                        public void run() {

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
                                if (listener != null) listener.onCartUpdated();
                                holder.handler.removeCallbacks(this);
                                return;
                            } else {
                                holder.tvQty.setText(String.valueOf(current));
                            }

                            if (listener != null) listener.onCartUpdated();

                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                notifyItemChanged(pos);
                            }

                            delay[0] = Math.max(50, delay[0] - 20);

                            holder.handler.postDelayed(this, delay[0]);
                        }
                    };

                    holder.handler.post(holder.runnable);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    scaleUp(v);
                    if (holder.runnable != null) {
                        holder.handler.removeCallbacks(holder.runnable);
                    }
                    return true;
            }
            return false;
        });

    }
    private void scaleDown(View v) {
        v.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .start();
    }

    private void scaleUp(View v) {
        v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start();
    }
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.handler.removeCallbacksAndMessages(null);
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
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQty;
        ImageView imgItem;

        MaterialButton btnAdd;
        LinearLayout layoutQty;
        Button btnPlus, btnMinus;
        android.os.Handler handler = new android.os.Handler();
        Runnable runnable;

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
    private void vibrate(View view) {
        // 🔥 Best: native haptic (no permission, no API issue)
        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

        // OPTIONAL: stronger vibration (uncomment if needed)

    Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);

    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(40);
        }
    }

    }
}