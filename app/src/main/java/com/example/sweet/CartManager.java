package com.example.sweet;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private List<CartItem> cartList;

    private CartManager() {
        cartList = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCart() {
        return cartList;
    }

    // 🔥 ADD / UPDATE ITEM
    public void addToCart(CartItem newItem) {

        boolean found = false;

        for (CartItem item : cartList) {
            if (item.getId() == newItem.getId()) {
                item.setQty(item.getQty() + newItem.getQty());
                found = true;
                break;
            }
        }

        if (!found) {
            cartList.add(newItem);
        }
    }

    public void clearCart() {
        cartList.clear();
    }
}