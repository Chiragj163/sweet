package com.example.sweet;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ItemDetailBottomSheet extends BottomSheetDialogFragment {

    Item item;
    OnAddToCartListener listener;

    public interface OnAddToCartListener {
        void onAddToCart(CartItem cartItem);
    }

    public ItemDetailBottomSheet(Item item, OnAddToCartListener listener) {
        this.item = item;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_item_detail, container, false);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvGst = view.findViewById(R.id.tvGst);
        EditText etQty = view.findViewById(R.id.etQty);
        Button btnAdd = view.findViewById(R.id.btnAddToCart);

        tvName.setText(item.getName());
        tvPrice.setText("₹" + item.getPrice() + " / " + item.getUnit());
        tvGst.setText("GST: " + item.getGstRate() + "%");

        btnAdd.setOnClickListener(v -> {

            double qty = 1;

            try {
                qty = Double.parseDouble(etQty.getText().toString());
            } catch (Exception ignored) {}

            CartItem cartItem = new CartItem(
                    item.getId(),
                    item.getName(),
                    qty,
                    item.getPrice(),
                    item.getGstRate()
            );

            // 🔥 GLOBAL ADD
            CartManager.getInstance().addToCart(cartItem);

            Toast.makeText(getContext(),
                    "Added to cart",
                    Toast.LENGTH_SHORT).show();

            dismiss();
        });
        return view;
    }
}