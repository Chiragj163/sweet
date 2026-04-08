package com.example.sweet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;


public class SalesByItemFragment extends Fragment {

    ListView listView;
    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.simple_list, container, false);

        listView = view.findViewById(R.id.list);
        View header = inflater.inflate(R.layout.row_report_header, listView,false);
        listView.addHeaderView(header);
        db = new DatabaseHelper(requireContext());

        List<String> data = db.getSalesByItem();
        if (data.isEmpty()) {
            listView.setAdapter(new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    java.util.Arrays.asList("No sales data available")
            ));
            return view;
        }
        SalesByItemAdapter adapter = new SalesByItemAdapter(requireContext(), data);
        listView.setAdapter(adapter);



        return view;
    }
}