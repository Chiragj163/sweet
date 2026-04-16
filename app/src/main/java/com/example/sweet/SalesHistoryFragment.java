package com.example.sweet;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class SalesHistoryFragment extends Fragment {

    ListView listView;
    DatabaseHelper db;
    EditText etSearchBill;
    Button btnSearch, btnFilterDate;

    SalesHistoryGroupedAdapter adapter;
    List<Sale> sales;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sales_history, container, false);

        listView = view.findViewById(R.id.list);
        etSearchBill = view.findViewById(R.id.etSearchBill);
       // btnSearch = view.findViewById(R.id.btnSearch);
        btnFilterDate = view.findViewById(R.id.btnFilterDate);

        db = new DatabaseHelper(requireContext());

        // 🔥 Load all sales initially
        sales = db.getAllSales();
        adapter = new SalesHistoryGroupedAdapter(getContext(), sales);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            Sale sale = (Sale) parent.getItemAtPosition(position);
            openBillDetail(sale);
        });
        // ================= SEARCH =================
        etSearchBill.addTextChangedListener(new android.text.TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String input = s.toString().trim();

                if (input.isEmpty()) {
                    // 🔥 Show all sales again
                    sales.clear();
                    sales.addAll(db.getAllSales());
                    adapter.notifyDataSetChanged();
                    return;
                }

                try {
                    int billNo = Integer.parseInt(input);

                    Sale sale = db.getSaleByBillId(billNo);

                    List<Sale> result = new ArrayList<>();

                    if (sale != null) {
                        result.add(sale);
                    }
                    if (result.isEmpty()) {
                        Toast.makeText(getContext(), "No bill found", Toast.LENGTH_SHORT).show();
                    }
                    sales.clear();
                    sales.addAll(result);
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    // ignore invalid input
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // ================= DATE FILTER =================
        btnFilterDate.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(getContext(), btnFilterDate);

            popup.getMenu().add("Today");
            popup.getMenu().add("This Week");
            popup.getMenu().add("This Month");
            popup.getMenu().add("6 Months");
            popup.getMenu().add("This Year");
            popup.getMenu().add("Custom");

            popup.setOnMenuItemClickListener(item -> {

                String selected = item.getTitle().toString();

                switch (selected) {

                    case "Today":
                        filterDays(0);
                        break;

                    case "This Week":
                        filterDays(7);
                        break;

                    case "This Month":
                        filterDays(30);
                        break;

                    case "6 Months":
                        filterDays(180);
                        break;

                    case "This Year":
                        filterDays(365);
                        break;

                    case "Custom":
                        openDatePickerRange();
                        break;
                }

                return true;
            });

            popup.show();
        });

        return view;
    }
    private void filterDays(int days) {

        Calendar cal = Calendar.getInstance();

        String endDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());

        if (days == 0) {
            // Today only
            List<Sale> filtered = db.getSalesByDate(endDate);

            sales.clear();
            sales.addAll(filtered);
            adapter.notifyDataSetChanged();
            return;
        }

        cal.add(Calendar.DAY_OF_YEAR, -days);

        String startDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());

        List<Sale> filtered = db.getSalesBetweenDates(startDate, endDate);

        sales.clear();
        sales.addAll(filtered);
        adapter.notifyDataSetChanged();
    }
    private String startDate = "";

    private void openDatePickerRange() {

        Calendar cal = Calendar.getInstance();

        DatePickerDialog startPicker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {

                    startDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, day);

                    // Now pick end date
                    openEndDatePicker();

                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        startPicker.setTitle("Select Start Date");
        startPicker.show();
    }

    private void openEndDatePicker() {

        Calendar cal = Calendar.getInstance();

        DatePickerDialog endPicker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {

                    String endDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, day);

                    List<Sale> filtered = db.getSalesBetweenDates(startDate, endDate);

                    sales.clear();
                    sales.addAll(filtered);
                    adapter.notifyDataSetChanged();

                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        endPicker.setTitle("Select End Date");
        endPicker.show();
    }

    // ================= OPEN BILL =================
    private void openBillDetail(Sale sale) {

        BillingFragment fragment = new BillingFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("sale", sale);
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}