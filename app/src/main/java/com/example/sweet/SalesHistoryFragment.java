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

            Calendar cal = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(getContext(),
                    (view1, year, month, day) -> {

                        String date = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month + 1, day);

                        List<Sale> filtered = db.getSalesByDate(date);

                        sales.clear();
                        sales.addAll(filtered);
                        adapter.notifyDataSetChanged();

                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });

        return view;
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