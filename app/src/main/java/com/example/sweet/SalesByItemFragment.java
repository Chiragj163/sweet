package com.example.sweet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SalesByItemFragment extends Fragment {

    ListView listView;
    Button btnItemFilter;
    Spinner spCategory;
    TextView tvTotal;

    SalesByItemAdapter adapter;
    List<String> data;
    DatabaseHelper db;

    String startDate, endDate;
    String selectedCategory = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sales_by_item, container, false);

        listView = view.findViewById(R.id.listItems);
        btnItemFilter = view.findViewById(R.id.btnItemFilter);
        spCategory = view.findViewById(R.id.spCategory);
        tvTotal = view.findViewById(R.id.tvTotal);

        db = new DatabaseHelper(requireContext());

        startDate = endDate = getDate(0);

        // 🔥 CATEGORY LIST
        String[] categories = {"All", "Sweet", "Snacks", "Soft Drink", "Ice Cream"};

        spCategory.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        ));

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                loadItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔥 DATE FILTER
        btnItemFilter.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(getContext(), btnItemFilter);

            menu.getMenu().add("Today");
            menu.getMenu().add("This Week");
            menu.getMenu().add("This Month");
            menu.getMenu().add("This Year");

            menu.setOnMenuItemClickListener(item -> {

                switch (item.getTitle().toString()) {

                    case "Today":
                        startDate = endDate = getDate(0);
                        break;

                    case "This Week":
                        startDate = getDate(7);
                        endDate = getDate(0);
                        break;

                    case "This Month":
                        startDate = getDate(30);
                        endDate = getDate(0);
                        break;

                    case "This Year":
                        startDate = getDate(365);
                        endDate = getDate(0);
                        break;
                }

                loadItems();
                return true;
            });

            menu.show();
        });

        loadItems();

        return view;
    }

    private String getDate(int daysAgo) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() - daysAgo * 86400000L));
    }

    private void loadItems() {

        data = db.getSalesByItemFiltered(startDate, endDate, selectedCategory);

        if (data == null || data.isEmpty()) {

            // 🔥 CLEAR LIST SAFELY
            adapter = new SalesByItemAdapter(getContext(), new java.util.ArrayList<>());
            listView.setAdapter(adapter);

            tvTotal.setText("Total: ₹0");
            Toast.makeText(getContext(), "No data for selected filter", Toast.LENGTH_SHORT).show();

            return;
        }

        adapter = new SalesByItemAdapter(getContext(), data);
        listView.setAdapter(adapter);

        double total = db.getSalesByItemTotal(startDate, endDate, selectedCategory);
        tvTotal.setText("Total: ₹" + String.format("%.2f", total));
    }
}