package com.example.sweet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

public class MenuFragment extends Fragment {

    RecyclerView rvCategories, rvItems;
    EditText etSearch;
    TextView tvCartCount;
    FloatingActionButton fabCart;
    DatabaseHelper db;

    List<String> categories = Arrays.asList(
            "All" ,"Mithai", "Snacks", "Soft Drink", "Ice Cream"
    );

    List<Item> fullList = new ArrayList<>();   // 🔥 original data
    MenuItemAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvItems = view.findViewById(R.id.rvItems);
        etSearch = view.findViewById(R.id.etSearch);
        tvCartCount = view.findViewById(R.id.tvCartCount);
        fabCart = view.findViewById(R.id.fabCart);

        db = new DatabaseHelper(getContext());
        fabCart.setOnClickListener(v -> {
            // 🔥 Open Billing Fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, new BillingFragment())
                    .addToBackStack(null)
                    .commit();
        });


        setupCategories();
        setupItemsRecycler();

        loadItems("All");

        setupSearch();
        updateCartCount();
        return view;
    }

    // ================= CATEGORY =================

    private void setupCategories() {

        rvCategories.setLayoutManager(new GridLayoutManager(getContext(), 3));

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, category -> {
            loadItems(category);
        });

        rvCategories.setAdapter(categoryAdapter);
    }

    // ================= ITEMS =================

    private void setupItemsRecycler() {
        rvItems.setLayoutManager(new GridLayoutManager(getContext(),3));

        adapter = new MenuItemAdapter(fullList, () -> {
            updateCartCount();
        });

        rvItems.setAdapter(adapter);
        rvItems.setHasFixedSize(true);
    }
    private void updateCartCount() {
        int count = CartManager.getInstance().getCart().size();

        if (count == 0) {
            tvCartCount.setVisibility(View.GONE);
        } else {
            tvCartCount.setVisibility(View.VISIBLE);
            tvCartCount.setText(String.valueOf(count));
        }
    }
    private void loadItems(String category) {

        fullList.clear();
        fullList.addAll(db.getItemsByCategory(category));
        List<Item> items = db.getItemsByCategory(category);
        adapter.updateList(fullList);
        adapter.notifyDataSetChanged();

        rvItems.setVisibility(View.VISIBLE);

        etSearch.setText("");
    }

    // ================= SEARCH =================

    private void setupSearch() {

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {

                List<Item> filtered = new ArrayList<>();

                for (Item i : fullList) {
                    if (i.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                        filtered.add(i);
                    }
                }

                adapter.updateList(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}