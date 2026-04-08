package com.example.sweet;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class SalesHistoryFragment extends Fragment {

    ListView listView;
    DatabaseHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.simple_list, container, false);

        listView = view.findViewById(R.id.list);
        db = new DatabaseHelper(requireContext());


        List<Sale> sales = db.getAllSales();

        SalesHistoryGroupedAdapter adapter =
                new SalesHistoryGroupedAdapter(getContext(), sales);

        listView.setAdapter(adapter);

        return view;
    }
}